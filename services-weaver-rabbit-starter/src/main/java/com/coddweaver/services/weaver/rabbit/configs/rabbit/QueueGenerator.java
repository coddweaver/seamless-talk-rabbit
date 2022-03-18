package com.coddweaver.services.weaver.rabbit.configs.rabbit;

import com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitQueue;
import com.coddweaver.services.weaver.rabbit.helpers.CaseUtils;
import com.google.common.base.CaseFormat;
import org.apache.logging.log4j.util.Strings;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QueueGenerator {


//region Fields
    //For debug purposes
    public final static boolean BASIC_DLQ_ENABLED = true;
    public final static String DEFAULT_DLX_NAME = "common.dlx";
    public final static String DEFAULT_EXCHANGE_NAME = "common";
    public final static String DEFAULT_DLX_BEAN_NAME = "defaultDlx";
    public final static String DEFAULT_EXCHANGE_BEAN_NAME = "defaultExchange";

    private final static String EXCHANGE_BEAN_NAME_POSTFIX = "Exchange";
    private final static String QUEUE_BEAN_NAME_POSTFIX = "Queue";
    private final static String BINDING_BEAN_NAME_POSTFIX = "Binding";
    private final static String DLQ_POSTFIX = ".dlq";
    private final static String DLQ_BEAN_NAME_POSTFIX = "Dlq";
    private final static String DLQ_BINDING_BEAN_NAME_POSTFIX = "DlqBinding";
    private final static Set<ExchangeDefinition> registeredExchanges = new HashSet<>();
    private final static Set<ExchangeDefinition> readyExchanges = new HashSet<>();
    private final static Set<Class<? extends RabbitApi>> readyContracts = new HashSet<>();
    private final static Set<Map.Entry<Class<? extends RabbitApi>, ExchangeDefinition>> readyBindings = new HashSet<>();
    private final static Map<Class<? extends RabbitApi>, List<ExchangeDefinition>> contractsToExchanges = new HashMap<>();
    private final Exchange defaultDlx;
    private final Exchange defaultExchange;
    private final ExchangeDefinition defaultDlxDefinition;
    private final ExchangeDefinition defaultExchangeDefition;
    private final ConfigurableBeanFactory beanFactory;

    public QueueGenerator(ConfigurableBeanFactory beanFactory,
            List<? extends ExchangeRegisterer> beanRegisterers) {
        this.beanFactory = beanFactory;
        registeredExchanges.addAll(beanRegisterers.stream()
                                                  .flatMap(registerer -> registerer.collectExchanges()
                                                                                   .stream())
                                                  .collect(Collectors.toList()));
        this.defaultDlxDefinition = generateDefaultDlxDefinition();
        this.defaultExchangeDefition = generateDefaultExchangeDefinition();

        this.defaultDlx = defineExchange(this.defaultDlxDefinition, DEFAULT_DLX_NAME, DEFAULT_DLX_BEAN_NAME);
        this.defaultExchange = defineExchange(this.defaultExchangeDefition, DEFAULT_EXCHANGE_NAME, DEFAULT_EXCHANGE_BEAN_NAME);

        createContractToExchangesMap();
    }


    public static String generateExchangeBeanName(String exchangeDefName) {
        return CaseUtils.convert(exchangeDefName, CaseFormat.LOWER_CAMEL) + EXCHANGE_BEAN_NAME_POSTFIX;
    }

    public static String generateQueueBeanName(Class<? extends RabbitApi> contract) {
        return generateQueueBeanName(getContractName(contract));
    }

    private static String generateQueueBeanName(String contractName) {
        return contractName + QUEUE_BEAN_NAME_POSTFIX;
    }

    private static String generateBindingBeanName(String contractName) {
        return contractName + BINDING_BEAN_NAME_POSTFIX;
    }

    private static String generateBindingBeanName(String contractName, String exchangeDefName) {
        return contractName + "To" + CaseUtils.convert(exchangeDefName, CaseFormat.UPPER_CAMEL) + BINDING_BEAN_NAME_POSTFIX;
    }

    private static String generateDlqBindingBeanName(String contractName) {
        return contractName + DLQ_BINDING_BEAN_NAME_POSTFIX;
    }

    private static String generateDlqBeanName(String contractName) {
        return CaseUtils.convert(contractName, CaseFormat.UPPER_CAMEL, CaseFormat.LOWER_CAMEL) + DLQ_BEAN_NAME_POSTFIX;
    }

    private static String getContractName(Class<? extends RabbitApi> contract) {
        return CaseUtils.convert(contract.getSimpleName(), CaseFormat.UPPER_CAMEL, CaseFormat.LOWER_CAMEL);
    }

    public Binding getBinding(Class<? extends RabbitApi> contract) {
        if (!readyContracts.contains(contract)) {
            processContract(contract);
        }

        if (!readyBindings.contains(contract)) {
            return defineDefaultBinding(contract);
        }

        return beanFactory.getBean(generateBindingBeanName(getContractName(contract)), Binding.class);
    }

    public Binding getExtendedBinding(Class<? extends RabbitApi> contract, ExchangeDefinition exchangeDefinition) {
        if (!readyContracts.contains(contract)) {
            processContract(contract);
        }

        if (!readyBindings.contains(contract)) {
            return defineDefaultBinding(contract);
        }

        return beanFactory.getBean(generateBindingBeanName(getContractName(contract), exchangeDefinition.getName()), Binding.class);
    }

    public ExchangeDefinition generateDefaultDlxDefinition() {
        return new ExchangeDefinition(DEFAULT_DLX_BEAN_NAME, ExchangeType.DIRECT, new ArrayList<>(), true);
    }

    public ExchangeDefinition generateDefaultExchangeDefinition() {
        return new ExchangeDefinition(DEFAULT_EXCHANGE_BEAN_NAME, ExchangeType.DIRECT, new ArrayList<>());
    }

    @Deprecated(forRemoval = true)
    public void processAllContracts() {
        for (ExchangeDefinition definition : registeredExchanges) {
            final Exchange exchange = defineExchange(definition);

            final List<Class<? extends RabbitApi>> contracts = definition.getContracts();
            for (Class<? extends RabbitApi> contract : contracts) {
                final Queue queue = defineQueue(contract);
                generateBindings(contract, definition, queue, exchange);
            }
        }
    }

    public void createContractToExchangesMap() {
        for (ExchangeDefinition exchange : registeredExchanges) {
            for (Class<? extends RabbitApi> contract : exchange.getContracts()) {
                contractsToExchanges.compute(contract, (key, value) -> {
                    if (value == null) {
                        return new ArrayList<>(List.of(exchange));
                    }
                    value.add(exchange);
                    return value;
                });
            }
        }
    }

    public Queue processContract(Class<? extends RabbitApi> contract) {

        List<ExchangeDefinition> definitions = contractsToExchanges.get(contract);
        final Queue queue = defineQueue(contract);
        generateBindings(contract, defaultExchangeDefition, queue, defaultExchange);

        if (definitions == null) {
            return queue;
        }

        for (ExchangeDefinition definition : definitions) {
            final Exchange exchange = defineExchange(definition);
            generateBindings(contract, definition, queue, exchange);
        }

        return queue;
    }

    private String generateDlqName(String queueName) {
        return queueName + DLQ_POSTFIX;
    }

    private String generateQueueName(String contractName) {
        return CaseUtils.convert(contractName, CaseFormat.UPPER_CAMEL, CaseFormat.LOWER_HYPHEN);
    }

    private String generateExchangeName(String name) {
        return CaseUtils.convert(name, CaseFormat.LOWER_HYPHEN);
    }

    private void generateBindings(Class<? extends RabbitApi> contract, ExchangeDefinition exchangeDefinition, Queue queue,
            Exchange exchange) {
        AutoGenRabbitQueue params = contract.getAnnotation(AutoGenRabbitQueue.class);
        if (params != null) {
            defineBinding(queue, exchange, contract, exchangeDefinition, params.rKey());
        } else {
            defineBinding(queue, exchange, contract, exchangeDefinition);
        }
    }

    private Exchange defineExchange(ExchangeDefinition exchangeDef) {
        return defineExchange(exchangeDef, generateExchangeName(exchangeDef.getName()), null);
    }

    private Exchange defineExchange(ExchangeDefinition exchangeDef, String exchangeName, String exchangeBeanName) {
        if (exchangeBeanName == null) {
            exchangeBeanName = generateExchangeBeanName(exchangeDef.getName());
        }

        if (readyExchanges.contains(exchangeDef)) {
            return beanFactory.getBean(exchangeBeanName, Exchange.class);
        }

        ExchangeBuilder exchangeBuilder;
        switch (exchangeDef.getType()) {
            case TOPIC:
                exchangeBuilder = ExchangeBuilder.topicExchange(exchangeName);
                break;
            case DIRECT:
                exchangeBuilder = ExchangeBuilder.directExchange(exchangeName);
                break;
            case FANOUT:
                exchangeBuilder = ExchangeBuilder.fanoutExchange(exchangeName);
                break;
            case HEADERS:
                exchangeBuilder = ExchangeBuilder.headersExchange(exchangeName);
                break;
            default:
                throw new UnsupportedOperationException("Cannot generate exchange with type " + exchangeDef.getType());
        }

        final Exchange exchange = exchangeBuilder.durable(exchangeDef.isDurable())
                                                 .build();
        beanFactory.registerSingleton(exchangeBeanName, exchange);
        readyExchanges.add(exchangeDef);

        return exchange;
    }

    private Queue defineQueue(Class<? extends RabbitApi> contract) {
        final String contractName = getContractName(contract);

        if (readyContracts.contains(contract)) {
            return beanFactory.getBean(generateQueueBeanName(contractName), Queue.class);
        }

        String queueName = generateQueueName(contractName);

        QueueBuilder queueBuilder;
        AutoGenRabbitQueue params = contract.getAnnotation(AutoGenRabbitQueue.class);
        if (params != null) {
            if (Strings.isNotBlank(params.name())) {
                queueName = params.name();
            }

            queueBuilder = params.durable()
                           ? QueueBuilder.durable(queueName)
                           : QueueBuilder.nonDurable(queueName);
            if (params.lazy()) {
                queueBuilder = queueBuilder.lazy();
            }
            if (params.messageTTL() > 0) {
                queueBuilder = queueBuilder.ttl(params.messageTTL());
            }

        } else {
            queueBuilder = QueueBuilder.nonDurable(queueName);
        }

        defineDlq(queueName, defaultDlx, contract);
        queueBuilder = queueBuilder
                .deadLetterExchange(DEFAULT_DLX_NAME)
                .deadLetterRoutingKey(queueName);

        final Queue queue = queueBuilder.build();

        final String queueBeanName = generateQueueBeanName(contractName);
        beanFactory.registerSingleton(queueBeanName, queue);
        readyContracts.add(contract);

        return queue;
    }

    private Binding defineDefaultBinding(Class<? extends RabbitApi> contract) {
        final String contractName = getContractName(contract);
        final Queue queue = beanFactory.getBean(generateQueueBeanName(contractName), Queue.class);
        final Binding binding = new Binding(queue.getName(), Binding.DestinationType.QUEUE, null, queue.getName(),
                                            null);

        return binding;
    }

    private Binding defineBinding(Queue queue, Exchange exchange, Class<? extends RabbitApi> contract,
            ExchangeDefinition exchangeDefinition) {
        return defineBinding(queue, exchange, contract, exchangeDefinition, Strings.EMPTY);
    }

    private Binding defineBinding(Queue queue, Exchange exchange, Class<? extends RabbitApi> contract,
            ExchangeDefinition exchangeDefinition, String routingKey) {
        if (Strings.isBlank(routingKey)) {
            routingKey = queue.getName();
        }

        final String contractName = getContractName(contract);
        Binding binding;

        if (exchange.getType()
                    .equals(ExchangeType.FANOUT.getExchangeTypeCode())) {
            binding = BindingBuilder.bind(queue)
                                    .to(exchange)
                                    .with(Strings.EMPTY)
                                    .noargs();
        } else {
            binding = BindingBuilder.bind(queue)
                                    .to(exchange)
                                    .with(routingKey)
                                    .noargs();
        }

        if (exchangeDefinition.equals(defaultExchangeDefition)) {
            final String bindingBeanName = generateBindingBeanName(contractName);
            beanFactory.registerSingleton(bindingBeanName, binding);
            readyBindings.add(new AbstractMap.SimpleEntry<>(contract, defaultExchangeDefition));
        } else {
            final String bindingName = generateBindingBeanName(contractName, exchangeDefinition.getName());
            beanFactory.registerSingleton(bindingName, binding);
            readyBindings.add(new AbstractMap.SimpleEntry<>(contract, exchangeDefinition));
        }

        return binding;
    }

    private Queue defineDlq(String queueName, Exchange exchange, Class<? extends RabbitApi> contract) {
        final String contractName = getContractName(contract);

        final Queue dlq = QueueBuilder.durable(generateDlqName(queueName))
                                      .build();

        final String dlqBeanName = generateDlqBeanName(contractName);
        beanFactory.registerSingleton(dlqBeanName, dlq);
        defineDlqBinding(dlq, queueName, exchange, contract);

        return dlq;
    }

    private Binding defineDlqBinding(Queue dlq, String queueName, Exchange exchange, Class<? extends RabbitApi> contract) {
        final String contractName = getContractName(contract);
        final Binding binding = BindingBuilder.bind(dlq)
                                              .to(exchange)
                                              .with(queueName)
                                              .noargs();

        final String dlqBindingName = generateDlqBindingBeanName(contractName);
        beanFactory.registerSingleton(dlqBindingName, binding);

        return binding;
    }
}

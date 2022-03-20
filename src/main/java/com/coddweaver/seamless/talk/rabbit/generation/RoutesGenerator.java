package com.coddweaver.seamless.talk.rabbit.generation;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import org.apache.logging.log4j.util.Strings;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.coddweaver.seamless.talk.rabbit.helpers.RoutesGenerationUtils.*;


@SuppressWarnings("UnusedReturnValue")
@Service
public final class RoutesGenerator {

    private final Set<ExchangeDefinition> readyExchanges = new HashSet<>();
    private final Set<Class<? extends BaseSeamlessTalkRabbitContract>> readyContracts = new HashSet<>();
    private final Set<Map.Entry<Class<? extends BaseSeamlessTalkRabbitContract>, ExchangeDefinition>> readyBindings = new HashSet<>();
    private final Map<String, ExchangeDefinition> nameToExchangeDefinition = new HashMap<>();

    private final Exchange defaultDlx;
    private final Exchange defaultExchange;
    private final ExchangeDefinition defaultDlxDefinition;
    private final ExchangeDefinition defaultExchangeDefinition;
    private final ConfigurableBeanFactory beanFactory;

    public RoutesGenerator(ConfigurableBeanFactory beanFactory,
            List<? extends ExchangeDefinition> allExchangeDefinitions) {
        this.beanFactory = beanFactory;

        this.defaultDlxDefinition = initDefaultExchangeDefinition(DEFAULT_DLX_BEAN_NAME, true);
        this.defaultExchangeDefinition = initDefaultExchangeDefinition(DEFAULT_EXCHANGE_BEAN_NAME, false);

        this.defaultDlx = defineExchange(this.defaultDlxDefinition, DEFAULT_DLX_NAME, DEFAULT_DLX_BEAN_NAME);
        this.defaultExchange = defineExchange(this.defaultExchangeDefinition, DEFAULT_EXCHANGE_NAME, DEFAULT_EXCHANGE_BEAN_NAME);

        initContractToExchangesMap(allExchangeDefinitions);
    }

    public Queue getQueue(Class<? extends BaseSeamlessTalkRabbitContract> contract) {
        if (!readyContracts.contains(contract)) {
            processContract(contract);
        }

        return beanFactory.getBean(generateQueueBeanName(contract), Queue.class);
    }

    public Binding getBinding(Class<? extends BaseSeamlessTalkRabbitContract> contract) {
        if (!readyContracts.contains(contract)) {
            processContract(contract);
        }

        return beanFactory.getBean(generateBindingBeanName(contract), Binding.class);
    }

    public Binding getBinding(Class<? extends BaseSeamlessTalkRabbitContract> contract, String exchangeDefinitionBeanName) {
        if (!readyContracts.contains(contract)) {
            processContract(contract);
        }

        return beanFactory.getBean(generateBindingBeanName(contract, exchangeDefinitionBeanName), Binding.class);
    }

    private void initContractToExchangesMap(List<? extends ExchangeDefinition> allExchangeDefinitions) {
        for (ExchangeDefinition exchangeDefinition : allExchangeDefinitions) {
            nameToExchangeDefinition.put(exchangeDefinition.getBeanName(), exchangeDefinition);
        }
    }

    private Queue processContract(Class<? extends BaseSeamlessTalkRabbitContract> contract) {

        List<ExchangeDefinition> definitions = new ArrayList<>();
        SeamlessTalkRabbitContract params = contract.getAnnotation(SeamlessTalkRabbitContract.class);
        if (params != null) {
            for (String exchangeDefinitionBeanName : params.exchangeDefs()) {
                final ExchangeDefinition exchangeDefinition = nameToExchangeDefinition.get(exchangeDefinitionBeanName);
                if (exchangeDefinition != null) {
                    definitions.add(exchangeDefinition);
                }
            }
        }

        final Queue queue = defineQueue(contract);
        defineDefaultBinding(contract, queue);

        if (definitions.isEmpty()) {
            return queue;
        }

        for (ExchangeDefinition definition : definitions) {
            final Exchange exchange = defineExchange(definition);
            defineBinding(contract, definition, queue, exchange);
        }

        return queue;
    }

    private Queue defineQueue(Class<? extends BaseSeamlessTalkRabbitContract> contract) {

        if (readyContracts.contains(contract)) {
            return beanFactory.getBean(generateQueueBeanName(contract), Queue.class);
        }

        String queueName = generateQueueName(contract);

        QueueBuilder queueBuilder;
        SeamlessTalkRabbitContract params = contract.getAnnotation(SeamlessTalkRabbitContract.class);
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

        final String queueBeanName = generateQueueBeanName(contract);
        beanFactory.registerSingleton(queueBeanName, queue);
        readyContracts.add(contract);

        return queue;
    }

    private Queue defineDlq(String queueName, Exchange exchange, Class<? extends BaseSeamlessTalkRabbitContract> contract) {
        final Queue dlq = QueueBuilder.durable(generateDlqName(queueName))
                                      .build();

        final String dlqBeanName = generateDlqBeanName(contract);
        beanFactory.registerSingleton(dlqBeanName, dlq);
        defineDlqBinding(dlq, queueName, exchange, contract);

        return dlq;
    }

    private Binding defineDlqBinding(Queue dlq, String queueName, Exchange exchange, Class<? extends BaseSeamlessTalkRabbitContract> contract) {
        final Binding binding = BindingBuilder.bind(dlq)
                                              .to(exchange)
                                              .with(queueName)
                                              .noargs();

        final String dlqBindingName = generateDlqBindingBeanName(contract);
        beanFactory.registerSingleton(dlqBindingName, binding);

        return binding;
    }

    private Binding defineDefaultBinding(Class<? extends BaseSeamlessTalkRabbitContract> contract, Queue queue) {
        Binding binding = createBinding(queue, defaultExchange, contract);

        final String bindingBeanName = generateBindingBeanName(contract);
        beanFactory.registerSingleton(bindingBeanName, binding);
        readyBindings.add(new AbstractMap.SimpleEntry<>(contract, defaultExchangeDefinition));

        return binding;
    }

    private Exchange defineExchange(ExchangeDefinition exchangeDef) {
        return defineExchange(exchangeDef, generateExchangeName(exchangeDef.getBeanName()), null);
    }

    private Exchange defineExchange(ExchangeDefinition exchangeDef, String exchangeName, String exchangeBeanName) {
        if (exchangeBeanName == null) {
            exchangeBeanName = generateExchangeBeanName(exchangeDef.getBeanName());
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

    private Binding defineBinding(Class<? extends BaseSeamlessTalkRabbitContract> contract, ExchangeDefinition exchangeDefinition, Queue queue,
            Exchange exchange) {
        Binding binding = createBinding(queue, exchange, contract);

        final String bindingName = generateBindingBeanName(contract, exchangeDefinition.getBeanName());
        beanFactory.registerSingleton(bindingName, binding);
        readyBindings.add(new AbstractMap.SimpleEntry<>(contract, exchangeDefinition));

        return binding;
    }


    private Binding createBinding(Queue queue, Exchange exchange, Class<? extends BaseSeamlessTalkRabbitContract> contract) {
        //For future purposes
        SeamlessTalkRabbitContract params = contract.getAnnotation(SeamlessTalkRabbitContract.class);

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
                                    .with(queue.getName())
                                    .noargs();
        }

        return binding;
    }

    private ExchangeDefinition initDefaultExchangeDefinition(String name, boolean durable) {
        final ExchangeDefinition exchangeDefinition = new ExchangeDefinition(ExchangeType.DIRECT, durable);
        exchangeDefinition.setBeanName(name);
        return exchangeDefinition;
    }
}

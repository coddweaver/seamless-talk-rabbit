package com.coddweaver.seamless.talk.rabbit.generation;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitListener;
import com.coddweaver.seamless.talk.rabbit.helpers.CaseUtils;
import com.coddweaver.seamless.talk.rabbit.helpers.RoutesGenerationUtils;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.coddweaver.seamless.talk.rabbit.helpers.RoutesGenerationUtils.*;


@SuppressWarnings("UnusedReturnValue")
@Service
@Slf4j
public final class RoutesGenerator {

    private final Set<Class<?>> readyExchanges = new HashSet<>();
    private final Set<Class<?>> readyContracts = new HashSet<>();

    private final Exchange defaultDlx;
    private final Exchange defaultExchange;
    private final ConfigurableBeanFactory beanFactory;

    @Value("${spring.application.name:}")
    private String applicationName;

    public RoutesGenerator(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;

        this.defaultDlx = defineDefaultExchange(DEFAULT_DLX_NAME, DEFAULT_DLX_BEAN_NAME, false);
        this.defaultExchange = defineDefaultExchange(DEFAULT_EXCHANGE_NAME, DEFAULT_EXCHANGE_BEAN_NAME, false);
    }

    public Queue getQueue(Class<?> contract, Class<?> listenerClass) {
        final SeamlessTalkRabbitContract params = getContractParams(contract);
        if (params.exchangeType() == ExchangeType.DIRECT && readyContracts.contains(contract)) {
            return beanFactory.getBean(generateQueueBeanName(contract), Queue.class);
        }

        return processContract(contract, listenerClass);
    }

    public Binding getBinding(Class<?> contract) {
        return generateBindingData(contract);
    }

    private Queue processContract(Class<?> contract, Class<?> listenerClass) {
        SeamlessTalkRabbitContract params = getContractParams(contract);
        final Queue queue = defineQueue(contract, listenerClass);
        if (params.exchangeType() == ExchangeType.DIRECT) {
            defineDefaultBinding(contract, queue);
        } else {
            final Exchange exchange = defineExchange(contract);
            defineBinding(contract, listenerClass, queue, exchange);
        }

        return queue;
    }

    private Binding generateBindingData(Class<?> contract) {
        final String destination = generateQueueName(contract);

        SeamlessTalkRabbitContract params = getContractParams(contract);
        if (params.exchangeType() == ExchangeType.DIRECT) {
            return new Binding(destination,
                               Binding.DestinationType.EXCHANGE,
                               defaultExchange.getName(),
                               destination, null);
        } else {
            return new Binding(destination,
                               Binding.DestinationType.EXCHANGE,
                               RoutesGenerationUtils.generateExchangeName(contract),
                               destination, null);
        }


    }

    private Queue defineQueue(Class<?> contract, Class<?> listenerClass) {
        SeamlessTalkRabbitContract params = getContractParams(contract);

        String queueName;
        String additionalBeanNamePart;
        QueueBuilder queueBuilder;
        if (params.exchangeType() == ExchangeType.DIRECT) {
            queueName = generateQueueName(contract);
            additionalBeanNamePart = "";
        } else {
            if (Strings.isNotBlank(this.applicationName)) {
                queueName = generateQueueName(contract) + "." + this.applicationName + "." + listenerClass.getSimpleName();
            } else {
                log.warn("It is highly recommended to set @" + SeamlessTalkRabbitListener.class.getSimpleName()
                                 + ".name() on listeners or spring.application.name property for contracts with type different than DIRECT otherwise it causes long listener's "
                                 + "queue name contains canonical name of class. Found in " + listenerClass);
                queueName = generateQueueName(contract) + "." + listenerClass.getCanonicalName();
            }
            additionalBeanNamePart = listenerClass.getSimpleName();
        }

        if (Strings.isNotBlank(params.name())) {
            queueName = params.name();
            additionalBeanNamePart = CaseUtils.convert(queueName, CaseFormat.UPPER_CAMEL);
        }

        String queueBeanName = generateQueueBeanName(contract, additionalBeanNamePart);
        queueBuilder = params.durable()
                       ? QueueBuilder.durable(queueName)
                       : QueueBuilder.nonDurable(queueName);
        if (params.lazy()) {
            queueBuilder = queueBuilder.lazy();
        }
        if (params.messageTTL() > 0) {
            queueBuilder = queueBuilder.ttl(params.messageTTL());
        }

        if (params.exchangeType() != ExchangeType.DIRECT) {
            queueBuilder = queueBuilder.autoDelete();
        }

        final Queue dlq = QueueBuilder.nonDurable(generateDlqName(queueName))
                                      .build();
        final String dlqBeanName = generateDlqBeanName(contract, additionalBeanNamePart);
        beanFactory.registerSingleton(dlqBeanName, dlq);

        final Binding binding = BindingBuilder.bind(dlq)
                                              .to(defaultDlx)
                                              .with(queueName)
                                              .noargs();
        final String dlqBindingName = generateDlqBindingBeanName(contract, additionalBeanNamePart);
        beanFactory.registerSingleton(dlqBindingName, binding);

        queueBuilder = queueBuilder
                .deadLetterExchange(DEFAULT_DLX_NAME)
                .deadLetterRoutingKey(queueName);

        final Queue queue = queueBuilder.build();

        beanFactory.registerSingleton(queueBeanName, queue);
        readyContracts.add(contract);

        return queue;
    }

    private Exchange defineDefaultExchange(String exchangeName, String exchangeBeanName, boolean durable) {
        final Exchange exchange = ExchangeBuilder.directExchange(exchangeName)
                                                 .durable(durable)
                                                 .build();

        beanFactory.registerSingleton(exchangeBeanName, exchange);
        return exchange;
    }

    private Binding defineDefaultBinding(Class<?> contract, Queue queue) {
        Binding binding = createBinding(queue, defaultExchange);

        final String bindingBeanName = generateBindingBeanName(contract);
        beanFactory.registerSingleton(bindingBeanName, binding);

        return binding;
    }


    private Exchange defineExchange(Class<?> contract) {
        String exchangeBeanName = generateExchangeBeanName(contract);

        if (readyExchanges.contains(contract)) {
            return beanFactory.getBean(exchangeBeanName, Exchange.class);
        }

        ExchangeBuilder exchangeBuilder;
        String exchangeName = generateExchangeName(contract);
        final SeamlessTalkRabbitContract contractParams = getContractParams(contract);
        final ExchangeType exchangeType = contractParams.exchangeType();
        switch (exchangeType) {
            case DIRECT:
                exchangeBuilder = ExchangeBuilder.directExchange(exchangeName);
                break;
            case FANOUT:
                exchangeBuilder = ExchangeBuilder.fanoutExchange(exchangeName);
                break;
            default:
                throw new UnsupportedOperationException("Cannot generate exchange with type " + exchangeType);
        }

        final Exchange exchange = exchangeBuilder.durable(contractParams.durable())
                                                 .build();
        beanFactory.registerSingleton(exchangeBeanName, exchange);
        readyExchanges.add(contract);

        return exchange;
    }

    private Binding defineBinding(Class<?> contract, Class<?> listenerClass, Queue queue, Exchange exchange) {
        Binding binding = createBinding(queue, exchange);

        final String bindingName = generateBindingBeanName(contract, listenerClass.getSimpleName());
        beanFactory.registerSingleton(bindingName, binding);

        return binding;
    }


    private Binding createBinding(Queue queue, Exchange exchange) {
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
}

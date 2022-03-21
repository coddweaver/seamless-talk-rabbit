package com.coddweaver.seamless.talk.rabbit.generation;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitListener;
import com.coddweaver.seamless.talk.rabbit.helpers.RoutesGenerationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.amqp.core.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.coddweaver.seamless.talk.rabbit.helpers.RoutesGenerationUtils.*;


/**
 * Performs routes auto-generation in Rabbit, depends on parameters in all found {@link SeamlessTalkRabbitContract}.
 *
 * <p>Depends on {@link SeamlessTalkRabbitContract#exchangeType()} it will create:</p>
 * <p>- either a single queue for all listeber with the name of contract and bind it to default direct exchange;</p>
 * <p>- or a special exchange for the contracnt and a queue for every listener with the name of it and bind them together; </p>
 *
 * @author Andrey Buturlakin
 * @see SeamlessTalkRabbitContract
 */

@SuppressWarnings("UnusedReturnValue")
@Service
@Slf4j
public final class RoutesGenerator implements ApplicationContextAware {

    private final Set<Class<?>> readyExchanges = new HashSet<>();
    private final Set<Class<?>> readyContracts = new HashSet<>();
    private final Set<String> listenerQueueNames = new HashSet<>();
    private final Set<String> listenerQueueBeanNames = new HashSet<>();

    private final Exchange defaultDlx;
    private final Exchange defaultExchange;
    private final ConfigurableBeanFactory beanFactory;


    @Value("${spring.application.name:}")
    private String applicationName;
    private String corePackageName;

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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        initCorePackageName(applicationContext);
    }

    private Queue processContract(Class<?> contract, Class<?> listenerClass) {
        SeamlessTalkRabbitContract params = getContractParams(contract);
        final Queue queue = defineQueue(contract, listenerClass);
        if (params.exchangeType() == ExchangeType.DIRECT) {
            defineDefaultBinding(contract, queue);
        } else {
            final Exchange exchange = defineExchange(contract);
            defineFanoutBinding(contract, listenerClass, queue, exchange);
        }

        return queue;
    }

    private Binding generateBindingData(Class<?> contract) {
        final String destination = generateQueueName(contract);

        SeamlessTalkRabbitContract params = getContractParams(contract);
        if (params.exchangeType() == ExchangeType.FANOUT) {
            return new Binding(destination,
                               Binding.DestinationType.EXCHANGE,
                               RoutesGenerationUtils.generateExchangeName(contract),
                               Strings.EMPTY, null);
        }

        return new Binding(destination,
                           Binding.DestinationType.EXCHANGE,
                           defaultExchange.getName(),
                           destination, null);
    }

    private Queue defineQueue(Class<?> contract, Class<?> listenerClass) {
        SeamlessTalkRabbitContract params = getContractParams(contract);

        String queueName;
        String beanNamePostfix;
        QueueBuilder queueBuilder;
        if (params.exchangeType() == ExchangeType.DIRECT) {
            queueName = generateQueueName(contract);
            beanNamePostfix = "";
        } else {
            if (Strings.isNotBlank(this.applicationName)) {
                queueName = generateQueueName(contract) + "." + this.applicationName + "." + listenerClass.getSimpleName();
            } else {
                log.warn("It is highly recommended to set @" + SeamlessTalkRabbitListener.class.getSimpleName()
                                 + ".name() on listeners or spring.application.name property for contracts with type different than DIRECT\notherwise it causes long listener's "
                                 + "queue name contains part of canonical name of class. Found in " + listenerClass);
                queueName = generateQueueName(contract) + "." + getRelativeClassPath(listenerClass);
            }
            if (listenerQueueNames.contains(queueName)) {
                throw new IllegalStateException(
                        "Generated two same queue names for listeners. Please provide unique class names for all listeners in module.");
            }
            listenerQueueNames.add(queueName);
            beanNamePostfix = listenerClass.getSimpleName();
        }

        String queueBeanName = generateQueueBeanName(contract, beanNamePostfix);
        if (listenerQueueBeanNames.contains(queueBeanName)) {
            throw new IllegalStateException(
                    "Generated two same queue beanNames for listeners. Please provide unique class names for all listeners in module.");
        }
        queueBuilder = params.durable()
                       ? QueueBuilder.durable(queueName)
                       : QueueBuilder.nonDurable(queueName);
        if (params.lazy()) {
            queueBuilder = queueBuilder.lazy();
        }
        if (params.messageTtl() > 0) {
            queueBuilder = queueBuilder.ttl(params.messageTtl());
        }

        if (params.dlqEnabled()) {
            defineDlq(contract, queueName, beanNamePostfix);
        }

        final Queue queue = queueBuilder
                .deadLetterExchange(DEFAULT_DLX_NAME)
                .deadLetterRoutingKey(queueName)
                .build();

        beanFactory.registerSingleton(queueBeanName, queue);
        readyContracts.add(contract);

        return queue;
    }

    private void defineDlq(Class<?> contract, String queueName, String beanNamePostfix) {
        final Queue dlq = QueueBuilder.nonDurable(generateDlqName(queueName))
                                      .build();
        final String dlqBeanName = generateDlqBeanName(contract, beanNamePostfix);
        beanFactory.registerSingleton(dlqBeanName, dlq);

        final Binding binding = BindingBuilder.bind(dlq)
                                              .to(defaultDlx)
                                              .with(queueName)
                                              .noargs();
        final String dlqBindingName = generateDlqBindingBeanName(contract, beanNamePostfix);
        beanFactory.registerSingleton(dlqBindingName, binding);
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

    private Binding defineFanoutBinding(Class<?> contract, Class<?> listenerClass, Queue queue, Exchange exchange) {
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

    private String getRelativeClassPath(Class<?> listenerClass) {
        final String canonicalName = listenerClass.getCanonicalName();

        if (canonicalName.startsWith(corePackageName)) {
            final String[] split = corePackageName.split("\\.");
            return split[split.length - 1] + canonicalName.substring(corePackageName.length());
        }

        return canonicalName;
    }

    private void initCorePackageName(ApplicationContext context) {
        final Object startClass = context.getBeansWithAnnotation(SpringBootApplication.class)
                                         .values()
                                         .stream()
                                         .findFirst()
                                         .orElseThrow();
        this.corePackageName = startClass.getClass()
                                         .getPackageName();
    }
}

package com.coddweaver.services.weaver.rabbit.annotations;

import com.coddweaver.services.weaver.rabbit.generation.QueueGenerator;
import com.coddweaver.services.weaver.rabbit.generation.RabbitApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.MultiMethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.adapter.ReplyPostProcessor;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Validator;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Slf4j
public class AutoGenRabbitListenerBeanPostProcessor implements BeanPostProcessor,
                                                               Ordered,
                                                               BeanFactoryAware,
                                                               EnvironmentAware,
                                                               SmartInitializingSingleton {

//region Fields
    public static final String DEFAULT_RABBIT_LISTENER_CONTAINER_FACTORY_BEAN_NAME = "rabbitListenerContainerFactory";

    public static final String RABBIT_EMPTY_STRING_ARGUMENTS_PROPERTY = "spring.rabbitmq.emptyStringArguments";
    private final Set<String> emptyStringArguments = new HashSet<>();
    private final RabbitHandlerMethodFactoryAdapter messageHandlerMethodFactory =
            new RabbitHandlerMethodFactoryAdapter();
    private final RabbitListenerEndpointRegistrar registrar = new RabbitListenerEndpointRegistrar();
    private final AtomicInteger counter = new AtomicInteger();
    private final String defaultContainerFactoryBeanName = DEFAULT_RABBIT_LISTENER_CONTAINER_FACTORY_BEAN_NAME;
    private final Charset charset = StandardCharsets.UTF_8;
    private BeanFactory beanFactory;
    @Nullable
    private RabbitListenerEndpointRegistry endpointRegistry;
    private BeanExpressionResolver resolver = new StandardBeanExpressionResolver();
    private BeanExpressionContext expressionContext;
    private QueueGenerator queueGenerator;

    public AutoGenRabbitListenerBeanPostProcessor() {
        this.emptyStringArguments.add("x-dead-letter-exchange");
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public void setEnvironment(Environment environment) {
        String property = environment.getProperty(RABBIT_EMPTY_STRING_ARGUMENTS_PROPERTY, String.class);
        if (property != null) {
            this.emptyStringArguments.addAll(StringUtils.commaDelimitedListToSet(property));
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.resolver = ((ConfigurableListableBeanFactory) beanFactory).getBeanExpressionResolver();
            this.expressionContext = new BeanExpressionContext((ConfigurableListableBeanFactory) beanFactory, null);
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.registrar.setBeanFactory(this.beanFactory);

        if (this.beanFactory instanceof ListableBeanFactory) {
            Map<String, RabbitListenerConfigurer> instances =
                    ((ListableBeanFactory) this.beanFactory).getBeansOfType(RabbitListenerConfigurer.class);
            for (RabbitListenerConfigurer configurer : instances.values()) {
                configurer.configureRabbitListeners(this.registrar);
            }
        }

        if (this.registrar.getEndpointRegistry() == null) {
            if (this.endpointRegistry == null) {
                Assert.state(this.beanFactory != null,
                             "BeanFactory must be set to find endpoint registry by bean name");
                this.endpointRegistry = this.beanFactory.getBean(
                        RabbitListenerConfigUtils.RABBIT_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME,
                        RabbitListenerEndpointRegistry.class);
            }
            this.registrar.setEndpointRegistry(this.endpointRegistry);
        }

        if (this.defaultContainerFactoryBeanName != null) {
            this.registrar.setContainerFactoryBeanName(this.defaultContainerFactoryBeanName);
        }

        // Set the custom handler method factory once resolved by the configurer
        MessageHandlerMethodFactory handlerMethodFactory = this.registrar.getMessageHandlerMethodFactory();
        if (handlerMethodFactory != null) {
            this.messageHandlerMethodFactory.setMessageHandlerMethodFactory(handlerMethodFactory);
        }

        // Actually register all listeners
        this.registrar.afterPropertiesSet();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        final AutoGenRabbitListener autoGenAnnotation = targetClass.getAnnotation(AutoGenRabbitListener.class);
        if (autoGenAnnotation == null) {
            return bean;
        }
        if (!RabbitApi.class.isAssignableFrom(targetClass)) {
            throw new IllegalStateException("Found a " + targetClass + " with @" +
                                                    AutoGenRabbitListener.class.getSimpleName()
                                                    + " which not implements any interface derived from " + RabbitApi.class);
        }

        this.queueGenerator = this.beanFactory.getBean(QueueGenerator.class);
        final Method[] rabbitHandlers = findRabbitHandlers(targetClass);

        Assert.state(rabbitHandlers.length > 0, "Class with @" + AutoGenRabbitListener.class.getSimpleName()
                + " must have at least one method with @RabbitHandler annotation. Found: " + rabbitHandlers.length + " at "
                + bean.getClass());

        processMultiMethodListeners(autoGenAnnotation, rabbitHandlers, bean, beanName);

        return bean;
    }


    protected void assertBeanFactory() {
        Assert.state(this.beanFactory != null, "BeanFactory must be set to obtain container factory by bean name");
    }

    protected String noBeanFoundMessage(Object target, String listenerBeanName, String requestedBeanName,
            Class<?> expectedClass) {
        return "Could not register rabbit listener endpoint on ["
                + target + "] for bean " + listenerBeanName + ", no '" + expectedClass.getSimpleName() + "' with id '"
                + requestedBeanName + "' was found in the application context";
    }

    protected String resolveExpressionAsString(String value, String attribute) {
        Object resolved = resolveExpression(value);
        if (resolved instanceof String) {
            return (String) resolved;
        } else {
            throw new IllegalStateException("The [" + attribute + "] must resolve to a String. "
                                                    + "Resolved to [" + resolved.getClass() + "] for [" + value + "]");
        }
    }

    protected void processListener(MethodRabbitListenerEndpoint endpoint,
            AutoGenRabbitListener rabbitListener, Object bean, Object target, String beanName) {

        endpoint.setBean(bean);
        endpoint.setMessageHandlerMethodFactory(this.messageHandlerMethodFactory);
        endpoint.setId(getEndpointId(rabbitListener));
        endpoint.setQueueNames(resolveQueues(bean));
        endpoint.setConcurrency(resolveExpressionAsStringOrInteger(rabbitListener.concurrency(), "concurrency"));
        endpoint.setBeanFactory(this.beanFactory);
        endpoint.setReturnExceptions(resolveExpressionAsBoolean(rabbitListener.returnExceptions()));
        resolveErrorHandler(endpoint, rabbitListener);
        String group = rabbitListener.group();
        if (StringUtils.hasText(group)) {
            Object resolvedGroup = resolveExpression(group);
            if (resolvedGroup instanceof String) {
                endpoint.setGroup((String) resolvedGroup);
            }
        }
        String autoStartup = rabbitListener.autoStartup();
        if (StringUtils.hasText(autoStartup)) {
            endpoint.setAutoStartup(resolveExpressionAsBoolean(autoStartup));
        }

        endpoint.setExclusive(rabbitListener.exclusive());
        String priority = resolveExpressionAsString(rabbitListener.priority(), "priority");
        if (StringUtils.hasText(priority)) {
            try {
                endpoint.setPriority(Integer.valueOf(priority));
            }
            catch (NumberFormatException ex) {
                throw new BeanInitializationException("Invalid priority value for " +
                                                              rabbitListener + " (must be an integer)", ex);
            }
        }

        resolveExecutor(endpoint, rabbitListener, target, beanName);
        resolveAdmin(endpoint, rabbitListener, target);
        resolveAckMode(endpoint, rabbitListener);
        resolvePostProcessor(endpoint, rabbitListener, target, beanName);
        resolveMessageConverter(endpoint, rabbitListener, target, beanName);
        resolveReplyContentType(endpoint, rabbitListener);

        this.registrar.registerEndpoint(endpoint, null);
    }

    private void processMultiMethodListeners(AutoGenRabbitListener classLevelListener, Method[] multiMethods,
            Object bean, String beanName) {

        List<Method> checkedMethods = new ArrayList<>();
        Method defaultMethod = null;
        for (Method method : multiMethods) {
            Method checked = checkProxy(method, bean);
            if (AnnotationUtils.findAnnotation(method, RabbitHandler.class)
                               .isDefault()) { // NOSONAR never null
                final Method toAssert = defaultMethod;
                Assert.state(toAssert == null, () -> "Only one @RabbitHandler can be marked 'isDefault', found: "
                        + toAssert.toString() + " and " + method);
                defaultMethod = checked;
            }
            checkedMethods.add(checked);
        }

        MultiMethodRabbitListenerEndpoint endpoint =
                new MultiMethodRabbitListenerEndpoint(checkedMethods, defaultMethod, bean);
        processListener(endpoint, classLevelListener, bean, bean.getClass(), beanName);
    }

    private Method[] findRabbitHandlers(Class<?> targetClass) {
        final List<Method> multiMethods = new ArrayList<>();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            RabbitHandler rabbitHandler = AnnotationUtils.findAnnotation(method, RabbitHandler.class);
            if (rabbitHandler != null) {
                multiMethods.add(method);
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);
        return multiMethods.stream()
                           .toArray(Method[]::new);
    }

    private Method checkProxy(Method methodArg, Object bean) {
        Method method = methodArg;
        if (AopUtils.isJdkDynamicProxy(bean)) {
            try {
                // Found a @AutoGenRabbitListener method on the target class for this JDK proxy ->
                // is it also present on the proxy itself?
                method = bean.getClass()
                             .getMethod(method.getName(), method.getParameterTypes());
                Class<?>[] proxiedInterfaces = ((Advised) bean).getProxiedInterfaces();
                for (Class<?> iface : proxiedInterfaces) {
                    try {
                        method = iface.getMethod(method.getName(), method.getParameterTypes());
                        break;
                    }
                    catch (@SuppressWarnings("unused") NoSuchMethodException noMethod) {
                    }
                }
            }
            catch (SecurityException ex) {
                ReflectionUtils.handleReflectionException(ex);
            }
            catch (NoSuchMethodException ex) {
                throw new IllegalStateException(String.format(
                        "@AutoGenRabbitListener method '%s' found on bean target class '%s', " +
                                "but not found in any interface(s) for a bean JDK proxy. Either " +
                                "pull the method up to an interface or switch to subclass (CGLIB) " +
                                "proxies by setting proxy-target-class/proxyTargetClass " +
                                "attribute to 'true'", method.getName(), method.getDeclaringClass()
                                                                               .getSimpleName()), ex);
            }
        }
        return method;
    }

    private void resolveErrorHandler(MethodRabbitListenerEndpoint endpoint, AutoGenRabbitListener rabbitListener) {
        Object errorHandler = resolveExpression(rabbitListener.errorHandler());
        if (errorHandler instanceof RabbitListenerErrorHandler) {
            endpoint.setErrorHandler((RabbitListenerErrorHandler) errorHandler);
        } else {
            String errorHandlerBeanName = resolveExpressionAsString(rabbitListener.errorHandler(), "errorHandler");
            if (StringUtils.hasText(errorHandlerBeanName)) {
                endpoint.setErrorHandler(
                        this.beanFactory.getBean(errorHandlerBeanName, RabbitListenerErrorHandler.class));
            }
        }
    }

    private void resolveAckMode(MethodRabbitListenerEndpoint endpoint, AutoGenRabbitListener rabbitListener) {
        String ackModeAttr = rabbitListener.ackMode();
        if (StringUtils.hasText(ackModeAttr)) {
            Object ackMode = resolveExpression(ackModeAttr);
            if (ackMode instanceof String) {
                endpoint.setAckMode(AcknowledgeMode.valueOf((String) ackMode));
            } else if (ackMode instanceof AcknowledgeMode) {
                endpoint.setAckMode((AcknowledgeMode) ackMode);
            } else {
                Assert.isNull(ackMode, "ackMode must resolve to a String or AcknowledgeMode");
            }
        }
    }

    private void resolveAdmin(MethodRabbitListenerEndpoint endpoint, AutoGenRabbitListener rabbitListener, Object adminTarget) {
        Object resolved = resolveExpression(rabbitListener.admin());
        if (resolved instanceof AmqpAdmin) {
            endpoint.setAdmin((AmqpAdmin) resolved);
        } else {
            String rabbitAdmin = resolveExpressionAsString(rabbitListener.admin(), "admin");
            if (StringUtils.hasText(rabbitAdmin)) {
                Assert.state(this.beanFactory != null, "BeanFactory must be set to resolve RabbitAdmin by bean name");
                try {
                    endpoint.setAdmin(this.beanFactory.getBean(rabbitAdmin, RabbitAdmin.class));
                }
                catch (NoSuchBeanDefinitionException ex) {
                    throw new BeanInitializationException("Could not register rabbit listener endpoint on [" +
                                                                  adminTarget + "], no " + RabbitAdmin.class.getSimpleName() + " with id '"
                                                                  +
                                                                  rabbitAdmin + "' was found in the application context", ex);
                }
            }
        }
    }

    private void resolveExecutor(MethodRabbitListenerEndpoint endpoint, AutoGenRabbitListener rabbitListener,
            Object execTarget, String beanName) {

        Object resolved = resolveExpression(rabbitListener.executor());
        if (resolved instanceof TaskExecutor) {
            endpoint.setTaskExecutor((TaskExecutor) resolved);
        } else {
            String execBeanName = resolveExpressionAsString(rabbitListener.executor(), "executor");
            if (StringUtils.hasText(execBeanName)) {
                assertBeanFactory();
                try {
                    endpoint.setTaskExecutor(this.beanFactory.getBean(execBeanName, TaskExecutor.class));
                }
                catch (NoSuchBeanDefinitionException ex) {
                    throw new BeanInitializationException(
                            noBeanFoundMessage(execTarget, beanName, execBeanName, TaskExecutor.class), ex);
                }
            }
        }
    }

    private void resolvePostProcessor(MethodRabbitListenerEndpoint endpoint, AutoGenRabbitListener rabbitListener,
            Object target, String beanName) {

        Object resolved = resolveExpression(rabbitListener.replyPostProcessor());
        if (resolved instanceof ReplyPostProcessor) {
            endpoint.setReplyPostProcessor((ReplyPostProcessor) resolved);
        } else {
            String ppBeanName = resolveExpressionAsString(rabbitListener.replyPostProcessor(), "replyPostProcessor");
            if (StringUtils.hasText(ppBeanName)) {
                assertBeanFactory();
                try {
                    endpoint.setReplyPostProcessor(this.beanFactory.getBean(ppBeanName, ReplyPostProcessor.class));
                }
                catch (NoSuchBeanDefinitionException ex) {
                    throw new BeanInitializationException(
                            noBeanFoundMessage(target, beanName, ppBeanName, ReplyPostProcessor.class), ex);
                }
            }
        }
    }

    private void resolveMessageConverter(MethodRabbitListenerEndpoint endpoint, AutoGenRabbitListener rabbitListener,
            Object target, String beanName) {

        Object resolved = resolveExpression(rabbitListener.messageConverter());
        if (resolved instanceof MessageConverter) {
            endpoint.setMessageConverter((MessageConverter) resolved);
        } else {
            String mcBeanName = resolveExpressionAsString(rabbitListener.messageConverter(), "messageConverter");
            if (StringUtils.hasText(mcBeanName)) {
                assertBeanFactory();
                try {
                    endpoint.setMessageConverter(this.beanFactory.getBean(mcBeanName, MessageConverter.class));
                }
                catch (NoSuchBeanDefinitionException ex) {
                    throw new BeanInitializationException(
                            noBeanFoundMessage(target, beanName, mcBeanName, MessageConverter.class), ex);
                }
            }
        }
    }

    private void resolveReplyContentType(MethodRabbitListenerEndpoint endpoint, AutoGenRabbitListener rabbitListener) {
        String contentType = resolveExpressionAsString(rabbitListener.replyContentType(), "replyContentType");
        if (StringUtils.hasText(contentType)) {
            endpoint.setReplyContentType(contentType);
            endpoint.setConverterWinsContentType(resolveExpressionAsBoolean(rabbitListener.converterWinsContentType()));
        }
    }

    private String getEndpointId(AutoGenRabbitListener rabbitListener) {
        if (StringUtils.hasText(rabbitListener.id())) {
            return resolveExpressionAsString(rabbitListener.id(), "id");
        } else {
            return "org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#" + this.counter.getAndIncrement();
        }
    }

    private String[] resolveQueues(Object bean) {
        final Class<?>[] allInterfaces = ClassUtils.getAllInterfaces(bean);
        Class<? extends RabbitApi> contract = null;
        for (Class<?> item : allInterfaces) {
            if (RabbitApi.class.isAssignableFrom(item)) {
                final Class<? extends RabbitApi> toAssert = contract;
                Assert.state(toAssert == null,
                             () -> "Only one interface derived from " + RabbitApi.class.getSimpleName() + " can be implemented " +
                                     "by class with @" + AutoGenRabbitListener.class.getSimpleName() + ", found: "
                                     + toAssert + " and " + item + " in " + bean.getClass());
                contract = (Class<? extends RabbitApi>) item;
            }
        }

        this.queueGenerator.processContract(contract);
        Queue queueBean = this.beanFactory.getBean(QueueGenerator.generateQueueBeanName(contract), Queue.class);

        return new String[]{queueBean.getName()};
    }

    private Object resolveExpression(String value) {
        String resolvedValue = resolve(value);

        return this.resolver.evaluate(resolvedValue, this.expressionContext);
    }

    private boolean resolveExpressionAsBoolean(String value) {
        return resolveExpressionAsBoolean(value, false);
    }

    private boolean resolveExpressionAsBooleanTrueDef(String value) {
        return resolveExpressionAsBoolean(value, true);
    }

    private boolean resolveExpressionAsBoolean(String value, boolean defaultValue) {
        Object resolved = resolveExpression(value);
        if (resolved instanceof Boolean) {
            return (Boolean) resolved;
        } else if (resolved instanceof String) {
            final String s = (String) resolved;
            return org.springframework.util.StringUtils.hasText(s)
                   ? Boolean.parseBoolean(s)
                   : defaultValue;
        } else {
            return defaultValue;
        }
    }

    private String resolveExpressionAsStringOrInteger(String value, String attribute) {
        if (!StringUtils.hasLength(value)) {
            return null;
        }
        Object resolved = resolveExpression(value);
        if (resolved instanceof String) {
            return (String) resolved;
        } else if (resolved instanceof Integer) {
            return resolved.toString();
        } else {
            throw new IllegalStateException("The [" + attribute + "] must resolve to a String. "
                                                    + "Resolved to [" + resolved.getClass() + "] for [" + value + "]");
        }
    }

    private void resolveAsString(Object resolvedValue, List<String> result, boolean canBeQueue, String what) {
        Object resolvedValueToUse = resolvedValue;
        if (resolvedValue instanceof String[]) {
            resolvedValueToUse = Arrays.asList((String[]) resolvedValue);
        }
        if (canBeQueue && resolvedValueToUse instanceof Queue) {
            result.add(((Queue) resolvedValueToUse).getName());
        } else if (resolvedValueToUse instanceof String) {
            result.add((String) resolvedValueToUse);
        } else if (resolvedValueToUse instanceof Iterable) {
            for (Object object : (Iterable<Object>) resolvedValueToUse) {
                resolveAsString(object, result, canBeQueue, what);
            }
        } else {
            throw new IllegalArgumentException(String.format(
                    "@AutoGenRabbitListener."
                            + what
                            + " can't resolve '%s' as a String[] or a String "
                            + (canBeQueue
                               ? "or a Queue"
                               : ""),
                    resolvedValue));
        }
    }

    private String resolve(String value) {
        if (this.beanFactory != null && this.beanFactory instanceof ConfigurableBeanFactory) {
            return ((ConfigurableBeanFactory) this.beanFactory).resolveEmbeddedValue(value);
        }
        return value;
    }

    private static class BytesToStringConverter implements Converter<byte[], String> {


        private final Charset charset;

        BytesToStringConverter(Charset charset) {
            this.charset = charset;
        }

        @Override
        public String convert(byte[] source) {
            return new String(source, this.charset);
        }

    }

    private class RabbitHandlerMethodFactoryAdapter implements MessageHandlerMethodFactory {

        private MessageHandlerMethodFactory factory;

        RabbitHandlerMethodFactoryAdapter() {
        }

        private MessageHandlerMethodFactory getFactory() {
            if (this.factory == null) {
                this.factory = createDefaultMessageHandlerMethodFactory();
            }
            return this.factory;
        }

        public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory rabbitHandlerMethodFactory1) {
            this.factory = rabbitHandlerMethodFactory1;
        }

        @Override
        public InvocableHandlerMethod createInvocableHandlerMethod(Object bean, Method method) {
            return getFactory().createInvocableHandlerMethod(bean, method);
        }

        private MessageHandlerMethodFactory createDefaultMessageHandlerMethodFactory() {
            DefaultMessageHandlerMethodFactory defaultFactory = new DefaultMessageHandlerMethodFactory();
            Validator validator = AutoGenRabbitListenerBeanPostProcessor.this.registrar.getValidator();
            if (validator != null) {
                defaultFactory.setValidator(validator);
            }
            defaultFactory.setBeanFactory(AutoGenRabbitListenerBeanPostProcessor.this.beanFactory);
            DefaultConversionService conversionService = new DefaultConversionService();
            conversionService.addConverter(
                    new AutoGenRabbitListenerBeanPostProcessor.BytesToStringConverter(AutoGenRabbitListenerBeanPostProcessor.this.charset));
            defaultFactory.setConversionService(conversionService);

            List<HandlerMethodArgumentResolver> customArgumentsResolver =
                    new ArrayList<>(AutoGenRabbitListenerBeanPostProcessor.this.registrar.getCustomMethodArgumentResolvers());
            defaultFactory.setCustomArgumentResolvers(customArgumentsResolver);
            defaultFactory.afterPropertiesSet();
            return defaultFactory;
        }

    }
}

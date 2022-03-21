package com.coddweaver.seamless.talk.rabbit.annotations;

import com.coddweaver.seamless.talk.rabbit.configs.LogToDlqRabbitErrorHandler;
import com.coddweaver.seamless.talk.rabbit.generation.RoutesGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils;
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.MultiMethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.adapter.ReplyPostProcessor;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Bean post-processor that registers methods annotated with {@link SeamlessTalkRabbitListener} to be invoked by a AMQP message listener
 * container created under the cover by a {@link org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory} according to the
 * parameters of the annotation.
 *
 * <p>Annotated classes can use flexible arguments as defined by {@link SeamlessTalkRabbitListener}.</p>
 *
 * <p>All processed listeners will have {@link LogToDlqRabbitErrorHandler} set by default which allows to put messages in bound dlq even
 * when listener's returnExceptions feature is enabled. By the way, this feature is enabled by default here and cannot be overridden.</p>
 *
 * @author Andrey Buturlakin
 * @see SeamlessTalkRabbitListener
 * @see RoutesGenerator
 * @see RabbitListenerEndpointRegistrar
 * @see MethodRabbitListenerEndpoint
 */
@Configuration
@Slf4j
public class SeamlessTalkRabbitListenerBeanPostProcessor implements BeanPostProcessor,
                                                                    Ordered,
                                                                    BeanFactoryAware,
                                                                    SmartInitializingSingleton {

    public static final String DEFAULT_RABBIT_LISTENER_CONTAINER_FACTORY_BEAN_NAME = "rabbitListenerContainerFactory";

    private final RabbitHandlerMethodFactoryAdapter messageHandlerMethodFactory =
            new RabbitHandlerMethodFactoryAdapter();
    private final RabbitListenerEndpointRegistrar registrar = new RabbitListenerEndpointRegistrar();
    private final AtomicInteger counter = new AtomicInteger();
    private final Charset charset = StandardCharsets.UTF_8;
    private BeanFactory beanFactory;
    @Nullable
    private RabbitListenerEndpointRegistry endpointRegistry;
    private BeanExpressionResolver resolver = new StandardBeanExpressionResolver();
    private BeanExpressionContext expressionContext;
    private RoutesGenerator routesGenerator;

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
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

        this.registrar.setContainerFactoryBeanName(DEFAULT_RABBIT_LISTENER_CONTAINER_FACTORY_BEAN_NAME);

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
        final SeamlessTalkRabbitListener autoGenAnnotation = targetClass.getAnnotation(SeamlessTalkRabbitListener.class);
        if (autoGenAnnotation == null) {
            return bean;
        }

        this.routesGenerator = this.beanFactory.getBean(RoutesGenerator.class);
        final Method[] rabbitHandlers = findRabbitHandlers(targetClass);

        Assert.state(rabbitHandlers.length > 0, "Class with @" + SeamlessTalkRabbitListener.class.getSimpleName()
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
            SeamlessTalkRabbitListener rabbitListener, Object bean, Object target, String beanName) {

        endpoint.setBean(bean);
        endpoint.setMessageHandlerMethodFactory(this.messageHandlerMethodFactory);
        endpoint.setId(getEndpointId(rabbitListener));
        endpoint.setQueueNames(resolveQueues(bean));
        endpoint.setConcurrency(resolveExpressionAsStringOrInteger(rabbitListener.concurrency(), "concurrency"));
        endpoint.setBeanFactory(this.beanFactory);
        endpoint.setReturnExceptions(true);
        endpoint.setErrorHandler(resolveErrorHandler());

        String group = rabbitListener.group();
        if (StringUtils.hasText(group)) {
            Object resolvedGroup = resolveExpression(group);
            if (resolvedGroup instanceof String) {
                endpoint.setGroup((String) resolvedGroup);
            }
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

        resolvePostProcessor(endpoint, rabbitListener, target, beanName);

        this.registrar.registerEndpoint(endpoint, null);
    }

    private void processMultiMethodListeners(SeamlessTalkRabbitListener classLevelListener, Method[] multiMethods,
            Object bean, String beanName) {

        List<Method> checkedMethods = new ArrayList<>();
        Method defaultMethod = null;
        for (Method method : multiMethods) {
            Method checked = checkProxy(method, bean);
            if (AnnotationUtils.findAnnotation(method, RabbitHandler.class)
                               .isDefault()) {
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
        final Class<?> contract = Arrays.stream(targetClass.getInterfaces())
                                        .filter(x -> x.getAnnotation(SeamlessTalkRabbitContract.class) != null)
                                        .findFirst()
                                        .orElseThrow();

        final List<Method> multiMethods = new ArrayList<>();
        ReflectionUtils.doWithMethods(contract, superMethod -> {
            final Method method = ReflectionUtils.findMethod(targetClass, superMethod.getName(), superMethod.getParameterTypes());
            RabbitHandler rabbitHandler = AnnotationUtils.findAnnotation(method, RabbitHandler.class);
            if (rabbitHandler == null) {
                throw new IllegalStateException("Found method '" + method.getName() + "' that overrides contract " + contract.getSimpleName() + " but don't have @"
                                                        + RabbitHandler.class.getSimpleName() + " annotation. Found in " + targetClass);
            }
            multiMethods.add(method);
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

    private RabbitListenerErrorHandler resolveErrorHandler() {
        return this.beanFactory.getBean(LogToDlqRabbitErrorHandler.BEAN_NAME, RabbitListenerErrorHandler.class);
    }

    private void resolvePostProcessor(MethodRabbitListenerEndpoint endpoint, SeamlessTalkRabbitListener rabbitListener,
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

    private String getEndpointId(SeamlessTalkRabbitListener rabbitListener) {
        if (StringUtils.hasText(rabbitListener.id())) {
            return resolveExpressionAsString(rabbitListener.id(), "id");
        } else {
            return "org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#" + this.counter.getAndIncrement();
        }
    }

    private String[] resolveQueues(Object bean) {
        final Class<?>[] allInterfaces = ClassUtils.getAllInterfaces(bean);
        Class<?> contract = null;
        for (Class<?> item : allInterfaces) {
            if (item.getAnnotation(SeamlessTalkRabbitContract.class) != null) {
                final Class<?> toAssert = contract;
                Assert.state(toAssert == null,
                             () -> "Only one @" + SeamlessTalkRabbitContract.class.getSimpleName() + " can be implemented " +
                                     "by class with @" + SeamlessTalkRabbitListener.class.getSimpleName() + ", found: "
                                     + toAssert + " and " + item + " in " + bean.getClass());
                contract = item;
            }
        }

        Assert.state(contract != null, () -> "Cannot detect implementable contact on " + bean.getClass()
                                                                                             .getCanonicalName());
        Queue queueBean = this.routesGenerator.getQueue(contract, bean.getClass());

        return new String[]{queueBean.getName()};
    }

    private Object resolveExpression(String value) {
        String resolvedValue = resolve(value);

        return this.resolver.evaluate(resolvedValue, this.expressionContext);
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
            Validator validator = SeamlessTalkRabbitListenerBeanPostProcessor.this.registrar.getValidator();
            if (validator != null) {
                defaultFactory.setValidator(validator);
            }
            defaultFactory.setBeanFactory(SeamlessTalkRabbitListenerBeanPostProcessor.this.beanFactory);
            DefaultConversionService conversionService = new DefaultConversionService();
            conversionService.addConverter(
                    new SeamlessTalkRabbitListenerBeanPostProcessor.BytesToStringConverter(
                            SeamlessTalkRabbitListenerBeanPostProcessor.this.charset));
            defaultFactory.setConversionService(conversionService);

            List<HandlerMethodArgumentResolver> customArgumentsResolver =
                    new ArrayList<>(SeamlessTalkRabbitListenerBeanPostProcessor.this.registrar.getCustomMethodArgumentResolvers());
            defaultFactory.setCustomArgumentResolvers(customArgumentsResolver);
            defaultFactory.afterPropertiesSet();
            return defaultFactory;
        }

    }
}

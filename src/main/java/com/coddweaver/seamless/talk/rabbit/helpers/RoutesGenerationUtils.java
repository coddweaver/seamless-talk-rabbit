package com.coddweaver.seamless.talk.rabbit.helpers;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.google.common.base.CaseFormat;

import java.util.HashMap;
import java.util.Map;

import static com.coddweaver.seamless.talk.rabbit.helpers.CaseUtils.firstToUpper;

/**
 * Contains methods generating names for all route elements (queues, exchanges, bindings, dlq).
 * <p> Most of the methods have internal caching for optimization. Ex. {@link RoutesGenerationUtils#getContractName(Class)}, {@link
 * RoutesGenerationUtils#getContractParams(Class)}, etc. </p>
 *
 * @author Andrey Buturlakin
 * @see CaseUtils
 */
public class RoutesGenerationUtils {

    public final static String COMMON_PREFIX = "seamless-talk.";
    public final static String DEFAULT_EXCHANGE_NAME = COMMON_PREFIX + "common";
    public final static String DEFAULT_DLX_NAME = DEFAULT_EXCHANGE_NAME + ".dlx";
    public final static String DEFAULT_DLX_BEAN_NAME = "defaultDlx";
    public final static String DEFAULT_EXCHANGE_BEAN_NAME = "defaultExchange";

    private final static String EXCHANGE_BEAN_NAME_POSTFIX = "Exchange";
    private final static String QUEUE_BEAN_NAME_POSTFIX = "Queue";
    private final static String BINDING_BEAN_NAME_POSTFIX = "Binding";
    private final static String DLQ_POSTFIX = ".dlq";
    private final static String DLQ_BEAN_NAME_POSTFIX = "Dlq";
    private final static String DLQ_BINDING_BEAN_NAME_POSTFIX = "DlqBinding";
    private final static Map<Class<?>, String> generatedContractNames = new HashMap<>();
    private final static Map<Class<?>, String> generatedQueueNames = new HashMap<>();
    private final static Map<Class<?>, String> generatedExchangeNames = new HashMap<>();
    private final static Map<Class<?>, String> generatedExchangeBeanNames = new HashMap<>();
    private final static Map<Class<?>, SeamlessTalkRabbitContract> contractParams = new HashMap<>();

    /**
     * Generates a bean name for {@link org.springframework.amqp.core.Exchange} according to the contract.
     *
     * @param contract {@link SeamlessTalkRabbitContract} interface.
     *
     * @return String exchange string beanName.
     */
    public static String generateExchangeBeanName(Class<?> contract) {
        return generatedExchangeBeanNames.computeIfAbsent(contract,
                                                          (value) -> CaseUtils.firstToLower(getContractName(contract)
                                                                                                    + EXCHANGE_BEAN_NAME_POSTFIX));
    }

    /**
     * Generates a bean name for {@link org.springframework.amqp.core.Queue} according to the contract with given postfix.
     *
     * @param contract        {@link SeamlessTalkRabbitContract} interface.
     * @param beanNamePostfix a postfix for the contract name.
     *
     * @return String queue beanName.
     */
    public static String generateQueueBeanName(Class<?> contract, String beanNamePostfix) {
        return getContractName(contract) + beanNamePostfix + QUEUE_BEAN_NAME_POSTFIX;
    }

    /**
     * Generates a bean name for {@link org.springframework.amqp.core.Exchange} according to the contract.
     *
     * @param contract {@link SeamlessTalkRabbitContract} interface.
     *
     * @return String queue beanName.
     */
    public static String generateQueueBeanName(Class<?> contract) {
        return getContractName(contract) + QUEUE_BEAN_NAME_POSTFIX;
    }

    /**
     * Generates a bean name for {@link org.springframework.amqp.core.Binding} according to the contract.
     *
     * @param contract {@link SeamlessTalkRabbitContract} interface.
     *
     * @return String binding beanName.
     */
    public static String generateBindingBeanName(Class<?> contract) {
        return getContractName(contract) + BINDING_BEAN_NAME_POSTFIX;
    }

    /**
     * Generates a bean name for {@link org.springframework.amqp.core.Binding} according to the contract with given postfix.
     *
     * @param contract        {@link SeamlessTalkRabbitContract} interface
     * @param beanNamePostfix a postfix for the contract name.
     *
     * @return String binding beanName.
     */
    public static String generateBindingBeanName(Class<?> contract, String beanNamePostfix) {
        return getContractName(contract) + beanNamePostfix + BINDING_BEAN_NAME_POSTFIX;
    }

    /**
     * Generates a bean name for Dead Letter {@link org.springframework.amqp.core.Queue} according to the contract.
     *
     * @param contract {@link SeamlessTalkRabbitContract} interface.
     *
     * @return String dlq beanName.
     */
    public static String generateDlqBeanName(Class<?> contract) {
        return getContractName(contract) + DLQ_BEAN_NAME_POSTFIX;
    }

    /**
     * Generates a bean name for Dead Letter {@link org.springframework.amqp.core.Queue} according to the contract  with given postfix.
     *
     * @param contract        {@link SeamlessTalkRabbitContract} interface.
     * @param beanNamePostfix a postfix for the contract name.
     *
     * @return String dlq beanName.
     */
    public static String generateDlqBeanName(Class<?> contract, String beanNamePostfix) {
        return getContractName(contract) + beanNamePostfix + DLQ_BEAN_NAME_POSTFIX;
    }

    /**
     * Generates a bean name for Dead Letter Queue {@link org.springframework.amqp.core.Binding} according to the contract.
     *
     * @param contract {@link SeamlessTalkRabbitContract} interface.
     *
     * @return String binding beanName.
     */
    public static String generateDlqBindingBeanName(Class<?> contract) {
        return getContractName(contract) + DLQ_BINDING_BEAN_NAME_POSTFIX;
    }

    /**
     * Generates a bean name for Dead Letter Queue {@link org.springframework.amqp.core.Binding} according to the contract with given
     * postfix.
     *
     * @param contract        {@link SeamlessTalkRabbitContract} interface.
     * @param beanNamePostfix a postfix for the contract name.
     *
     * @return String binding beanName.
     */
    public static String generateDlqBindingBeanName(Class<?> contract, String beanNamePostfix) {
        return getContractName(contract) + beanNamePostfix + DLQ_BINDING_BEAN_NAME_POSTFIX;
    }

    /**
     * Generates string name for the given contract.
     *
     * @param contract {@link SeamlessTalkRabbitContract} interface.
     *
     * @return String contract name.
     */
    public static String getContractName(Class<?> contract) {
        getContractParams(contract);
        return generatedContractNames.computeIfAbsent(contract, (value) -> CaseUtils.convert(value.getSimpleName(), CaseFormat.UPPER_CAMEL,
                                                                                             CaseFormat.LOWER_CAMEL));
    }


    /**
     * Generates exchange name for the given contract.
     *
     * @param contract {@link SeamlessTalkRabbitContract} interface.
     *
     * @return String exchange name.
     */
    public static String getExchangeName(Class<?> contract) {
        final SeamlessTalkRabbitContract params = getContractParams(contract);
        return getContractName(contract) + firstToUpper(params.exchangeType()
                                                              .getExchangeTypeCode());
    }

    /**
     * Generates dlq name for the given contract.
     *
     * @param queueName String queue name.
     *
     * @return String exchange name.
     */
    public static String generateDlqName(String queueName) {
        return queueName + DLQ_POSTFIX;
    }

    /**
     * Generates queue name for the given contract.
     *
     * @param contract {@link SeamlessTalkRabbitContract} interface.
     *
     * @return String queue name.
     */
    public static String generateQueueName(Class<?> contract) {
        return generatedQueueNames.computeIfAbsent(contract, (value) -> COMMON_PREFIX + CaseUtils.convert(value.getSimpleName(),
                                                                                                          CaseFormat.UPPER_CAMEL,
                                                                                                          CaseFormat.LOWER_HYPHEN));
    }

    /**
     * Generates exchange name for the given contract.
     *
     * @param contract {@link SeamlessTalkRabbitContract} interface.
     *
     * @return String exchange name.
     */
    public static String generateExchangeName(Class<?> contract) {
        return generatedExchangeNames.computeIfAbsent(contract,
                                                      (value) -> COMMON_PREFIX + CaseUtils.convert(getExchangeName(value),
                                                                                                   CaseFormat.LOWER_HYPHEN));
    }

    /**
     * Tries to get an instance of {@link SeamlessTalkRabbitContract} annotation from given class.
     *
     * @param contract {@link SeamlessTalkRabbitContract} interface.
     *
     * @return String exchange name.
     *
     * @throws IllegalStateException if there is no annotation.
     */
    public static SeamlessTalkRabbitContract getContractParams(Class<?> contract) {
        SeamlessTalkRabbitContract params = contract.getAnnotation(SeamlessTalkRabbitContract.class);
        if (params == null) {
            throw new IllegalStateException(
                    "Cannot process Seamless Talk contract without @" + SeamlessTalkRabbitContract.class.getSimpleName() + ". Found "
                            + contract);
        }
        return contractParams.computeIfAbsent(contract, (value) -> params);
    }
}

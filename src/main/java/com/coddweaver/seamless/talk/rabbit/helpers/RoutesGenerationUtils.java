package com.coddweaver.seamless.talk.rabbit.helpers;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.google.common.base.CaseFormat;

import java.util.HashMap;
import java.util.Map;

import static com.coddweaver.seamless.talk.rabbit.helpers.CaseUtils.firstToUpper;

public class RoutesGenerationUtils {

    //region Fields
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

    public static String generateExchangeBeanName(Class<?> contract) {
        return generatedExchangeBeanNames.computeIfAbsent(contract,
                                                          (value) -> CaseUtils.firstToLower(getContractName(contract)
                                                                                                    + EXCHANGE_BEAN_NAME_POSTFIX));
    }

    public static String generateQueueBeanName(Class<?> contract, String additionalBeanNamePart) {
        return getContractName(contract) + additionalBeanNamePart + QUEUE_BEAN_NAME_POSTFIX;
    }

    public static String generateQueueBeanName(Class<?> contract) {
        return getContractName(contract) + QUEUE_BEAN_NAME_POSTFIX;
    }

    public static String generateBindingBeanName(Class<?> contract) {
        return getContractName(contract) + BINDING_BEAN_NAME_POSTFIX;
    }

    public static String generateBindingBeanName(Class<?> contract, String additionalBeanNamePart) {
        return getContractName(contract) + additionalBeanNamePart + BINDING_BEAN_NAME_POSTFIX;
    }

    public static String generateDlqBindingBeanName(Class<?> contract) {
        return getContractName(contract) + DLQ_BINDING_BEAN_NAME_POSTFIX;
    }

    public static String generateDlqBindingBeanName(Class<?> contract, String additionalBeanNamePart) {
        return getContractName(contract) + additionalBeanNamePart + DLQ_BINDING_BEAN_NAME_POSTFIX;
    }

    public static String generateDlqBeanName(Class<?> contract) {
        return getContractName(contract) + DLQ_BEAN_NAME_POSTFIX;
    }

    public static String generateDlqBeanName(Class<?> contract, String additionalBeanNamePart) {
        return getContractName(contract) + additionalBeanNamePart + DLQ_BEAN_NAME_POSTFIX;
    }

    public static String getContractName(Class<?> contract) {
        return generatedContractNames.computeIfAbsent(contract, (value) -> CaseUtils.convert(value.getSimpleName(), CaseFormat.UPPER_CAMEL,
                                                                                             CaseFormat.LOWER_CAMEL));
    }

    public static String getExchangeName(Class<?> contract) {
        final SeamlessTalkRabbitContract params = getContractParams(contract);
        return getContractName(contract) + firstToUpper(params.exchangeType()
                                                              .getExchangeTypeCode());
    }

    public static String generateDlqName(String queueName) {
        return queueName + DLQ_POSTFIX;
    }

    public static String generateQueueName(Class<?> contract) {
        return generatedQueueNames.computeIfAbsent(contract, (value) -> COMMON_PREFIX + CaseUtils.convert(value.getSimpleName(),
                                                                                                          CaseFormat.UPPER_CAMEL,
                                                                                                          CaseFormat.LOWER_HYPHEN));
    }

    public static String generateExchangeName(Class<?> contract) {
        return generatedExchangeNames.computeIfAbsent(contract,
                                                      (value) -> COMMON_PREFIX + CaseUtils.convert(getExchangeName(value),
                                                                                                   CaseFormat.LOWER_HYPHEN));
    }

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

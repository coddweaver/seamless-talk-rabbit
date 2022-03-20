package com.coddweaver.seamless.talk.rabbit.helpers;

import com.coddweaver.seamless.talk.rabbit.generation.BaseSeamlessTalkRabbitContract;
import com.google.common.base.CaseFormat;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class RoutesGenerationUtils {

    public final static String COMMON_PREFIX = "seamless-talk.";
    public final static String DEFAULT_EXCHANGE_NAME = COMMON_PREFIX + "common";
    public final static String DEFAULT_DLX_NAME =  DEFAULT_EXCHANGE_NAME + ".dlx";
    public final static String DEFAULT_DLX_BEAN_NAME = "defaultDlx";
    public final static String DEFAULT_EXCHANGE_BEAN_NAME = "defaultExchange";

    private final static String EXCHANGE_BEAN_NAME_POSTFIX = "Exchange";
    private final static String QUEUE_BEAN_NAME_POSTFIX = "Queue";
    private final static String BINDING_BEAN_NAME_POSTFIX = "Binding";
    private final static String DLQ_POSTFIX = ".dlq";
    private final static String DLQ_BEAN_NAME_POSTFIX = "Dlq";
    private final static String DLQ_BINDING_BEAN_NAME_POSTFIX = "DlqBinding";
    private final static Map<Class<? extends BaseSeamlessTalkRabbitContract>, String> generatedContractNames = new HashMap<>();
    private final static Map<Class<? extends BaseSeamlessTalkRabbitContract>, String> generatedQueueNames = new HashMap<>();
    private final static Map<String, String> generatedExchangeNames = new HashMap<>();
    private final static Map<String, String> generatedExchangeBeanNames = new HashMap<>();
    private final static Map<Map.Entry<Class<? extends BaseSeamlessTalkRabbitContract>, String>, String> generatedBindingBeanNames = new HashMap<>();

    public static String generateExchangeBeanName(String exchangeDefinitionBeanName) {
        return generatedExchangeBeanNames.computeIfAbsent(exchangeDefinitionBeanName,
                                                          (value) -> CaseUtils.convert(exchangeDefinitionBeanName, CaseFormat.LOWER_CAMEL)
                                                                  + EXCHANGE_BEAN_NAME_POSTFIX);
    }

    public static String generateQueueBeanName(Class<? extends BaseSeamlessTalkRabbitContract> contract) {
        return getContractName(contract) + QUEUE_BEAN_NAME_POSTFIX;
    }

    public static String generateBindingBeanName(Class<? extends BaseSeamlessTalkRabbitContract> contract) {
        return getContractName(contract) + BINDING_BEAN_NAME_POSTFIX;
    }

    public static String generateBindingBeanName(Class<? extends BaseSeamlessTalkRabbitContract> contract, String exchangeDefinitionBeanName) {
        return generatedBindingBeanNames.computeIfAbsent(new AbstractMap.SimpleEntry<>(contract, exchangeDefinitionBeanName),
                                                         (value) -> getContractName(contract) + "To" + CaseUtils.convert(
                                                                 exchangeDefinitionBeanName, CaseFormat.UPPER_CAMEL)
                                                                 + BINDING_BEAN_NAME_POSTFIX);
    }

    public static String generateDlqBindingBeanName(Class<? extends BaseSeamlessTalkRabbitContract> contract) {
        return getContractName(contract) + DLQ_BINDING_BEAN_NAME_POSTFIX;
    }

    public static String generateDlqBeanName(Class<? extends BaseSeamlessTalkRabbitContract> contract) {
        return getContractName(contract) + DLQ_BEAN_NAME_POSTFIX;
    }

    public static String getContractName(Class<? extends BaseSeamlessTalkRabbitContract> contract) {
        return generatedContractNames.computeIfAbsent(contract, (value) -> CaseUtils.convert(value.getSimpleName(), CaseFormat.UPPER_CAMEL,
                                                                                             CaseFormat.LOWER_CAMEL));
    }

    public static String generateDlqName(String queueName) {
        return queueName + DLQ_POSTFIX;
    }

    public static String generateQueueName(Class<? extends BaseSeamlessTalkRabbitContract> contract) {
        return generatedQueueNames.computeIfAbsent(contract, (value) -> COMMON_PREFIX + CaseUtils.convert(contract.getSimpleName(),
                                                                                                          CaseFormat.UPPER_CAMEL,
                                                                                                          CaseFormat.LOWER_HYPHEN));
    }

    public static String generateExchangeName(String exchangeDefinitionBeanName) {
        return generatedExchangeNames.computeIfAbsent(exchangeDefinitionBeanName,
                                                      (value) -> COMMON_PREFIX + CaseUtils.convert(exchangeDefinitionBeanName,
                                                                                                   CaseFormat.LOWER_HYPHEN));
    }

}

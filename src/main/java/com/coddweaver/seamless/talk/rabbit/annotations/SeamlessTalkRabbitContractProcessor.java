package com.coddweaver.seamless.talk.rabbit.annotations;

import com.coddweaver.seamless.talk.rabbit.generation.BaseSeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.generation.RoutesGenerator;
import com.squareup.javapoet.*;
import org.apache.logging.log4j.util.Strings;
import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.context.annotation.Primary;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SupportedAnnotationTypes(SeamlessTalkRabbitContractProcessor.ANNOTATION_PATH)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class SeamlessTalkRabbitContractProcessor extends AbstractProcessor {

    public final static String ANNOTATION_PATH = "com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract";
    private final static String CLASS_NAME_POSTFIX = "RabbitApi";
    private final static String EXCHANGE_DEFINITION_BEAN_NAME_PATTERN = "[\\w_-]+";

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.size() == 0) {
            return false;
        }

        annotations.forEach(annotation -> processAnnotation(annotation, roundEnv));
        return true;
    }

    private void processAnnotation(TypeElement annotation, RoundEnvironment roundEnv) {
        info("Started api generation for found contracts: \n" + Strings.join(roundEnv.getElementsAnnotatedWith(annotation)
                                                                                     .stream()
                                                                                     .map(Object::toString)
                                                                                     .collect(
                                                                                             Collectors.toList()), '\n'));

        final Set<TypeElement> elementsAnnotatedWith = ElementFilter.typesIn(
                roundEnv.getElementsAnnotatedWith(annotation));
        elementsAnnotatedWith.forEach(this::processElement);
    }

    private void processElement(TypeElement typeElement) {
        if (typeElement.getKind() != ElementKind.INTERFACE) {
            error("Only interface can be annotated with " + SeamlessTalkRabbitContract.class + ". Skipping " + typeElement);
            return;
        }

        final List<ExecutableElement> methodElements = ElementFilter.methodsIn(
                elementUtils.getAllMembers(typeElement)
                            .stream()
                            .filter(x -> x.getEnclosingElement()
                                          .getKind() == ElementKind.INTERFACE
                                    && !x.getEnclosingElement()
                                         .getSimpleName()
                                         .contentEquals(
                                                 BaseSeamlessTalkRabbitContract.class.getSimpleName())
                            )
                            .collect(
                                    Collectors.toList())
        );

        final SeamlessTalkRabbitContract annotation = typeElement.getAnnotation(SeamlessTalkRabbitContract.class);
        if (annotation != null) {
            for (String exchangeDefinitionBeanName : annotation.exchangeDefs()) {
                if (!validateExchangeDefinitionBeanName(exchangeDefinitionBeanName)) {
                    throw new IllegalArgumentException(
                            "One of @" + SeamlessTalkRabbitContract.class.getSimpleName() + ".exchangeDefs value on " + typeElement
                                    + " has wrong format. Found: '" + exchangeDefinitionBeanName + "', the format is "
                                    + EXCHANGE_DEFINITION_BEAN_NAME_PATTERN);
                }

                generateApi(typeElement, methodElements, exchangeDefinitionBeanName);
            }
        }
        generateApi(typeElement, methodElements);
    }

    private void generateApi(TypeElement type, List<ExecutableElement> methods) {
        generateApi(type, methods, null);
    }

    private void generateApi(TypeElement type, List<ExecutableElement> methods, String exchangeDefinitionBeanName) {
        final String typeName = type.getSimpleName()
                                    .toString();

        final String amqpTemplateFieldName = "amqpTemplate";
        final String bindingFieldName = "binding";
        final String routesGeneratorParamName = "routesGenerator";

        final boolean isExDefBeanNameSet = Strings.isNotBlank(exchangeDefinitionBeanName);
        final String routesGeneratorCtorStatement = routesGeneratorParamName + ".getBinding("
                + type.asType() + ".class"
                + (isExDefBeanNameSet
                   ? ", \"" + exchangeDefinitionBeanName + "\""
                   : "")
                + ")";

        final MethodSpec ctor = MethodSpec.constructorBuilder()
                                            .addModifiers(Modifier.PUBLIC)
                                            .addParameter(AmqpTemplate.class, amqpTemplateFieldName)
                                            .addParameter(RoutesGenerator.class, routesGeneratorParamName)
                                            .addStatement(String.format("this.%1$s = %1$s", amqpTemplateFieldName))
                                            .addStatement(String.format("this.%s = %s", bindingFieldName,
                                                                        routesGeneratorCtorStatement))
                                            .build();

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(generateClassName(typeName, exchangeDefinitionBeanName))
                                               .addSuperinterface(type.asType())
                                               .addModifiers(Modifier.PUBLIC)
                                               .addAnnotation(SeamlessTalkRabbitApi.class)
                                               .addField(FieldSpec.builder(AmqpTemplate.class, amqpTemplateFieldName,
                                                                           Modifier.PRIVATE)
                                                                  .build())
                                               .addField(FieldSpec.builder(Binding.class, bindingFieldName, Modifier.PRIVATE)
                                                                  .build())
                                               .addMethod(ctor);

        if (!isExDefBeanNameSet) {
            typeBuilder = typeBuilder.addAnnotation(Primary.class);
        }

        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (ExecutableElement method : methods) {
            final TypeKind returnTypeKind = method.getReturnType()
                                                  .getKind();
            final String methodName = method.getSimpleName()
                                            .toString();
            final List<? extends VariableElement> parameters = method.getParameters();


            if (parameters.size() != 1) {
                error("Only one parameter required for api generation. Found " + parameters.size() + " in method " + method
                              + " of interface " + type);
                return;
            }

            VariableElement payloadParameter = parameters.get(0);
            final String payloadParameterName = "payload";

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                                                         .addModifiers(Modifier.PUBLIC)
                                                         .addParameter(ParameterSpec.builder(TypeName.get(payloadParameter.asType()),
                                                                                             payloadParameterName)
                                                                                    .build());

            if (returnTypeKind == TypeKind.VOID) {
                methodBuilder = methodBuilder.returns(TypeName.VOID)
                                             .addCode("convertAndSend(" + amqpTemplateFieldName + ", " + bindingFieldName
                                                              + ".getExchange(), "
                                                              + bindingFieldName + ".getRoutingKey(), " + payloadParameterName + ");");
            } else {
                final TypeName returnType = TypeName.get(method.getReturnType());
                methodBuilder = methodBuilder.returns(returnType)
                                             .addException(AmqpTimeoutException.class)
                                             .addCode("return " + "convertSendAndReceive(" + amqpTemplateFieldName + ", " + bindingFieldName
                                                              + ".getExchange(), "
                                                              + bindingFieldName + ".getRoutingKey(), " + payloadParameterName + ");");
            }

            methodSpecs.add(methodBuilder.build());
        }

        final TypeSpec build = typeBuilder.addMethods(methodSpecs)
                                          .build();

        final JavaFile build1 = JavaFile.builder(elementUtils.getPackageOf(type)
                                                             .toString(), build)
                                        .build();
        try {
            build1.writeTo(filer);
        }
        catch (IOException e) {
            error(e.toString());
        }
    }

    private void warn(String msg) {
        messager.printMessage(Diagnostic.Kind.WARNING, msg);
    }

    private void error(String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private void info(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private String generateClassName(String typeName, String exchangeDefinitionBeanName) {
        if (Strings.isNotBlank(exchangeDefinitionBeanName)) {
            return typeName + "To" + firstToUpper(exchangeDefinitionBeanName) + CLASS_NAME_POSTFIX;
        }

        return typeName + CLASS_NAME_POSTFIX;
    }

    private String firstToUpper(String str) {
        return str.substring(0, 1)
                  .toUpperCase() + str.substring(1);
    }

    private boolean validateExchangeDefinitionBeanName(String exchangeDefinitionBeanName) {
        return Pattern.matches("[\\w]+", exchangeDefinitionBeanName);
    }
}

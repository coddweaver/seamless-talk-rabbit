package com.coddweaver.seamless.talk.rabbit.annotations;

import com.coddweaver.seamless.talk.rabbit.generation.AbstractRabbitApi;
import com.coddweaver.seamless.talk.rabbit.generation.ExchangeType;
import com.coddweaver.seamless.talk.rabbit.generation.RoutesGenerator;
import com.squareup.javapoet.*;
import org.apache.logging.log4j.util.Strings;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Annotation processor that performs code generation of Api classes for the @{@link SeamlessTalkRabbitContract} interfaces. It will throw
 * an {@link IllegalStateException} if found some mistakes in SeamlessTalk contract defining.
 *
 * <p>Annotated interfaces can use flexible arguments as defined by {@link SeamlessTalkRabbitContract}.</p>
 *
 * @author Andrey Buturlakin
 * @see SeamlessTalkRabbitContract
 */
@SupportedAnnotationTypes(SeamlessTalkRabbitContractProcessor.ANNOTATION_PATH)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SeamlessTalkRabbitContractProcessor extends AbstractProcessor {

    public final static String ANNOTATION_PATH = "com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract";
    private final static String CLASS_NAME_POSTFIX = "RabbitApi";

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
        info("Started Seamless Talk rabbit api generation for found contracts: \n" +
                     Strings.join(roundEnv.getElementsAnnotatedWith(annotation)
                                          .stream()
                                          .map(Object::toString)
                                          .collect(Collectors.toList()),
                                  '\n'));

        final Set<TypeElement> elementsAnnotatedWith = ElementFilter.typesIn(
                roundEnv.getElementsAnnotatedWith(annotation));
        elementsAnnotatedWith.forEach(this::processElement);
    }

    private void processElement(TypeElement typeElement) {
        if (typeElement.getKind() != ElementKind.INTERFACE) {
            throw new IllegalStateException(
                    "Only interface can be annotated with " + SeamlessTalkRabbitContract.class + ". Skipping " + typeElement);
        }

        final List<ExecutableElement> methodElements = ElementFilter.methodsIn(
                elementUtils.getAllMembers(typeElement)
                            .stream()
                            .filter(element -> element.getEnclosingElement()
                                                      .getKind() == ElementKind.INTERFACE)
                            .collect(Collectors.toList())
        );

        final SeamlessTalkRabbitContract annotation = typeElement.getAnnotation(SeamlessTalkRabbitContract.class);
        generateApi(typeElement, methodElements, annotation.exchangeType());
    }

    private void generateApi(TypeElement type, List<ExecutableElement> methods, ExchangeType exchangeType) {
        final String typeName = type.getSimpleName()
                                    .toString();

        final String amqpTemplateFieldName = "amqpTemplate";
        final String bindingFieldName = "binding";
        final String routesGeneratorParamName = "routesGenerator";

        final String routesGeneratorCtorStatement = routesGeneratorParamName + ".getBinding(" + type.asType() + ".class)";
        final MethodSpec ctor = MethodSpec.constructorBuilder()
                                          .addModifiers(Modifier.PUBLIC)
                                          .addParameter(AmqpTemplate.class, amqpTemplateFieldName)
                                          .addParameter(RoutesGenerator.class, routesGeneratorParamName)
                                          .addStatement(String.format("this.%1$s = %1$s", amqpTemplateFieldName))
                                          .addStatement(String.format("this.%s = %s", bindingFieldName,
                                                                      routesGeneratorCtorStatement))
                                          .build();

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(generateClassName(typeName))
                                               .addJavadoc("This class was auto-generated by SeamlessTalk (RabbitMQ) library.\n"
                                                                   + "In contains the client implementation of " + typeName
                                                                   + " interface.\n")
                                               .addSuperinterface(type.asType())
                                               .superclass(AbstractRabbitApi.class)
                                               .addModifiers(Modifier.PUBLIC)
                                               .addAnnotation(Service.class)
                                               .addAnnotation(Primary.class)
                                               .addField(FieldSpec.builder(AmqpTemplate.class, amqpTemplateFieldName,
                                                                           Modifier.PRIVATE)
                                                                  .build())
                                               .addField(FieldSpec.builder(Binding.class, bindingFieldName, Modifier.PRIVATE)
                                                                  .build())
                                               .addMethod(ctor);

        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (ExecutableElement method : methods) {
            final TypeMirror returnType = method.getReturnType();
            final TypeKind returnTypeKind = returnType.getKind();
            final String methodName = method.getSimpleName()
                                            .toString();

            checkGenericType(returnType, type, method);
            final List<? extends VariableElement> parameters = method.getParameters();

            if (parameters.size() != 1) {
                throw new IllegalStateException(
                        "Only one parameter can exists for rabbit api generation. Found " + parameters.size() + " in method " + method
                                + " of interface " + type);
            }

            VariableElement payloadParameter = parameters.get(0);
            final String payloadParameterName = "payload";

            final TypeMirror payloadType = payloadParameter.asType();
            checkGenericType(payloadType, type, method);

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                                                         .addModifiers(Modifier.PUBLIC)
                                                         .addAnnotation(Override.class)
                                                         .addParameter(ParameterSpec.builder(TypeName.get(payloadType),
                                                                                             payloadParameterName)
                                                                                    .build());

            String commonStatementPart = amqpTemplateFieldName + ", " + bindingFieldName
                    + ".getExchange(), "
                    + bindingFieldName + ".getRoutingKey(), " + payloadParameterName + ");";


            if (returnTypeKind == TypeKind.VOID) {
                methodBuilder = methodBuilder.returns(TypeName.VOID)
                                             .addCode("convertAndSend(" + commonStatementPart);
            } else {
                if (exchangeType == ExchangeType.FANOUT) {
                    throw new IllegalStateException(
                            "Found method " + method + " with return value despite of set exchangeType=FANOUT in contract " + type
                                    + ".\n ReplyTo functionality cannot work with FANOUT exchanges. Please provide only void methods for FANOUT contracts.");
                }

                final TypeName returnTypeName = TypeName.get(returnType);
                methodBuilder = methodBuilder.returns(returnTypeName)
                                             .addCode("return " + "convertSendAndReceive(" + commonStatementPart);
            }

            methodSpecs.add(methodBuilder.build());
        }

        final TypeSpec build = typeBuilder.addMethods(methodSpecs)
                                          .build();

        final JavaFile file = JavaFile.builder(elementUtils.getPackageOf(type)
                                                           .toString(), build)
                                      .addFileComment(
                                              "This file was autogenerated by SeamlessTalk (RabbitMQ) library. \n"
                                                      + "Do not make any changes here. All them will be overwritten on rebuild!")
                                      .build();
        try {
            file.writeTo(filer);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void checkGenericType(TypeMirror type, TypeElement classType, ExecutableElement method) {
        if (type instanceof DeclaredType) {
            DeclaredType dclt = (DeclaredType) type;
            if (dclt.getTypeArguments()
                    .size() > 0) {
                throw new IllegalStateException(
                        "Detected generic type " + dclt + " in " + classType.getSimpleName() + "." + method.getSimpleName()
                                + " method. Contracts cannot have generics due to 'Type Erasure' feature in "
                                + "Java. Correct deserialization of such value in runtime is impossible.\n"
                                + "Possible solutions: \n"
                                + "* wrap it as DTO with generic field\n"
                                + "* create new class extends from this generic type");
            }
        }
    }

    private void warn(String msg) {
        messager.printMessage(Diagnostic.Kind.WARNING, msg);
    }

    private void info(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private String generateClassName(String typeName) {
        return typeName + CLASS_NAME_POSTFIX;
    }
}

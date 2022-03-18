package com.coddweaver.services.weaver.rabbit.processors;

import com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitApi;
import com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitQueue;
import com.coddweaver.services.weaver.rabbit.generation.QueueGenerator;
import com.coddweaver.services.weaver.rabbit.generation.RabbitApi;
import com.squareup.javapoet.*;
import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;

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
import java.util.stream.Collectors;

@SupportedAnnotationTypes(AutoGenRabbitQueueProcessor.ANNOTATION_PATH)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class AutoGenRabbitQueueProcessor extends AbstractProcessor {

//region Fields
    public final static String ANNOTATION_PATH = "com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitQueue";

    private final static String CLASS_NAME_POSTFIX = "Api";

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
        debug("Found contracts" + roundEnv.getElementsAnnotatedWith(annotation));

        final Set<TypeElement> elementsAnnotatedWith = ElementFilter.typesIn(
                roundEnv.getElementsAnnotatedWith(annotation));
        elementsAnnotatedWith.forEach(this::processElement);
    }

    private void processElement(TypeElement typeElement) {
        if (typeElement.getKind() != ElementKind.INTERFACE) {
            error("Only interface can be annotated with " + AutoGenRabbitQueue.class + ". Skipping " + typeElement);
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
                                                 RabbitApi.class.getSimpleName())
                            )
                            .collect(
                                    Collectors.toList())
        );

        generateApi(typeElement, methodElements);
    }

    private void generateApi(TypeElement type, List<ExecutableElement> methods) {
        final String typeName = type.getSimpleName()
                                    .toString();

        final String amqpTemplateFieldName = "amqpTemplate";
        final String bindingFieldName = "binding";
        final String queueGeneratorParamName = "queueGenerator";

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(generateClassName(typeName))
                                               .addSuperinterface(type.asType())
                                               .addModifiers(Modifier.PUBLIC)
                                               .addAnnotation(AutoGenRabbitApi.class)
                                               .addField(FieldSpec.builder(AmqpTemplate.class, amqpTemplateFieldName,
                                                                           Modifier.PRIVATE)
                                                                  .build())
                                               .addField(FieldSpec.builder(Binding.class, bindingFieldName, Modifier.PRIVATE)
                                                                  .build())
                                               .addMethod(MethodSpec.constructorBuilder()
                                                                    .addModifiers(Modifier.PUBLIC)
                                                                    .addParameter(AmqpTemplate.class, amqpTemplateFieldName)
                                                                    .addParameter(QueueGenerator.class, queueGeneratorParamName)
                                                                    .addStatement(String.format("this.%1$s = %1$s", amqpTemplateFieldName))
                                                                    .addStatement(String.format("this.%s = %s", bindingFieldName,
                                                                                                queueGeneratorParamName + ".getBinding("
                                                                                                        + type.asType() + ".class)"))
                                                                    .build());


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

    private void debug(String msg) {
        messager.printMessage(Diagnostic.Kind.OTHER, msg);
    }

    private String generateClassName(String typeName) {
        return typeName + CLASS_NAME_POSTFIX;
    }
}

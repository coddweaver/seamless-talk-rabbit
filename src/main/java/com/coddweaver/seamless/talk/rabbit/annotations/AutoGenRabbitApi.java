package com.coddweaver.seamless.talk.rabbit.annotations;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Service
@DependsOn("queueGenerator")
public @interface AutoGenRabbitApi {

}
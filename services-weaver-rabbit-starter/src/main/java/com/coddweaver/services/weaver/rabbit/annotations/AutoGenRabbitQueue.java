package com.coddweaver.services.weaver.rabbit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoGenRabbitQueue {

    //region Public Methods
    String name() default "";

    boolean durable() default false;

    String rKey() default "";

    boolean lazy() default false;

    int messageTTL() default 0;
//endregion Public Methods
}
package com.coddweaver.seamless.talk.rabbit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SeamlessTalkRabbitContract {

    String name() default "";

    boolean durable() default false;

    String[] exchangeDefs() default {};

    boolean lazy() default false;

    int messageTTL() default 0;
}

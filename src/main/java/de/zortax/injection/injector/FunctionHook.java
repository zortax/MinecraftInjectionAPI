package de.zortax.injection.injector;// Created by leo on 27.05.18

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FunctionHook {
    String targetClass();
    String targetMethod();
    InjectPosition position() default InjectPosition.AFTER;
}

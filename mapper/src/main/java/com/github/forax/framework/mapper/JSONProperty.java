package com.github.forax.framework.mapper;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME) // SOURCE : voir l'annotation qu'à la compilation, RUNTIME : voir l'annotation à l'exécution
@Target({METHOD, RECORD_COMPONENT}) // Sur quoi j'ai le droit de mettre mon annotation (JSONProperty peut être mise sur un record et une méthode)
public @interface JSONProperty { // (@interface à lire @annotation) Annotation JSONProperty
  String value();
}
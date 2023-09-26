package org.github.forax.framework.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Stream;

public final class InterceptorRegistry {
  private final Map<Class<? extends Annotation>, List<AroundAdvice>> advices = new HashMap<>();

  private final Map<Class<? extends Annotation>, List<Interceptor>> interceptors = new HashMap<>();


  private final HashMap<Method, Invocation> cache = new HashMap<>();

  private Invocation computeInvocation(Method method) {
      return cache.computeIfAbsent(method, m  -> getInvocation(findInterceptors(m)));
  }

  InterceptorRegistry() {}

  public void addAroundAdvice(Class<? extends Annotation> annotationClass, AroundAdvice advice) {
      Objects.requireNonNull(annotationClass);
      Objects.requireNonNull(advice);
      // Put if absent the array of advices (with findAdvices)
      //advices.computeIfAbsent(annotationClass, __ -> new ArrayList<>()).add(advice);

      addInterceptor(annotationClass, (instance, method, args, invocation) -> {
          advice.before(instance, method, args);
          Object result = null;
          try {
            return result = invocation.proceed(instance, method, args);
          } finally {
              advice.after(instance, method, args, result);
          }
      });
  }

  public void addInterceptor(Class<? extends Annotation> annotationClass, Interceptor interceptor) {
        Objects.requireNonNull(annotationClass);
        Objects.requireNonNull(interceptor);
        // Put if absent the array of advices (with findAdvices)
        interceptors.computeIfAbsent(annotationClass, __ -> new ArrayList<>()).add(interceptor);
        cache.clear();
  }



  public static Invocation getInvocation(List<Interceptor> interceptorList) {
      Objects.requireNonNull(interceptorList);
      Invocation invocation = Utils::invokeMethod;

      List<Invocation> invocations = new ArrayList<>();
      for (Interceptor interceptor : interceptorList.reversed()) {
        Invocation finalInvocation = invocation;
        invocation = (instance, method, args) -> interceptor.intercept(instance, method, args, finalInvocation);
      }
      return invocation;
  }


  /*
   */
  List<Interceptor> findInterceptors(Method method) {
      Objects.requireNonNull(method);
      return Stream.of(
              Arrays.stream(method.getDeclaringClass().getAnnotations()),
              Arrays.stream(method.getAnnotations()),
              Arrays.stream(method.getParameterAnnotations()).flatMap(Arrays::stream)
      )
      .flatMap(s -> s)
      .flatMap(annotation -> interceptors.getOrDefault(annotation.annotationType(), List.of()).stream())
      .toList();




  }

  // Get all around advices for a given method from its annotation
  List<AroundAdvice> findAdvices(Method method) {
      Objects.requireNonNull(method);
      return Arrays.stream(method.getDeclaredAnnotations())
              .flatMap(annotation -> advices.getOrDefault(annotation.annotationType(), List.of()).stream())
              .toList();
  }

  // create the proxy implementing  the interface T (but calling before and after for each method)
  public <T> T createProxy(Class<T> interfaceType, T instance) {
    Objects.requireNonNull(interfaceType);
    Objects.requireNonNull(instance);

    return interfaceType.cast( // casting the proxy to the interface tye
        Proxy.newProxyInstance(
        interfaceType.getClassLoader(),
        new Class<?>[] { interfaceType },

        (Object __, Method method, Object[] args) -> {


            /* Version with only advices
          // call before of advice
          var advices = findAdvices(method);
          for(var advice : advices) {
            advice.before(instance, method, args);
          }

          // call method of advice
          Object result = null;
          try {
              result = Utils.invokeMethod(instance, method, args);
              return result;
          } finally {
            // call method after (event if exception)
              for(var advice : advices.reversed()) {
                  advice.after(instance, method, args, result);
              }
          }
          */

            //var interceptors = findInterceptors(method);
            var invocation = computeInvocation(method);
            return invocation.proceed(instance, method, args);
        }));
  }
}

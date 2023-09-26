package com.github.forax.framework.injector;

import javax.swing.text.html.Option;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class InjectorRegistry {
    private final Map<Class<?>, Supplier<?>> injector = new HashMap<>();

    public <T> void registerInstance(Class<T> type, T instance) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(instance);
        registerProvider(type, () -> instance);
    }

    public <T> T lookupInstance(Class<T> type) {
        Objects.requireNonNull(type);

        var instance = injector.get(type);
        if(instance == null) throw new IllegalStateException("There is no instance for the type " + type.getName());

        return type.cast(instance.get());
    }

    public <T> void registerProvider(Class<T> type, Supplier<? extends T> creator) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(creator);

        var object = injector.putIfAbsent(type, creator);
        if(object != null) throw new IllegalStateException("Type "+ type.getName() +" already registered !");
    }

    // Visible for testing
    public static List<PropertyDescriptor> findInjectableProperties(Class<?> type) {
        return Arrays.stream(Utils.beanInfo(type).getPropertyDescriptors())
                .filter(p -> {
                    var method = p.getWriteMethod();
                    return method != null && method.isAnnotationPresent(Inject.class);
                })
                .collect(Collectors.toList());
    }

    private <T> Optional<Constructor<?>> findInjectableConstructor(Class<?> type) {
        var constructors =
                Arrays.stream(type.getConstructors())
                        .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                        .toList();

        return switch (constructors.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(constructors.get(0));
            default -> throw new IllegalStateException("Too many injectable constructors !");
        };
    }


    public <T> void registerProviderClass(Class<T> providerClass) {
        Objects.requireNonNull(providerClass);
        registerProviderClass(providerClass, providerClass);
    }

    public <T> void registerProviderClass(Class<T> type, Class<? extends T> providerClass) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(providerClass);

        var constructor = findInjectableConstructor(providerClass)
                .orElseGet(() -> Utils.defaultConstructor(providerClass));

        var properties = findInjectableProperties(providerClass);

        registerProvider(type, () -> {
            var arguments = Arrays.stream(constructor.getParameterTypes())
                    .map(this::lookupInstance)
                    .toArray();

            var instance = Utils.newInstance(constructor, arguments);
            for (var p : properties) {
                Utils.invokeMethod(instance, p.getWriteMethod(), lookupInstance(p.getPropertyType()));
            }
            return providerClass.cast(instance);
        });

    }
}
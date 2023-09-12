package com.github.forax.framework.mapper;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;



public final class JSONWriter {
/*

  // Question 3
  private static final ClassValue<PropertyDescriptor[]> PROPERTY_DESCRIPTOR_CACHE = new ClassValue<>() {
    @Override
    protected PropertyDescriptor[] computeValue(Class<?> type) {
      return Utils.beanInfo(type).getPropertyDescriptors();
    }
  };
*/


  // Question 4
  @FunctionalInterface
  private interface Generator {
    String generate(JSONWriter writer, Object o);
  }

  // Question 4
  private static final ClassValue<List<Generator>> JSON_OBJECT_GENERATOR = new ClassValue<>() {
    @Override
    protected List<Generator> computeValue(Class<?> type) {
      return Arrays.stream(Utils.beanInfo(type).getPropertyDescriptors())
              .filter(p -> !p.getName().equals("class"))
              // Stream de generator car on type
              .<Generator>map(p -> {
                // Intérêt de faire des variables : certaines parties de la lambdas sont exécuter avant l'appel à la lambda
                var readMethod = p.getReadMethod(); // Precalculer la méthode qui vas être appellée

                var annotation = readMethod.getAnnotation(JSONProperty.class); // Récupère l'annotation JSONProperty si elle n'est pas nulle // Question 6

                //var key = "\"" + p.getName() + "\": "; // Permet d'exécuter la concaténation qu'une seule fois pour la classe // Question 4
                var key = "\"" + (annotation == null ? p.getName() : annotation.value()) + "\": "; // On prend la valeur d'annotation si elle n'est pas nulle // Question 6

                // On génère la lambda
                return (writer, o) -> key + writer.toJSON(Utils.invokeMethod(o, readMethod));
              })
              .toList();

    }
  };
 /*
  // Question 6
  @Retention(RUNTIME) // SOURCE : voir l'annotation qu'à la compilation, RUNTIME : voir l'annotation à l'exécution
  @Target({METHOD, RECORD_COMPONENT}) // Sur quoi j'ai le droit de mettre mon annotation (JSONProperty peut être mise sur un record et une méthode)
  public @interface JSONProperty { // (@interface à lire @annotation) Annotation JSONProperty
    String value();
  }
 */


  // Question 1
  public String toJSON(Object o) {
    return switch (o) {
      case null -> "null";
      case String s -> "\""+s+"\"";
      case Integer i -> i.toString();
      case Double d -> d.toString();
      case Boolean b ->  b.toString();
      //default -> throw new IllegalArgumentException("The object cannot be transformed to JSON"); // Question 1
      default -> objectToJson(o); // Question 2
    };
  }

  private String objectToJson(Object o) {
    /*

    // Question 2
    return Arrays
            .stream(Utils.beanInfo(o).getPropertyDescriptors();)
            .filter(p -> !p.getName().equals("class"))
            .map(p -> "\"" + p.getName() + "\": " + toJSON(Utils.invokeMethod(o, p.getReadMethod())))
            .collect(Collectors.joining(", ", "{", "}"));
     */

    /*

    // Question 3
    return Arrays
            .stream(PROPERTY_DESCRIPTOR_CACHE.get(o.getClass()))
            .filter(p -> !p.getName().equals("class"))
            .map(p -> "\"" + p.getName() + "\": " + toJSON(Utils.invokeMethod(o, p.getReadMethod())))
            .collect(Collectors.joining(", ", "{", "}"));
     */


    // Question 4
    return JSON_OBJECT_GENERATOR.get(o.getClass())
            .stream()
            .map(generator -> generator.generate(this, o))
            .collect(Collectors.joining(", ", "{", "}"));
  }




}

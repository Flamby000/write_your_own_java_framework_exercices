# TP2 Java Inside : Injection de dépendances
### Max Ducoudré - INFO2

-> L'injection d'objets dans des objets

InjectorRegistry 
    - lookUpInstance : Donne moi l'instance pour un type donné (apelle le constructeur pour nous)


Register instance 
    à partir d'un type et d'une instance

    Register provider : à partir d'un supplier de l'instance, ce supplier fabrique une instance

    registerProviderClass : Pour ma liste, tu vas utiliser un arraylist 



var registry = new InjectorRegistry();
registry.registerInstance(Point.class, new Point(0, 0)); // Si je demande un Point, j'aurais l'inst ance créée
registry.registerProvider(String.class, () -> "hello"); // Si je demande une string, j'aurais un hello
registry.registerProviderClass(Circle.class, Circle.class); //

var circle = registry.lookupInstance(Circle.class);
System.out.println(circle.center);  // Point(0, 0)
System.out.println(circle.name);  // hello    


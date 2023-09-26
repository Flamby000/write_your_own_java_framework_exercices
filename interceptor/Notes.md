# Interceptor

## Definition
- Function called before/after a method call
- Can handle arguments of return value of the method call
- Use annotation to define interceptor


## The around device API
- Interface with 2 methods : 
    - `void before(Object instance, Method method, Object[] args) throws Throwable;`
    - `void after(Object instance, Method method, Object[] args, Object result) throws Throwable;`
- Theses methods are respectively called before and after a call. 

## InterceptionRegisdtry
- Create a proxy : object that intercepts method calls implementing interface
- InvocationHandler is a functional interface called  to implement each method of the proxy : `Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);`
- The proxy give an instance of the interface to intercept (not directly the implemented interface). The methods of this interface will be called by the proxy before and after the call of the original implementation.

## The interceptor API
- Method calling the method to intercept (interface `Interceptor`) : `Object intercept(Method method, Object proxy, Object[] args, Invocation invocation) throws Throwable;`
- Invocation is the next interceptor in the chain : `Object proceed() throws Throwable;`
- Use example : Before each calls, check if the user is authenticated

## Aspect Oriented Programming (AOP)
- Interceptor is a way to use the AOP


## Interceptor annotation
- An inteceptor/advice is registered with an annotation


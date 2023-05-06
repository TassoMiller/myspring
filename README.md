# 手写一个简易的Spring IoC

本项目使用了大量的自定义注解和反射API，实现了一个简易的Spring IoC容器。

## IoC 核心思想

举个例子：

```java
// 对象的创建和使用在一起，耦合度强
private HelloDao helloDao = new HelloDaoImpl();
// 对象的创建交给了工厂，但当需求改变时，仍需要修改工厂类中的代码，硬编码问题始终没有得到解决
private HelloDao helloDao = （HelloDao）BeanFactory.getDao("helloDao");
// 使用注解或者XML的方式，方便进行管理
@Autowired
@Qulifier("hello01")
private HelloDao helloDao;
```

1、强依赖/紧耦合，编译之后无法修改，没有扩展性。

2、弱依赖/松耦合，编译之后仍然可以修改，让程序具有更好的扩展性。

自己放弃了创建对象的权限，将创建对象的权限交给了 BeanFactory，这种将控制权交给别人的思想，就是控制反转 IoC。

## Spring IoC 的使用

XML 和注解，XML 已经被淘汰了，目前主流的是基于注解的方式，Spring Boot 就是基于注解的方式。

```java
package com.southwind.spring.entity;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component("myOrder")
public class Order {
    @Value("xxx123")
    private String orderId;
    @Value("1000.0")
    private Float price;
}
```

```java
package com.southwind.spring.entity;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class Account {
    @Value("1")
    private Integer id;
    @Value("张三")
    private String name;
    @Value("22")
    private Integer age;
    @Autowired
    @Qualifier("order")
    private Order myOrder;
}
```

```java
package com.southwind.spring.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
    public static void main(String[] args) {
        //加载IoC容器
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext("com.southwind.spring.entity");
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        System.out.println(applicationContext.getBeanDefinitionCount());
        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
            System.out.println(applicationContext.getBean(beanDefinitionName));
        }
    }
}
```

当然在Spring Boot框架中，IoC容器已经不需要手动加载了，Spring Boot会自动识别注解所在的包进行扫描，同时也开放了一些自动的配置类实现扩展。

## IoC 基于注解的执行原理

### 手写代码的思路：

1、自定义一个 MyAnnotationConfigApplicationContext，构造器中传入要扫描的包。

2、获取这个包下的所有类。

3、遍历这些类，找出添加了 @Component 注解的类，获取它的 Class 和对应的 beanName，封装成一个 BeanDefinition，**存入集合 Set**，这个机会就是 IoC 自动装载的原材料。

4、遍历 Set 集合，通过反射机制创建对象，同时检测属性有没有添加 @Value 注解，如果有还需要给属性赋值，再将这些动态创建的对象以 k-v 的形式存入缓存区。

5、提供 getBean 等方法，通过 beanName 取出对应的 bean 即可。



这里举个例子，比如A依赖B，B依赖C，C依赖D。那么执行的过程将是这样：首先通过包扫描（ABCD肯定是使用了@Component注解的，因此一定能扫描到）将ABCD的类路径和beanName存入BeanDefinitions的集合中，其次遍历BeanDefinitions集合，通过类路径和无参构造器来创建实例，并且存入到缓存（Map集合）当中。然后再次进行扫描，将所有被@Autowired注解扫描的属性进行set注入。最终实现Ioc容器的创建。

这里有个问题，假如在进行set注入的时候，我先注入的是A中的B，但是B并没有完全赋值，那么A岂不是不完整吗？其实在单例的情况下，这个是不影响的，和顺序无关，因为A中放的是B的引用，而B的引用只对应IoC容器中的那唯一一个，那个B对象无论如何都会初始化完整的，所以A也是完整的。

但这里强调一下，**本项目并没有考虑循环依赖的情况，因为缓存只有一级**，后续可能会进行完善。



### 一些考虑的细节

#### Bean作用域

Spring Bean的作用域有以下五种：

- singleton：在spring IoC容器仅存在一个Bean实例，Bean以单例方式存在，bean作用域范围的默认值。
- prototype：每次从容器中调用Bean时，都返回一个新的实例，即每次调用getBean()时，相当于执行new XxxBean()。
- request：每次HTTP请求都会创建一个新的Bean，该作用域仅适用于WebApplicationContext环境。
- session：同一个HTTP Session共享一个Bean实例，不同Session使用不同的实例。
- global-session：一般用于Portlet应用环境，该作用域仅适用于WebApplicationContext环境。

本项目实现了singleton。注意一点，Spring单例Bean并不是线程安全的。



#### Spring懒加载

懒加载就是Spring容器启动的时候，先不创建对象，在第一次使用（获取）bean的时候再来创建对象，并进行一些初始化。使用@Lazy即可声明，只对单例Bean有效。

在代码实现的时候，我们可以只加载它的定义，而不去创建，直到调用getBean的时候（比如需要Autowired的时候，或者主动调用getBean的时候），才去创建。



#### Bean注解

Spring的@Bean注解用于告诉方法，产生一个Bean对象，然后这个Bean对象交给Spring管理。产生这个Bean对象的方法Spring**只会调用一次**，随后这个Spring将会将这个Bean对象放在自己的IOC容器中。

SpringIOC 容器管理一个或者多个bean，这些bean都需要在@Configuration注解下进行创建，在一个方法上使用@Bean注解就表明这个方法需要交给Spring进行管理。

@Bean不仅可以作用在方法上，也可以作用在注解类型上，在运行时提供注册。

value：name属性的别名，在不需要其他属性时使用，也就是说value 就是默认值

name：此bean 的名称，或多个名称，主要的bean的名称加别名。如果未指定，则bean的名称是带注解方法的名称。如果指定了，方法的名称就会忽略，如果没有其他属性声明的话，bean的名称和别名可能通过value属性配置


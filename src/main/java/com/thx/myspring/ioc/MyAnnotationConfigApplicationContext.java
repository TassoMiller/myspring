package com.thx.myspring.ioc;

import com.thx.myspring.ioc.annotation.*;
import com.thx.myspring.ioc.tool.MyTools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MyAnnotationConfigApplicationContext {
    // 缓存
    private Map<String,Object> singletonObjects = new HashMap<>();
    private Map<Class, Object> cache = new HashMap<>();
    private List<String> beanNames = new ArrayList<>();

    private List<String> packs = new ArrayList<>();

    // 含有Component注解的注解集合
    // 当然spring肯定不是这么实现的
    private HashSet<Class> componentSet = new HashSet<>();

    {
        componentSet.addAll(Arrays.asList(Component.class, Service.class, Controller.class, Configuration.class));
    }
    private Set<BeanDefinition> beanDefinitions;
    public MyAnnotationConfigApplicationContext() {
        Set<Class<?>> classes = MyTools.getClasses("com.thx");
        Iterator<Class<?>> iterator = classes.iterator();
        while (iterator.hasNext()) {
            Class<?> next = iterator.next();
        }
    }

    public MyAnnotationConfigApplicationContext(String path) {
        // 获取bean的定义
        beanDefinitions = findBeanDefinitions(path);
        // 创建bean
        createObjects(beanDefinitions);
        // 注入bean
        autowireObject(beanDefinitions);
    }

    public Object getBean(String beanName) {
        Object bean = singletonObjects.get(beanName);
        if (bean != null) {
            return bean;
        } else { // 懒加载
            // 创建对象并且扫描其中的属性，进行set注入
            Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
            while (iterator.hasNext()) {
                BeanDefinition beanDefinition = iterator.next();
                if (beanDefinition.getBeanName().equals(beanName)) {
                    Class beanClass = beanDefinition.getBeanClass();
                    // 创建bean
                    bean = createBean(beanClass, beanName);

                    // 装载bean
                    doAutowire(beanClass);

                }
            }
        }
        return bean;

    }

    public Object getBean(Class clazz) {
        Object bean = cache.get(clazz);
        if (bean != null) {
            return bean;
        } else { // 懒加载
            // 创建对象并且扫描其中的属性，进行set注入
            Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
            while (iterator.hasNext()) {
                BeanDefinition beanDefinition = iterator.next();
                if (beanDefinition.getBeanClass().equals(clazz)) {
                    Class beanClass = beanDefinition.getBeanClass();
                    String beanName = beanDefinition.getBeanName();
                    // 创建bean
                    bean = createBean(beanClass, beanName);

                    // 装载bean
                    doAutowire(beanClass);

                }
            }
        }
        return bean;
    }
    
    public String[] getBeanDefinitionNames() {
        return beanNames.toArray(new String[0]);
    }

    public Integer getBeanDefinitionCount() {
        return beanNames.size();
    }


    public Set<BeanDefinition> findBeanDefinitions(String pack){
        //1、获取包下的所有类
        Set<Class<?>> classes = MyTools.getClasses(pack);
        Iterator<Class<?>> iterator = classes.iterator();
        Set<BeanDefinition> beanDefinitions = new HashSet<>();
        while (iterator.hasNext()) {
            // 2、遍历这些类，找到添加了Component注解或者被Component注解（如Service注解）修饰的注解的类
            Class<?> clazz = iterator.next();
            Component componentAnnotation = clazz.getAnnotation(Component.class);
            // 需要把Component修饰的注解排除在外，比如Service注解，它是不可以被装载到Ioc容器中的
            if(componentAnnotation != null && !clazz.isAnnotation()){
                String beanName = componentAnnotation.value();
                // 如果没有指定Component中的value，那么就按类名首字母小写作为beanName
                if("".equals(beanName)){
                    // 获取类名首字母小写
                    String className = clazz.getName().replaceAll(clazz.getPackage().getName() + ".", "");
                    beanName = className.substring(0, 1).toLowerCase()+className.substring(1);
                }
                // 3、将这些类封装成BeanDefinition，装载到集合中
                beanDefinitions.add(new BeanDefinition(beanName, clazz));
                beanNames.add(beanName);
            }
        }
        return beanDefinitions;
    }

    // 根据Class去创建bean实例并且根据Value注解进行set注入，同时将(beanName,bean)、(clazz,bean)放入到缓存当中
    private Object createBean(Class clazz, String beanName) {
        Object bean = null;
        try {
            //创建的对象
            bean = clazz.getConstructor().newInstance();
            //完成属性的赋值
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                Value valueAnnotation = declaredField.getAnnotation(Value.class);
                if (valueAnnotation != null) {
                    String value = valueAnnotation.value();
                    String fieldName = declaredField.getName();
                    // 获取set方法
                    String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    Method method = clazz.getMethod(methodName, declaredField.getType());
                    //完成数据类型转换
                    Object val = null;
                    switch (declaredField.getType().getName()) {
                        case "java.lang.Integer":
                            val = Integer.parseInt(value);
                            break;
                        case "java.lang.String":
                            val = value;
                            break;
                        case "java.lang.Float":
                            val = Float.parseFloat(value);
                            break;
                    }
                    method.invoke(bean, val);
                }
            }
            //存入缓存
            singletonObjects.put(beanName, bean);
            cache.put(clazz, bean);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return bean;
    }

    // 根据beanDefinitions来创建相应的对象，同时根据Value注解进行set注入
    public void createObjects(Set<BeanDefinition> beanDefinitions){
        Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
        while (iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();
            Class clazz = beanDefinition.getBeanClass();
            // 如果没有Lazy注解，则立即创建bean实例，否则，先不创建
            if (clazz.getAnnotation(Lazy.class) == null) {
                String beanName = beanDefinition.getBeanName();
                createBean(clazz, beanName);
            }

        }
    }

    // 注入对象
    public void autowireObject(Set<BeanDefinition> beanDefinitions){
        // 遍历所有的beanDefinitions，扫描所有被Autowired注解修饰的类
        Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
        while (iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();
            Class clazz = beanDefinition.getBeanClass();
            doAutowire(clazz);
        }
    }

    private void doAutowire(Class clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Autowired annotation = declaredField.getAnnotation(Autowired.class);
            if(annotation!=null){
                Qualifier qualifier = declaredField.getAnnotation(Qualifier.class);
                String fieldName = declaredField.getName();
                String methodName = "set"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
                try {
                    Object bean = null;
                    if(qualifier!=null){
                        //byName
                        String beanName = qualifier.value();
                        bean = getBean(beanName);

                    }else{
                        //byType
                        bean = getBean(clazz);
                    }
                    Method method = clazz.getMethod(methodName, declaredField.getType());
                    // 获取这个属性对应的对象
                    Object object = getBean(clazz);
                    method.invoke(object, bean);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

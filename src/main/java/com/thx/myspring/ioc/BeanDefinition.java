package com.thx.myspring.ioc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeanDefinition {
    private String beanName;
    private Class beanClass;
}

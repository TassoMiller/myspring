package com.thx.entity;

import com.thx.myspring.ioc.annotation.Component;
import com.thx.myspring.ioc.annotation.Value;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    @Value("Tasso")
    private String name;
    @Value("21")
    private Integer age;
}

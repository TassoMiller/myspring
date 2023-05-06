package com.thx.entity;

import com.thx.myspring.ioc.annotation.Component;
import com.thx.myspring.ioc.annotation.Lazy;
import com.thx.myspring.ioc.annotation.Value;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@Lazy
public class Doctor {
    @Value("凯尔希")
    private String name;
    @Value("女")
    private String sex;
}

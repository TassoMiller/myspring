package com.thx.entity;

import com.thx.myspring.ioc.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@Lazy
public class Teacher {
    @Autowired
    @Qualifier("student")
    private Student student;
    @Value("30")
    private Integer age;
    @Value("Alice")
    private String name;
}

package com.thx.entity;

import com.thx.myspring.ioc.annotation.Autowired;
import com.thx.myspring.ioc.annotation.Component;
import com.thx.myspring.ioc.annotation.Qualifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeachingTeam {
    @Autowired
    @Qualifier("teacher")
    private Teacher teacher;
}

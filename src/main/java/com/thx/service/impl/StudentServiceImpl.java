package com.thx.service.impl;

import com.thx.entity.Student;
import com.thx.myspring.ioc.annotation.Autowired;
import com.thx.myspring.ioc.annotation.Service;
import com.thx.service.IStudentService;

@Service
public class StudentServiceImpl implements IStudentService {
    @Autowired
    Student student;
    @Override
    public void print() {
        System.out.println(student);
    }
}

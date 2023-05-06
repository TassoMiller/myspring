package com.thx.controller;

import com.thx.myspring.ioc.annotation.Autowired;
import com.thx.myspring.ioc.annotation.Controller;
import com.thx.service.IStudentService;

@Controller
public class StudentController {
    @Autowired
    IStudentService studentService;
    public void print() {
        studentService.print();
    }
}

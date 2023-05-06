package com.thx;

import com.thx.controller.StudentController;
import com.thx.entity.Doctor;
import com.thx.entity.Student;
import com.thx.entity.Teacher;
import com.thx.entity.TeachingTeam;
import com.thx.myspring.ioc.MyAnnotationConfigApplicationContext;
import com.thx.myspring.ioc.annotation.Controller;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        MyAnnotationConfigApplicationContext myAnnotationConfigApplicationContext = new MyAnnotationConfigApplicationContext("com.thx");
        Student student = (Student)myAnnotationConfigApplicationContext.getBean("student");
        System.out.println(student);
        Teacher teacher = (Teacher)myAnnotationConfigApplicationContext.getBean("teacher");
        System.out.println(teacher);
        TeachingTeam teachingTeam = (TeachingTeam)myAnnotationConfigApplicationContext.getBean("teachingTeam");
        System.out.println(teachingTeam);
        // 懒加载检验
        Doctor doctor = (Doctor) myAnnotationConfigApplicationContext.getBean(Doctor.class);
        System.out.println(doctor);
    }
}

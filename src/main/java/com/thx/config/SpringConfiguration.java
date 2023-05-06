package com.thx.config;

import com.thx.myspring.ioc.annotation.Bean;
import com.thx.myspring.ioc.annotation.ComponentScan;
import com.thx.myspring.ioc.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan({"com.thx.entity", "com.thx.service"})
public class SpringConfiguration {
    @Bean
    public Map<String, String> dict() {
        Map<String, String> dictionary = new HashMap<>();
        dictionary.put("A", "a");
        dictionary.put("B", "b");
        dictionary.put("C", "c");
        dictionary.put("D", "d");
        return dictionary;
    }
}

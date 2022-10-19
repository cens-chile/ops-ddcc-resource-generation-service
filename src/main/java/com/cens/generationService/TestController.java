/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.generationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author José <jose.m.andrade@gmail.com>
 */
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    ApplicationProperties properties;
    
    @GetMapping
    public String findAllUsers() {
        // Implement
        System.out.println("properties = " + properties.getBaseUrl());
        return "HOLA";
    }
    
}

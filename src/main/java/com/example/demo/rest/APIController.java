package com.example.demo.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class APIController {

/*
    @GetMapping(value = "/")
    public String index() {
        return "Hello";
    }
 */

    @GetMapping(value = { "/test/", "/test"})
    public String test() {
        return "Hello test";
    }
    
}

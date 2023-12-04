package com.example.demo.order;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @GetMapping( value = {"/order", "/order/"} )
    public String order() {
        return "order";
    }
}

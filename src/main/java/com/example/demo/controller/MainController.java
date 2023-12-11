package com.example.demo.controller;

import org.springframework.stereotype.Controller;

import com.example.demo.service.ItemService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

}

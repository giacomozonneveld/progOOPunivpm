package com.example.demo.controller;

import com.example.demo.service.BulkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class BulkController {
	
	@Autowired
	BulkService bulkService;
	
}

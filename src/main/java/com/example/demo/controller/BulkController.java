package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.BulkService;



@RestController
public class BulkController {
	
	@Autowired
	BulkService bulkService;
	
	@RequestMapping(value="/bulks", method=RequestMethod.GET)
	public ResponseEntity<Object> getBulks(){
		return new ResponseEntity<>(bulkService.getBulks(), HttpStatus.OK);	
	}
	
	@RequestMapping(value="/bulks/statistics/{columnHeader}", method=RequestMethod.POST)
	public ResponseEntity<Object> getStatistics(@PathVariable("columnHeader") String columnHeader){
		return new ResponseEntity<>(bulkService.getStatistics(columnHeader), HttpStatus.OK);
	}
	
	
}

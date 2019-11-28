package com.example.demo.controller;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
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
	
	@RequestMapping(value="/bulks/statistics/{columnHeader}", method=RequestMethod.GET)
	public ResponseEntity<Object> getStatistics(@PathVariable("columnHeader") @NotBlank String columnHeader){
		
		ResponseEntity <Object> response= new ResponseEntity<>(bulkService.getStatistics(columnHeader), HttpStatus.OK);
		if(!response.hasBody()) {
			throw new AttribNotFoundException(columnHeader);
		}
		return response;
	}
	
	@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Attributo non presente")
	public class AttribNotFoundException extends RuntimeException{
	 
		AttribNotFoundException(String columnHeader){
	}
	}

	
	@RequestMapping(value="/bulks/metadata", method=RequestMethod.GET)
	public ResponseEntity<Object> getMetadata() throws NoSuchFieldException{
		return new ResponseEntity<>(bulkService.getMetadata(), HttpStatus.OK);
	}
	
	
}

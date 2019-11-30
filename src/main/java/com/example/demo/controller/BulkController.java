package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.BulkService;



@RestController
@ControllerAdvice
public class BulkController{
	
	@Autowired
	BulkService bulkService;
	/**
	 * Metodo che mostra tutte le operazioni possibili
	 * @return
	 */
	@RequestMapping(value="/", method=RequestMethod.GET)
	public ResponseEntity<Object> welcome(){
		return new ResponseEntity<>(bulkService.welcome(), HttpStatus.OK);	
	}
	
	/**
	 * Metodo che restituisce i dati
	 * @return oggetto costituito da vettore di oggetti e HttpStatus.OK
	 */
	@RequestMapping(value="/bulks", method=RequestMethod.GET)
	public ResponseEntity<Object> getBulks(){
		return new ResponseEntity<>(bulkService.getBulks(), HttpStatus.OK);	
	}
	
	/**
	 * Metodo che restituisce le statistiche
	 * @param columnHeader nome della colonna inserita nel path
	 * @return	oggetto costituito da mappa con risultati statistiche e HttpStatus.OK
	 */
	@RequestMapping(value="/bulks/statistics/{columnHeader}", method=RequestMethod.GET)
	public ResponseEntity<Object> getStatistics(@PathVariable("columnHeader") String columnHeader){
		
		ResponseEntity <Object> response= new ResponseEntity<>(bulkService.getStatistics(columnHeader), HttpStatus.OK);
		if(!response.hasBody()) {
			throw new AttribNotFoundException(columnHeader); //Eccezione custom
		}
		return response;
	}
	/**
	 * Classe custom che produce il messaggio personalizzato "Attributo non presente"
	 * @author jackz
	 *
	 */
	@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Attributo non presente")
	public class AttribNotFoundException extends RuntimeException{
	 
		AttribNotFoundException(String columnHeader){
	}
	}
	

	/**
	 * Metodo che restituisce i metadati
	 * @return oggetto costituito da mappa con metadati e HttpStatus.OK
	 * @throws NoSuchFieldException
	 */
	@RequestMapping(value="/bulks/metadata", method=RequestMethod.GET)
	public ResponseEntity<Object> getMetadata() throws NoSuchFieldException{
		return new ResponseEntity<>(bulkService.getMetadata(), HttpStatus.OK);
	}
	
	
}

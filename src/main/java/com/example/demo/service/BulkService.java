package com.example.demo.service;

import java.util.Map;
import java.util.Vector;

import com.example.demo.model.Bulk;
/**
 * Interfaccia costituita dai metodi che producono le richieste effettuate dal client
 * @author jackz
 *
 */
public interface BulkService {
	
	public abstract Vector <Bulk> getBulks();
	
	public abstract Map<String, Float> getStatistics(String columnHeader);
	
	public abstract Map<String, String> getMetadata() throws NoSuchFieldException;
}

package com.example.demo.service;

import java.util.Map;
import java.util.Vector;

import com.example.demo.model.Bulk;

public interface BulkService {
	
	public abstract Vector <Bulk> getBulks();
	
	public abstract Map<String, Float> getStatistics(String columnHeader);
	
	public abstract Map<String, String> getMetadata() throws NoSuchFieldException;
}

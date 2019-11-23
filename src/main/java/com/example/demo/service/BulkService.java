package com.example.demo.service;

import java.util.Vector;
import com.example.demo.model.Bulk;

public interface BulkService {
	public abstract Vector <Bulk> getBulks();
	public abstract void searchDataset();
}

package com.example.demo.service;


import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import org.springframework.stereotype.Service;
import com.example.demo.model.Bulk;

@Service
public class BulkServiceImpl implements BulkService{
	
	Bulk bObj = new Bulk();
	Vector<Bulk> vett = new Vector<Bulk>();
	boolean flagDownload=false;
	boolean flagObjectGenerated=false;
	
	public void searchDataset() {
	
		String url = "http://data.europa.eu/euodp/data/api/3/action/package_show?id=CLYAN2vR2Tu2Z1soIQZHQ";
		try {
			
			URLConnection openConnection = new URL(url).openConnection();
			openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			InputStream in = openConnection.getInputStream();
			
			 String dataj = "";
			 String linej = "";
			 try {
			   BufferedReader buf = new BufferedReader( new InputStreamReader( in ));
			  
			   while ( ( linej = buf.readLine() ) != null ) {
				   dataj+= linej;
			   }
			 } finally {
			   in.close();
			 }
			 //PARSING JSON
			JSONObject obj = (JSONObject) JSONValue.parseWithException(dataj);
			JSONObject objI = (JSONObject) (obj.get("result"));	
			JSONArray objA = (JSONArray) (objI.get("resources"));
			
			for(Object o: objA){
			    if ( o instanceof JSONObject ) {
			        JSONObject o1 = (JSONObject)o; 
			        String format = (String)o1.get("format");
			        String urlD = (String)o1.get("url");
			        System.out.println(format + " | " + urlD);
			        if(format.endsWith("file-type/TSV")) {
		    			 System.out.println( "OK" );
		    			 download(urlD, "file.tsv");
		    			 flagDownload=true;
		    			 }
			        }
			    }
			} catch (IOException | ParseException e) {
		    				 e.printStackTrace();
		    				 }catch (Exception e) {
		    					 e.printStackTrace();
		    				 }
	}

	private void download(String url, String fileName) throws Exception {
		HttpURLConnection openConnection = (HttpURLConnection) new URL(url).openConnection();
		openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		InputStream in = openConnection.getInputStream();
		 //String data = "";
		 //String line = "";
		 try {
		   if(openConnection.getResponseCode() >= 300 && openConnection.getResponseCode() < 400) {
			   download(openConnection.getHeaderField("Location"),fileName);
			   in.close();
			   openConnection.disconnect();
			   return;
		   }
		   Files.copy(in, Paths.get(fileName));
		   System.out.println("File size " + Files.size(Paths.get(fileName)));  
		 } finally {
		   in.close();
		 }
	}

	public void generateObjects() {
		if(!flagDownload) {
			searchDataset();
		}
		try {
			BufferedReader reader= new BufferedReader(new FileReader ("file.tsv"));
			String riga="";
			riga=reader.readLine();
			//String [] var = riga.split("\t"); //il garbage collection lo eliminerà perchè non viene più usato
			String[] header=riga.split("\t"); //Legge la prima riga dove c'è l'header e lo uso sulla hashmap
			
			while((riga=reader.readLine())!=null) {
				String[] appoggio1;
				Map<String, Float> years = new HashMap<>();
				appoggio1=riga.split("\t");
				String[] primaColonna=appoggio1[0].split(",");
				bObj.setCrops(primaColonna[0]);
				bObj.setStrucPro(primaColonna[1]);
				bObj.setGeoTime(primaColonna[2]);
				//i per header e j per appoggio1... inserisce automaticamente chiavi e valori
				for(int i=1, j=i; i<header.length && j<appoggio1.length; i++) {
					String key = header[i].trim();
					appoggio1[j]=appoggio1[j].replaceAll(" ",""); //rimuove ogni spazio
					if(!(appoggio1[j].contains(":")|| appoggio1[j].contains("u"))) {	//se non trova nessuno dei due caratteri
						float value = Float.valueOf(appoggio1[j]);
						years.put(key, value);
					}
					else {
					years.put(header[i],  (float) 0);
					}
				}
				bObj.setMap(years);
				vett.add(bObj);
				
			}
			reader.close();
			flagObjectGenerated=true;
			System.out.println("n righe inserite = "+vett.size());
			}
			catch(IOException e) {
				e.printStackTrace();
			}
	}
	
	public Vector<Bulk> getBulks() {
		if(!flagDownload) {
			searchDataset();
		}
		if(!flagObjectGenerated) {
			generateObjects();
		}
		
		return vett;
	}
	
}

package com.example.demo.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import com.example.demo.model.Bulk;

@Service
public class BulkServiceImpl implements BulkService{
	
	Vector<Bulk> vett = new Vector<Bulk>();
	boolean flagDownload=false;
	boolean flagObjectGenerated=false;
	
	
	private void searchDataset() {
		File tmpDir = new File("file.tsv");
		boolean exists = tmpDir.exists();
		if(exists) {
			return;
		}
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

	private void generateObjects() {
		if(!flagDownload) {
			searchDataset();
		}
		try {
			BufferedReader reader= new BufferedReader(new FileReader ("file.tsv"));
			String riga="";
			riga=reader.readLine();
			String[] header=riga.split("\t"); //Legge la prima riga dove c'è l'header e lo uso sulla hashmap
			//int x=2;//variabile per debug righe
			while((riga=reader.readLine())!=null) {
				Bulk bObj = new Bulk();
				String[] appoggio1;
				Map<String, Float> years = new HashMap<>();
				appoggio1=riga.split("\t");	//inserisco in appoggio la riga spezzata da \t per separare le colonne
				String[] primaColonna=appoggio1[0].split(","); //la prima colonna viene separata in 3 a causa della virgola
				bObj.setCrops(primaColonna[0]);		//inserisco il primo valore contenuto all'interno della prima colonna della prima colonna
				bObj.setStrucPro(primaColonna[1]); //uguale
				bObj.setGeoTime(primaColonna[2]); //uguale
				//Ok fino a qui
				//i per header(chiavi) e per appoggio1(valori)... inserisce automaticamente chiavi e valori
				//basta escludere la prima colonna del file quindi per appoggio1 i=1 e per header j=1. header e appoggio hanno stessa dimensione
				//int colonna=1; variabile per debug colonne
				for(int i=1 ; i<appoggio1.length ; i++) {
					
					String key = header[i].trim(); //rimuove gli spazi a destra e sinistra
					
					appoggio1[i]=appoggio1[i].replaceAll(" ",""); //rimuove ogni spazio interno
					
					if(!(appoggio1[i].contains(":")|| appoggio1[i].contains("u"))) {	
						//se non trova nessuno dei due caratteri, e quindi il valore è gia numerico
						float value = Float.valueOf(appoggio1[i]);//CAST DA STRING A FLOAT
						years.put(key, value);
					}
					else {
						float value=0;
					years.put(key,  value);
					}
					//System.out.println("Riga "+x+ " \t Colonna "+colonna);	DEBUG
					//colonna++;
				}
				//System.out.println("Riga "+x);
				//x++; DEBUG
				bObj.setMap(years); //INSERISCE LA MAPPA NELL'ATTRIBUTO YEARS DELL'OGGETTO
				
				//STAMPA OGGETTO SERIALIZZATO
/*				FileOutputStream out = new FileOutputStream("myfile.txt", true);
				// Create the stream to the file you want to write too.
				ObjectOutputStream objOut = new ObjectOutputStream(out);
				// Use the FileOutputStream as the constructor argument for your object.

				objOut.writeObject(bObj);
				// Write your object to the output stream.
				objOut.close();
*/				
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

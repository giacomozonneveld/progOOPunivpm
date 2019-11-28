package com.example.demo.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
	/**
	 * Il costruttore scarica il data-set e lo rappresenta ad oggetti autonomamente, senza doverlo 
	 * richiedere esplicitamente
	 */
	BulkServiceImpl(){
		searchDataSet();
		generateObjects();
	}
	
	/**
	 * Questo metodo entra nel sito, analizza gli oggetti JSON e, se trova il formato impostato (file-type/TSV)
	 * effettua il download del data-set dall'url corrispondente.
	 */
	private void searchDataSet() {
		File tmpDir = new File("file.tsv");	//viene verificato che il file non esista già per evitare conflitti
		boolean exists = tmpDir.exists();	//se la condizione dell'if è true, esce dal metodo, sennò effettua
		if(exists) {						//tutte le operazioni
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
		    			 }
			        }
			    }
			} catch (IOException | ParseException e) {
		    				 e.printStackTrace();
		    				 }catch (Exception e) {
		    					 e.printStackTrace();
		    				 }
	}
/**
 * Metodo che effettua il download del data-set
 * @param url	link alla pagina contenente il data-set ottenuto nel metodo searchDataSet()
 * @param fileName	nome del file che verrà creato, default "file.tsv
 * @throws Exception IOException
 */
	private void download(String url, String fileName) throws Exception {

		HttpURLConnection openConnection = (HttpURLConnection) new URL(url).openConnection();
		openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		InputStream in = openConnection.getInputStream();
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
	
	/**
	 * Questo metodo rappresenta il data-set ad oggetti
	 */
	private void generateObjects() {
		try {
			BufferedReader reader= new BufferedReader(new FileReader ("file.tsv"));
			String riga="";
			riga=reader.readLine();
			String[] header=riga.split("\t"); //Legge la prima riga dove c'è l'header e lo uso sulla map
			while((riga=reader.readLine())!=null) {
				
				Bulk bObj = new Bulk();
				String[] appoggio;
				
				int differenza=0;
				
				Map<String, Float> years = new HashMap<>();
				appoggio=riga.split("\t");	//inserisco in appoggio la riga spezzata da \t per separare le colonne
				
				
				if(header.length!=appoggio.length) { 				//valuta se una riga contiene valori fino ad una certa colonna 
					differenza=header.length-appoggio.length;		//e poi non ne ha più per il resto della riga
				}													//se differenza!=0 si attiva l'if prima di inserire la hashmap nell'attributo
																	//dell'oggetto
				
				
				String[] primaColonna=appoggio[0].split(","); //la prima colonna viene separata in 3 a causa della virgola
				bObj.setCrops(primaColonna[0]);		//inserisco il primo valore contenuto all'interno della prima colonna della prima colonna
				bObj.setStrucPro(primaColonna[1]); //uguale
				bObj.setGeoTime(primaColonna[2]); //uguale
				
				//i per header(chiavi) e per appoggio1(valori)... inserisce automaticamente chiavi e valori
				//basta escludere la prima colonna del file quindi per appoggio1 i=1 e per header j=1. header e appoggio hanno stessa dimensione

				for(int i=1 ; i<appoggio.length ; i++) {
					
					String key = header[i].trim(); //rimuove gli spazi a destra e sinistra
					
					appoggio[i]=appoggio[i].replaceAll(" ",""); //rimuove ogni spazio interno
					
					if(!(appoggio[i].contains(":")|| appoggio[i].contains("u"))) {	
						//se non trova nessuno dei due caratteri, e quindi il valore è gia numerico
						float value = Float.valueOf(appoggio[i]);	//CAST DA STRING A FLOAT
						years.put(key, value);
					}
					else {
						years.put(key,  null);
					}
				}
				
				if(differenza!=0) {										//la riga ha valori fino ad una certa colonna e poi non ne ha più
					for(int i=differenza; i<header.length; i++) {		//in questo modo inserisce 0 come valore per ogni colonna nulla
						String key = header[i].trim();
						years.put(key,null);
					}
				}
				
				bObj.setMap(years); //INSERISCE LA MAPPA NELL'ATTRIBUTO YEARS DELL'OGGETTO
				vett.add(bObj);
			}
			reader.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
	}
	
	
	/**
	 * Questo metodo viene chiamato dal controller per restituire i dati.
	 * Questi sono stati salvati all'interno della struttura dati Vector nel metodo generateObjects()
	 */
	@Override
	public Vector<Bulk> getBulks() {
		
		return vett;
	}
	
	@Override
	/**
	 * Questo metodo implementa la funzione delle statistiche
	 */
	public Map<String, Float> getStatistics(String columnHeader) {
		Bulk obj = new Bulk();	//questo oggetto va creato per poter effettuare la ricerca della chiave su una mappa riempita, 
		obj=vett.get(0);		//altrimenti non avrebbe alcuna chiave o valore
		columnHeader=columnHeader.trim();
		if(obj.searchKeyInYears(columnHeader)) {	//searchKeyInYears ritorna true/false, metodo implementato in Bulk.java
			return numResults(columnHeader);		//e usato per valutare se le statistiche vanno effettuate su numeri o stringhe
		}
		else {
			return countUnique(columnHeader);	//chiamata metodo che implementa statistiche su stringhe
		}
	}
	
	/**
	 * Metodo che effettua le statistiche sui valori correlati al nome della colonna, andando a scorrere tutti i valori con 
	 * chiave columnHeader di tutte le mappe di tutti gli oggetti
	 * @param columnHeader nome della colonna
	 * @return mappa contenente tutti i risultati della statistiche mappa<tipo statistica, risultato>
	 */

	private Map<String, Float> numResults(String columnHeader){
		Map<String, Float> results = new HashMap<>();
		float avg=getAvg(columnHeader);
		results.put("Average", avg);
		float sum= getSum(columnHeader);
		results.put("Sum", sum);
		float devStd = getDevStd(columnHeader, avg);
		results.put("Standard Deviation", devStd);
		float []maxMin=getMaxMin(columnHeader);
		results.put("Min", maxMin[0]);
		results.put("Max", maxMin[1]);
		float countValues= getCount(columnHeader);
		results.put("Count", countValues);
		return results;
	}
	
	/**
	 * Metodo che effettua la statistica su valori di tipo stringa. Conta gli elementi unici
	 * @param columnHeader	nome colonna
	 * @return mappa con risultati mappa<valore stringa, occorrenze>
	 */
	
	private Map<String, Float> countUnique(String columnHeader){
		Map<String, Float> res=new HashMap<>();
		
		if(columnHeader.equalsIgnoreCase("crops")) {
		for(Bulk obj :vett) {
			String value= obj.getCrops();
			float num= (res.get(value)==null) ? 1 : res.get(value)+1; //se non trova il valore-> num=1, sennò num=frq+1
			res.put(value, num);
		}
		return res;
		}
		if(columnHeader.equalsIgnoreCase("strucPro")) {
			for(Bulk obj :vett) {
				String value= obj.getStrucPro();
				float num= (res.get(value)==null) ? 1 : res.get(value)+1; //se non trova il valore-> num=1, sennò num=frq+1
				res.put(value, num);
			}
			return res;
			}
		if(columnHeader.equalsIgnoreCase("geoTime")) {
			for(Bulk obj :vett) {
			String value= obj.getGeoTime();
			float num= (res.get(value)==null) ? 1 : res.get(value)+1; //se non trova il valore-> num=1, sennò num=frq+1
			res.put(value, num);
			}
			return res;
		}
		return null;
	}
	/**
	 * Metodo che calcola la media
	 * @param columnHeader nome colonna
	 * @return valore calcolato
	 */
	
	
	private float getAvg(String columnHeader) {
		float avg=0;
		for(Bulk obj :vett) {
			Map<String, Float> mappa = new HashMap<>();
			mappa= obj.getMap();
			if(mappa.get(columnHeader)!=null) {
			float value= mappa.get(columnHeader);
			avg+=value;
			}
		}
		avg=avg/vett.size();
		return avg;
	
	}
	/**
	 * Metodo che calcola la somma dei valori
	 * @param columnHeader nome colonna
	 * @return valore calcolato
	 */

	private float getSum(String columnHeader) {
		float sum=0;
		for(Bulk obj : vett) {
			Map<String, Float> mappa = new HashMap<>();
			mappa= obj.getMap();
			if(mappa.get(columnHeader)!=null) {
			float value= mappa.get(columnHeader);
			sum+=value;
			}
		}
		return sum;
	}
	/**
	 * Metodo che calcola la Deviazione Standard
	 * @param columnHeader	nome colonna
	 * @param avg	valore ottenuto da metodo getAvg(columnHeader)
	 * @return	valore calcolato
	 */

	private float getDevStd(String columnHeader, float avg) {
		float devStd=0;
		float sommatDiffQuadr = 0;
		float differenza=0;
		for(Bulk obj : vett) {
			Map<String, Float> mappa = new HashMap<>();
			mappa= obj.getMap();
			if(mappa.get(columnHeader)!=null) {
			float value= mappa.get(columnHeader);
		    differenza = value - avg;
		    sommatDiffQuadr += differenza * differenza;
		    }
			}
		float variance = sommatDiffQuadr/(vett.size()-1);
		devStd= (float) Math.sqrt(variance);
		return devStd;
	}
	/**
	 * Metodo che calcola massimo e minimo
	 * @param columnHeader	nome della colonna
	 * @return array con valori minimo in posizione 0 e massimo in posizione 1
	 */

	private float[] getMaxMin(String columnHeader) {
		float max=0;
		float min=0;
		for(Bulk obj :vett) {
			Map<String, Float> mappa = new HashMap<>();
			mappa= obj.getMap();
			if(mappa.get(columnHeader)!=null) {
			float value= mappa.get(columnHeader);
			if(value>max) {
				max=value;
			}
			if(value<min) {
				min=value;
			}
			}
		}
		float[] res= {min, max};
		return res;
	}
	/**
	 * Metodo che conta il numero di valori
	 * @param columnHeader nome colonna
	 * @return valore calcolato
	 */
	
	private float getCount(String columnHeader) {
		float conta=0;
		for(Bulk o: vett) {
			Map<String, Float> mappa = new HashMap<>();
			mappa= o.getMap();
			if(mappa.get(columnHeader)!=null) {	//se il data-set ha valori non numerici, questi vengono sostituiti con null,
			conta++;							//quindi per effettuare il calcolo, questi vanno esclusi
			}
		}
		return conta;
	}
	/**
	 * Metodo che produce i metadati
	 */

	
	
	@Override
	public Map<String, String> getMetadata() throws NoSuchFieldException {
		Map<String, String> metadata = new HashMap<>();
		Bulk obj=vett.get(0);
		Class objVar=obj.getClass();
		Field[] metadati = objVar.getDeclaredFields();	//i metadati vengono inseriti nella mappa metadata
		for(Field f : metadati) {
			metadata.put(f.getName(), f.getType().toString());
		}
		metadata.remove("years");		//viene rimosso, perchè non esprime il tipo di dato dei valori legati alle chiavi
		Map<String, Float> mappoggio=new HashMap<>();
		mappoggio=obj.getMap();		//per conoscere il contenuto della mappa, questa deve essere riempita, e quindi bisogna estrarla da un oggetto
		
		 try {
	            ParameterizedType pt = (ParameterizedType)Bulk.class.getDeclaredField("years").getGenericType();
	            for(Type type : pt.getActualTypeArguments()) {
	            	for(Map.Entry<String, Float> entry : mappoggio.entrySet()) {
	            		metadata.put(entry.getKey(), type.toString() );	//come chiavi ci saranno i nomi delle chiavi di mappoggio, 
	            		}												//come valori il tipo di dato dei valori di mappoggio
	            	}
	            }catch(NoSuchFieldException e) {
	            	e.printStackTrace();
	            	}
		 return metadata;
	}
	
}

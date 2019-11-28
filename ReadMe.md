# Documentazione
## Indice
1. Analisi del data-set ed implementazione funzionalità
    1.1. Classe Bulk.java
    1.2 Classe BulkServiceImpl.java
    + Metodo searchDataSet()
    + Metodo generateObjects()
    + Metodo getBulks()
    + Metodo getStatistics(String columnHeader)
    + Metodo getMetadata()
2. Esempi di test per verifica funzionalità
    + Richiesta dei dati
    + Richiesta dei metadati
    + Richiesta delle statistiche
    
## 1. Analisi del data-set ed implementazione funzionalità
Osservando il data-set, si nota che nella prima colonna sono presenti valori di tipo stringa separati da virgola,
tutte le altre colonne rappresentano valori numerici che fanno riferimento a campi appartenenti all'insieme "Anni". 
### 1.1 Classe Bulk.java
A seguito dell'analisi, si è deciso di organizzare le occorrenze suddividendole nei seguenti attributi:
* **crops** di tipo *String*
* **strucPro** di tipo *String*
* **geoTime** di tipo *String*
* **years** di tipo *Map<String, Float>*

Utilizzando la classe Map è possibile utilizzare l'oggetto years per raggruppare le colonne rappresentanti gli anni andando a scegliere come chiave l'anno (di tipo String) e come valore il valore corrispondente dell'occorrenza (di tipo Float).
I metodi implementati all'interno di questa classe sono gli standard Getters and Setters con in aggiunta il metodo **public boolean searchKeyInYears(String key)**, che verrà utilizzato per vedere se nella mappa years è presente un anno tra le chiavi(maggiori dettagli in Sviluppo metodi per statistiche).


### 1.2 Classe BulkServiceImpl.java
Questa è l'implementazione dell'interfaccia **BulkService**.
Come *variabili globali* sono presenti:
+ **Vector<Bulk> vett**
+ **boolean flagDownload**
+ **boolean flagObjectGenerated**

La variabile **vett** è un vettore di oggetti Bulk, all'interno di questa saranno presenti tutte le occorrenze del data-set.
Le variabili booleane, **flagDownload** e **flagObjectGenerated** ,inizializzate a **false**, verranno utilizzate per verificare che, prima di qualsiasi operazione, sia stato scaricato il data-set e che questo sia stato rappresentato ad oggetti. In questo modo eviteremo errori di tipo **Error 500**, nel caso in cui le suddette operazioni non siano state fatte in partenza.
#### Metodo searchDataSet()
Questo metodo, con modificatore `private`, viene chiamato dal metodo `getBulks()`. 
Prevede la connessione con l'url http://data.europa.eu/euodp/data/api/3/action/package_show?id=CLYAN2vR2Tu2Z1soIQZHQ, la lettura bufferizzata del documento JSON per considerare gli *url* e i *formati* presenti tramite il **parsing JSON**  e il download del data-set. 
Se il data-set è già stato scaricato in una precedente esecuzione del programma, questo non va in errore, dovuto al fatto che il file è già presente, grazie al seguente codice che cerca se è presente il file con nome `file.tsv`.
```java
File tmpDir = new File("file.tsv");
		boolean exists = tmpDir.exists();
		if(exists) {
			return;
		}
```		
Tramite la condizione
`if(format.endsWith("file-type/TSV"))` è possibile attivare il metodo `download(String url, String fileName)` solo sull'url che ha il formato *file-type/TSV*.    

#### Metodo generateObjects()
Questo metodo, con modificatore `private`, viene chiamato dal metodo `getBulks()`. 
Permette di generare oggetti che rappresentino il data-set.
In un array di String viene salvata la prima riga suddivisa da *\t*
```String[] header=riga.split("\t"); ```
Quindi avremo che header=[(crops,strucpro,geo\time), 1997, 1996, ...]
Tramite la successiva condizione `while((riga=reader.readLine())!=null)` si andrà a leggere ogni la riga.
Come stabilito dall'**Analisi del data-set**, per inserire i valori della prima colonna in tre attributi diversi si è usato il seguente codice
```java
String[] primaColonna=appoggio[0].split(","); 
bObj.setCrops(primaColonna[0]);
bObj.setStrucPro(primaColonna[1]);
bObj.setGeoTime(primaColonna[2]); 
```
Per quanto riguarda i valori legati agli anni, è stato implementato questo codice che, per i valori non numerici sfrutta la possibilità di una mappa di avere valori `null`.
```java
for(int i=1 ; i<appoggio.length ; i++) {
	String key = header[i].trim();
	appoggio[i]=appoggio[i].replaceAll(" ","");
	if(!(appoggio[i].contains(":")|| appoggio[i].contains("u"))) {	
		float value = Float.valueOf(appoggio[i]);
		years.put(key, value);
		}
	else {
		years.put(key,  null);
		}
    }
```
La mappa verrà passata come parametro al metodo `setMap(Map<String,Float> years)` per andarla ad associare all'oggetto `Bulk`.

A questo punto bisogna gestire il problema dell'ultima riga del data-set che presenta celle vuote(probabilmente `EOF`) da una certa colonna fino alla fine. 
Se la riga avesse avuto un solo valore `null`, il resto delle colonne sarebbero state riconosciute automaticamente tramite il separatore `\t`, ma in questo caso ciò non accade. 
Per ovviare questo problema si è scelto di introdurre una variabile `int differenza` all'interno della quale viene salvato il risultato della sottrazione tra la **lunghezza di `header`** e la **lunghezza di `appoggio`** se risulta valida la condizione del seguente **if**
```java
if(header.length!=appoggio.length) { 			
	differenza=header.length-appoggio.length;
    }
```
In questo modo, se **appoggio**, array di stringhe che contiene tutti i valori della riga, ha una dimensione diversa da **header**, allora significa che la riga termina prematuramente. Quindi viene salvata la differenza e successivamente verrà attivata la condizione
```java
    if(differenza!=0) {									
		for(int i=differenza; i<header.length; i++) {
			String key = header[i].trim();
			years.put(key,null);
			}
```
Senza il codice sopra inserito, la mappa `years` avrebbe tante chiavi quante sono le colonne finali vuote della riga senza valori.
In conclusione l'oggetto `bObj` verrà inserito in coda al vettore `vett` e il `flagObjectGenerated` verrà settato a `true`.

### Metodo getBulks()
Questo metodo, con modificatore `public`, verifica tramite i *flags* che sia stato effettuato il download e che siano stati generati gli oggetti da mostrare. Si è scelto l'uso dei flags per evitare che, se a run-time questo metodo viene invocato più volte, vengano più volte effettuate operazioni inutili con conseguente aumento del tempo di risposta. Verificati i flags, viene ritornato il vettore di oggetti `vett` che sarà restituito al client sotto forma di oggetto JSON.

### Metodo getStatistics(String columnHeader)
Questo metodo, con modificatore `public`, permette di effettuare le statistiche sui dati, decidendo autonomamente se attivare le statistiche sui numeri tramite il metodo **numResults(columnHeader)** o su stringhe tramite il metodo **countUnique(columnHeader)**.
Il parametro `columnHeader` viene ricevuto tramite Path, a questo punto, grazie al seguente codice, si stabilisce automaticamente il tipo di statistiche da effettuare.
```java
if(obj.searchKeyInYears(columnHeader)) {
	return numResults(columnHeader);
	}
    else {
	return countUnique(columnHeader);
}
```


* #### Metodo numResults(String columnHeader)
Questo metodo, con modificatore `private`, permette di effettuare le statistiche sui valori numerici. Viene creata una mappa su cui vengono salvati i risultati delle statistiche e viene ritornata indietro per produrre l'oggetto JSON. 
Questa mappa avrà come chiave il tipo di operazione e come valore il risultato ottenuto.

I metodi invocati sono **getAvg(String columnHeader)**, **getSum(String columnHeader)**, **getDevStd(String columnHeader, float avg)**, **getMaxMin(String columnHeader)**, e **getCount(String columnHeader)**.

+ ##### Metodo getAvg(String columnHeader)
Tramite il costrutto `foreach` viene fatto scorrere tutto il vettore di oggetti. Ad ogni iterazione viene creata una mappa di appoggio per poter operare sui valori con chiave columnHeader.
Se il valore estratto dalla mappa non è nullo, allora viene usato per effettuare la media.
Si inizializza `float avg=0`, così che sia possibile salvare il valore raccolto ad ogni iterazione senza perderlo. Questa variabile sarà poi ritornata al metodo **numResults(String columnHeader)**.
```java
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
```

+ ##### Metodo getSum(String columnHeader)
Valgono le stesse considerazioni del metodo **getAvg(String columnHeader)**.

+ ##### Metodo getDevStd(String columnHeader, float avg)
In questo metodo, si effettua la deviazione standard calcolata tramite la seguente formula.
![Formula Deviazione Standard](![Alt](/stddev.PNG))
Il parametro `float media` viene fornito tramite il metodo **getAvg(String columnHeader)** già chiamato in **numResults(String columnHeader)** poco prima della chiamata di questo metodo.
```java
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
```

+ ##### Metodo getMaxMin(String columnHeader)
Questo metodo restituisce un array di Float, all'interno del quale vengono salvate le variabili `min` e `max`. Si è scelto l'array per poter valutare massimo e minimo con un unico metodo. 
```java
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
```
+ ##### Metodo getCount(String columnHeader)
Questo metodo calcola per la colonna `columnHeader` il numero di elementi numerici, escludendo i valori `null` dovuti alla presenza di caratteri particolari(: , u, :u) e di celle vuote.
```java
private float getCount(String columnHeader) {
	float conta=0;
	for(Bulk o: vett) {
		Map<String, Float> mappa = new HashMap<>();
		mappa= o.getMap();
		if(mappa.get(columnHeader)!=null) {
		conta++;
		}
	}
	return conta;
}
```
* #### Metodo countUnique(String columnHeader)
Questo metodo viene invocato all'interno del metodo **getStatistics (String columnHeader)** se il risultato della ricerca di `columnHeader` tra le chiavi della mappa dell'oggetto non ha avuto successo. Quindi all'interno di questo metodo troviamo tre costrutti `if` che verificano se `columnHeader` corrisponda a uno dei tre attributi String dell'oggetto. Se risulta vero, allora all'interno di una mappa vengono inseriti come chiavi le stringhe uniche presenti nella colonne e come valori il numero di volte che si sono ripetute.
```java
for(Bulk obj :vett) {
    String value= obj.getStrucPro();
	float num= (res.get(value)==null) ? 1 : res.get(value)+1;
	res.put(value, num);
}
```

Se nessuna delle tre condizioni `if` viene rispettata, e quindi se il nome della colonna inserito non è presente, allora viene restituito `null`. Questo valore sarà importante all'interno del **Controller** per la gestione degli errori.

### Metodo getMetadata()
Questo metodo restituisce i metadati in formato JSON all'interno di una mappa facendo uso dei metodi forniti da `java.lang.reflect`.
Considerando che l'attributo `years` dell'oggetto della classe `Bulk`, all'atto dell'istanziazione, risulta vuoto, allora per produrre i metadati è necessario utilizzare un oggetto con gli attributi popolati, altrimenti per l'attributo `years` si potrebbe avere soltato `interface java.util.Map` come metadato. All'inizio si producono i metadati di tutti gli attributi che vengono quindi salvati nella mappa da restituire. 
Ora per ovviare il problema di `years`, si è deciso prima di eliminare tramite `metadata.remove("years")`il metadato prodotto, poi tramite un costrutto `try-catch` di estrarre i metadati dei valori.
1. viene istanziato un oggetto dell'interfaccia `ParameterizedType` (array di oggetti `Type`) a cui vengono associati oggetti `Type` che rappresentano il tipo di dato di ogni elemento della mappa `years`
2. tramite il primo `foreach` si estrae il tipo di dato del valore, rappresentato poi in stringa
3. tramite il secondo `foreach` si estrae ogni elemento della mappa e di questo elemento si prenderà la chiave (`entry.getKey()`)
```java
try {
        ParameterizedType pt = (ParameterizedType)Bulk.class.getDeclaredField("years").getGenericType();
	    for(Type type : pt.getActualTypeArguments()) {
	       for(Map.Entry<String, Float> entry : mappoggio.entrySet()) {
	            	metadata.put(entry.getKey(), type.toString() );
	       }
	   	}
	}catch(NoSuchFieldException e) {
	       	e.printStackTrace();
	}
```
## 2. Esempi di test per verifica funzionalità
* ##### Richiesta dei dati in formato JSON 
  
  path: `localhost:8080/bulks` `[GET]`
* ##### Richiesta dei metadati in formato Json

  path: `localhost:8080/bulks/metadata` `[GET]`
  
* ##### Richiesta delle statistiche in formato Json
    + dati numerici -> path: `localhost:8080/bulks/statistics/{valore intero compreso tra 1950 e 1999}` `[GET]`
    + dati in formato stringa -> path: `localhost:8080/bulks/statistics/{crops\strucpro\geotime}` `[GET]`
L'attributo richiesto è *Case-Insensitive*.
Se si inserisce un attributo non valido, viene prodotto il messaggio di errore:"Attributo non presente" con errore "`400 Bad Request `"
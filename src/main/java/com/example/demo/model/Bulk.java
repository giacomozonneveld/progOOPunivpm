package com.example.demo.model;


import java.util.HashMap;
import java.util.Map;
/**
 * In questa classe si impostano la struttura, i metodi getters e setters degli oggetti creati 
 * che devono rappresentare le occorrenze del data-set e un metodo per cercare una chiave nella
 * mappa years.
 * @author jackz
 *
 */


public class Bulk {
private String crops;
private String strucPro;
private String geoTime;
/**
 * years rappresenta gli anni e i relativi valori del dataset mappa<anno,valore>
 */
private Map<String, Float> years = new HashMap<>();

//GETTERS AND SETTERS
public String getCrops() {
	return crops;
}
public void setCrops(String crops) {
	this.crops = crops;
}
public String getStrucPro() {
	return strucPro;
}
public void setStrucPro(String strucPro) {
	this.strucPro = strucPro;
}
public String getGeoTime() {
	return geoTime;
}
public void setGeoTime(String geoTime) {
	this.geoTime = geoTime;
}
public Map<String, Float> getMap() {
	return years;
}
public void setMap(Map<String, Float> years) {
	this.years= years;
}

public boolean searchKeyInYears(String key){
	if(this.years.containsKey(key)) {
	return true;
	}
	else return false;
}


}

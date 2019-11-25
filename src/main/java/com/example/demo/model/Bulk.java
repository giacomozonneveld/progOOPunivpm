package com.example.demo.model;

//import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
//implements Serializable
public class Bulk {
private String crops;
private String strucPro;
private String geoTime;
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

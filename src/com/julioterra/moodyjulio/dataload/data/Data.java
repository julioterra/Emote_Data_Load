package com.julioterra.moodyjulio.dataload.data;

import com.julioterra.moodyjulio.dataload.basicelements.Element;
import com.julioterra.moodyjulio.dataload.basicelements.Date;
import com.julioterra.moodyjulio.dataload.basicelements.Time;

import processing.core.PApplet;

public class Data extends Element {

	public Time time_stamp;
	public Date date_stamp;
	public Time time_end;
	public Date date_end;
	
	Data () {
		this.time_stamp = new Time("0", "0", "0", "0");
		this.date_stamp = new Date("0", "0", "0");
		this.time_end = new Time("0", "0", "0", "0");
		this.date_end = new Date("0", "0", "0");
	}
	
	Data (String[] data_entry[]) {		
		this.time_stamp = new Time("0", "0", "0", "0");
		this.date_stamp = new Date("0", "0", "0");
		this.time_end = new Time("0", "0", "0", "0");
		this.date_end = new Date("0", "0", "0");
	}
	
	Data (Data data_entry) {		
		this.time_stamp = new Time(data_entry.time_stamp);
		this.date_stamp = new Date(data_entry.date_stamp);
		this.time_end = new Time(data_entry.time_end);
		this.date_end = new Date(data_entry.date_end);
	}

	public String getTimeStamp() {
		return this.time_stamp.get_time_with_millis_in_string();
	}

	public void setTimeStamp(String timeStamp) {
		this.time_stamp = new Time(timeStamp);
	}

	public String getDateStamp() {
		return this.date_stamp.get_string();
	}

	public void setDateStamp(String dateStamp) {
		this.date_stamp = new Date(dateStamp);
	}

	public String getString() {
		return "";
	}
	
	public String getSQLInsertString() {
		return null;
	}

	public String getSQLInsertString(String data_table) {
		return null;
	}
	
	public String getSQLUpdateSetString(String data_table) {
		return "";
	}
	
	public String getSQLUpdateQueryString(String data_table) {
		return "";
	}


}

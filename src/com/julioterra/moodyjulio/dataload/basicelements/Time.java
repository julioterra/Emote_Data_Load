package com.julioterra.moodyjulio.dataload.basicelements;

import com.julioterra.moodyjulio.dataload.datahandlers.DataProcessor;
import com.julioterra.moodyjulio.dataload.basicelements.Date;

import processing.core.PApplet;

public class Time {
	public int hour;
	public int minute;
	public int second;
	public int millis;

	
	/*************************
	 ** CONSTRUCTOR
	 *************************/	

	public Time () {
		hour = 0;
		minute = 0;
		second = 0;
		millis = 0;
	}
	
	public Time(Time new_time){
		set(new_time);
	}

	public Time (String new_time) {
		this.set(new_time);
	}
	
	public Time (String new_hour, String new_minute, String new_second, String new_millis) {
		this.set(new_hour, new_minute, new_second, new_millis);
	}

	public Time (String new_hour, String new_minute, String new_second) {
		this.set(new_hour, new_minute, new_second);
	}

	public Time (int new_hour, int new_minute, int new_second) {
		this.setInt(new_hour, new_minute, new_second);
	}

	/*************************
	 ** SET FUNCTIONS
	 *************************/	

	public void set(String new_hour, String new_minute, String new_second){
		this.hour = Integer.parseInt(new_hour);
		this.minute = Integer.parseInt(new_minute);
		this.second = Integer.parseInt(new_second);
		this.millis = 0;	
	}

	public void set(String new_time){
		if (new_time.length() >= 9) {
			this.set(PApplet.trim(new_time.substring(0,2)), PApplet.trim(new_time.substring(2,4)), PApplet.trim(new_time.substring(4,6)), PApplet.trim(new_time.substring(7)));	
		} else if (new_time.length() == 8) {
			this.set(PApplet.trim(new_time.substring(0,2)), PApplet.trim(new_time.substring(3,5)), PApplet.trim(new_time.substring(6,8)));
		} else if (new_time.length() == 6) {
			this.set(PApplet.trim(new_time.substring(0,2)), PApplet.trim(new_time.substring(2,4)), PApplet.trim(new_time.substring(4,6)));
		}
	}
	
	public void set(String new_hour, String new_minute, String new_second, String new_millis){
		this.set(new_hour, new_minute, new_second);
		this.millis = Integer.parseInt(new_millis);
	}

	public void setInt(int new_hour, int new_minute, int new_second){
		this.hour = new_hour;
		this.minute = new_minute;
		this.second = new_second;
		this.millis = 0;	
	}

	public void set(Time new_time){
		this.hour = 	new_time.hour;
		this.minute = 	new_time.minute;
		this.second = 	new_time.second;
		this.millis = 	new_time.millis;	
	}


	/*************************
	 ** UPDATE FUNCTIONS
	 *************************/	

	// function will return 1 for a new day, and -1 for change to the day before
	public int update_seconds(long new_seconds) {
		this.second += new_seconds;
		int new_day = 0;
//		PApplet.println(" update seconds " + new_seconds);
		if (this.second < 0) { 
			new_day = this.update_minutes((int) (this.second/60-1));
			this.second = (this.second % 60) + 60;
		} else if (this.second >= 60) {
			new_day = this.update_minutes(this.second/60);
			this.second = this.second % 60;
		}
		return new_day;
	}
	
	public int update_minutes(int new_minutes) {
		this.minute += new_minutes;
		int new_day = 0;
		if (this.minute < 0) { 
			new_day = this.update_hours((this.minute-60)/60);
			this.minute = (this.minute % 60) + 60;
		} else if (this.minute >= 60) {
			new_day = this.update_hours(this.minute/60);
			this.minute = (this.minute % 60);
		}
		return new_day;
	}

	public int update_hours(float new_hours) {
		this.hour += new_hours;
		float new_days = 0;
		if (this.hour < 0) { 
			new_days = this.hour / 24;
			this.hour = PApplet.abs(this.hour % 24);
			return -1;
		} else if (this.hour >= 24) {
			new_days = this.hour / 24;
			this.hour = PApplet.abs(this.hour % 24);
			return 1;
		}
		return (int)new_days;
	}

	public boolean new_day(Time start_time, double millis_delta_per_step, int step_index) {
		double actual_delta = 	millis_delta_per_step * step_index;
		double delta_millis = 	actual_delta;
		double delta_seconds = 	(delta_millis + start_time.millis) / 1000;
		double delta_minutes = 	(delta_seconds + start_time.second) / 60.0;
		double delta_hours = 	(delta_minutes + start_time.minute) / 60.0;

		if ((start_time.hour + delta_hours) > 24) return true;
		else return false;
	}
	
	public void calculate_step_time(Time start_time, float millis_delta_per_step, int step_index) {
		step_index++;
		
		if (Element.debug_code) System.out.println("*************************" + millis_delta_per_step + " index "  + step_index);
		if (Element.debug_code) System.out.println("Time.calculate_step_time(): old time " + start_time.millis + " secs " + start_time.second +
				" mins " + start_time.minute + " hours " + start_time.hour);

		float actual_delta = 	millis_delta_per_step * step_index;
		float delta_millis = 	actual_delta; 
		float delta_seconds = 	(delta_millis + start_time.millis) / 1000;  
		float delta_minutes = 	(float)((delta_seconds + start_time.second) / 60.0); 
		float delta_hours = 	(float)((delta_minutes + start_time.minute) / 60.0);

		if (Element.debug_code) System.out.println("Time.calculate_step_time(): mills " + delta_millis + " secs " + delta_seconds +
				 " mins " + delta_minutes + " hours " + delta_hours);

			this.millis = 	PApplet.abs((int)((start_time.millis + delta_millis) % 1000));
			this.second = 	PApplet.abs((int)((start_time.second + delta_seconds) % 60));		
			this.minute = 	PApplet.abs((int)((start_time.minute + delta_minutes) % 60));
			this.hour = 	PApplet.abs((int)((start_time.hour + delta_hours) % 24));			

		if (millis_delta_per_step < 0) {	
			if (PApplet.abs(delta_millis) > start_time.millis && PApplet.abs(delta_millis) >= 1) this.millis = (1000 - (int)PApplet.abs((start_time.millis + delta_millis)) % 1000);
			if (PApplet.abs(delta_seconds) > start_time.second && PApplet.abs(delta_seconds) >= 1) this.second = (60 - (int)PApplet.abs((start_time.second + delta_seconds)) % 60);		
			if (PApplet.abs(delta_minutes) > start_time.minute &&  PApplet.abs(delta_minutes) >= 1) this.minute = (60 - (int)PApplet.abs((start_time.minute + delta_minutes)) % 60);		
			if (PApplet.abs(delta_hours) > start_time.hour &&  PApplet.abs(delta_hours) >= 1) this.hour = (60 - (int)PApplet.abs((start_time.hour + delta_hours)) % 60);		

			if (this.millis == 1000) this.millis = 0;
			if (this.second == 60) this.second = 0;
			if (this.minute == 60) this.minute = 0;
			if (this.hour == 24) this.hour = 0;
			
		}

		if (Element.debug_code) System.out.println("Time.calculate_step_time(): new time " + this.millis + " secs " + this.second +
				" mins " + this.minute + " hours " + this.hour);

		
	}
	
	/*************************
	 ** GET FUNCTIONS
	 *************************/	

	public static long get_time_in_seconds(Time convert_time) {
		long hours_sec = convert_time.hour * 60 * 60;
		long minutes_sec = convert_time.minute * 60;
		long time_in_seconds = hours_sec + minutes_sec + convert_time.second;
		return time_in_seconds;	
	}
	
	
	public long get_time_in_seconds() {
		return get_time_in_seconds(this);
	}

	public long get_time_in_millis() {
		long hours_millis = this.hour * 60 * 60 * 1000;
		long minutes_millis = this.minute * 60 * 1000;
		long seconds_millis = this.second * 1000;
		return (hours_millis + minutes_millis + seconds_millis + this.millis);	
	}

	public boolean equals(Time new_time) {
		Time check_time = new Time(new_time);
		if (this.hour == check_time.hour && this.minute == check_time.minute && this.second == check_time.second) return true;
		else return false;
	}

	public boolean equals_hrs_mins(Time new_time) {
		Time check_time = new Time(new_time);
		if (this.hour == check_time.hour && this.minute == check_time.minute) return true;
		else return false;
	}

	public long get_millis() {
		return this.millis;
	}

	public String get_string() {		
		return DataProcessor.time_date_part_to_string(this.hour) + DataProcessor.time_date_part_to_string(this.minute) + DataProcessor.time_date_part_to_string(this.second); 	
	}	

	public String get_time_with_millis_in_string() {		
		return DataProcessor.time_date_part_to_string(this.hour) + DataProcessor.time_date_part_to_string(this.minute) + DataProcessor.time_date_part_to_string(this.second) + "." + convert_millis_to_string(this.millis); 	
	}	

	public String get_time_for_sql() {		
		return DataProcessor.time_date_part_to_string(this.hour) + ":" +  DataProcessor.time_date_part_to_string(this.minute) + ":" + DataProcessor.time_date_part_to_string(this.second);	
	}

	public String get_time_millis_for_sql() {		
		return convert_millis_to_string(this.millis);	
	}

	public static String get_timestamp_for_sql(Date date, Time time) {		
		return date.get_date_for_sql() + " " + time.get_time_for_sql();
	}

	/*************************
	 ** CALCULATE FUNCTIONS
	 *************************/	
	
	public long calculate_time_dif_millis (Time end_time) {
		return calculate_time_dif_millis(this, end_time);
	}
	
	public String convert_millis_to_string(long millis_time) {
		String millis_string = String.valueOf(millis_time);
		if (millis_time < 100) {
			millis_string = "0" + millis_time;
		} else if (millis_time < 10) {
			millis_string = "00" + millis_time;
		} else if (millis == 0) return "000";
		return millis_string;
	}

	/*************************
	 ** STATIC FUNCTIONS
	 *************************/	

	public static long calculate_time_dif_seconds(Time _start_time, Time _end_time) {	
		Time start_time = new Time (_start_time);
		Time end_time = new Time (_end_time);
		long time_dif = end_time.get_time_in_seconds() - start_time.get_time_in_seconds();		
		if (time_dif < 0) time_dif += (24*60*60) - end_time.get_time_in_seconds();
//		if (end_time.get_time_in_seconds() < start_time.get_time_in_seconds())
//			time_dif = (end_time.get_time_in_seconds() + (24*60*60)) - start_time.get_time_in_seconds();		
		return time_dif; 	
	}

	public static long calculate_time_dif_seconds_maxout(Time start_time, Time end_time) {		
		if (end_time.get_time_in_seconds() < start_time.get_time_in_seconds()) end_time = new Time("23:59:59");		
		long time_dif = end_time.get_time_in_seconds() - start_time.get_time_in_seconds();		
		return time_dif; 	
	}

	public static long calculate_time_dif_millis(Time start_time, Time end_time) {		
		long seconds_dif = calculate_time_dif_seconds(start_time, end_time);
		long millis_dif = end_time.millis - start_time.millis;
		if (end_time.millis < start_time.millis) millis_dif = (end_time.millis + 1000) - start_time.millis;
		return (seconds_dif*1000) + millis_dif; 	
	}	

	
}

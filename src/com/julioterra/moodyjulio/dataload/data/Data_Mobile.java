package com.julioterra.moodyjulio.dataload.data;

import com.julioterra.moodyjulio.dataload.basicelements.Date;
import com.julioterra.moodyjulio.dataload.basicelements.Time;

public class Data_Mobile extends Data{

	// core database values attributes
	public long arduino_time;
	public String entry_id;
	public int gsr;
	public float gsr_high_pass;
	public int heart_rate;
	public int humidity; 
	public int temperature;
	public int button_one; 
	public int minutes_measured;

	// constructor for mobile data that is being accessed from a raw file
	public Data_Mobile(int file_number, String data_entry[]) {
		super();
		if (data_entry.length > 5) 
			setAll(data_entry[0], String.valueOf(file_number), data_entry[4], data_entry[5],  data_entry[1], "0",
				   data_entry[2], "0", "0", "0", "0");
	}

	// constructor for mobile data that is being accessed from the database
	public Data_Mobile(String data_entry[]) {
		super();
		if (data_entry.length == 7) 
			setAll("0", data_entry[0], "00/00/00", data_entry[1], data_entry[2], "0", data_entry[3], data_entry[4], data_entry[5], "0", data_entry[6]);
		else if (data_entry.length == 8) 
			setAll("0", "0", data_entry[0], data_entry[1], data_entry[2], data_entry[3], data_entry[4], data_entry[5], data_entry[6],
				   data_entry[7], "0");
		else if (data_entry.length == 10) 
			setAll(data_entry[0], data_entry[1], data_entry[2], data_entry[3], data_entry[4], data_entry[5], data_entry[6],
				   data_entry[7], data_entry[8], data_entry[9], "0");
		else if (data_entry.length == 11) {
			setAll(data_entry[0], data_entry[1], data_entry[2], data_entry[3], data_entry[5], data_entry[6],
				   data_entry[7], data_entry[8], data_entry[9], data_entry[10], "0");
			this.time_stamp.millis = Integer.parseInt(data_entry[4]);

		}
	}
	
	public Data_Mobile(Date date, Time time) {
		this.date_stamp = new Date(date);
		this.time_stamp = new Time(time);
	}
	
	public Data_Mobile() {
		super();
		this.arduino_time = 0;
		this.entry_id = "";
		this.gsr = 0;
		this.gsr_high_pass = 0f;
		this.heart_rate = 0;
		this.humidity = 0; 
		this.temperature = 0;
		this.button_one = 0; 
		this.minutes_measured = 0;
	}

	public Data_Mobile(Data_Mobile new_data) {
		this.date_stamp = new Date(new_data.date_stamp);
		this.time_stamp = new Time(new_data.time_stamp);
		this.arduino_time = new_data.arduino_time;
		this.entry_id = new_data.entry_id;
		this.gsr = new_data.gsr;
		this.gsr_high_pass = new_data.gsr_high_pass;
		this.heart_rate = new_data.heart_rate;
		this.humidity = new_data.humidity; 
		this.temperature = new_data.temperature;
		this.button_one = new_data.button_one; 
		this.minutes_measured = new_data.minutes_measured;	
	}

	
	/*******************************
	 * GETTER AND SETTER FUNCTIONS FOR ALL MAIN CLASS ATTRIBUTES
	 */

//	public void setAll(String arduino_time, String file_number, String date_stamp, String time_stamp, String gsr, String gsr_high_pass, 
//			String heart_rate, String humidity, String temperature, String button_one) {

	public void setAll(String arduino_time, String entry_id, String date_stamp, String time_stamp, String gsr, String gsr_high_pass, 
			String heart_rate, String humidity, String temperature, String button_one, String minutes_measured) {
		this.time_stamp = new Time(time_stamp);
		this.date_stamp = new Date(date_stamp);
		this.entry_id = entry_id;
		try {
			this.arduino_time = Long.parseLong(arduino_time);
			this.gsr = Integer.parseInt(gsr);
			this.gsr_high_pass = Integer.parseInt(gsr_high_pass);
			this.heart_rate = Integer.parseInt(heart_rate);
			this.humidity = Integer.parseInt(humidity); 
			this.temperature = Integer.parseInt(temperature);
			this.button_one = Integer.parseInt(button_one); 
			this.minutes_measured = Integer.parseInt(minutes_measured);
		} catch (NumberFormatException e) {
			System.out.println("Data_Mobile_New.setAll(): error converting string to integer " + e.getMessage());
		}
	}
	
	public int getGsr() {
		return gsr;
	}

	public void setGsr(int gsr) {
		this.gsr = gsr;
	}

	public float setGsrHighPass() {
		return this.gsr_high_pass;
	}

	public void setGsrHighPass(float gsr) {
		this.gsr_high_pass = gsr;
	}

	public int getHeartRate() {
		return heart_rate;
	}

	public void setHeartRate(int heartRate) {
		this.heart_rate = heartRate;
	}

	@Override
	public String getSQLInsertString(String data_table) {
		super.getSQLInsertString();
		if (data_table.equals("data_minutes_avg")) {
			return "(\'" + this.entry_id + "\', \'" + 
				this.time_stamp.get_time_for_sql() + "\', \'" +  
				this.gsr + "\', \'" +
				this.heart_rate + "\', \'" + 
				this.humidity + "\', \'" +
				this.temperature + "\', \'" + 
				this.minutes_measured + "\')";
		}		
		else if (data_table.equals("data_minutes")) {
			return "(\'" + this.date_stamp.get_date_for_sql() + "\', \'" + 
				this.time_stamp.get_time_for_sql() + "\', \'" +  
				this.gsr + "\', \'" + 
				this.gsr_high_pass + "\', \'" +
				this.heart_rate + "\', \'" + 
				this.humidity + "\', \'" +
				this.temperature + "\', \'" + 
				this.button_one + "\')";
		}		
		else return "(\'" + this.arduino_time +  "\', \'" + this.entry_id +  "\', \'" + 
				this.date_stamp.get_date_for_sql() + "\', \'" + 
				this.time_stamp.get_time_for_sql() + "\', \'" +  
				this.time_stamp.get_time_millis_for_sql() + "\', \'" +
				this.gsr + "\', \'" + this.gsr_high_pass + "\', \'" +
				this.heart_rate + "\', \'" + this.humidity + "\', \'" +
				this.temperature + "\', \'" + this.button_one + "\')";
	}

	@Override
	public String getSQLUpdateQueryString(String data_table) {
		String query_str = "";
		if (data_table.equals("data_raw")) 
			query_str = " date_stamp=\'" + this.date_stamp.get_date_for_sql() + "\' AND " + 
						" date_stamp=\'" + this.time_stamp.get_time_for_sql() + "\' AND " + 
						" time_stamp_millis=\'" + this.time_stamp.get_time_millis_for_sql() + "\'\n";
		else if (data_table.equals("data_minutes")) 
			query_str = " date_stamp=\'" + this.date_stamp.get_date_for_sql() + "\' AND " + 
						" date_stamp=\'" + this.time_stamp.get_time_for_sql() + "\'\n";
		else if (data_table.equals("data_minutes_avg")) 
			query_str = " entry_id=\'" + this.entry_id + "\' AND " +
						" time_stamp=\'" + this.time_stamp.get_time_for_sql() + "\'\n";
		return query_str;
	}
	
	@Override
	public String getSQLUpdateSetString(String data_table) {
		String query_str = "";
		if (data_table.equals("data_raw"))
			query_str = " arduino_time=\'" + this.arduino_time +  "\', " +
						" entry_id=\'" + this.entry_id +  "\', " +
						" gsr=\'" + this.gsr + "\', "+
						" gsr_filtered=\'" + this.gsr_high_pass + "\', " +
						" heart_rate=\'" + this.heart_rate + "\', " +
						" humidity=\'" + this.humidity + "\', " +
						" temperature=\'" + this.temperature + "\', " + 
						" button_one=\'" + this.button_one + "\'";
		else if (data_table.equals("data_minutes")) 
			query_str = " gsr=\'" + this.gsr + "\', "+
						" gsr_filtered=\'" + this.gsr_high_pass + "\', " +
						" heart_rate=\'" + this.heart_rate + "\', " +
						" humidity=\'" + this.humidity + "\', " +
						" temperature=\'" + this.temperature + "\', " + 
						" button_one=\'" + this.button_one + "\'";
		else if (data_table.equals("data_minutes_avg")) 
			query_str = " gsr=\'" + this.gsr + "\', "+
						" heart_rate=\'" + this.heart_rate + "\', " +
						" humidity=\'" + this.humidity + "\', " +
						" temperature=\'" + this.temperature + "\', " + 
						" minutes_measured=\'" + this.minutes_measured + "\'";
		return query_str;
	}

}

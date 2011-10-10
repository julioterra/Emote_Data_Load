package com.julioterra.moodyjulio.dataload.data;

import java.util.ArrayList;

import com.julioterra.moodyjulio.dataload.basicelements.Element;
import com.julioterra.moodyjulio.dataload.basicelements.Date;
import com.julioterra.moodyjulio.dataload.basicelements.Time;
import com.julioterra.moodyjulio.dataload.datahandlers.DataProcessor_Journal;

import processing.core.PApplet;

public class Data_Log extends Data {

	public String entry_id; 
	public String log_name; 
	public String notes; 
	public long seconds_measured;
	
	/**************************************
	 ** CURRENT -- CONSTRUCTOR FUNCTIONS
	 **************************************/
	public Data_Log() {
		super();
		this.entry_id = 			"";
		this.log_name =				"";
		this.notes = 				"";
		this.seconds_measured = 	0;
	}
	
	public Data_Log(Date date_stamp, Time time_stamp, Date date_end, Time time_end) {
		this();
		this.date_stamp = new Date(date_stamp);
		this.time_stamp = new Time(time_stamp);
		this.date_end = new Date(date_end);
		this.time_end = new Time(time_end);	
		this.calculate_seconds_measured();
	}

	public Data_Log(String[] data_entry) {
		super();
		if (data_entry.length == 4) { 
			this.process_raw_data(data_entry);
		}
		else if (data_entry.length == 8) { 
			this.entry_id = data_entry[0];
			this.date_stamp = new Date(data_entry[1]);
			this.time_stamp = new Time(data_entry[2]);
			this.date_end = new Date(data_entry[3]);
			this.time_end = new Time(data_entry[4]);
			this.seconds_measured = Long.parseLong(data_entry[5]);			
			this.log_name = data_entry[6];
			this.notes = data_entry[7];		
		}
	}
	
	public Data_Log(Data_Log new_data) {
		super();
		this.entry_id = new_data.entry_id;
		this.log_name = new_data.log_name;
		this.notes = new_data.notes;
		this.seconds_measured = new_data.seconds_measured;
		
		this.date_stamp = new Date(new_data.date_stamp);
		this.time_stamp = new Time(new_data.time_stamp);
		this.date_end = new Date(new_data.date_end);
		this.time_end = new Time(new_data.time_end);
	}

	/*******************************
	 ** GETTER AND SETTER FUNCTIONS INDIVIDUAL CLASS ATTRIBUTES
	 **/

	public void process_raw_data(String[] data_entry) {
		this.entry_id = data_entry[0];
		this.log_name = data_entry[1].substring(0, data_entry[1].length()-1);

		// setting the date stamp
		int month = 0;
		String[] process_data = PApplet.split(data_entry[2], " ");		
		for (int i = 0; i < NamesOfMonths.length; i++) if (NamesOfMonths[i].contains(process_data[1])) month = i+1;
		this.date_stamp = new Date("2011", ""+month, process_data[2].substring(0, process_data[2].length()-1));

		// setting the time stamp
		process_data = PApplet.split(data_entry[3], " ");
		this.time_stamp = new Time(process_data[0]);

//		System.out.print("Data_Log.process_raw_data() - process_data, third string in pieces ");
//		for (int i = 0; i < process_data.length; i++) System.out.print(i + ": " + process_data[i] + " ");
//		System.out.println();
		
		// setting the seconds_measured variable
		int start_loc = data_entry[3].indexOf("for") + 4;
		int end_loc = data_entry[3].indexOf("(");
		if (end_loc == -1) end_loc = data_entry[3].length();
		if (start_loc == (-1+4)) this.seconds_measured = 1;
		else {
			String temp_seconds_measured = data_entry[3].substring(start_loc, end_loc);
			
			if (temp_seconds_measured.indexOf(",") == -1) {
				process_data = PApplet.split(temp_seconds_measured, " ");
				if (process_data[1].contains("hour")) this.seconds_measured = Integer.parseInt(process_data[0])*60*60;
				else if (process_data[1].contains("mins")) this.seconds_measured = Integer.parseInt(process_data[0])*60;
			} else {
				process_data = PApplet.split(temp_seconds_measured, ",");
				for (int j = 0; j < process_data.length; j++) {
					process_data[j] = PApplet.trim(process_data[j]);
					if(process_data[j].contains("hrs")) this.seconds_measured = Integer.parseInt(PApplet.trim(process_data[0].substring(0,process_data[j].length()-3)))*60*60;
					else if(process_data[j].contains("hour")) this.seconds_measured = Integer.parseInt(PApplet.trim(process_data[0].substring(0,process_data[j].length()-4)))*60*60;
					else if(process_data[j].contains("mins")) this.seconds_measured +=  Integer.parseInt(PApplet.trim(process_data[j].substring(0,process_data[j].length()-5)))*60;							
					else if(process_data[j].contains("secs")) this.seconds_measured +=  Integer.parseInt(PApplet.trim(process_data[j].substring(0,process_data[j].length()-4)));							
					else if(process_data[j].contains("hr")) this.seconds_measured = Integer.parseInt(PApplet.trim(process_data[0].substring(0,process_data[j].length()-2)))*60*60;
					else if(process_data[j].contains("m")) this.seconds_measured +=  Integer.parseInt(PApplet.trim(process_data[j].substring(0,process_data[j].length()-1)))*60;							
					else if(process_data[j].contains("s")) this.seconds_measured +=  Integer.parseInt(PApplet.trim(process_data[j].substring(0,process_data[j].length()-1)));							
				}
			}
		}		

		// setting the notes variable
		start_loc = data_entry[3].indexOf("(")+1;
		end_loc = data_entry[3].indexOf(")");
		if (start_loc == -1) start_loc = data_entry[3].length();
		if (end_loc == -1) end_loc = data_entry[3].length();
		this.notes = data_entry[3].substring(start_loc, end_loc);
		if (start_loc == -1 && end_loc == -1) this.notes = "";
		
		this.date_end = new Date(date_stamp);
		this.time_end = new Time(time_stamp);
		
		this.date_end.update_day(this.time_end.update_seconds(this.seconds_measured));
	
	}

	public void setEndDateTime(Date end_date, Time end_time) {
		this.date_end = new Date(end_date);
		this.time_end = new Time(end_time);
		this.calculate_seconds_measured();
	}
	
	public void calculate_seconds_measured() {
		this.seconds_measured = Time.calculate_time_dif_seconds(this.time_stamp, this.time_end);
	}
	
	public String getActivity() {
		return notes;
	}

	public void setActivity(String activity) {
		this.notes = activity;
	}

	/*****************************
	 * GET_RELATIONAL_ATTRIBUTE_STRING
	 * Returns the the appropriate SQL insert string based on the version of this object instance.
	 * 
	 * @return
	 */
	public String[] getRelationalAttributeStringArray() {
		ArrayList<String> string_values = new ArrayList<String>();
		
		// add code here that creates a separate string with the appropriate category for each element
		// categories for log will include 

		String[] query_str = new String[string_values.size()];
		for (int i = 0; i < query_str.length; i++) query_str[i] = string_values.get(i);
		return query_str;
	}

	@Override
	/*****************************
	 * GET_SQL_INSERT_STRING
	 * Returns the the appropriate SQL insert string based on the version of this object instance.
	 * 
	 * @return
	 */
	public String getSQLInsertString(String data_table) {
		String query_str = "";
		if (data_table.equals("data_raw")) query_str = this.getSQLInsertString();
		return query_str;
	}

	@Override
	/*****************************
	 * GET_SQL_INSERT_STRING
	 * Returns the version of the SQL insert string for the data_raw data table.
	 * @return
	 */
	public String getSQLInsertString() {
		return " (\'" + 
				this.entry_id +  "\', \'" + 
				this.date_stamp.get_date_for_sql() + "\', \'" + 
				this.time_stamp.get_time_for_sql() + "\', \'" +  
				this.date_end.get_date_for_sql() + "\', \'" + 
				this.time_end.get_time_for_sql() + "\', \'" +  
				this.seconds_measured + "\', \'" +  
				this.log_name +  "\', \'" + 
				DataProcessor_Journal.prep_string_upload(this.notes) +   
				"\')";
	}
	

	/*****************************
	 * GET_SQL_UPDATE_SET_STRING
	 * Returns the appropriate set string for an update query. This method should be used
	 * along with the getSQLUpdateQueryString method.
	 * 
	 * @return
	 */
	public String getSQLUpdateSetString(String data_table) {
		String query_str = "";
		if (data_table.equals("data_raw")) {
			query_str = " entry_id=\'" + this.entry_id +  "\', " +
						" date_stamp=\'" + this.date_stamp.get_date_for_sql() + "\', " +
						" time_stamp=\'" + this.time_stamp.get_time_for_sql() + "\', " + 
						" date_end=\'" + this.date_end.get_date_for_sql() + "\', " +
						" time_end=\'" + this.time_end.get_time_for_sql() + "\', " +
						" seconds_measured=\'" + this.seconds_measured + "\', "+
						" log_name=\'" + this.log_name +  "\', " + 
						" notes=\'" + DataProcessor_Journal.prep_string_upload(this.notes) + "\'";  
		}
		return query_str;
	}

	/*****************************
	 * GET_SQL_STRING
	 * Returns the the appropriate SQL insert string based on the version of this object instance.
	 * 
	 * @return
	 */
	public String getSQLUpdateQueryString(String data_table) {
		String query_str = "";
		if (data_table.equals("data_raw")) query_str = " entry_id=\'" + this.log_name + "\'";
		else if (data_table.equals("data_hourly")) query_str = " date_stamp=\'" + this.date_stamp.get_date_for_sql() + "\' AND " +
															   " time_stamp=\'" + this.time_stamp.get_time_for_sql() + "\'\n";
		else if (data_table.equals("data_hourly_avg")) query_str = " entry_id=\'" + this.log_name + "\' AND " +
																   " time_stamp=\'" + this.time_stamp.get_time_for_sql() + "\' AND " +
																   " time_end=\'" + this.time_end.get_time_for_sql() + "\'\n";
		return query_str;
	}

	public String getString() {
		return  " entry_id=\'" + this.entry_id +  "\', " +
				" date_stamp=\'" + this.date_stamp.get_date_for_sql() + "\', " +
				" time_stamp=\'" + this.time_stamp.get_time_for_sql() + "\', " + 
				" date_end=\'" + this.date_end.get_date_for_sql() + "\', " +
				" time_end=\'" + this.time_end.get_time_for_sql() + "\', " +
				" seconds_measured=\'" + this.seconds_measured + "\', "+
				" log_name=\'" + this.log_name +  "\', " + 
				" notes=\'" + this.notes + "\'";
	}
	
}

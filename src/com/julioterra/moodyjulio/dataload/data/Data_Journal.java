package com.julioterra.moodyjulio.dataload.data;

import java.util.ArrayList;

import com.julioterra.moodyjulio.dataload.basicelements.Element;
import com.julioterra.moodyjulio.dataload.basicelements.Date;
import com.julioterra.moodyjulio.dataload.basicelements.Time;
import com.julioterra.moodyjulio.dataload.datahandlers.DataProcessor_Journal;

import processing.core.PApplet;

public class Data_Journal extends Data {

	// version 1 variables
	public float data_version; 
	public String entry_id; 
	public float emotion_valence; 
	public float emotion_intensity; 
	public float emotion_1_intensity; 
	public float emotion_2_intensity; 
	public float emotion_3_intensity; 
	public String activity; 
	public String location;
	public String people;
	public int people_count;
	public String description;

	// version 2 variables
	public String emotion_1_name; 
	public String emotion_2_name;
	public String emotion_3_name; 
	public long seconds_measured;

	/**************************************
	 ** CURRENT -- CONSTRUCTOR FUNCTIONS
	 **************************************/
	public Data_Journal() {
		super();
		this.data_version = 		0f;
		this.entry_id =				"0";
		this.emotion_valence = 		0f;
		this.emotion_intensity = 	0f;
		this.emotion_1_name = 		"";
		this.emotion_2_name = 		"";
		this.emotion_3_name =		"";
		this.activity = 			"";
		this.location = 			"";
		this.people = 				"";
		this.people_count =			0;
		this.description = 			"";
		
		this.emotion_1_intensity = 	0f;
		this.emotion_2_intensity = 	0f;
		this.emotion_3_intensity = 	0f;
		this.seconds_measured = 	0;
	}
	
	public Data_Journal(Date date_stamp, Time time_stamp, Date date_end, Time time_end) {
		this();
		this.date_stamp = new Date(date_stamp);
		this.time_stamp = new Time(time_stamp);
		this.date_end = new Date(date_end);
		this.time_end = new Time(time_end);	
		this.calculate_seconds_measured();
		this.description = 			"";
	}

	public Data_Journal(float data_version, String post_id, Date date_stamp, Time time_stamp, float emotion_valence, float emotion_intensity, 
			String emotion_1_name, String emotion_2_name, String emotion_3_name, String activity, String location, String people, 
			int people_count, String description) {
		super();
		this.data_version = data_version;
		this.entry_id = post_id;
		this.date_stamp = new Date(date_stamp);
		this.time_stamp = new Time(time_stamp);
		this.emotion_valence = emotion_valence;
		this.emotion_intensity = emotion_intensity;
		this.emotion_1_name = emotion_1_name;
		this.emotion_2_name = emotion_2_name;
		this.emotion_3_name = emotion_3_name;
		this.activity = activity;
		this.location = location;
		this.people = people;
		this.people_count = people_count;
		this.description = description;
//		this.date_end = new Date(date_end);
//		this.time_end = new Time(time_end);		
	}
	
	public Data_Journal(float data_version, String post_id, Date date_stamp, Time time_stamp, float emotion_valence, float emotion_intensity, 
			String emotion_1_name, String emotion_2_name, String emotion_3_name, String activity, String location, String people, 
			int people_count, String description, float emotion_1_intensity, float emotion_2_intensity, float emotion_3_intensity, 
			long seconds_measured) {
		super();
		this.data_version = data_version;
		this.entry_id = post_id;
		this.date_stamp = new Date(date_stamp);
		this.time_stamp = new Time(time_stamp);
		this.emotion_valence = emotion_valence;
		this.emotion_intensity = emotion_intensity;
		this.emotion_1_name = emotion_1_name;
		this.emotion_2_name = emotion_2_name;
		this.emotion_3_name = emotion_3_name;
		this.activity = activity;
		this.location = location;
		this.people = people;
		this.people_count = people_count;
		this.description = description;
		this.emotion_1_intensity = emotion_1_intensity;
		this.emotion_2_intensity = emotion_2_intensity;
		this.emotion_3_intensity = emotion_3_intensity;
		this.seconds_measured = seconds_measured;
//		this.date_end = new Date(date_end);
//		this.time_end = new Time(time_end);		
	}

	public Data_Journal(String[] data_entry) {
		super();
		if (Element.debug_code) System.out.print("Data_Journal() - new object " + " ");
		if (Element.debug_code) for (int j = 0; j < data_entry.length; j++) System.out.print(data_entry[j] + ", ");
		if (Element.debug_code) System.out.println();

		if (data_entry.length >= 1) this.data_version = Float.parseFloat(data_entry[0]);	
		// data_version 0 through 0.9: raw data input
		if (this.data_version < 1) newRawData(data_entry);
		// data_version 1 through 1.9: "hourly" data input
		else if (this.data_version < 2) newProcessed_Hourly(data_entry);
		// data_version 2 through 2.9: "average hourly" data input
		else if (this.data_version < 3) newProcessed_Avg(data_entry);
	}
	
	public Data_Journal(Data_Journal new_data) {
		super();
		this.entry_id = new_data.entry_id;
		this.data_version = new_data.data_version;
		this.date_stamp = new Date(new_data.date_stamp);
		this.time_stamp = new Time(new_data.time_stamp);
		this.emotion_valence = new_data.emotion_valence;
		this.emotion_intensity = new_data.emotion_intensity;
		this.emotion_1_name = new_data.emotion_1_name;
		this.emotion_2_name = new_data.emotion_2_name;
		this.emotion_3_name = new_data.emotion_3_name;
		this.activity = new_data.activity;
		this.location = new_data.location;
		this.people = new_data.people;
		this.people_count = new_data.people_count;
		this.description = new_data.description;
		this.emotion_1_intensity = new_data.emotion_1_intensity;
		this.emotion_2_intensity = new_data.emotion_2_intensity;
		this.emotion_3_intensity = new_data.emotion_3_intensity;
		this.date_end = new Date(new_data.date_end);
		this.time_end = new Time(new_data.time_end);
		this.seconds_measured = new_data.seconds_measured;
	}

	/*******************************
	 ** GETTER AND SETTER FUNCTIONS INDIVIDUAL CLASS ATTRIBUTES
	 **/

	public void newRawData(String[] data_entry) {
		if (this.data_version == 0.0f) {
			if (data_entry.length >= 2) this.entry_id = data_entry[1];
			if (data_entry.length >= 3) this.date_stamp = new Date(data_entry[2]);
			if (data_entry.length >= 4) this.time_stamp = new Time(data_entry[3]);
			if (data_entry.length >= 5) this.date_end = new Date(data_entry[4]);
			if (data_entry.length >= 6) this.time_end = new Time(data_entry[5]);
			if (data_entry.length >= 7) this.seconds_measured = Long.parseLong(data_entry[6]);			
			if (data_entry.length >= 8) this.emotion_valence = Float.parseFloat(data_entry[7]);
			if (data_entry.length >= 9) this.emotion_intensity = Float.parseFloat(data_entry[8]);
			if (data_entry.length >= 10) this.emotion_1_name = data_entry[9];		
			if (data_entry.length >= 11) this.emotion_1_intensity = Float.parseFloat(data_entry[10]);
			if (data_entry.length >= 12) this.emotion_2_name = data_entry[11];		
			if (data_entry.length >= 13) this.emotion_2_intensity = Float.parseFloat(data_entry[12]);
			if (data_entry.length >= 14) this.emotion_3_name = data_entry[13];		
			if (data_entry.length >= 15) this.emotion_3_intensity = Float.parseFloat(data_entry[14]);			
			if (data_entry.length >= 16) this.activity = data_entry[15];		
			if (data_entry.length >= 17) this.location = data_entry[16];		
			if (data_entry.length >= 18) this.people = data_entry[17];		
			if (data_entry.length >= 19) this.people_count = Integer.parseInt(data_entry[18]);		
			if (data_entry.length >= 20) this.description = data_entry[19];					
		}
		if (this.data_version >= 0.1) {
			if (data_entry.length >= 2) this.entry_id = data_entry[1];
			if (data_entry.length >= 3) this.date_stamp = new Date(data_entry[2]);
			if (data_entry.length >= 4) this.time_stamp = new Time(data_entry[3]);
			if (data_entry.length >= 5) this.emotion_valence = Float.parseFloat(data_entry[4]);
			if (data_entry.length >= 6) this.emotion_intensity = Float.parseFloat(data_entry[5]);
			if (data_entry.length >= 7) this.emotion_1_name = data_entry[6];		
			if (data_entry.length >= 8) this.emotion_2_name = data_entry[7];		
			if (data_entry.length >= 9) this.emotion_3_name = data_entry[8];		
			if (data_entry.length >= 10) this.activity = data_entry[9];		
			if (data_entry.length >= 11) this.location = data_entry[10];		
			if (data_entry.length >= 12) this.people = data_entry[11];		
			if (data_entry.length >= 13) this.people_count = Integer.parseInt(data_entry[12]);		
			if (data_entry.length >= 14) this.description = data_entry[13];		
		}
		if (this.data_version >= 0.2) {
			if (data_entry.length >= 15) this.emotion_1_intensity = Float.parseFloat(data_entry[14]);
			if (data_entry.length >= 16) this.emotion_2_intensity = Float.parseFloat(data_entry[15]);
			if (data_entry.length >= 17) this.emotion_3_intensity = Float.parseFloat(data_entry[16]);			
		}
	}

	public void newProcessed_Hourly(String[] data_entry) {
		if (data_entry.length >= 2) this.entry_id = data_entry[1];
		if (data_entry.length >= 3) this.date_stamp = new Date(data_entry[2]);
		if (data_entry.length >= 4) this.time_stamp = new Time(data_entry[3]);
		if (data_entry.length >= 5) this.date_end = new Date(data_entry[4]);
		if (data_entry.length >= 6) this.time_end = new Time(data_entry[5]);
		if (data_entry.length >= 7) this.seconds_measured = Long.parseLong(data_entry[6]);			
		if (data_entry.length >= 8) this.emotion_valence = Float.parseFloat(data_entry[7]);
		if (data_entry.length >= 9) this.emotion_intensity = Float.parseFloat(data_entry[8]);
		if (data_entry.length >= 10) this.emotion_1_name = data_entry[9];		
		if (data_entry.length >= 11) this.emotion_1_intensity = Float.parseFloat(data_entry[10]);
		if (data_entry.length >= 12) this.emotion_2_name = data_entry[11];		
		if (data_entry.length >= 13) this.emotion_2_intensity = Float.parseFloat(data_entry[12]);
		if (data_entry.length >= 14) this.emotion_3_name = data_entry[13];		
		if (data_entry.length >= 15) this.emotion_3_intensity = Float.parseFloat(data_entry[14]);			
		if (data_entry.length >= 16) this.activity = data_entry[15];		
		if (data_entry.length >= 17) this.location = data_entry[16];		
		if (data_entry.length >= 18) this.people = data_entry[17];		
		if (data_entry.length >= 19) this.people_count = Integer.parseInt(data_entry[18]);		
		if (data_entry.length >= 20) this.description = data_entry[19];			
	}

	public void newProcessed_Avg(String[] data_entry) {
		if (data_entry.length >= 2) this.entry_id = data_entry[1];
		if (data_entry.length >= 3) this.time_stamp = new Time(data_entry[2]);
		if (data_entry.length >= 4) this.time_end = new Time(data_entry[3]);
		if (data_entry.length >= 5) this.seconds_measured = Long.parseLong(data_entry[4]);			
		if (data_entry.length >= 6) this.emotion_valence = Float.parseFloat(data_entry[5]);
		if (data_entry.length >= 7) this.emotion_intensity = Float.parseFloat(data_entry[6]);
		if (data_entry.length >= 8) this.emotion_1_name = data_entry[7];		
		if (data_entry.length >= 9) this.emotion_1_intensity = Float.parseFloat(data_entry[8]);
		if (data_entry.length >= 10) this.emotion_2_name = data_entry[9];
		if (data_entry.length >= 11) this.emotion_2_intensity = Float.parseFloat(data_entry[10]);
		if (data_entry.length >= 12) this.emotion_3_name = data_entry[11];
		if (data_entry.length >= 13) this.emotion_3_intensity = Float.parseFloat(data_entry[12]);			
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
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPeople() {
		return people;
	}

	public void setPeople(String people) {
		this.people = people;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	/*****************************
	 * GET_RELATIONAL_ATTRIBUTE_STRING
	 * Returns the the appropriate SQL insert string based on the version of this object instance.
	 * 
	 * @return
	 */
	public String[] getRelationalAttributeStringArray() {
		ArrayList<String> string_values = new ArrayList<String>();

		if (data_version < 1.0f) {
			string_values.add(this.emotion_1_name);
			string_values.add(this.emotion_2_name);
			string_values.add(this.emotion_3_name);
			
			if (this.emotion_intensity < 0.25) string_values.add("low_emotion_intensity");	
			else if (this.emotion_intensity >= 0.25 && this.emotion_intensity < 0.5) string_values.add("mid_emotion_intensity");	
			else if (this.emotion_intensity >= 0.5 && this.emotion_intensity < 0.75) string_values.add("elevated_emotion_intensity");	
			else string_values.add("high_emotion_intensity");	

			String[] activity_list = PApplet.split(this.activity, ",");
			for (int i = 0; i < activity_list.length; i++) string_values.add(PApplet.trim(activity_list[i]));

			String[] location_list = PApplet.split(this.location, ",");
			for (int i = 0; i < location_list.length; i++) string_values.add(PApplet.trim(location_list[i]));

			String[] people_list = PApplet.split(this.people, ",");
			for (int i = 0; i < people_list.length; i++) string_values.add(PApplet.trim(people_list[i]));

			if (this.people_count == 0) string_values.add("no_people");	
			else if (this.people_count == 1) string_values.add("one_people");	
			else if (this.people_count > 1 && this.people_count < 5) string_values.add("few_people");	
			else string_values.add("many_people");				
		}

		String[] query_str = new String[string_values.size()];
		for (int i = 0; i < query_str.length; i++) query_str[i] = string_values.get(i);
		return query_str;
	}

	/*****************************
	 * GET_SQL_INSERT_STRING
	 * Returns the the appropriate SQL insert string based on the version of this object instance.
	 * 
	 * @return
	 */
	public String getSQLInsertString(String data_table) {
		String query_str = "";
		if (data_table.equals("data_raw") || data_table.equals("data_hourly")) query_str = this.getSQLInsertStringLong();
		else if (data_table.equals("data_hourly_avg")) query_str = this.getSQLInsertStringShort();			
		return query_str;
	}

	/*****************************
	 * GET_SQL_INSERT_STRING_LONG
	 * Returns the long version of the SQL insert string with values in appropriate order.
	 * This format is used for raw and hourly data tables.
	 * @return
	 */
	private String getSQLInsertStringLong() {
		return " (\'" + 
				this.data_version +  "\', \'" + 
				this.entry_id +  "\', \'" + 
				this.date_stamp.get_date_for_sql() + "\', \'" + 
				this.time_stamp.get_time_for_sql() + "\', \'" +  
				this.date_end.get_date_for_sql() + "\', \'" + 
				this.time_end.get_time_for_sql() + "\', \'" +  
				this.seconds_measured + "\', \'" +  
				this.emotion_valence + "\', \'"  + 
				this.emotion_intensity + "\', \'"  +
				this.emotion_1_name + "\', \'0\', \'" + 
				this.emotion_2_name + "\', \'0\', \'" + 
				this.emotion_3_name + "\', \'0\', \'" + 
				this.activity + "\', \'" +  
				DataProcessor_Journal.prep_string_upload(this.location) + "\', \'" +  
				this.people + "\', \'" +  
				this.people_count + "\', \'" +  
				DataProcessor_Journal.prep_string_upload(this.description) + 
				"\')";
	}
	
	/*****************************
	 * GET_SQL_INSERT_STRING_SHORT
	 * Returns the short version of the SQL insert string with values in appropriate order.
	 * This format is used for hourly average data table.
	 * @return
	 */
	private String getSQLInsertStringShort() {
		return " (\'" + 
				this.data_version +  "\', \'" + 
				this.entry_id +  "\', \'" + 
				this.time_stamp.get_time_for_sql() + "\', \'" +  
				this.time_end.get_time_for_sql() + "\', \'" +  
				this.seconds_measured + "\', \'" +  
				this.emotion_valence + "\', \'"  + 
				this.emotion_intensity + "\', \'"  +
				this.description + 
				"\')";
	}
	
	/*****************************
	 * GET_SQL_STRING
	 * Returns the the appropriate SQL insert string based on the version of this object instance.
	 * 
	 * @return
	 */
	public String getSQLUpdateSetString(String data_table) {
		String query_str = "";
//		else if (data_table.equals("data_raw") || data_table.equals("data_hourly")) query_str = this.getSQLInsertStringLong();
		if (data_table.equals("data_hourly_avg")) query_str = this.getSQLUpdateSetStringShort();			
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
		if (data_table.equals("data_raw")) query_str = " entry_id=\'" + this.entry_id + "\'";
		else if (data_table.equals("data_hourly")) query_str = " date_stamp=\'" + this.date_stamp.get_date_for_sql() + "\' AND " +
															   " time_stamp=\'" + this.time_stamp.get_time_for_sql() + "\'\n";
		else if (data_table.equals("data_hourly_avg")) query_str = " entry_id=\'" + this.entry_id + "\' AND " +
																   " time_stamp=\'" + this.time_stamp.get_time_for_sql() + "\' AND " +
																   " time_end=\'" + this.time_end.get_time_for_sql() + "\'\n";
		return query_str;
	}

	/*****************************
	 * GET_SQL_IUPDATE_STRING_SHORT
	 * Returns the short version of the SQL insert string with values in appropriate order.
	 * This format is used for hourly average data table.
	 * @return
	 */
	private String getSQLUpdateSetStringShort() {
		return 	" data_version=\'" + this.data_version +  "\'," +
			   	" entry_id=\'" + this.entry_id +  "\'," + 
			   	" time_stamp=\'" + this.time_stamp.get_time_for_sql() + "\'," +
			   	" time_end=\'" + this.time_end.get_time_for_sql() + "\'," +
			   	" seconds_measured=\'" + this.seconds_measured + "\'," +
			   	" emotion_valence=\'" + this.emotion_valence + "\',"+
			   	" emotion_intensity=\'" + this.emotion_intensity + "\',"+
			   	" description=\'" + this.description + "\'\n";
	}
	


	public String getString() {
		return this.data_version + ", " + this.entry_id  + ", " +  this.date_stamp.get_string()  + ", " +  
			   this.time_stamp.get_string()  + ", " +  this.date_end.get_string()  + ", " +   
			   this.time_end.get_string() + ", " +  this.seconds_measured + ", " + 
			   this.emotion_valence + ", " + this.emotion_intensity  + ", " + 
			   this.emotion_1_name + ", " +  this.emotion_1_intensity  + ", " +  this.emotion_2_name + ", " + this.emotion_2_intensity + ", " + 
			   this.emotion_3_name + ", " +  this.emotion_3_intensity + ", " + this.activity + ", " + this.location + ", " + 
			   this.people + ", " + this.people_count + ", " +  this.description;
	}
	
}

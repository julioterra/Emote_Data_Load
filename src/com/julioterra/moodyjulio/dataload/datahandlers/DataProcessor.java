package com.julioterra.moodyjulio.dataload.datahandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import processing.core.PApplet;
import com.julioterra.moodyjulio.dataload.basicelements.Element;
import com.julioterra.moodyjulio.dataload.basicelements.Date;
import com.julioterra.moodyjulio.dataload.basicelements.Time;
import com.julioterra.moodyjulio.dataload.data.Data;
import com.julioterra.moodyjulio.dataload.data.Data_Journal;
import com.julioterra.moodyjulio.dataload.data.Data_Mobile;

import de.bezier.data.sql.MySQL;


public class DataProcessor extends Element{

	protected MySQL database;
	protected DataReader reader;

	protected ArrayList <Data> data_list_pre_process;
	protected ArrayList <Data> data_list_post_process;
	protected ArrayList<ArrayList<Data>> days_of_week_avg;
	protected ArrayList<Data> overall_avg;	
	protected HashMap<String, Integer> date_list;

	protected int convert_time;
	protected long latest_entry_number; 
	public boolean ready_to_start;			// flags master start
	public boolean reading_data;			// flags part of cycle
	public boolean processing_data;
	public boolean loading_data;
	
	/***************************
	 *** CONSTRUCTORS
	 ***************************/

	public DataProcessor() {
		super();
		this.data_list_pre_process = 	new ArrayList<Data>();
		this.data_list_post_process = 	new ArrayList<Data>();
		this.days_of_week_avg = 		new ArrayList<ArrayList<Data>>();
		this.overall_avg = 				new ArrayList<Data>();
		this.date_list = 				new HashMap<String, Integer>();

		this.convert_time = 			0;
		this.latest_entry_number = 		0; 
		this.ready_to_start =			false;
		this.reading_data = 			false;
		this.processing_data = 			false;
		this.loading_data = 			false;
	}

	
	/***************************
	 * INIT
	 * initializes all key variables. Called by the constructor or any other function that
	 * needs to re-initialize an instance of this class.
	 * 
 	 * Input parameters: n/a 
	 * Returns data type: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */
	public void init() {
		this.ready_to_start = 		false;
		this.reading_data = 		false;
		this.processing_data = 		false;
		this.loading_data = 		false;
	}
	
	/***************************
	 * REGISTER_DATA_SOURCE
	 * This method registers a database that it receives as an argument, then it initializes the reader 
	 * variable by registering a file name, username and password. The file name registered here is used 
	 * as the base file name for reading sequentially named files.
	 * 
 	 * Input parameters: a MySQL database 
	 * Returns data type: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public void register_data_source(MySQL database) {
		this.database = database;
	}
	
	/***************************
	 * READ_RAW_DATA [method from parent class]
	 * This method reads the data from an appropriate instance of the the DataReader class. 
	 * 
 	 * Input parameters: n/a 
	 * Returns data type: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public void read_raw_data() {
	}
	
	protected boolean add_raw_data(String[] data_entry) {
		return false;
	}

	public boolean add(String[] data_entry) {
		return false;
	}

	public boolean add(Data data_entry) {
		return false;
	}

	public void process_raw_data() {
	}
		
	protected void fix_time_date() {
	}
	
	/***************************
	 *** GET FUNCTIONS 
	 ***************************/
	
	public int get_size() {
		return data_list_pre_process.size();
	}
	
	public Data get(int index) {
		return data_list_pre_process.get(index);
	}
	
	public ArrayList<Data> get() {
		return data_list_pre_process;
	}
	
	/***************************
	 * PRINT_PRE_PROCESSED 
	 * prints the pre_processed data list to the console. For debugging purposes.
	 * 
 	 * Input parameters: n/a 
	 * Returns data type: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public void print_pre_processed() {
		for (int i = 0; i < data_list_pre_process.size(); i++) {
			Data_Mobile current_reading = (Data_Mobile) data_list_pre_process.get(i);
			System.out.println("ID# " + i + " : " + current_reading.getSQLInsertString());
		}
	}
	
	
	/***************************
	 * ADD_DATE 
	 * adds new date to the date_list hashmap, which is used to update the dates available table on the database.
	 * 
 	 * Input parameters: a Date object 
	 * Returns data type: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	protected void add_to_date_list(Date new_date) {
		if (new_date.year == 0 || new_date.month == 0 || new_date.day == 0) return;

		String date_check = new_date.get_date_for_sql();
		if (date_list.get(date_check) == null) {
			date_list.put(date_check, new_date.get_day_of_week());
			System.out.println("DataProcessor.addDate() - date added to list: " + date_check + " day of week " + new_date.get_day_of_week());
		} 
	}


	/************************************
	 * CREATE_DATED_TIME_BASED_LIST - USING A TIME RANGE
	 * This method creates an arraylist of empty Data objects. Each object is "empty" except for the 
	 * start and end dates and time, which are generated based on the input parameters. The type of data 
	 * objects that are added to the table are determined by data_type parameter.
	 * 
	 * Functions to call before: n/a
	 * Functions call before 1: process() [defined in child class], this method takes the empty arraylist created using 
	 * the method described above and then fills it with data by processing the raw data from the database
	 * Functions to call after 2: load_2_database() [defined in child class], this method loads the data from the 
	 * data_list_processed arraylist into the appropriate database.
	 * 
	 */	
	public static ArrayList<Data> createDatedTimeBasedList(int data_type, Date start_date, Time start_time, Date date_end, Time time_end, float interval_minutes) {
		ArrayList<Data> data_list = new ArrayList<Data>();							// initialize the data_list_processed array
		Time time_range_start = 	new Time(start_time);
		Date date_range_end = 		new Date(date_end);
//		if (debug_code) PApplet.println("DataProcessor.createDatedTimeBasedList() - creating data list from date " + date_range_start.get_date_for_sql() + " at " + start_time.get_time_for_sql() + 
//										" to " + date_range_end.get_date_for_sql()  + " at " + time_end.get_time_for_sql() + " interval " + interval_minutes);
		
		boolean done_creating = false;
		int end_of_day = 0;
		Time temp_time_start = 	new Time(start_time);
		Date temp_day_start = 	new Date(start_date);
		Time temp_time_end = 	new Time(temp_time_start);
		Date temp_day_end = 	new Date(temp_day_start);		

		while (!done_creating) {
			// calculate the time of the end of each sequence
			end_of_day = temp_time_end.update_minutes((int) interval_minutes); 
			if (end_of_day != 0) temp_day_end.update_day(1);

			// CREATE NEW ARRAY ELEMENT
			// current this methods only supports creating arrays using emotion objects
			if (data_type == 0) {
				Data_Journal new_reading = new Data_Journal(temp_day_start, temp_time_start, temp_day_end, temp_time_end);
				data_list.add(new_reading);				
//				if (debug_code) PApplet.println("DataProcessor.createDatedTimeBasedList(): add Data_Journal object, start date " + new_reading.date_stamp.get_string() + " at " + new_reading.time_stamp.get_string() + 
//												" to " +  new_reading.date_end.get_string() + " at " + new_reading.time_end.get_string());
			}
			else if (data_type == 1) {
				Data_Mobile new_reading = new Data_Mobile(temp_day_start, temp_time_start);
				data_list.add(new_reading);				
//				if (debug_code) PApplet.println("DataProcessor.createDatedTimeBasedList(): add Data_Mobile object, start date " + new_reading.date_stamp.get_string() + " at " + new_reading.time_stamp.get_string());
			}

			// TIME CHECK
			// check if we have reached the end of the date range
			if (date_range_end.equals(temp_day_start)) {
//				if (debug_code) PApplet.println("DataProcessor.createDatedTimeBasedList() - time check - final date reached, time remaining: " + Time.calculate_time_dif_seconds(temp_time_end, time_range_start));
				if (Time.calculate_time_dif_seconds(temp_time_end, time_range_start) < (60*interval_minutes)) {
					done_creating = true;						
//					if (debug_code) PApplet.println("DataProcessor.createDatedTimeBasedList() - time check - final date and time reached, array size " + data_list.size());
				}
			}

			temp_time_start = new Time (temp_time_end);
			temp_day_start = new Date (temp_day_end);
		}
		return data_list;
	}

	public static ArrayList<Data> createDatedTimeBasedList(int data_type, Date start_date, float interval_minutes) {
		return createDatedTimeBasedList(data_type, start_date, new Time("00:00:00"), start_date, new Time("23:59:59"), interval_minutes);
	}
	
	public static ArrayList<Data> createTimeBasedList(int data_type, Time start_time, Time time_end, float interval_minutes) {
		ArrayList<Data> data_list = new ArrayList<Data>();							// initialize the data_list_processed array
		Time time_range_start = 	new Time(start_time);
		Time time_range_end = 		new Time(time_end);
//		if (debug_code) PApplet.println("DataProcessor.createTimeBasedList() - creating data list from time " + start_time.get_time_for_sql() + 
//										" time " + time_end.get_time_for_sql() + " interval " + interval_minutes);
		
		boolean done_creating = false;
		int end_of_day = 0;
		Time temp_time_start = 	new Time(start_time);
		Date temp_day_start = 	new Date("2001/01/01");
		Time temp_time_end = 	new Time(temp_time_start);
		Date temp_day_end = 	new Date("2001/01/01");		

		while (!done_creating) {
			// calculate the time of the end of each sequence
			temp_time_end.update_minutes((int) interval_minutes); 

			// CREATE NEW ARRAY ELEMENT
			// current this methods only supports creating arrays using emotion objects
			if (data_type == 0) {
				Data_Journal new_emotion_reading = new Data_Journal(temp_day_start, temp_time_start, temp_day_end, temp_time_end);
				data_list.add(new_emotion_reading);				
//				if (debug_code) PApplet.println("DataProcessor.createTimeBasedList(): add JournalData_New object - start date " + new_emotion_reading.date_stamp.get_string() + " at " + new_emotion_reading.time_stamp.get_string() + 
//												" to " +  new_emotion_reading.date_end.get_string() + " at " + new_emotion_reading.time_end.get_string());
			} else if (data_type == 1) {
				Data_Mobile new_reading = new Data_Mobile(temp_day_start, temp_time_start);
				data_list.add(new_reading);				
//				if (debug_code) PApplet.println("DataProcessor.createTimeBasedList(): add Data_Mobile object, start date " + new_reading.date_stamp.get_string() + " at " + new_reading.time_stamp.get_string());
			}

			// TIME CHECK
			// check if we have reached the end of the date range
			if (Time.calculate_time_dif_seconds(temp_time_end, time_range_start) < (60*interval_minutes)) {
				done_creating = true;						
//				if (debug_code) PApplet.println("DataProcessor.createTimeBasedList() - final time reached");
			}

			temp_time_start = new Time (temp_time_end);
		}
		return data_list;
	}

	
	/************************************
	 * UPLOAD_DATA_FROM_ARRAYLIST 
	 * Uploads new data to a specified mySql datatable using data from an array list of data objects. 
	 * This function uploads to the database that is registered with this object. 
	 * 
	 * Input parameters: datatable_name holds the name of the datatable where the data will be inserted. data_list is the
	 * 					 arraylist that holds <Data> objects, which will be inserted into the datatable.
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * Notes: only supports ArrayList that hold JournalData_New objects.
	 * 
	 */		
	public void uploadDataFromDataArrayList(String datatable_name, ArrayList<Data> data_list) {
		for (int i =  0; i < data_list.size(); i++) {
			String insert_data = "";
			String insert_format = "INSERT INTO " + datatable_name + "\n";
//			if (data_list.get(i) instanceof Data_Journal || data_list.get(i) instanceof Data_Mobile_New) {
			Data current_reading = data_list.get(i);				
			insert_data = "VALUES " + current_reading.getSQLInsertString(datatable_name); 
			if (Element.debug_code) System.out.println("DataProcessor.uploadDataFromArrayList(): upload data query " + insert_format + insert_data);
			if (database.connection != null && Element.load_data) database.execute(insert_format + insert_data);
//			} 
		}
	}
	
	/************************************
	 * UPLOAD_DATE_AVAILABLE_DATA 
	 * Uploads data into the appropriate mySql database (not yet working). 
	 * 
	 * Input parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */		
	public void uploadDateAvailableData() {
		this.loading_data = true;

		// go through the date_list hashmap to upload the dates from the date list to the database
		Iterator<Map.Entry<String, Integer>> date_list_items = date_list.entrySet().iterator();
		while (date_list_items.hasNext()) {
			Map.Entry<String, Integer> map_entry = date_list_items.next();
			String date_key = map_entry.getKey();	 
			Integer day_of_week = map_entry.getValue();	 	
			if (!DataProcessor.confirm_data_exists_on_date(this.database, "ctrl_avail_dates", new Date(date_key))) {			
				String insert_format = "INSERT INTO ctrl_avail_dates \n";
				String insert_data = "VALUES (\'" + date_key + "\', \'" + day_of_week + "')";
				if(database.connection != null && Element.load_data) database.execute(insert_format + insert_data);
				if (Element.debug_code) { System.out.println("DataProcessor_Journal.uploadRawData() - add new date " + insert_format + insert_data); }
			}
			if (Element.debug_code) { System.out.println("DataProcessor_Journal.uploadRawData() - date already exists "); }
		}
		this.loading_data = false;
	}
		
	
	
	/***************************
	 *** STATIC FUNCTIONS 
	 *********************
	 *************
	 *****
	 *** DATA TYPE CONVERSION METHODS  
	 ***************************/

	// ************************************
	// ** CONVERT INT TO STRING FOR TIME AND DATE (BASE-10)
	// ** Join two strings that contain comma separate lists, and replaces specified text
	// **
	public static String time_date_part_to_string(long current_time) {
		String time_date_in_string = String.valueOf(current_time);
		if (current_time < 10) {
			time_date_in_string = "0" + String.valueOf(current_time);
		} 
		return time_date_in_string;
	}
	

	/***************************
	 *** STATIC FUNCTIONS 
	 *********************
	 *************
	 *****
	 *** LIST PROCESSING METHODS  
	 ***************************/

	// JOIN LIST METHOD - Joins two strings that contain comma separate lists
	// input: two strings with command separated values
	// output: a string with all entries from both original lists, dedupped
	public static String joinLists(String base_string, String new_string) {
		String[] raw_string_array = PApplet.split((base_string + ", " + new_string), ",");
		ArrayList<String> clean_string_array = new ArrayList<String>();
		ArrayList<String> final_string_array = new ArrayList<String>();
		String final_string = "";

		// go through raw_string_array and transfer all valid entries into clean_string_array 
		for (int i = 0; i < raw_string_array.length; i ++) {
			raw_string_array[i] = PApplet.trim(raw_string_array[i].toLowerCase());
			if (!raw_string_array[i].equals("") && !raw_string_array[i].equals(" ") && raw_string_array[i] != null && !raw_string_array[i].contains("null") ) clean_string_array.add(raw_string_array[i]);
		}

		// go through clean_string_array and de-duplicated entries into final_string_array 
		for (int k = 0; k < clean_string_array.size(); k ++) {
			boolean word_already_exists = false;
			for (int j = 0; j < final_string_array.size(); j ++)
	 			if (clean_string_array.get(k).equals(final_string_array.get(j))) word_already_exists = true;
			if (!word_already_exists) final_string_array.add(clean_string_array.get(k));
		}		

		// go through final_string_array and create the final_string variable
		if (final_string_array.size() > 0) {
			final_string = final_string_array.get(0);
			for (int i = 1; i < final_string_array.size(); i ++) final_string += ", " + final_string_array.get(i);
		}
		
		return final_string;
	}	

	// ************************************
	// ** JOIN LIST AND REPLACE STRING METHOD
	// ** Join two strings that contain comma separate lists, and replaces specified text.
	// ** the text to remove can be matched using Regex.
	// **
	public static String joinReplaceLists(String base_string, String new_string, String remove_string, String replace_string) {
		String updated_string = joinLists(base_string, new_string);
		updated_string = updated_string.replaceAll(remove_string, replace_string);
		updated_string = joinLists(updated_string, "");
		return updated_string;
	}

	// ************************************
	// ** JOIN STRINGS AND REPLACE METHOD
	// ** Join two strings that contain comma separate lists, and replaces specified text
	// **
	public static int countListItems(String base_string) {
		String[] string_count = PApplet.split(base_string, ",");
		int count = string_count.length;
		if (string_count[0].equals("") || string_count[0].equals(" ")) count = 0;
		if(base_string.contains("more")) count += 5;
		if(base_string.contains("many")) count += 5;
		if(base_string.contains("no one") && count <= 1) count = 0;
		if(base_string.contains("no one") && count > 1) count --;
		return count;
	}

	// ************************************
	// ** APPLY LINE BREAKS
	// ** applies line breaks to text creating lines of a specific length
	// **
	public static String applyLineBreaks(String input_string, int chars_per_line) {
		String output_string = "";
		while (input_string.length() > chars_per_line) {
			int index = input_string.indexOf(" ", chars_per_line);
			if (index < 0) break;
			String temp_size = input_string.substring(index);
			if (temp_size.length() < 10) break;
			output_string += input_string.substring(0, index) + "\n";
			input_string = input_string.substring(index);
		}
		if (PApplet.trim(input_string).length() > 1) output_string += input_string + "\n";
		output_string = output_string.replace("\\", "");
		return output_string;
	}

	
	
	/***************************
	 *** STATIC FUNCTIONS 
	 *********************
	 *************
	 *****
	 *** DATABASE QUERIES - Time Based 
	 ***************************/
	
	// GET_MAX_ELEMENT - finds out what is the most recent time in the database 
	// input:  most recent date from database
	// returns: most recent time from the database		
	public static String get_max_element(MySQL database, String data_table, String element) {
		String element_content = "";
		if(database.connection != null) {
			database.query("SELECT MAX(" + element + ") FROM "  + data_table);

			if(database.next()) {
				element_content = database.getString("MAX(" + element + ")");
				if (Element.debug_code) PApplet.println("DataProcessor.get_max_element() - element found " + element_content);
			} else {
				if (Element.debug_code) PApplet.println("DataProcessor.get_max_element() - element not found ");
			}
		}
		return element_content;
	}

	// GET_MAX_ELEMENT_FROM_DATE - finds out what is the most recent time in the database 
	// input:  most recent date from database
	// returns: most recent time from the database		
	public static String get_max_element_from_date(MySQL database, String data_table, Date search_date, String element) {
		String element_content = "";
		if(database.connection != null) {
			database.query("SELECT MAX(" + element + ") FROM "  + data_table + " WHERE date_stamp = \'search_date\'");

			if(database.next()) {
				element_content = database.getString("MAX(" + element + ")");
				if (Element.debug_code) PApplet.println("DataProcessor.get_max_element_from_date() - element found " + element_content);
			} else {
				if (Element.debug_code) PApplet.println("DataProcessor.get_max_element_from_date() - element not found ");
			}
		}
		return element_content;
	}

	// MOST_RECENT_DATE - finds out what is the most recent date in the database 
	// input: none
	// returns: most recent date from database		
	public Date most_recent_date() {
		PApplet.println("GOT HERE");
		Date most_recent = new Date();
		if(database_journal.connection != null) {
			database_journal.query("SELECT MAX(date_stamp) FROM " + Element.active_database);
			database_journal.next();
			most_recent.setYMD(database_journal.getString("MAX(date_stamp)"));
			if (Element.debug_code) PApplet.println(most_recent.get_string());
		}
		return most_recent;
	}
	
	// MOST_RECENT_TIME - finds out what is the most recent time in the database 
	// input:  most recent date from database
	// returns: most recent time from the database		
	public Time most_recent_time(Date search_date) {
		Time most_recent = new Time();
		if(database_journal.connection != null) {
			database_journal.execute("SELECT MAX(time_stamp) FROM "  + Element.active_database + " WHERE date_stamp = \'search_date\'");
			database_journal.next();
			most_recent.set(database_journal.getString("MAX(time_stamp)"));
			if (Element.debug_code) PApplet.println(most_recent.get_string());
		}
		return most_recent;
	}
	
	// MOST_RECENT_DATE - finds out what is the most recent date in the database 
	// input: none
	// returns: most recent date from database		
	public static Date get_most_recent_date(MySQL database, String data_table) {
		Date most_recent = new Date();
		if(database.connection != null) {
			database.query("SELECT MAX(date_stamp) FROM " + data_table);
	
			if(database.next()) {
				most_recent.setYMD(database.getString("MAX(date_stamp)"));
				if (Element.debug_code) PApplet.println(most_recent.get_string());
			}
		}
		return most_recent;
	}
	
	// CONFIRM_DATA_EXISTS_ON_DATE - finds out if there is data in the database on a specific date 
	// input:  database object, data table in string format, date to confirm whether data exists
	// returns: true if data exists, false if no data exists		
	public static boolean confirm_data_exists_on_date(MySQL database, String data_table, Date search_date) {
		if(database.connection != null) {
			String query_string = "SELECT * FROM "  + data_table + " WHERE date_stamp = \'" + search_date.get_date_for_sql() + "\'";
			database.query(query_string);
			if (Element.debug_code) PApplet.println("DataProcessor.confirm_data_exists_on_date() - database query " + query_string);

			if(database.next()) { 
				if (Element.debug_code) PApplet.println("DataProcessor.confirm_data_exists_on_date() - data exists on date");
				return true;
			}
		}
		if (Element.debug_code) PApplet.println("DataProcessor.confirm_data_exists_on_date() - date DOES NOT exist on date ");
		return false;
	}

	// CONFIRM_DATA_EXISTS_ON_ELEMENT - finds out if a specific element exists in the database 
	// input:  database object, data table in string format, date to confirm whether data exists
	// returns: true if data exists, false if no data exists		
	public static boolean confirm_data_entry_exists(MySQL database, String data_table, String field, String content) {
		if(database.connection != null) {
			database.query("SELECT * FROM "  + data_table + " WHERE " + field + " = \'" + content + "\'");

			if(database.next()) { 
				if (Element.debug_code) PApplet.println("DataProcessor.confirm_data_entry_exists() - data entry exists");
				return true;
			}
		}
		if (Element.debug_code) PApplet.println("DataProcessor.confirm_data_entry_exists() - data entry DOES NOT exists ");
		return false;
	}

	// LOAD_DATE_RANGE - loads data from database into an array list of data objects.  
	// input: the number of the source data table, the start date and end date of the range to laod 
	// returns: an array list with data objects - specific class of objects will depend on database that was read		
	public static ArrayList<Data> load_date_range(MySQL database, String data_table, Date date_range_start, Date date_range_end) {
		ArrayList<Data> new_list = new ArrayList<Data>();
		String query_str = "SELECT * FROM " + data_table + 
		 					" WHERE date_stamp >= \"" + date_range_start.get_string() + 
		 					"\" AND date_stamp <= \"" + date_range_end.get_string() + "\"";

		if (debug_code) PApplet.println("DataProcessor.load_date_range() - query string: " + query_str);
		if(Element.read_data && database.connection != null) {
			int number_of_fields = load_field_count(database, Element.databases.get(database), data_table);
			database.query(query_str);			
			new_list = read_query_results_data(database, data_table, number_of_fields);
		}
		return new_list;
	}

	// LOAD_DATE_TIME - loads data from database into an array list of data objects.  
	// input: the number of the source data table, the start date and time, and the end date and time of the range to laod 
	// returns: an array list with data objects - specific class of objects will depend on database that was read		
	public static ArrayList<Data> load_date_time(MySQL database, String data_table, Date date_range_start, Time time_range_start) {
		ArrayList<Data> new_list = new ArrayList<Data>();
		String query_str = "SELECT * FROM "  + data_table + 
						   " WHERE ( date_stamp=\"" + date_range_start.get_date_for_sql() + 
						   "\") AND ( time_stamp=\"" + time_range_start.get_time_for_sql() + "\")";

//		if (debug_code) PApplet.println("DataProcessor.load_date_time(): query string " + query_str);
		if(Element.read_data && database.connection != null) {
			int number_of_fields = load_field_count(database, Element.databases.get(database), data_table);
			database.query(query_str);			
			new_list = read_query_results_data(database, data_table, number_of_fields);
		}
		return new_list;
	}
	
	// LOAD_DATE_TIME_RANGE - loads data from database into an array list of data objects.  
	// input: the number of the source data table, the start date and time, and the end date and time of the range to laod 
	// returns: an array list with data objects - specific class of objects will depend on database that was read		
	public static ArrayList<Data> load_date_time_range(MySQL database, String data_table, Date date_range_start, Time time_range_start, Date date_range_end, Time time_range_end) {
		ArrayList<Data> new_list = new ArrayList<Data>();
		String query_str = "SELECT * FROM "  + data_table + 
						   " WHERE ( date_stamp >= \"" + date_range_start.get_date_for_sql() + 
						   "\" AND date_stamp <= \"" + date_range_end.get_date_for_sql() +
						   "\") AND ( time_stamp >= \"" + time_range_start.get_time_for_sql();
		if (time_range_end.hour >= time_range_start.hour) query_str += "\" AND time_stamp <= \"" + time_range_end.get_time_for_sql() + "\")";
		else query_str += "\" OR time_stamp <= \"" + time_range_end.get_time_for_sql() + "\")";

//		if (debug_code) PApplet.println("DataProcessor.load_date_time_range() - query string: " + query_str);
		if(Element.read_data && database.connection != null) {
			int number_of_fields = load_field_count(database, Element.databases.get(database), data_table);
			database.query(query_str);			
			new_list = read_query_results_data(database, data_table, number_of_fields);
		}
		return new_list;
	}

	// LOAD_DATE_TIME_RANGE - loads data from database into an array list of data objects.  
	// input: the number of the source data table, the start date and time, and the end date and time of the range to laod 
	// returns: an array list with data objects - specific class of objects will depend on database that was read		
	public static ArrayList<Data> load_date_time_range_end(MySQL database, String data_table, Date date_range_start, Time time_range_start, Date date_range_end, Time time_range_end) {
		ArrayList<Data> new_list = new ArrayList<Data>();
		String query_str = "SELECT * FROM "  + data_table + 
						   " WHERE" + 
						   " ((date_stamp >= \'" + date_range_start.get_date_for_sql() + "\'" +
						   " AND date_stamp <= \'" + date_range_end.get_date_for_sql() + "\')" + 
						   " AND (time_stamp >= \'" + time_range_start.get_time_for_sql() + "\'" +
						   " AND time_stamp <= \'" + time_range_end.get_time_for_sql() + "\'))" + 
						   " OR" + 
						   " ((date_end >= \'" + date_range_start.get_date_for_sql() + "\'" +
						   " AND date_end <= \'" + date_range_end.get_date_for_sql() + "\')" + 
						   " AND (time_end >= \'" + time_range_start.get_time_for_sql() + "\'" +
						   " AND time_end <= \'" + time_range_end.get_time_for_sql() + "\'))"; 
						   
		if (debug_code) PApplet.println("DataProcessor.load_date_time_range_end() - query string: " + query_str);
		if(Element.read_data && database.connection != null) {
			int number_of_fields = load_field_count(database, Element.databases.get(database), data_table);
			database.query(query_str);			
			new_list = read_query_results_data(database, data_table, number_of_fields);
		}
		return new_list;
	}

	// LOAD_DATE_TIME_RANGE - loads data from database into an array list of data objects.  
	// input: the number of the source data table, the start date and time, and the end date and time of the range to laod 
	// returns: an array list with data objects - specific class of objects will depend on database that was read		
	public static ArrayList<Data> load_multiple_date_time_range(MySQL database, String data_table, String[] date_array, Time time_range_start, Time time_range_end) {
		ArrayList<Data> new_list = new ArrayList<Data>();
		String query_str = "SELECT * FROM "  + data_table + " WHERE ( "; 
		for (int i = 0; i < date_array.length; i++) {
			if(i != 0) query_str += "\" OR "; 
			query_str += "date_stamp = \"" + date_array[i];
		}
		query_str += "\" ) AND ( time_stamp >= \"" + time_range_start.get_time_for_sql();
		if (time_range_end.hour >= time_range_start.hour) query_str += "\" AND time_stamp < \"" + time_range_end.get_time_for_sql() + "\")";
		else query_str += "\" OR time_stamp < \"" + time_range_end.get_time_for_sql() + "\")";

		if (debug_code) PApplet.println("DataProcessor.load_multiple_date_time_range() - query string: " + query_str);
		if(Element.read_data && database.connection != null) {
			int number_of_fields = load_field_count(database, Element.databases.get(database), data_table);
			database.query(query_str);			
			new_list = read_query_results_data(database, data_table, number_of_fields);
		}
		return new_list;
	}
	
	// LOAD_DATE - loads data from database into an array list of data objects.  
	// input: the number of the source data table, the date for the query 
	// returns: an array list with data objects - specific class of objects will depend on database that was read		
	public static ArrayList<Data> load_date(MySQL database, String data_table, Date date_selected) {
		ArrayList<Data> new_list = new ArrayList<Data>();
		String query_str = "SELECT * FROM "  + data_table + 
						   " WHERE date_stamp >= \"" + date_selected.get_string() + 
						   "\" AND date_stamp <= \"" + date_selected.get_string() + "\"";

		if (debug_code) PApplet.println("DataProcessor.load_date() - query string: " + query_str);
		if(Element.read_data && database.connection != null) {
			int number_of_fields = load_field_count(database, Element.databases.get(database), data_table);
			database.query(query_str);			
			new_list = read_query_results_data(database, data_table, number_of_fields);
		}
		return new_list;
	}

	// LOAD_TIME_RANGE - loads data from database into an array list of data objects.  
	// input: the number of the source data table, the start and end time of the time range 
	// returns: an array list with data objects - specific class of objects will depend on database that was read		
	// note: the results will use include records from the same window time from multiple different days
	public static ArrayList<Data> load_time_range(MySQL database, String data_table, Time time_range_start, Time time_range_end) {
		ArrayList<Data> new_list = new ArrayList<Data>();
		String query_str = "SELECT * FROM "  + data_table + 
						 " WHERE time_stamp >= \"" + time_range_start.get_string() + 
						 "\" AND time_stamp <= \"" + time_range_end.get_string() + "\"";

		if (debug_code) PApplet.println("DataProcessor.load_time_range() - query string: " + query_str);
		if(Element.read_data && database.connection != null) {
			int number_of_fields = load_field_count(database, Element.databases.get(database), data_table);
			database.query(query_str);			
			new_list = read_query_results_data(database, data_table, number_of_fields);
		}
		return new_list;
	}

	
	/***************************
	 ** STATIC FUNCTIONS
	 ** DATABASE QUERIES - NOT Time Based
	 ** 
	 */
	// LOAD_QUERY_MATCH - loads data from database into an array list of data objects.  
	// input: the number of the source data table, the start and end time of the time range 
	// returns: an array list with data objects - specific class of objects will depend on database that was read		
	// note: the results will use include records from the same window time from multiple different days
	public static ArrayList<Data> load_query_match(MySQL database, String data_table, String field_name, String query_value) {
		ArrayList<Data> new_list = new ArrayList<Data>();
		String query_str = "SELECT * FROM "  + data_table + "\n" +
		   				   "WHERE " + field_name + " = \'" + query_value + "\'";

		if (debug_code) PApplet.println("DataProcessor.load_query_match() - query string: " + query_str);
		if(Element.read_data && database.connection != null) {
			int number_of_fields = load_field_count(database, Element.databases.get(database), data_table);
			database.query(query_str);			
			new_list = read_query_results_data(database, data_table, number_of_fields);
		}
		return new_list;
	}


	// LOAD_QUERY_MATCH_STRING - loads data from database into an array list of data objects.  
	// input: the number of the source data table, the start and end time of the time range 
	// returns: an array list with data objects - specific class of objects will depend on database that was read		
	// note: the results will use include records from the same window time from multiple different days
	public static ArrayList<String[]> load_query_match_string(MySQL database, String data_table, String field_name, String query_value) {
		ArrayList<String[]> new_list = new ArrayList<String[]>();
		String query_str = "SELECT * FROM "  + data_table + "\n";
		if (!field_name.equals("") && !query_value.equals("")) query_str += "WHERE " + field_name + " = \'" + query_value + "\'";

//		if (debug_code) PApplet.println("DataProcessor.load_query_match() - query string: " + query_str);
		if(Element.read_data && database.connection != null) {
			int number_of_fields = load_field_count(database, Element.databases.get(database), data_table);
			database.query(query_str);			
			new_list = read_query_results_string(database, data_table, field_name, number_of_fields);
		}
		return new_list;
	}


	// LOAD_DATATABLE_FIELD_NAMES - loads the name of each field in a datatable
	// input: the number of the source database object, the database name, and the datatable name 
	// returns: an arraylist that holds Strings with the name of each field from the given datatable		
	// note: 	
	public static ArrayList<String> load_field_names(MySQL database, String database_name, String datatable_name) {
		ArrayList<String[]> field_names_temp = new ArrayList<String[]>();
		ArrayList<String> field_names = new ArrayList<String>();

		// create query for information_schema database to get the name of each field (data column) from any database table
		String query_str = "SELECT information_schema.columns.column_name\n" + 
						   "FROM information_schema.columns\n" +
						   "WHERE information_schema.columns.table_schema = \'" + database_name + "\'\n" +
						   "AND information_schema.columns.table_name = \'" + datatable_name + "\'";	
		
//		if (debug_code) PApplet.println("DataProcessor.load_field_names() - query string: \n" + query_str);
		if(Element.read_data && database.connection != null) {
			database.query(query_str);			
			field_names_temp = read_query_results_string(database, "information_schema.columns", "column_name", 1);
			for (int i = 0; i < field_names_temp.size(); i++) {
				field_names.add(field_names_temp.get(i)[0]);
			}
//			if (debug_code) PApplet.println("DataProcessor.load_field_names() - results loaded");
		}
		
		return field_names;
	}
	
	// LOAD_DATATABLE_FIELD_NAMES - loads the name of each field in a datatable
	// input: the number of the source database object, the database name, and the datatable name 
	// returns: an arraylist that holds Strings with the name of each field from the given datatable		
	// note: 	
	public static int load_field_count(MySQL database, String database_name, String datatable_name) {
		// load the field names for a database table
		ArrayList<String> fields = load_field_names(database, database_name, datatable_name);
		
		// return the size of the fields array which is equal to the number of fields in a database table
//		if (debug_code) PApplet.println("DataProcessor.load_field_count() - number of fields: " + fields.size());
		return fields.size();
	}
		
	
	/************************************
	 * UPDATE_DATA_FROM_ARRAYLIST 
	 * Updates data into existing records on a specified mySql datatable using data from an array 
	 * list of data objects. This function uploads to the database that is registered with this object. 
	 * 
	 * Input parameters: datatable_name holds the name of the datatable where the data will be inserted. 
	 * 					 data_list is the arraylist that holds <Data> objects, which will be inserted into the datatable.
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * Notes: only supports ArrayList that hold JournalData_New objects.
	 * 
	 */		
	public void updateDataFromArrayList(String datatable_name, ArrayList<Data> data_list) {
		for (int i =  0; i < data_list.size(); i++) {
			String insert_format = "UPDATE " + datatable_name + "\n";
			String insert_data = "";
//			if (data_list.get(i) instanceof Data_Journal) {
				Data current_reading = data_list.get(i);				
				insert_data = " SET" + current_reading.getSQLUpdateSetString(datatable_name) +
							  " WHERE " + current_reading.getSQLUpdateQueryString(datatable_name);
				if (Element.debug_code) { System.out.println("DataProcessor_Journal.updateDataFromArrayList() - upload data query " + insert_format + insert_data); }
				if(database.connection != null && Element.load_data) database.execute(insert_format + insert_data);
//			} 
		}
	}
	
	

	/***************************
	 * STATIC FUNCTIONS - DATABASE QUERIES READ
	 * 
	 */
	// READ_QUERY_RESULTS_STRING - reads results from any query and returns it as an arraylist with arrays of string.  
	// input: the number of the source data table 
	// returns: an array list with data objects - specific class of objects will depend on database that was read		
	public static ArrayList<String[]> read_query_results_string(MySQL database, String datatable_name, String field_name, int number_of_fields) {
		ArrayList<String[]> query_results = new ArrayList<String[]>();
		
		if (datatable_name.equals("data_relational") || datatable_name.equals("ctrl_cat_list") || datatable_name.equals("information_schema.columns")) {
		while (database.next()) {
			String [] content = new String[number_of_fields];
			for (int field_num = 1; field_num <= number_of_fields; field_num++)
				content[field_num - 1] = database.getString(field_num);
			query_results.add(content);
			
//				if (debug_code) PApplet.print("DataProcessor.read_query_results() - data avaialble: " + content.length + " ");
//				if (debug_code) for (int i = 0; i < content.length; i++) PApplet.print(content[i] + " ");
//				if (debug_code) PApplet.println();
			}
		}
		return query_results;
	}
	
	// READ_QUERY_RESULTS_DATA - reads results from any query using the appropriate data type.  
	// input: the number of the source data table 
	// returns: an array list with data objects - specific class of objects will depend on database that was read		
	public static ArrayList<Data> read_query_results_data(MySQL database, String data_table_name, int number_of_fields) {
		ArrayList<Data> query_results = new ArrayList<Data>();

		String database_name = Element.databases.get(database);
		
		if (database_name.contains("Journal")) {
			if (data_table_name.contains("data_raw")) {
				while (database.next()) {
					String [] content = {"0.0"};
					for (int field_num = 1; field_num <= number_of_fields; field_num++)
						if (field_num > 1) content = PApplet.append(content, database.getString(field_num));
					Data_Journal most_recent = new Data_Journal(content);
					query_results.add(most_recent);
	
//					if (debug_code) PApplet.print("DataProcessor.read_query_results(): Emotion database, data_raw table, count " + content.length + " ");
//					if (debug_code) for (int i = 0; i < content.length; i++) PApplet.print(content[i] + " ");
//					if (debug_code) PApplet.println();
				}
			}
	
			else if (data_table_name == "data_hourly_avg") {
				while (database.next()) {
					String [] content = {"2.0"};
					for (int field_num = 1; field_num <= number_of_fields; field_num++)
						if (field_num > 1) content = PApplet.append(content, database.getString(field_num));
					Data_Journal most_recent = new Data_Journal(content);
					query_results.add(most_recent);
	
//					if (debug_code) PApplet.print("DataProcessor.read_query_results(): Physio database, data_hourly_avg table, count " + content.length + " ");
//					if (debug_code) for (int i = 0; i < content.length; i++) PApplet.print(content[i] + " ");
//					if (debug_code) PApplet.println();
				}
			}
			return query_results;
		}

		else if (database_name.contains("Physio")) {
			if (data_table_name.contains("data_raw")) {
				while (database.next()) {
					String [] content = new String [1];
					for (int field_num = 1; field_num <= number_of_fields; field_num++)						
						if (field_num == 1) content[0] = database.getString(field_num);
						else content = PApplet.append(content, database.getString(field_num));
					
					Data_Mobile most_recent = new Data_Mobile(content);
					query_results.add(most_recent);
	
//					if (debug_code) PApplet.print("DataProcessor.read_query_results(): Physio database, data_raw table, count " + content.length + " ");
//					if (debug_code) for (int i = 0; i < content.length; i++) PApplet.print(content[i] + " ");
//					if (debug_code) PApplet.println();
				}
			} else if (data_table_name.contains("data_minutes")) {
				while (database.next()) {
					String [] content = new String [1];
					for (int field_num = 1; field_num <= number_of_fields; field_num++)						
						if (field_num == 1) content[0] = database.getString(field_num);
						else content = PApplet.append(content, database.getString(field_num));

					Data_Mobile most_recent = new Data_Mobile(content);
					query_results.add(most_recent);
	
					if (debug_code) PApplet.print("DataProcessor.read_query_results(): Physio database, data_minutes_avg table, count " + content.length + " ");
					if (debug_code) for (int i = 0; i < content.length; i++) PApplet.print(content[i] + " ");
					if (debug_code) PApplet.println();
				}
			}

			return query_results;
		}
		
		return null;
	}
	
	
	
}

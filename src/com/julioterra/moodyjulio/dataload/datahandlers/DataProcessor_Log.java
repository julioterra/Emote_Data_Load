// TO DO - UPDATE
//		add code that calculates time for records after the last GPS time entry
//		finish the database adding section 

package com.julioterra.moodyjulio.dataload.datahandlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.julioterra.moodyjulio.dataload.basicelements.Date;
import com.julioterra.moodyjulio.dataload.basicelements.Element;
import com.julioterra.moodyjulio.dataload.basicelements.Time;
import com.julioterra.moodyjulio.dataload.data.Data_Log;
import com.julioterra.moodyjulio.dataload.data.Data_Mobile;
import com.julioterra.moodyjulio.dataload.data.Data;
import de.bezier.data.sql.MySQL;
import processing.core.PApplet;

public class DataProcessor_Log extends DataProcessor {
		
	
	/***************************
	 *** CONSTRUCTORS
	 ***************************/
	
	public DataProcessor_Log() {
		super();
		this.init();
		this.reader = new DataReader_BigFile();
		this.convert_time = -5;
	}
	
	@Override
	/***************************
	 * INIT [method from parent class]
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
		super.init();
		this.data_list_pre_process = new ArrayList<Data>();
	}
	
	@Override
	/***************************
	 * REGISTER_DATA_SOURCE [method from parent class]
	 * This method registers a database that is passed as an argument into this DataProcessor object, 
	 * then it initializes the reader variable by registering a file name, username and password. The 
	 * file name registered here is used as the base file name for reading sequentially named files.
	 * 
 	 * Input parameters: a MySQL database 
	 * Returns data type: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public void register_data_source(MySQL database) {
		super.register_data_source(database);		
		String temp_file_number = get_max_element(this.database, "data_raw", "entry_id");
		if (temp_file_number == null) temp_file_number = "0";

		String file_name = "/Users/julioterra/Documents/ITP/Thesis/Emote/data/log_data/act_log";
		String file_extension = "txt";
		String number_placeholders = "4";
		String[] file_params = {temp_file_number, file_extension, number_placeholders};		
		
		reader.register_file_name(file_name, file_params);
		if (Element.debug_code) System.out.println("DataProcessor_Log.registerDataSource() - register http source " + file_name + " " + file_params);		
	}
	
	@Override
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
		String current_page = "";
		int missing_file_count = 0;
		
		while (missing_file_count < 5) {
			boolean new_file = false;
			try { 
				reader.open_next_file(); 
				new_file = true;
			}
			catch (Exception e) { System.out.println("DataProcessor_Log.readRawData(): error openning file  " + e +", " + e.getMessage()); }

			String [] old_data_entry = {""};
				
			while((current_page = reader.read_full_file()) != null && !current_page.equals("")) {
				this.init();
				String entry_id = this.reader.file_num + "";
				String[] lines = current_page.split("\n");					// parse and trim data that was read from file
				String log_name = lines[0].substring(20);
				boolean new_date_needed = false;	
				String date = "";
				
				// save the log name into a variable
				for (int i = 1; i < lines.length; i ++) {
					System.out.println("DataProcessor_Log.readRawData(): new line  " + lines[i]);						
					if (lines[i].length() < 3) {
						new_date_needed = true;
					}
					else {
						// figure out date
						if (new_date_needed) {
							date = lines[i];
							new_date_needed = false;
						}
						else {
							String[] contents = {entry_id, log_name, date, lines[i]};
							Data_Log data_entry = new Data_Log(contents);
							System.out.println("DataProcessor_Log.readRawData(): new record before array list  " + entry_id + " " + log_name + " " + date + " " + lines[i]);							
							System.out.println("DataProcessor_Log.readRawData(): new record before array list  " + data_entry.getSQLInsertString());							
							this.data_list_pre_process.add(new Data_Log(contents));
						}
					}
				}
	
				this.process_raw_data();
			}
	
			if (Element.debug_code && current_page == null) System.out.println("DataProcessor_Mobile.readRawData() - No content in file");
			else if (Element.debug_code) System.out.println("DataProcessor_Mobile.readRawData() - Reading content from file");		

			if (current_page == null) return;
				
		}
		
	}
		
	@Override
	/***************************
	 * ADD_RAW_DATA [method from parent class]
	 * takes an array and confirms that the it is of the correct size, before cleaning the contents of each string
	 * and then creating a new data_journal object to add to the data_list_pre_process arraylist.
	 * 
 	 * Input parameters: a String array  
	 * Returns data type: boolean value set to true if the array was of the correct size, and false otherwise
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	protected boolean add_raw_data(String[] data_entry) {
		return false;
	}

	@Override
	/************************************
	 * PROCESS_RAW_DATA [method from parent class]
	 * This processes the raw data that was read from a file by (1) fixing the time and date, (2) adding 
	 * time, date, and heart rate information to all records that don't have that data yet, (3) calculating
	 * the filtered gsr values using the high-pass filter.
	 * 
	 * Input parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */		
	public void process_raw_data() {
		if (Element.debug_code) { System.out.println("DataProcessor_Log.process(): processing raw data"); }
		this.uploadDataFromDataArrayList("data_raw", this.data_list_pre_process);
		this.uploadDateAvailableData();
	}

	
}

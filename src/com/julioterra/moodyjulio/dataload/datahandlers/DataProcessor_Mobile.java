// TO DO - UPDATE
//		add code that calculates time for records after the last GPS time entry
//		finish the database adding section 

package com.julioterra.moodyjulio.dataload.datahandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.julioterra.moodyjulio.dataload.basicelements.Date;
import com.julioterra.moodyjulio.dataload.basicelements.Element;
import com.julioterra.moodyjulio.dataload.basicelements.Time;
import com.julioterra.moodyjulio.dataload.data.Data_Journal;
import com.julioterra.moodyjulio.dataload.data.Data_Mobile;
import com.julioterra.moodyjulio.dataload.data.Data;
import de.bezier.data.sql.MySQL;
import processing.core.PApplet;

public class DataProcessor_Mobile extends DataProcessor {
		
	final int GMT_to_EST = -5;
	public Data_Mobile first_data_entry;
	
	boolean gps_time_flag;
	int gps_time_readings;
	ArrayList<Long> gps_time_arduino_time;
	ArrayList<Integer> gps_time_index;

	boolean heart_beat_flag;
	ArrayList<Long> heart_beat_arduino_time;
	ArrayList<Integer> heart_beat_index;

	public static int last_heart_rate = 				0;
	public static float time_interval_between_reads = 	0;

	
	/***************************
	 *** CONSTRUCTORS
	 ***************************/
	
	public DataProcessor_Mobile() {
		super();
		this.init();
		this.reader = new DataReader_BigFile();
		this.convert_time = -5;
	}
	
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
	public void init_data_flags() {
		this.data_list_pre_process = new ArrayList<Data>();
		this.data_list_post_process = new ArrayList<Data>();

		this.gps_time_flag = false;
		this.gps_time_readings = 0;
		this.gps_time_arduino_time = new ArrayList<Long>();
		this.gps_time_index = new ArrayList<Integer>();

		this.heart_beat_flag = false;
		this.heart_beat_arduino_time = new ArrayList<Long>();
		this.heart_beat_index = new ArrayList<Integer>();
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
		if (temp_file_number == null) temp_file_number = "247";
		else temp_file_number = String.valueOf(Integer.parseInt(temp_file_number) + 1);
//		temp_file_number = "380";
		
		String file_name = "/Users/julioterra/Documents/ITP/Thesis/Emote/data/physio_data/LOG";
		String file_extension = "txt";
		String number_placeholders = "5";
		String[] file_params = {temp_file_number, file_extension, number_placeholders};		
		
		reader.register_file_name(file_name, file_params);
		if (Element.debug_code) System.out.println("DataProcessor_Mobile.registerDataSource() - register http source " + file_name + " " + file_params);		
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
		int missing_file_count = 0;
		String current_page = "";
		
		boolean read_file = false;
		boolean file_exists = false;
		
		while (missing_file_count < 10) {
			try { 
				reader.open_next_file(); 
				file_exists = true;
				read_file = false;
			}
			catch (Exception e) { 
				file_exists = false;
				read_file = false;
				System.out.println("DataProcessor_Mobile.readRawData(): error openning file  " + e +", " + e.getMessage()); 
			}
			
			String [] old_data_entry = {""};
			
			if (file_exists) {
				this.init_data_flags();				
				int gps_reading_found = 0;
				while((current_page = reader.read_partial_file(1)) != null && !current_page.equals("")) {
					String[] lines = current_page.split("\n");					// parse and trim data that was read from file
					for (int i = 0; i < lines.length; i ++) {
						if (!lines[i].contains(", 0, 0, 0, 0, 0, 0, 0")) {
							if (!read_file) read_file = true;
							gps_reading_found ++;
						}
						if (read_file) {
							String[] data_entry = lines[i].split(",");					
							boolean data_clean = true;
							for (int j = 0; j < data_entry.length; j++) 
								if (data_entry[j].contains("*")) data_clean = false;
							if (data_entry.length >= 10 && data_clean) {
								this.add_raw_data(data_entry);
								if (Element.debug_code) System.out.println("DataProcessor_Mobile.readRawData() - current line " + lines[i]);		
							}
						}
					}

					if (gps_reading_found >= 800) {
						this.process_raw_data();
//						transformData();
						gps_reading_found = 0;
						this.init_data_flags();				
					}		
				}
				if (gps_reading_found > 0) {
					this.process_raw_data();
//					transformData();
					gps_reading_found = 0;
					this.init_data_flags();				
				}
			}
			if (Element.debug_code && current_page == null) System.out.println("DataProcessor_Mobile.readRawData() - No content in file");
			else if (Element.debug_code) System.out.println("DataProcessor_Mobile.readRawData() - Reading content from file");		
			if (current_page == null) {
				missing_file_count ++;	
			}
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
		if (data_entry.length >= 10) {
			for (int j = 0; j < data_entry.length; j++) data_entry[j] = PApplet.trim(data_entry[j]);
			String temp_time = data_entry[4];
			data_entry[4] = data_entry[5];
			data_entry[5] = temp_time;

			this.gps_time_flag = false; 
			this.reading_data = true;
			Data_Mobile mobile_data_entry = new Data_Mobile(this.reader.file_num-1, data_entry);
			mobile_data_entry.date_stamp.setDMY(data_entry[4]);

			// check if reading contains a gps time
			if (mobile_data_entry.date_stamp.day != 0) { 
				if (Element.debug_code) { System.out.println("DataProcessor_Mobile.add_raw_data(): valid gps reading:  " + mobile_data_entry.time_stamp.get_time_with_millis_in_string() + ", " + mobile_data_entry.date_stamp.get_string() + ", " + gps_time_readings); }
				this.gps_time_readings++;
				this.gps_time_flag = true; 
				this.gps_time_arduino_time.add(mobile_data_entry.arduino_time);
				this.gps_time_index.add(data_list_pre_process.size());
			}
		
			// check if reading contains a heart rate
			if (mobile_data_entry.heart_rate > 0) { 
				this.heart_beat_flag = true; 
				this.heart_beat_arduino_time.add(mobile_data_entry.arduino_time);
				this.heart_beat_index.add(data_list_pre_process.size());	
				last_heart_rate = mobile_data_entry.heart_rate;
			}
		
			// check if ready_to_start has been set to true, if so then save new data element
			this.data_list_pre_process.add(mobile_data_entry);	
			
			return true;
		} 
		else return false;
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
		if (Element.debug_code) { System.out.println("DataProcessor_Mobile.process(): processing raw data"); }

		this.fix_time_date();
		this.distribute_time_date();
		this.distribute_heart_rate();
		this.gsr_high_pass();

		this.data_list_post_process = new ArrayList<Data>();
		for (int i = 0; i < this.data_list_pre_process.size(); i++) {
			if (i > 0) this.data_list_post_process.add(this.data_list_pre_process.get(i));
		}
		
		this.uploadDataFromDataArrayList("data_raw", this.data_list_pre_process);
		this.uploadDateAvailableData();
	}

	@Override
	/************************************
	 * FIX_TIME_DATE [method from parent class]
	 * This method goes through the data_list_preprocessed_array and fixes the dates so that they reflect
	 * the appropriate time zone.
	 * 
	 * Inputs parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	protected void fix_time_date() {
		// fix the gps date and time readings on 
		if (Element.debug_code) System.out.println("DataProcessor_Mobile.fix_time_date(): gps readings " + gps_time_index.size());		
		for (int i = gps_time_index.size() - 1; i >= 0; i--) {
			Data active_reading = data_list_pre_process.get(gps_time_index.get(i));			
			if (Element.debug_code) System.out.println("DataProcessor_Mobile.fix_time_date(): prior update " + active_reading.time_stamp.get_string() + " " + active_reading.date_stamp.get_string());		
			int date_adjust = active_reading.time_stamp.update_hours(GMT_to_EST); 
			active_reading.date_stamp.update_day(date_adjust); 
			if (Element.debug_code) System.out.println("DataProcessor_Mobile.fix_time_date(): after update" + active_reading.time_stamp.get_string() + " " + active_reading.date_stamp.get_string());		
		}
	}
	

	// calculates time and date for all the records that do not have time and date stamps
	protected void distribute_time_date() {
		if (debug_code) System.out.print("DataProcessor_Mobile.distribute_time_date(): distribute date and time, ");
		if (debug_code) System.out.println("gps array size: " + gps_time_index.size() + " main array size: " + data_list_pre_process.size()); 

		if ((gps_time_index.size() > 0) && (gps_time_index.get(0) != 0)) { 
				distribute_time_date(gps_time_index.get(0), 0);
		}

		// process data for data sets that include more than one GPS time reading
		if (gps_time_index.size() > 1) {			
			for (int i = 0; i < gps_time_index.size() - 1; i++) 
				distribute_time_date(gps_time_index.get(i), gps_time_index.get(i+1));
			if (gps_time_index.get(gps_time_index.size()-1) < data_list_pre_process.size())
				distribute_time_date(gps_time_index.get(gps_time_index.size()-1), data_list_pre_process.size()-1);				
		} 
		
		// process data for data sets that include only one GPS time reading
		else if (gps_time_index.size() == 1) {
			distribute_time_date(gps_time_index.get(0), data_list_pre_process.size()-1);
			distribute_time_date(gps_time_index.get(0), 0);
		}

	}	

	protected void distribute_time_date(int start_index, int end_index) {
		if (data_list_pre_process.size() > start_index && data_list_pre_process.size() > end_index) {
			Data_Mobile start_record = (Data_Mobile) data_list_pre_process.get(start_index);
			Data_Mobile end_record = (Data_Mobile) data_list_pre_process.get(end_index);
			int reads_between_gps_time = PApplet.abs(end_index - start_index);
			float time_difference = end_record.arduino_time - start_record.arduino_time;
			time_interval_between_reads = time_difference / reads_between_gps_time;

			for (int j = 0; j < reads_between_gps_time; j ++) {
				int index = (start_index + 1) + j;
				if (start_index > end_index)  index = (start_index - 1) - j;
				Data_Mobile active_record = (Data_Mobile) data_list_pre_process.get(index);
				active_record.time_stamp.calculate_step_time(start_record.time_stamp, time_interval_between_reads, j);
				active_record.date_stamp.calculate_dates(start_record.date_stamp, start_record.time_stamp, time_interval_between_reads, j);
				if (debug_code) System.out.println("DataProcessor_Mobile.distribute_time_date(): interval " + time_interval_between_reads + " loop " + j);
			}
			
			add_to_date_list(start_record.date_stamp);
			add_to_date_list(end_record.date_stamp);
		}							
	}
	
	protected void distribute_heart_rate() {
		if (heart_beat_index.size() > 1) {
			if (debug_code) System.out.println("** mobile handler [process] : processing heart rate data"); 
			if (debug_code) System.out.println("   heart rate array size: " + heart_beat_index.size() + " main array size: " + data_list_pre_process.size()); 

			if ((heart_beat_index.size() > 0) && (heart_beat_index.get(0) != 0)) {
					distribute_time_date(heart_beat_index.get(0), 0);
			}

			if (heart_beat_index.size() > 1) {
				for (int i = 0; i < heart_beat_index.size()-1; i++) 
					distribute_heart_rate(heart_beat_index.get(i), heart_beat_index.get(i+1));
				if (heart_beat_index.get(heart_beat_index.size()-1) < data_list_pre_process.size())
					distribute_time_date(heart_beat_index.get(heart_beat_index.size()-1), data_list_pre_process.size()-1);				
			}
			else if (heart_beat_index.size() == 1) {
				distribute_heart_rate(heart_beat_index.get(0), data_list_pre_process.size()-1);
			}
		}	
	}

	
	protected void distribute_heart_rate(int anchor_index, int second_index) {
		// calculate number of reads between GPS data input
		if (data_list_pre_process.size() > anchor_index && data_list_pre_process.size() > second_index) {
			Data_Mobile anchor_record = (Data_Mobile) data_list_pre_process.get(anchor_index);
			int reads_between_heartbeat_time = PApplet.abs(second_index - anchor_index);
			for (int j = 0; j < reads_between_heartbeat_time; j ++) {
				int index = (anchor_index) + j;
				Data_Mobile active_record = (Data_Mobile) data_list_pre_process.get(index);
				active_record.heart_rate = anchor_record.heart_rate;
			}
			if (second_index == data_list_pre_process.size() - 1) {
				Data_Mobile active_record = (Data_Mobile) data_list_pre_process.get(second_index);
				active_record.heart_rate = anchor_record.heart_rate;
			}					
		}

	}
	
	void gsr_high_pass() {
		// fix the gps date and time readings on 
		for (int i = 0; i < data_list_pre_process.size(); i++) {
			Data_Mobile active_reading = (Data_Mobile) data_list_pre_process.get(i);			
			active_reading.gsr_high_pass = (int) DataProcessor_Mobile.gsr_high_pass(active_reading.arduino_time, active_reading.gsr);
		}
	}

	
	/************************************
	 * TRANSFORM_DATA 
	 * Calls the methods responsible for processing the raw data from the "data_raw" data table to create
	 * and upload the data for the following data tables: "data_hourly" which holds journal data averaged our 
	 * for each hour of each day during which data was collected; "data_hourly_avg" which holds journal data
	 * averaged for each hour of each day of week, as well as the overall average for each our all the days on 
	 * which data was recorded; "data_relational" holds the information about how strongly different categories 
	 * of emotions relate to the different activities, people, and places in my life.
	 * 
	 * Input parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */		
	public void transformData() {
		if (debug_code) PApplet.println("DataProcessor_Mobile.transformData(): start data transformation ");
		boolean data_already_exists = false;
		
		Iterator<Map.Entry<String, Integer>> date_list_items = date_list.entrySet().iterator();
		while (date_list_items.hasNext()) {
			Map.Entry<String, Integer> map_entry = date_list_items.next();
			String date_key = (String) map_entry.getKey();	 
			Date date_current = new Date(date_key);
			
			this.data_list_post_process = new ArrayList<Data>();
			if (debug_code) PApplet.println("DataProcessor_Mobile.transformData(): processing date " + date_key);
			
			// create an empty data array with an element for each hour of a specific date
			ArrayList<Data> data_for_date_post_process = createDatedTimeBasedList(1, date_current, 1);
			for (int i = 0; i < data_for_date_post_process.size(); i++) {		
				
				// Get one pie emotion object from the array and query database for related JournalData entries
				Data_Mobile post_process_data_element = (Data_Mobile) data_for_date_post_process.get(i);
				post_process_data_element.arduino_time = 0;
				post_process_data_element.entry_id = "";								
				if (debug_code) System.out.println("DataProcessor_Mobile.transformData(): process date " + post_process_data_element.date_stamp.get_string() + " time " + post_process_data_element.time_stamp.get_string());

//				COMMENTING OUT TEMPORARILY TO SPEED UP CODE
//				WHEN COMMENTED NEED TO MAKE SURE YOU DON'T READ THE SAME FILE TWICE, SO THAT IT DOES NOT CREATE TWO DB RECORDS FOR AN ENTRY
//				ArrayList<Data> existing_data = load_date_time(this.database, "data_minutes", post_process_data_element.date_stamp, post_process_data_element.time_stamp);				
//				data_already_exists = false;
//				for (int j = 0; j < existing_data.size(); j++) {
//					if (post_process_data_element.date_stamp.equals(existing_data.get(j).date_stamp) &&
//						post_process_data_element.time_stamp.equals(existing_data.get(j).time_stamp)) {
//							data_already_exists = true; 
//					}
//				}

				if (!data_already_exists) {
					this.data_list_pre_process = load_date_time(this.database, "data_raw", post_process_data_element.date_stamp, post_process_data_element.time_stamp);				
					if (this.data_list_pre_process.size() >  0) { 		
						// go through each entry and add data into minute average variable
						for (int k =  0; k < this.data_list_pre_process.size(); k++) {
								Data_Mobile pre_process_data_element = (Data_Mobile) data_list_pre_process.get(k);						
								post_process_data_element.heart_rate +=  pre_process_data_element.heart_rate;
								post_process_data_element.gsr +=  pre_process_data_element.gsr;
								post_process_data_element.gsr_high_pass +=  pre_process_data_element.gsr_high_pass;
								post_process_data_element.humidity +=  pre_process_data_element.humidity;
								post_process_data_element.temperature +=  pre_process_data_element.temperature;
								if (pre_process_data_element.button_one > 0) post_process_data_element.button_one = 1;					
						}
						// divide the minute average total in each variable by the number of readings that were added together
						post_process_data_element.heart_rate = post_process_data_element.heart_rate / this.data_list_pre_process.size();
						post_process_data_element.gsr =  post_process_data_element.gsr / this.data_list_pre_process.size();
						post_process_data_element.gsr_high_pass =  post_process_data_element.gsr_high_pass / this.data_list_pre_process.size();
						post_process_data_element.humidity =  post_process_data_element.humidity / this.data_list_pre_process.size();
						post_process_data_element.temperature =  post_process_data_element.temperature / this.data_list_pre_process.size();					
					}
					if (post_process_data_element.heart_rate > 0 || post_process_data_element.gsr > 0) {
						this.data_list_post_process.add(post_process_data_element);
					}
				}
			}			
			
			this.uploadDataFromDataArrayList("data_minutes", this.data_list_post_process);
			transformAvgWeekdayMinutesData(this.data_list_post_process);
			transformAvgOverallMinutesData(this.data_list_post_process);
			date_list = new HashMap<String,Integer>();
		}		
	}

	public void transformDataByHour(int number_of_days) {
		String temp_start_date = get_max_element(this.database, "data_minutes", "date_stamp");
		if (temp_start_date == null) return;
		Date date_current = new Date(temp_start_date);
		date_current.update_day(1);
		System.out.println("HERE -- lastests date " + date_current.get_date_for_sql());
		int days_completed = 0;		
		while (days_completed < number_of_days) {
			transformDataByHour(date_current);
			date_current.update_day(1); 
			days_completed++;
		}

		
		
	}
	
	/************************************
	 * TRANSFORM_DATA_BY_HOUR
	 * Calls the methods responsible for processing the raw data from the "data_raw" data table to create
	 * and upload the data for the following data tables: "data_hourly" which holds journal data averaged our 
	 * for each hour of each day during which data was collected; "data_hourly_avg" which holds journal data
	 * averaged for each hour of each day of week, as well as the overall average for each our all the days on 
	 * which data was recorded; "data_relational" holds the information about how strongly different categories 
	 * of emotions relate to the different activities, people, and places in my life.
	 * 
	 * Input parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */		
	public void transformDataByHour(Date date_to_process) {
		Date date_current = new Date(date_to_process);
		if (debug_code) PApplet.println("DataProcessor_Mobile.transformDataByHour(): start processing date " + date_current.get_date_for_sql());
		
		int interval_time_minutes = 240;
		boolean interval_processed = false;
		Time time_range_start = new Time("00:00:00");
		Time temp_time_start = 	new Time(time_range_start);
		Time temp_time_end = 	new Time(temp_time_start);
		temp_time_end.update_minutes((int) interval_time_minutes);
		HashMap<String, Integer> readings_per_minute = new HashMap<String, Integer>();
		
		while (!interval_processed) {
			if (debug_code) PApplet.println("DataProcessor_Mobile.transformDataByHour(): while loop - time start " + 
											temp_time_start.get_time_for_sql() + " time end "+ temp_time_end.get_time_for_sql());
			
			this.data_list_post_process = new ArrayList<Data>();
			this.data_list_pre_process = load_date_time_range(this.database, "data_raw", date_current, temp_time_start, date_current, temp_time_end);				
			if (debug_code) PApplet.println("DataProcessor_Mobile.transformDataByHour(): existing data record in this timeframe: " + this.data_list_pre_process.size());
			
			if (this.data_list_pre_process.size() > 0) {	
	
				for (int i = this.data_list_pre_process.size()-1; i >= 0 ; i--) {		
					Data_Mobile pre_process_data_element = (Data_Mobile) this.data_list_pre_process.get(i);						
					Time time_hr_mins = new Time(pre_process_data_element.time_stamp.hour, pre_process_data_element.time_stamp.minute, 0);

					boolean match_found = false;
					if (this.data_list_post_process.size() > 0) {
						for (int k =  0; k < this.data_list_post_process.size(); k++) {
							if (debug_code) PApplet.println("DataProcessor_Mobile.transformDataByHour(): update existing post element " + k + " time " + time_hr_mins.get_time_for_sql());
							Data_Mobile post_process_data_element = (Data_Mobile) data_list_post_process.get(k);						
							if (time_hr_mins.equals_hrs_mins(post_process_data_element.time_stamp)) {
								post_process_data_element.heart_rate +=  pre_process_data_element.heart_rate;
								post_process_data_element.gsr +=  pre_process_data_element.gsr;
								post_process_data_element.gsr_high_pass +=  pre_process_data_element.gsr_high_pass;
								post_process_data_element.humidity +=  pre_process_data_element.humidity;
								post_process_data_element.temperature +=  pre_process_data_element.temperature;
								if (pre_process_data_element.button_one > 0) post_process_data_element.button_one = 1;	
								match_found = true;
								
								Integer readings = readings_per_minute.get(time_hr_mins.get_time_for_sql());
								readings_per_minute.put(time_hr_mins.get_time_for_sql(), (readings+1));
								if (debug_code) PApplet.println("DataProcessor_Mobile.transformDataByHour(): reading " + readings);
							}
						}
					}
					if (!match_found) {
						if (debug_code) PApplet.println("DataProcessor_Mobile.transformDataByHour(): create new post element " + i + " time " + time_hr_mins.get_time_for_sql());
						Data_Mobile post_process_data_element = new Data_Mobile(pre_process_data_element.date_stamp, time_hr_mins);						
						post_process_data_element.arduino_time = 0;
						post_process_data_element.entry_id = "";								
						post_process_data_element.heart_rate =  pre_process_data_element.heart_rate;
						post_process_data_element.gsr =  pre_process_data_element.gsr;
						post_process_data_element.gsr_high_pass =  pre_process_data_element.gsr_high_pass;
						post_process_data_element.humidity =  pre_process_data_element.humidity;
						post_process_data_element.temperature =  pre_process_data_element.temperature;
						if (pre_process_data_element.button_one > 0) post_process_data_element.button_one = 1;	
						this.data_list_post_process.add(post_process_data_element);
						readings_per_minute.put(time_hr_mins.get_time_for_sql(), 1);
					}
					data_list_pre_process.remove(i);
				}

				
				for (int i = 0; i < data_list_post_process.size(); i++) {
					Data_Mobile post_process_data_element = (Data_Mobile) data_list_post_process.get(i);						
					int readings = readings_per_minute.get(post_process_data_element.time_stamp.get_time_for_sql());
					post_process_data_element.heart_rate = post_process_data_element.heart_rate / readings;
					post_process_data_element.gsr =  post_process_data_element.gsr / readings;
					post_process_data_element.gsr_high_pass =  post_process_data_element.gsr_high_pass / readings;
					post_process_data_element.humidity =  post_process_data_element.humidity / readings;
					post_process_data_element.temperature =  post_process_data_element.temperature / readings;					
					if (debug_code) PApplet.println("DataProcessor_Mobile.transformDataByHour(): fixing averages " + i + " data " + post_process_data_element.getSQLInsertString("data_minutes"));
					
				}

				this.data_list_pre_process = new ArrayList<Data>();				
				this.uploadDataFromDataArrayList("data_minutes", this.data_list_post_process);
				transformAvgWeekdayMinutesData(this.data_list_post_process);
				transformAvgOverallMinutesData(this.data_list_post_process);
				this.data_list_post_process = new ArrayList<Data>();
			}
		
			// TIME CHECK AND UPDATE
			// check if we have reached the end of the date range
			if (Time.calculate_time_dif_seconds(temp_time_end, time_range_start) < (60*interval_time_minutes))
				interval_processed = true;						
			temp_time_start = new Time (temp_time_end);
			temp_time_end.update_minutes((int) interval_time_minutes); 
			if (debug_code) PApplet.println("DataProcessor_Mobile.transformDataByHour() - finished processing current hour");

		}
		
		if (debug_code) PApplet.println("DataProcessor_Mobile.transformDataByHour() - finished processing current day");
	}
	
	void transformAvgWeekdayMinutesData(ArrayList<Data> new_data_list) {
		boolean[] new_data_weekdays = {false, false, false, false, false, false, false};
		ArrayList<Data> dow_existing_data = new ArrayList<Data>();
		ArrayList<ArrayList<Data>> week_existing_data = new ArrayList<ArrayList<Data>>();
		ArrayList<Data> dow_new_data = new ArrayList<Data>();
		ArrayList<ArrayList<Data>> week_new_data = new ArrayList<ArrayList<Data>>();
		
		for (Integer i = 0; i < new_data_list.size(); i++) {
			for (int j = 0; j < new_data_weekdays.length; j++) {				
				if (new_data_list.get(i).date_stamp.get_day_of_week()+1 == j) new_data_weekdays[j] = true;
			}
		}		
		
		for (Integer i = 1; i <= 7; i++) {
			if (new_data_weekdays[i-1]) dow_existing_data = load_query_match(this.database, "data_minutes_avg", "entry_id", "dow_" + i);
			else dow_existing_data = new ArrayList<Data>();
			week_existing_data.add(dow_existing_data);			
			
			dow_new_data = new ArrayList<Data>();			
			week_new_data.add(dow_new_data);
		}

		// pieces of data to be transferred
		for (int i = 0; i < new_data_list.size(); i ++) {
			Data_Mobile new_raw_minute_data = (Data_Mobile) new_data_list.get(i);
			
			dow_existing_data = week_existing_data.get(new_raw_minute_data.date_stamp.get_day_of_week()-1);
			dow_new_data = week_new_data.get(new_raw_minute_data.date_stamp.get_day_of_week()-1);	
			
			if (debug_code) PApplet.println("DataProcessor_Mobile.transformAvgWeekdayMinutesData() - New Data: " + i + " "); 
			for (int j = 0; j < dow_existing_data.size(); j++) PApplet.println(dow_existing_data.get(j).getSQLInsertString("data_minutes_avg"));
			if (debug_code) PApplet.println("DataProcessor_Mobile.transformAvgWeekdayMinutesData() - Existing Data: " + i  + " "); 
			for (int j = 0; j < dow_new_data.size(); j++) PApplet.println(dow_new_data.get(j).getSQLInsertString("data_minutes_avg"));
			
			boolean existing_data_matched = false;

			for (int j = 0; j < dow_existing_data.size(); j++) {
				Data_Mobile dow_existing_minute_avg = (Data_Mobile) dow_existing_data.get(j);
				if (dow_existing_minute_avg.time_stamp.equals(new_raw_minute_data.time_stamp)) {
					existing_data_matched = true;
					dow_existing_minute_avg.minutes_measured ++;
					dow_existing_minute_avg.gsr = (dow_existing_minute_avg.gsr * (dow_existing_minute_avg.minutes_measured-1) + new_raw_minute_data.gsr) / dow_existing_minute_avg.minutes_measured;  
					dow_existing_minute_avg.heart_rate = (dow_existing_minute_avg.heart_rate * (dow_existing_minute_avg.minutes_measured-1) + new_raw_minute_data.heart_rate) / dow_existing_minute_avg.minutes_measured;  
					dow_existing_minute_avg.humidity = (dow_existing_minute_avg.humidity * (dow_existing_minute_avg.minutes_measured-1) + new_raw_minute_data.humidity) / dow_existing_minute_avg.minutes_measured;  
					dow_existing_minute_avg.temperature = (dow_existing_minute_avg.temperature * (dow_existing_minute_avg.minutes_measured-1) + new_raw_minute_data.temperature) / dow_existing_minute_avg.minutes_measured;  
					j = dow_existing_data.size();
					if (debug_code) PApplet.println("DataProcessor_Mobile.transformAvgWeekdayMinutesData() - DATA MATCHED: " + dow_existing_minute_avg.getSQLInsertString("data_minutes_avg")); 
				} 				
			}
			
			if (!existing_data_matched) {
				Data_Mobile old_minute_data = new Data_Mobile(new_raw_minute_data);
				old_minute_data.entry_id = "dow_" + (old_minute_data.date_stamp.get_day_of_week()-1);
				dow_new_data.add(old_minute_data);
				if (debug_code) PApplet.println("DataProcessor_Mobile.transformAvgWeekdayMinutesData() - CREATE NEW DATE " + dow_new_data.size()); 
			}
		}

		//	add this value to the emotion_valence and emotion_intensity of the day_of_week_record
		for (int i = 0; i < week_existing_data.size(); i++) {
			dow_existing_data = week_existing_data.get(i);
			if (debug_code) PApplet.println("DataProcessor_Mobile.transformAvgWeekdayMinutesData() - existing data: " + i); 
			for (int j = 0; j < dow_existing_data.size(); j++) PApplet.println(dow_existing_data.get(j).getSQLInsertString("data_minutes_avg"));
			if (dow_existing_data.size() > 0) updateDataFromArrayList("data_minutes_avg", dow_existing_data);
		}
		
		for (int i = 0; i < week_new_data.size(); i++) {
			dow_new_data = week_new_data.get(i);
			if (debug_code) PApplet.println("DataProcessor_Mobile.transformAvgWeekdayMinutesData() - new data: " + i); 
			for (int j = 0; j < dow_new_data.size(); j++) PApplet.println(dow_new_data.get(j).getSQLInsertString("data_minutes_avg"));
			if (dow_new_data.size() > 0) uploadDataFromDataArrayList("data_minutes_avg", dow_new_data);
		}
		

	}

	void transformAvgOverallMinutesData(ArrayList<Data> new_data_list){
		boolean current_data_exists = false;
		
		data_list_post_process = load_query_match(this.database, "data_minutes_avg", "entry_id", "overall");		
		if (data_list_post_process.size() >= (24*60)) {
			current_data_exists = true;
		} else {
			current_data_exists = false;
			data_list_post_process = createTimeBasedList(1, new Time("00:00:00"), new Time("23:59:59"), 1);	
			for (int j = 0; j < data_list_post_process.size(); j++) {	// properly initialize the seconds_measured variables in the new array
				Data_Mobile current_record = (Data_Mobile)data_list_post_process.get(j);
				current_record.entry_id = "overall";				
			}
		} 
		
		for (int i = 0; i < new_data_list.size(); i ++) {
			Data_Mobile current_record = (Data_Mobile)new_data_list.get(i);
			for (int j = 0; j < data_list_post_process.size(); j++) {
				Data_Mobile overall_avg_record = (Data_Mobile) data_list_post_process.get(j);
				if (overall_avg_record.time_stamp.equals(current_record.time_stamp)) {
						overall_avg_record.minutes_measured ++;
						overall_avg_record.gsr = (overall_avg_record.gsr * (overall_avg_record.minutes_measured-1) + current_record.gsr) / overall_avg_record.minutes_measured;  
						overall_avg_record.heart_rate = (overall_avg_record.heart_rate * (overall_avg_record.minutes_measured-1) + current_record.heart_rate) / overall_avg_record.minutes_measured;  
						overall_avg_record.humidity = (overall_avg_record.humidity * (overall_avg_record.minutes_measured-1) + current_record.humidity) / overall_avg_record.minutes_measured;  
						overall_avg_record.temperature = (overall_avg_record.temperature * (overall_avg_record.minutes_measured-1) + current_record.temperature) / overall_avg_record.minutes_measured;  
						if (debug_code) PApplet.println("DataProcessor_Mobile.transformAvgOverallMinutesData() - processing data: " + overall_avg_record.getSQLInsertString("data_minutes_avg")); 
				}
			}			
		}
		
		//	add this value to the emotion_valence and emotion_intensity of the day_of_week_record
		if (current_data_exists) updateDataFromArrayList("data_minutes_avg", new_data_list);
//		else uploadDataFromDataArrayList("data_minutes_avg", this.data_list_post_process);
		
	}


	/***************************
	 *** GET FUNCTIONS 
	 ***************************/
		
	@Override
	public Data_Mobile get(int index) {
		return (Data_Mobile) data_list_pre_process.get(index);
	}
	
	/***************************
	 *** STATIC FUNCTION - GSR HIGH PASS FILTER
	 *** these variables and functions are not re-initialized when a new instance of the MobileDataHandler object is created
	 ***************************/
	
	public static float rawGsrVal = 0;
	public static float processedGsrVal = 0;

	protected static int startupCounter = 0;
	protected static int maxStartupCounter = 500;

	protected static long timeElasped = 0;
	protected static long previousMillis = 0;
	protected static long currentMillis = 0;

	protected static float smoothBuffer = (float) 0.0;  	// current skin resistance value
	protected static float normBuffer = (float) 0.0;  	// average skin resistance value

	// period over which data is smoothed
	protected static float smoothReadingCount = (float) 1.0;
	protected static float maxSmoothReadingCount = (float) (100.0);

	// period over which average skin resistance is calculated
	protected static float normReadingCount = (float) 1.0;
	final static float maxNormReadingCount = (float) (150.0); 	 

	public static float gsr_high_pass(long time_stamp, int current_gsr_reading) {
		currentMillis = time_stamp;
		timeElasped = currentMillis - previousMillis;
		rawGsrVal = current_gsr_reading; 

//		timeElasped = currentMillis - previousMillis;
//		if (timeElasped > maxSmoothPeriod + maxNormPeriod) { previousMillis = currentMillis; }
		
		// smooth the data by averaging it out using a weighted average
		smoothBuffer = (float) (smoothBuffer * (smoothReadingCount - 1.0f));
		smoothBuffer = smoothBuffer + (rawGsrVal);
		smoothBuffer = smoothBuffer / smoothReadingCount;
		if (smoothReadingCount < maxSmoothReadingCount) smoothReadingCount = smoothReadingCount + 1.0f;

		// if enough time has passed then calculate baseline skin resistance by using an average 
		else  {
			normBuffer = (float) (normBuffer * (normReadingCount - 1.0f));
			normBuffer = normBuffer + smoothBuffer;
			normBuffer = normBuffer/normReadingCount;
			if ((normReadingCount - maxSmoothReadingCount) < maxNormReadingCount) normReadingCount = normReadingCount + 1.0f; 
		}

		// else indicate calibration in progress and update startupCounter
		// if calibration is done subtract baseline average
		if (startupCounter < maxStartupCounter) { startupCounter++; }
		else { processedGsrVal = normBuffer - smoothBuffer; }
  
		return processedGsrVal;
	} 

	public static void init_gsr_high_pass() {
		rawGsrVal = 0f;
		processedGsrVal = 0f;
		startupCounter = 0;
		timeElasped = 0;
		previousMillis = 0;
		currentMillis = 0;
		smoothBuffer = (float) 0.0f;  	
		normBuffer = (float) 0.0f;  
		smoothReadingCount = (float) 1.0f;
		normReadingCount = (float) 1.0f;
	}
	
}

package com.julioterra.moodyjulio.dataload.datahandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import processing.core.PApplet;

import com.julioterra.moodyjulio.dataload.basicelements.Authenticate;
import com.julioterra.moodyjulio.dataload.basicelements.Element;
import com.julioterra.moodyjulio.dataload.basicelements.Date;
import com.julioterra.moodyjulio.dataload.basicelements.Time;
import com.julioterra.moodyjulio.dataload.data.Data;
import com.julioterra.moodyjulio.dataload.data.Data_Journal;

import de.bezier.data.sql.MySQL;

public class DataProcessor_Journal extends DataProcessor {

	/***************************
	 *** CONSTRUCTORS
	 ***************************/
	public DataProcessor_Journal() {
		super();
		this.init();
		this.reader = new DataReader_AuthHttp();
		this.convert_time = 3;
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
		
//		Add functionality to pull all of this information from a database, include the lastest_post_id
//		Load old latest post id and all of the dates for which data already exists
		String temp_post_id = get_max_element(this.database, "data_raw", "entry_id");
		if (temp_post_id == null) temp_post_id = "0";

		String file_name = "http://posterous.com/api/2/users/826484/sites/mymoods/posts?page=";
		String api_token = "&api_token=" + "jgbEGmFszmhawDuHpzwosyxCEDAcqaBu";
		String since_id = "&since_id=" + temp_post_id;
		String callback_method = "&callback=" + "GET";
		String[] file_params = {api_token, since_id, callback_method};
		String username = Authenticate.username;
	    String password = Authenticate.password;
		
		reader.register_file_name(file_name, file_params);
		reader.register_username_password(username, password);
		if (Element.debug_code) System.out.println("DataProcessor_Journal.registerDataSource() - register http source " + file_name + " " + file_params + " username: " + username + " password: " + password);		

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
		String current_page = "";
		try {
			reader.open_next_file();
			current_page = reader.read_full_file();						

			if (Element.debug_code)
				if (current_page == null) System.out.println("DataProcessor_Journal.readRawData() - No Content in File");
				else System.out.println("DataProcessor_Journal.readRawData() - Reading Content from File");		
			if (current_page == null) return;
			
		} catch (Exception e) { 
			PApplet.println("DataProcessor_Journal.readRawData() error reading file into current_page var - " + e +", " + e.getMessage()); 
		}
		
		// check if the page is valid by confirming it starts with "GET" and that it is longer than 20 characters
		while (current_page.substring(0, 3).equals("GET") && current_page.length() > 20) {
			try {
				String[][] post_ids = PApplet.matchAll(current_page, "\"id\":826484,(?:.*?),\"id\":(.*?)?[,}]" ); 
				String[][] date_time_string = PApplet.matchAll(current_page, "\"display_date\":\"([\\d][\\d][\\d][\\d][/][\\d][\\d][/][\\d][\\d])[ ]([\\d][\\d][:][\\d][\\d][:][\\d][\\d])");
				String[][] message_titles = PApplet.matchAll(current_page, "\"title\":\"(?:(Untitled)|(?:(.*?),(.*?),(.*?))?)?(?=\")");
				String[][] message_bodies_new = PApplet.matchAll(current_page, "\"body_html\":\"(?:.*?[Vv]?alence:(.*?)[Ee]?motions?:(.*?),(.*?),(.*?)[Aa]?ctivity:(.*?)[Ll]?ocations?:(.*?)[Pp]?eople:(.*?)[Dd]escription:(.*?)(?=\"))");
				String[][] message_bodies_old = PApplet.matchAll(current_page, "\"body_html\":\"(?:.*?[Aa]ctivity:(.*?)[Ll]ocations?:(.*?)[Pp]eople:(.*?)[Dd]escription:(.*?)(?=\"))");
	
				if (Element.debug_code) System.out.println("DataProcessor_Journal.readRawData() - Read New Page " + post_ids.length);		

				for (int i = 0; i < post_ids.length; i++) {
					if (message_titles != null && message_bodies_old != null) {
						if(message_titles.length > i && message_bodies_old.length > i) {
								if (message_titles[i][2] != null && message_titles[i][3] != null && message_titles[i][4] != null &&
									message_bodies_old[i][1] != null && message_bodies_old[i][2] != null && message_bodies_old[i][3] != null && 
									message_bodies_old[i][4] != null) {
									String [] new_entry = {"0.1", clean_string(post_ids[i][1]), 
														   clean_string(date_time_string[i][1]), clean_string(date_time_string[i][2]),
														   String.valueOf(convertEmotionToFloat(clean_string(message_titles[i][2]))),
														   String.valueOf(convertEmotionIntensityToFloat(clean_string(message_titles[i][3]))),
														   clean_string(message_titles[i][2]), clean_string(message_titles[i][3]), clean_string(message_titles[i][4]), 
														   clean_string(message_bodies_old[i][1]), clean_string(message_bodies_old[i][2]), 
														   clean_string(message_bodies_old[i][3]), String.valueOf(countListItems(message_bodies_old[i][3])),
														   clean_string(message_bodies_old[i][4])}; 	

//									if (Element.debug_code) System.out.print("DataProcessor_Journal.readRawData() - NEW ENTRY " + i + " ");
//									if (Element.debug_code) for (int j = 0; j < new_entry.length; j++) System.out.print(new_entry[j] + ", ");
//									if (Element.debug_code) System.out.println();

									this.add_raw_data(new_entry);
									
								}
							else if (message_bodies_new[i].length >=9) { 
								if(message_bodies_new[i][1] != null && message_bodies_new[i][2] != null && message_bodies_new[i][3] != null && message_bodies_new[i][4] != null &&
								message_bodies_new[i][5] != null && message_bodies_new[i][6] != null && message_bodies_new[i][7] != null && message_bodies_new[i][8] != null) {
									float emotion_number = Float.parseFloat(clean_string(message_bodies_new[i][1]));
									String emotion_valence = "1";
									String emotion_intensity = String.valueOf(PApplet.abs(emotion_number/5f));
									if (emotion_number < 0) emotion_valence = "-1";
									
									String [] new_entry = {"0.2", clean_string(post_ids[i][1]), 
											   clean_string(date_time_string[i][1]), clean_string(date_time_string[i][2]),
											   emotion_valence, emotion_intensity,
											   clean_string(message_bodies_new[i][2]), clean_string(message_bodies_new[i][3]), 
											   clean_string(message_bodies_new[i][4]), clean_string(message_bodies_new[i][5]), 
											   clean_string(message_bodies_new[i][6]), clean_string(message_bodies_new[i][7]), 
											   String.valueOf(countListItems(message_bodies_old[i][3])), clean_string(message_bodies_new[i][8])};

//									if (Element.debug_code) System.out.print("DataProcessor_Journal.readRawData() - NEW ENTRY " + i + " ");
//									if (Element.debug_code) for (int j = 0; j < new_entry.length; j++) System.out.print(new_entry[j] + ", ");
//									if (Element.debug_code) System.out.println();

									this.add_raw_data(new_entry);

								}
							}
						}
					}
				}
				reader.open_next_file();
				current_page = reader.read_full_file();		
			} catch (Exception e) { 
				PApplet.println("DataProcessor_Journal.readRawData() error reading data from current_page var - " + e +", " + e.getMessage()); 
			}
		}

		// reverse the list order so that older items are at the beginning (position 0)
		this.data_list_pre_process = this.reverse_array_order(this.data_list_pre_process);		
		process_raw_data();
//		transformData();
	}
	

	@Override
	/***************************
	 * ADD_RAW_DATA 
	 * takes an array and confirms that the it is of the correct size, before cleaning the contents of each string
	 * and then creating a new data_journal object to add to the data_list_pre_process arraylist.
	 * 
 	 * Input parameters: a String array or a JournalData_New object 
	 * Returns data type: boolean value set to true if ready for next entry, false if it is time to process data
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	protected boolean add_raw_data(String[] data_entry) {

		Element.total_readings++;

		if (data_entry.length >= 14) {
			if (Element.debug_code) System.out.print("DataProcessor_Journal.addRawData() - data entry: ");
			if (Element.debug_code) for (int j = 0; j < data_entry.length; j++) System.out.print(data_entry[j] + ", ");
			if (Element.debug_code) System.out.println();
			
			Element.valid_readings++;																
			for (int i = 0; i < data_entry.length; i++) data_entry[i] = PApplet.trim(data_entry[i]);
			Data_Journal new_journal_entry = new Data_Journal(data_entry);
//			if (Element.debug_code) System.out.println("DataProcessor_Journal.addRawData() - data entry: " + new_journal_entry.getString());
			this.data_list_pre_process.add(new_journal_entry);
			return true;
		}

		return false;
	}

	@Override
	/************************************
	 * PROCESS_RAW_DATA 
	 * This processes the raw data that was downloaded from the web by fixing the time and date, and resorting
	 * the array order. Then it uploads the processed journal data to the "data_raw" database. It also 
	 * updates the dates available database with the new dates for which data is available.
	 * 
	 * Input parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */		
	public void process_raw_data() {
		if (Element.debug_code) { System.out.println("DataProcessor_Journal.processRawData() - processing data"); }
		
		this.fix_time_date();
		this.create_time_date_end();		
		
		this.uploadDataFromDataArrayList("data_raw", data_list_pre_process);
		this.uploadDateAvailableData();
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
		transformHourlyData();
//		transformAvgWeekdayHourlyData();
//		transformAvgOverallHourlyData();
//		transformRelationalData();
	}
	
	/************************************
	 * TRANSFORM_HOURLY_DATA 
	 * This function reads data from the "data_raw" table on the database and processes it to
	 * figure out the average emotion for each hour of each day on which data was collected.
	 * Then it loads the processed data to the "data_hourly" data table. 
	 * 
	 * Input parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */			
	public void transformHourlyData() {
		
		// FUTURE FUNCTIONALITY: download the latest record from the database to determine whether it should be added here
		Data_Journal last_journal_entry = new Data_Journal();
		float last_journal_entry_age = Element.EmotionAgeThresholdHours+1;
		ArrayList<Data> data_list_loaded = new ArrayList<Data>();
		
		if (debug_code) PApplet.println("DataProcess_Journal.transformData() - start data transformation ");
		
		// CREATE HOURLY DATA FOR EACH DAY
		// loop through each date on the date list to process the data
		Iterator<Map.Entry<String, Integer>> date_list_items = date_list.entrySet().iterator();
		while (date_list_items.hasNext()) {
			Map.Entry<String, Integer> map_entry = date_list_items.next();
			String date_key = (String) map_entry.getKey();	 
			Date date_current = new Date(date_key);
			if (debug_code) PApplet.println("DataProcessor_Journal.transformData - process date: " + date_key);
			
			// create an empty data array with an element for each hour of a specific date
			ArrayList<Data> data_for_date_post_process = createDatedTimeBasedList(0, date_current, 60);

			// loop through each element in the array to add the appropriate data
			for (int i = 0; i < data_for_date_post_process.size(); i++) {
				
				// Get one pie emotion object from the array and query database for related JournalData entries
				Data_Journal post_process_data_element = (Data_Journal) data_for_date_post_process.get(i);
				data_list_loaded = load_date_time_range_end(this.database, "data_raw", post_process_data_element.date_stamp, post_process_data_element.time_stamp, post_process_data_element.date_end, post_process_data_element.time_end);				

				if (debug_code) PApplet.println("DataProcessor_Journal.transformData - process data batch number: " + i);
				for(int j = 0; j < data_list_pre_process.size(); j ++) PApplet.println("DataProcessor_Journal.transformData - raw data read from databasae: " + data_list_pre_process.get(j).getString());
				
				// initialize all variables for the calculations
				float current_minute = 60;
				String people_text_remove = "no one,?(\\s)?";
				String people_text_replace = "";
									
				// if there are one or more journal entries from the query then process them
				if (data_list_loaded.size() >  0) { 
					if (debug_code) PApplet.println("DataProcessor_Journal.transformData - data exists from database");
					
					// go through each journal entry and capture data to calculate values for average for each hour
					// start from the end of the hour and work your way back
					for (int k = data_list_loaded.size() - 1; k >= 0 ; k--) {
						Data_Journal pre_process_data_element = (Data_Journal) data_list_loaded.get(k);
						post_process_data_element.data_version = 1.0f;
						post_process_data_element.emotion_valence += (float) (pre_process_data_element.emotion_valence * ((current_minute - pre_process_data_element.time_stamp.minute)/60.0) );
						post_process_data_element.emotion_intensity += (float) (pre_process_data_element.emotion_intensity * ((current_minute - pre_process_data_element.time_stamp.minute)/60.0) );
						post_process_data_element.emotion_1_name = joinLists(post_process_data_element.emotion_1_name, (pre_process_data_element.emotion_2_name + ", " + pre_process_data_element.emotion_3_name));
						post_process_data_element.activity = joinLists(post_process_data_element.activity, pre_process_data_element.activity);
						post_process_data_element.people = joinReplaceLists(post_process_data_element.people, pre_process_data_element.people, people_text_remove, people_text_replace);
						post_process_data_element.people_count = countListItems(post_process_data_element.people);
						post_process_data_element.location = joinLists(post_process_data_element.location, pre_process_data_element.location);
						current_minute = pre_process_data_element.time_stamp.minute;
					}

					// check if the journal entry prior to the ones pulled above is less old than the threshold
					if (last_journal_entry_age < Element.EmotionAgeThresholdHours) {
						post_process_data_element.data_version = 1.0f;
						post_process_data_element.emotion_valence += (float) (last_journal_entry.emotion_valence * (current_minute/60.0));
						post_process_data_element.emotion_valence += (float) (convertEmotionIntensityToFloat(last_journal_entry.emotion_2_name) * ((current_minute - last_journal_entry.time_stamp.minute)/60.0) );
						post_process_data_element.emotion_1_name = joinLists(post_process_data_element.emotion_1_name, (last_journal_entry.emotion_1_name));
						post_process_data_element.activity = joinLists(post_process_data_element.activity, last_journal_entry.activity);
						post_process_data_element.people = joinReplaceLists(post_process_data_element.people, last_journal_entry.people, people_text_remove, people_text_replace);
						post_process_data_element.people_count = countListItems(post_process_data_element.people);
						post_process_data_element.location = joinLists(post_process_data_element.location, last_journal_entry.location);
					}
										
					post_process_data_element.description = "from " + post_process_data_element.time_stamp.get_time_for_sql() + 
								" to " + post_process_data_element.time_end.get_time_for_sql() + "\n" +
								"feeling " + post_process_data_element.emotion_1_name.toLowerCase() + "\n" +
								"doing " + post_process_data_element.activity + "\n" +
								"at " + post_process_data_element.location + "\n" +
								"with " + countListItems(post_process_data_element.people) + " people";

					// set the last element returned by the database as the last_journal entry
					last_journal_entry = new Data_Journal(post_process_data_element);			
					last_journal_entry_age = 0;
										
				} else if (last_journal_entry_age < EmotionAgeThresholdHours) {
					if (debug_code) PApplet.println("DataProcessor_Journal.transformData - use old data for this element ");
					post_process_data_element.data_version = last_journal_entry.data_version;
					post_process_data_element.emotion_valence = last_journal_entry.emotion_valence;
					post_process_data_element.emotion_intensity += last_journal_entry.emotion_intensity;
					post_process_data_element.emotion_1_name = last_journal_entry.emotion_1_name;
					post_process_data_element.activity = last_journal_entry.activity;
					post_process_data_element.people = joinReplaceLists(post_process_data_element.people, last_journal_entry.people, people_text_remove, people_text_replace);
					post_process_data_element.people_count = countListItems(post_process_data_element.people);
					post_process_data_element.location = joinLists(post_process_data_element.location, last_journal_entry.location);
					post_process_data_element.description = "from " + post_process_data_element.time_stamp.get_time_for_sql() + " to " + post_process_data_element.time_end.get_time_for_sql() + "\n" +
							   "feeling " + post_process_data_element.emotion_1_name.toLowerCase() + "\n" +
							   "activities: " + post_process_data_element.activity + "\n" +
							   "location: " + post_process_data_element.location + "\n" +
							   "number of people: " + post_process_data_element.people_count;

				} else {
					if (debug_code) PApplet.println("DataProcessor_Journal.transformData - no data for this element ");
					post_process_data_element.data_version = 1.0f;
					post_process_data_element.emotion_1_name = "unplugged";
					post_process_data_element.description = "from " + post_process_data_element.time_stamp.get_time_for_sql() + " to " + post_process_data_element.time_end.get_time_for_sql();
				}

				// increment the age of the last journal entry by an hour
				last_journal_entry_age++;

				if (debug_code) PApplet.println("DataProcessor_Journal.transformData() - updated hour element " + post_process_data_element.getString());
			}

			// add data from a given day to the overall data_list
			for(int i = 0; i < data_for_date_post_process.size(); i++)
				this.data_list_post_process.add(data_for_date_post_process.get(i));
		}
		
		// upload the Hourly data into the data_hourly database
		this.uploadDataFromDataArrayList("data_hourly", data_list_post_process);

	}

	/************************************
	 * TRANSFORM_AVG_WEEKDAY_HOURLY_DATA 
	 * This function reads data from the "data_raw" table on the database and processes it to
	 * figure out the average emotion for each hour of day or week (i.e. monday, tuesday, etc.).
	 * Then it loads the processed data to the "data_hourly_avg" data table. 
	 * 
	 * Input parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */			
	public void transformAvgWeekdayHourlyData() {		
		ArrayList<Boolean> day_of_week_exists = new ArrayList<Boolean>();
		this.days_of_week_avg = new ArrayList<ArrayList<Data>>();
		for (Integer i = 1; i <= 7; i++) {
			// load data from the data_hourly_avg data table
			ArrayList<Data> weekday = load_query_match(this.database, "data_hourly_avg", "entry_id", "dow_" + i);
			// if the array from the database does not include 24 elements (one for each hour of the day) create a new one
			if (weekday.size() != 24) {
				day_of_week_exists.add(false);
				weekday = createTimeBasedList(0, new Time("00:00:00"), new Time("23:59:59"), 60);
				for (int j = 0; j < weekday.size(); j++) {	// properly initialize the seconds_measured variables in the new array
					Data_Journal weekday_hours = (Data_Journal)weekday.get(j);
					weekday_hours.seconds_measured = 0;
					weekday_hours.data_version = 2.0f;
					weekday_hours.entry_id = "dow_"+ i;
				}
			} 
			else day_of_week_exists.add(true);
			this.days_of_week_avg.add(weekday);
			if (debug_code) PApplet.println("DataProcessor_Journal.transformAvgHourlyData() - day of week: " + i + " boolean " + day_of_week_exists.get(i-1)); 
		}
		
		// pieces of data to be transferred
		for (int i = 0; i < this.data_list_post_process.size(); i ++) {
			Data_Journal current_record = (Data_Journal) this.data_list_post_process.get(i);
			if(!current_record.emotion_1_name.equals("unplugged")) {
				ArrayList<Data> weekday = this.days_of_week_avg.get(current_record.date_stamp.get_day_of_week()-1);
				for (int j = 0; j < weekday.size(); j++) {
					Data_Journal weekday_avg_hour = (Data_Journal) weekday.get(j);
					if (weekday_avg_hour.time_stamp.equals(current_record.time_stamp)) {
						weekday_avg_hour.seconds_measured += current_record.seconds_measured;
						float weighted_avg_multiplier = current_record.seconds_measured / weekday_avg_hour.seconds_measured;
						weekday_avg_hour.emotion_valence = (float)current_record.emotion_valence * (float)weighted_avg_multiplier + (weekday_avg_hour.emotion_valence*(1-weighted_avg_multiplier));
						weekday_avg_hour.emotion_intensity = (float)current_record.emotion_intensity * (float)weighted_avg_multiplier + (weekday_avg_hour.emotion_intensity*(1-weighted_avg_multiplier));
						weekday_avg_hour.description = joinLists(current_record.emotion_1_name, weekday_avg_hour.description);
						if (debug_code) PApplet.println("DataProcessor_Journal.transformAvgHourlyData() - emotion description " + weekday_avg_hour.description); 
					}
				}
			}			
		}

		//	add this value to the emotion_valence and emotion_intensity of the day_of_week_record
		for (int i = 0; i < days_of_week_avg.size(); i++) {
			ArrayList<Data> weekday = days_of_week_avg.get(i);
			if (day_of_week_exists.get(i)) updateDataFromArrayList("data_hourly_avg", weekday);
			else uploadDataFromDataArrayList("data_hourly_avg", weekday);
		}
		
	}

	/************************************
	 * TRANSFORM_AVG_OVERALL_HOURLY_DATA 
	 * This function reads data from the "data_raw" table on the database and processes it to
	 * figure out the overall average emotion for each hour of all days on which data was 
	 * collected. Then it loads the processed data to the "data_hourly_avg" data table. 
	 * 
	 * Input parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */			
	public void transformAvgOverallHourlyData() {
		boolean current_data_exists = true;
		ArrayList<Data> database_data = load_query_match(this.database, "data_hourly_avg", "entry_id", "overall");		
		if (database_data.size() != 24) {
			current_data_exists = false;
			database_data = createTimeBasedList(0, new Time("00:00:00"), new Time("23:59:59"), 60);	
			for (int j = 0; j < database_data.size(); j++) {	// properly initialize the seconds_measured variables in the new array
				Data_Journal weekday_hours = (Data_Journal)database_data.get(j);
				weekday_hours.seconds_measured = 0;
				weekday_hours.data_version = 2.0f;
				weekday_hours.entry_id = "overall";
			}
		} 
		
		for (int i = 0; i < this.data_list_post_process.size(); i ++) {
			Data_Journal current_record = (Data_Journal) this.data_list_post_process.get(i);
			if(!current_record.emotion_1_name.equals("unplugged")) {
				for (int j = 0; j < database_data.size(); j++) {
					Data_Journal database_data_hour = (Data_Journal) database_data.get(j);
					if (database_data_hour.time_stamp.equals(current_record.time_stamp)) {
						database_data_hour.seconds_measured += current_record.seconds_measured;
						float weighted_avg_multiplier = current_record.seconds_measured / database_data_hour.seconds_measured;
						database_data_hour.emotion_valence = (float)current_record.emotion_valence * (float)weighted_avg_multiplier + (database_data_hour.emotion_valence*(1-weighted_avg_multiplier));
						database_data_hour.emotion_intensity = (float)current_record.emotion_intensity * (float)weighted_avg_multiplier + (database_data_hour.emotion_intensity*(1-weighted_avg_multiplier));						
						database_data_hour.description = joinLists(current_record.emotion_1_name, database_data_hour.description);
						if (debug_code) PApplet.println("DataProcessor_Journal.updateHourlyAvgData() - emotion description " + database_data_hour.description); 
					}
				}
			}			
		}
		
		if (current_data_exists) updateDataFromArrayList("data_hourly_avg", database_data);
		else uploadDataFromDataArrayList("data_hourly_avg", database_data);
	}
	
	/************************************
	 * TRANSFORM_RELATIONAL_DATA 
	 * This function reads data from the "data_raw" table on the database and processes it to
	 * figure out how strongly related the emotion categories are to each activity, person, and 
	 * place being tracked in the journal. Then it loads the processed data to the 
	 * "data_relational" data table. 
	 * 
	 * Input parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */			
	public void transformRelationalData() {

		for (int i = 0; i < data_list_pre_process.size(); i++) {

			ArrayList<String[]> database_fields_key = load_query_match_string(this.database, "ctrl_cat_list", "", "");

			ArrayList<String> database_fields = load_field_names(this.database, Element.databases.get(this.database), "data_relational");
			Data_Journal new_record = (Data_Journal) data_list_pre_process.get(i);
			String[] new_record_attributes = new_record.getRelationalAttributeStringArray();
			
			if (debug_code) PApplet.print("DataProcessor_Journal.transformRelationalData() - new_record_attributes PRE - length: " + new_record_attributes.length + " content: "); 
			if (debug_code) for (int j = 0; j < new_record_attributes.length; j++) PApplet.print(new_record_attributes[j] + ", "); 
			if (debug_code) PApplet.println();	

			for (int k = 0; k < new_record_attributes.length; k++) {
				for (int j = 0; j < database_fields_key.size(); j++) 
					if (database_fields_key.get(j)[2].equals(new_record_attributes[k])) new_record_attributes[k] = database_fields_key.get(j)[1];
			}
			
			if (debug_code) PApplet.print("DataProcessor_Journal.transformRelationalData() - new_record_attributes POST - length: " + new_record_attributes.length + " content: "); 
			if (debug_code) for (int j = 0; j < new_record_attributes.length; j++) PApplet.print(new_record_attributes[j] + ", "); 
			if (debug_code) PApplet.println();	
			
			for (int j = 0; j < new_record_attributes.length; j++) {				
				// load the record from the database for this attribute
				ArrayList<String[]> database_record_holder = load_query_match_string(this.database, "data_relational", "category_name", new_record_attributes[j]);

				// confirm that database_data contains a single String[] that features the name of the current attribute on the first location 
				if (database_record_holder.size() == 1) {
					String[] database_record = database_record_holder.get(0);
					
					if (database_record[0].equals(new_record_attributes[j]) && database_record.length == database_fields.size()) {

						// update the number of seconds_measured for this category (located on second position of array)
						int total_seconds = Integer.parseInt(database_record[1]);
						database_record[1] = String.valueOf(total_seconds + new_record.seconds_measured);
						if (debug_code) PApplet.println("DataProcessor_Journal.transformRelationalData() - main record seconds " + new_record_attributes[j]); 
						
						// loop through all the attributes from this entry to calculate relationships
						for (int k = 0; k < new_record_attributes.length; k++) {
							// if the new_attribute is the same as the main relationship_attribute, then skip to next new_attribute
							if (k == j) ;		

							// otherwise, go through the relationship_attributes to update the data appropriately
							else for (int l = 2; l < database_record.length; l++) {
								String current_field = database_fields.get(l);
								if (current_field.equals(new_record_attributes[k])) {
									int updated_seconds = 0;
									if (database_record[l] != null) 
										if (database_record[l] != "0" && !database_record[l].equals("0")) updated_seconds = Integer.parseInt(database_record[l]);
									database_record[l] = String.valueOf(updated_seconds + new_record.seconds_measured);

									if (debug_code) PApplet.println("DataProcessor_Journal.transformRelationalData() - match found - sub record, name: " + current_field + " time: "  + database_record[l] ); 									
									break;
								}							
							}			
						}
					}
					
					// UPDATE RECORD ON DATABASE
					ArrayList<String> relationship_attributes_upload = new ArrayList<String>();
					for (int n = 0; n < database_record.length; n++) relationship_attributes_upload.add(database_record[n]);
					updateDataFromStringArrays("data_relational", relationship_attributes_upload.get(0), database_fields.get(0), relationship_attributes_upload, database_fields);
					
				}
			}
		}
				
				

	}
		
//	/************************************
//	 * UPDATE_DATA_FROM_ARRAYLIST 
//	 * Updates data into existing records on a specified mySql datatable using data from an array 
//	 * list of data objects. This function uploads to the database that is registered with this object. 
//	 * 
//	 * Input parameters: datatable_name holds the name of the datatable where the data will be inserted. 
//	 * 					 data_list is the arraylist that holds <Data> objects, which will be inserted into the datatable.
//	 * Functions to call before: n/a
//	 * Functions to call after: n/a
//	 * Notes: only supports ArrayList that hold JournalData_New objects.
//	 * 
//	 */		
//	public void updateDataFromArrayList(String datatable_name, ArrayList<Data> data_list) {
//		for (int i =  0; i < data_list.size(); i++) {
//			String insert_format = "UPDATE " + datatable_name + "\n";
//			String insert_data = "";
//			if (data_list.get(i) instanceof Data_Journal) {
//				Data_Journal current_reading = (Data_Journal) data_list.get(i);				
//				insert_data = "SET" + current_reading.getSQLUpdateSetString(datatable_name) +
//							  "WHERE " + current_reading.getSQLUpdateQueryString(datatable_name);
//				if (Element.debug_code) { System.out.println("DataProcessor_Journal.updateDataFromArrayList() - upload data query " + insert_format + insert_data); }
//				if(database.connection != null && Element.load_data) database.execute(insert_format + insert_data);
//			} 
//		}
//	}
//	
//	
//	/************************************
//	 * UPLOAD_DATA_FROM_ARRAYLIST 
//	 * Uploads new data to a specified mySql datatable using data from an array list of data objects. 
//	 * This function uploads to the database that is registered with this object. 
//	 * 
//	 * Input parameters: datatable_name holds the name of the datatable where the data will be inserted. data_list is the
//	 * 					 arraylist that holds <Data> objects, which will be inserted into the datatable.
//	 * Functions to call before: n/a
//	 * Functions to call after: n/a
//	 * Notes: only supports ArrayList that hold JournalData_New objects.
//	 * 
//	 */		
//	public void uploadDataFromDataArrayList(String datatable_name, ArrayList<Data> data_list) {
//		for (int i =  0; i < data_list.size(); i++) {
//			String insert_data = "";
//			String insert_format = "INSERT INTO " + datatable_name + "\n";
//			if (data_list.get(i) instanceof Data_Journal) {
//				Data_Journal current_reading = (Data_Journal) data_list.get(i);				
//				insert_data = "VALUES" + current_reading.getSQLInsertString(); 
//				if (Element.debug_code) { System.out.println("DataProcessor_Journal.uploadDataFromArrayList() - upload data query " + insert_format + insert_data); }
//				if(database.connection != null && Element.load_data) database.execute(insert_format + insert_data);
//			} 
//		}
//	}
	
	
	/************************************
	 * UPDATE_DATA_FROM_STRING_ARRAYS 
	 * Updates data into existing records on a specified mySql datatable using two separate string 
	 * ArrayLists - one holding the field names, the other holding the values. This functions also
	 * accepts a field name and value for the query.  This function uploads to the database that is 
	 * registered with this object. 
	 * 
	 * Input parameters: datatable_name (String)
	 * 					 query_value (String)
	 * 					 query_field (String)
	 * 					 value_list (Arraylist<String)
	 * 					 field_list (Arraylist<String)
	 * 
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * Notes: only supports ArrayList that hold JournalData_New objects.
	 * 
	 */		
	public void updateDataFromStringArrays(String datatable_name, String query_value, String query_field, ArrayList<String> value_list, ArrayList<String> field_list) {
		String insert_format = "UPDATE " + datatable_name + "\n";
		String insert_data = "SET ";
		for (int i =  0; i < field_list.size() && i < value_list.size(); i++) {
			if (value_list.get(i) != null && !value_list.get(i).equals(query_value)) {
				if (insert_data.length() > 4) insert_data += ", ";
				insert_data += field_list.get(i) + "=\'" + value_list.get(i) + "\' ";
			}
		}
		insert_data += "\nWHERE " + query_field + "=\'" + query_value + "\'";
		if (Element.debug_code) { System.out.println("DataProcessor_Journal.updateDataFromStringArrays() - upload data query " + insert_format + insert_data); }
		if(database.connection != null && Element.load_data) database.execute(insert_format + insert_data);
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
		// fix the date and time readings on 
		if (Element.debug_code) System.out.println("DataProcessor_Mobile.fix_time_date(): data readings " + data_list_pre_process.size());		
		for (int i = this.data_list_pre_process.size() - 1; i >= 0; i--) {
			Data_Journal active_reading = (Data_Journal) this.data_list_pre_process.get(i);			
			if (Element.debug_code) System.out.println("DataProcessor_Mobile.fix_time_date(): old date " + active_reading.date_stamp.get_date_for_sql()+ " time " + active_reading.time_stamp.get_time_for_sql());		
			int date_adjust = active_reading.time_stamp.update_hours(convert_time); 
			if (Element.debug_code) System.out.println("DataProcessor_Mobile.fix_time_date(): date adjust " + date_adjust + " convert_time " + convert_time +
														" updated date " + active_reading.date_stamp.get_date_for_sql() + " time " + active_reading.time_stamp.get_time_for_sql());		
			active_reading.date_stamp.update_day(date_adjust); 
		}
	}
	
	/************************************
	 * CREAT_TIME_DATE_END 
	 * This method calculates the end time and date for each entry.The end time is calculated using the following logic: 
	 * each emotion entry has a maximum duration that is specified by the variable EmotionAgeThresholdSeconds and
	 * EmotionAgeThresholdHours. Therefore, each emotion will last for the length of the threshold if no other emotion 
	 * entries are made sooner than that. 
	 * 
	 * Inputs parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	void create_time_date_end() {

		// add the end date and time to each reading 
		for (int i = 0; i < this.data_list_pre_process.size(); i++) {
			Data_Journal current_record = (Data_Journal) this.data_list_pre_process.get(i);
			if (Element.debug_code) PApplet.println("DataProcessor_Journal.create_time_date_end() - current record START date : "  + current_record.date_stamp.get_date_for_sql() + ", start time = " + current_record.time_stamp.get_time_for_sql());	

			// if this is not the last element in the array check the next entry to see if it within the expected timeframe
			if (i < this.data_list_pre_process.size()-1) {
				Data_Journal next_record = new Data_Journal((Data_Journal) this.data_list_pre_process.get(i+1));

				if (Element.debug_code) PApplet.println("DataProcessor_Journal.create_time_date_end() - current record TIME dif : " + Time.calculate_time_dif_seconds(current_record.time_stamp, next_record.time_stamp) + " start " + current_record.time_stamp.get_time_for_sql() + " end " + next_record.time_stamp.get_time_for_sql());	

				// if a new entry is made before the time threshold then set the end of this entry based on the start of the next one
				if (Time.calculate_time_dif_seconds(current_record.time_stamp, next_record.time_stamp) < Element.EmotionAgeThresholdSeconds) {
					if (Element.debug_code) PApplet.println("DataProcessor_Journal.create_time_date_end() - creating from next record");	
					current_record.setEndDateTime(next_record.date_stamp, next_record.time_stamp);

				// if a no new entry is made before the threshold time set the end time and date based on the maximum time window
				} else {
					if (Element.debug_code) PApplet.println("DataProcessor_Journal.create_time_date_end() - creating from threshold (nxt too far)");	
					next_record.date_end = new Date (current_record.date_stamp);
					next_record.time_end = new Time (current_record.time_stamp);
					next_record.date_end.update_day(next_record.time_end.update_seconds((int)Element.EmotionAgeThresholdSeconds));					
					current_record.setEndDateTime(next_record.date_end, next_record.time_end);
				}

			// if this is the last element on the list, or if the next element on the list is more than 2 hours away then set the time appropriately
			} else { 	
				if (Element.debug_code) PApplet.println("DataProcessor_Journal.create_time_date_end() - creating from threshold (no nxt)");	
				Data_Journal next_record = new Data_Journal();
				next_record.date_end = new Date (current_record.date_stamp);
				next_record.time_end = new Time (current_record.time_stamp);
				next_record.date_end.update_day(next_record.time_end.update_seconds((int)Element.EmotionAgeThresholdSeconds));					
				current_record.setEndDateTime(next_record.date_end, next_record.time_end);
			}

			// add dates to the date list after they have been fixed
			add_to_date_list(current_record.date_stamp);
			add_to_date_list(current_record.date_end);
		}
	}	

	
	/************************************
	 * FIX_ARRAY_ORDER 
	 * This method fixes the order of the data_list_pre_process array. It reverses the order so
	 * that items are uploaded into the database in the correct sequence. 
	 * 
	 * Input parameters: n/a
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	protected ArrayList<Data> reverse_array_order(ArrayList<Data> data_list) {
		ArrayList<Data> temp_list = new ArrayList<Data>();
		for (int i = data_list.size() - 1; i >= 0; i--) { temp_list.add(data_list.get(i)); }
		if (Element.debug_code) PApplet.println("DataProcessor_Journal.fix_array_order() - array order reversed");	
		return temp_list;
	}	

	/************************************
	 * PRINT_ARRAY 
	 * This method prints a double array to the console. 
	 * 
	 * Input parameters: double array of strings.
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public void printArray(String[][] string_array) {
		if (string_array == null) {
			PApplet.println("Null String");
			return;
		}
		for (int i = 0; i < string_array.length; i++) {
			for (int j = 0; j < string_array[i].length; j++) {
				if (j != 0) PApplet.print(i + " " + j + " : " + string_array[i][j] + ", ");
				PApplet.println();
			}
		}		
	}

	
	/************************************
	 * STATIC METHODS
	 * various static methods used to clean raw strings of unicode characters and to 
	 * convert emotion string data into valence and intensity numbers and vice versa.
	 * 
	 */

	/************************************
	 * CLEAN_STRING 
	 * This methods cleans a string by removing unicode characters, which are commonly found in data
	 * downloaded via web APIs (such as the posterous one). It then returns the clean string.
	 * 
	 * Input parameters: input_string (String) holds the string that will be cleaned
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public static String clean_string(String input_string) {
		String [] clean_elements ={"\\u003C/p", "\\u003E", "/", "\\r\\n", "\\u0026nbsp;", "\\u003Cp", "\\u003Cdiv", "\\u003Cbr", "\\u003Cspan", "style=\\\"\\\""};
		String [] old_elements = {"'"};
		String [] new_elements = {"\'"};
//		String [] new_elements = {"\\\'"};
		input_string = PApplet.trim(input_string);
		for (int i = 0; i < clean_elements.length; i++)
			input_string = input_string.replace(clean_elements[i], "");
		for (int j = 0; j < old_elements.length; j++)
			input_string = input_string.replace(old_elements[j], new_elements[j]);		
		return PApplet.trim(input_string.toLowerCase());
	}

	/************************************
	 * PREP_STRING_UPLOAD 
	 * This methods prepares a string for uploading into a database by making sure that proper escape
	 * characters are added to where ever is appropriate.
	 * 
	 * Input parameters: input_string (String) holds the string that will be cleaned
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public static String prep_string_upload(String input_string) {
		if (input_string != null) {
			String [] old_elements = {"'"};
			String [] new_elements = {"\\\'"};
			input_string = PApplet.trim(input_string);
			for (int j = 0; j < old_elements.length; j++)
				input_string = input_string.replace(old_elements[j], new_elements[j]);		
			return PApplet.trim(input_string);
		}
		return null;
	}


	/************************************
	 * CONVERT_EMOTION_TO_STRING 
	 * This methods converts an emotion_valence number into a string. If the value it receives is greater that 0 
	 * then it returns "positive", if the value is less than 0 then it returns "negative".
	 * 
	 * Input parameters: emotion_number (float)
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public static String convertEmotionToString(float emotion_number) {
		if(emotion_number >= 0) return "positive";
		else if (emotion_number < 0) return "negative";			
		return "";
	}


	/************************************
	 * CONVERT_EMOTION_INTENSITY_TO_STRING 
	 * This methods converts an emotion_intensity number into a string. If the value it receives is less than 1 
	 * then it returns "low", else if the value is less than "2" then it returns "medium", else if the value is 
	 * less than "3" then it returns "elevated", otherwise it returns "high".
	 * 
	 * Input parameters: emotion_number (float)
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public static String convertEmotionIntensityToString(float emotion_intensity) {
		if (emotion_intensity < 1.0) return "low";
		else if (emotion_intensity < 2.0) return "medium";
		else if (emotion_intensity < 3.0) return "elevated";
		else return "high";	
	}
	
	/************************************
	 * CONVERT_EMOTION_TO_FLOAT 
	 * This methods converts an emotion_valence string into a float value. If the string it receives contains
	 * "positive" then it returns 1, if the string it receives contains "negative" then it returns 0.
	 * 
	 * Input parameters: emotion_number (float)
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public static float convertEmotionToFloat(String emotion_type) {
		emotion_type = emotion_type.toLowerCase();
		if(emotion_type.contains("positive")) return 1;
		else if (emotion_type.contains("negative")) return -1;			
		return 0;
	}

	/************************************
	 * CONVERT_EMOTION_INTENSITY_TO_FLOAT 
	 * This methods converts an emotion_intensity string into a float value. Depending on the contents of
	 *  the string it receives it will return a value between 0 and 1.
	 * 
	 * Input parameters: emotion_number (float)
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public static float convertEmotionIntensityToFloat(String emotion_type) {
		emotion_type = PApplet.trim(emotion_type.toLowerCase());
		if(emotion_type.contains("forceful") || emotion_type.contains("lively")) return 1.0f;
		else if(emotion_type.contains("control") || emotion_type.contains("caring")) return 0.75f;
		else if(emotion_type.contains("agitation") || emotion_type.contains("reactive")) return 0.5f;
		else if(emotion_type.contains("thought")) return 0.5f;
		else if(emotion_type.contains("passive") || emotion_type.contains("quiet") || emotion_type.contains("quite")) return 0.25f;
		PApplet.println("DataProcessor_Journal.convertEmotionIntensityToFloat() - conversion issue, emotion type " + emotion_type);
		return 0.0f;
	}
	
}

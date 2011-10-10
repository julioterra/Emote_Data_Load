package com.julioterra.moodyjulio.dataload.basicelements;

import java.util.ArrayList;
import java.util.HashMap;

import com.julioterra.moodyjulio.dataload.datahandlers.DataProcessor;
import de.bezier.data.sql.MySQL;
import processing.core.PApplet;

public class Element {

	public static PApplet 			processing_app;
	public static MySQL 			database_journal;
	public static MySQL 			database_physio;
	public static MySQL 			database_log;
	public static HashMap<MySQL, String> databases = new HashMap<MySQL, String>();

	public static DataProcessor 	data_processor;

	public static int		data_source = 		0;
	static final public int		mobile = 			1;
	static final public int		journal = 			2;
	
	public static boolean 	debug_code = 		true;
	public static boolean 	auto_read = 		true;
	public static boolean 	read_data = 		true;	
	public static boolean 	load_data = 		false;	

	public static float 	EmotionAgeThresholdHours = 2f;
	public static float 	EmotionAgeThresholdSeconds = EmotionAgeThresholdHours * 60 * 60;
	public static String	active_database		= "JournalData";
	
	public static String 	file_name_current = 		"";
	public static String 	file_name_short = 	"";
	public static boolean 	file_active;
	public static boolean 	file_read;
	public static boolean 	file_completed;
	public static int 		total_readings;
	public static int 		valid_readings;	
	public static int 		data_batch;

	/*******************************************
	 ** CALENDAR RELATED CONSTANTS
	 *******************************************/
	public final static String[]	Sunday = {"2011/02/27", "2011/03/05"};
	public final static String[]	Monday = {"2011/02/28", "2011/03/06"};
	public final static String[]	Tuesday = {"2011/02/23", "2011/03/01"};
	public final static String[]	Wednesday = {"2011/02/23", "2011/03/02"};
	public final static String[]	Thursday = {"2011/02/24", "2011/03/03"};
	public final static String[]	Friday = {"2011/02/25", "2011/03/04"};
	public final static String[]	Saturday = {"2011/02/26", "2010/11/27"};
	public final static String[]	AllWeeks = {	"2011/02/22","2011/02/23", "2011/02/24",
													"2011/02/25", "2011/02/26", "2011/02/27", 
													"2011/02/28", "2011/03/01", "2011/03/02", 
													"2011/03/03", "2011/03/04", "2011/03/05",
													"2011/03/06"};

//	public final static String[]	Sunday = {"2010/11/21", "2010/11/28"};
//	public final static String[]	Monday = {"2010/11/22", "2010/11/29"};
//	public final static String[]	Tuesday = {"2010/11/23", "2010/11/30"};
//	public final static String[]	Wednesday = {"2010/11/17", "2010/11/24", "2010/12/01"};
//	public final static String[]	Thursday = {"2010/11/18", "2010/11/25", "2010/12/02"};
//	public final static String[]	Friday = {"2010/11/19", "2010/11/26", "2010/12/03"};
//	public final static String[]	Saturday = {"2010/11/20", "2010/11/27", "2010/12/04"};
//	public final static String[]	AllWeeks = {	"2010/11/17", "2010/11/18", "2010/11/19",
//													"2010/11/20", "2010/11/21", "2010/11/22",
//													"2010/11/23", "2010/11/24", "2010/11/25",
//													"2010/11/26", "2010/11/27", "2010/11/28",
//													"2010/11/29", "2010/11/30",
//													"2010/12/01", "2010/12/02",
//													"2010/12/03", "2010/12/04"};

	public final static ArrayList<String[]>	DaysOfWeek = new ArrayList<String[]>();	
	public final static String[]	NamesOfDays = {"Sunday", "Monday" , "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Overall", "Dubstep", "Reggae", "Indie Dance", "Ambient", "Eighties"};
	public final static String[]	NamesOfMonths = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	public final static String[]	NamesOfMonthsShort = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

	public final static String[]	Dubstep = {"2011/02/21", "2011/02/26", "2011/03/03"};
	public final static String[]	Reggae = {"2011/02/22", "2011/02/27", "2011/03/04"};
	public final static String[]	Indie = {"2011/02/23", "2011/02/28", "2011/03/05"};
	public final static String[]	Ambient = {"2011/02/24", "2011/03/01", "2011/03/06"};
	public final static String[]	Eighties = {"2011/02/25", "2011/03/02", "2011/03/07"};

	
	
	public static void application_init (PApplet processing_app) {
		Element.processing_app = processing_app; 
		Element.database_connect();
	}

	public static void database_connect () {
		Element.database_journal = new MySQL(processing_app, "localhost:8889", "EmotionTracker_Journal", "root", "root" );
		Element.database_physio = new MySQL(processing_app, "localhost:8889", "EmotionTracker_Physio", "root", "root" );
		Element.database_log = new MySQL(processing_app, "localhost:8889", "EmotionTracker_Log", "root", "root" );

		Element.database_journal.connect();
		Element.database_physio.connect();
		Element.database_log.connect();
		
		Element.databases.put(database_journal, "EmotionTracker_Journal");
		Element.databases.put(database_physio, "EmotionTracker_Physio");
		Element.databases.put(database_log, "EmotionTracker_Log");
		
	}
	
	public static void updated_data_source(int source_number) {
		Element.data_source = source_number;
		if (Element.data_source == Element.mobile) Element.active_database = "MobileData";
		else if (Element.data_source == Element.journal) Element.active_database = "JournalData";
	}
	
}

package com.julioterra.moodyjulio.dataload.datahandlers;

import com.julioterra.moodyjulio.dataload.basicelements.Element;
import com.julioterra.moodyjulio.dataload.data.Data;

public class DataReader extends Element{

	    protected String file_name_base; 
	    protected String file_name_active;
		protected String[] file_params;
		protected int file_num;
		public boolean file_is_registered;
		public boolean new_file;

		protected String username;
		protected String password;
		public boolean login_is_registered;
		public boolean ready_to_read;

		public DataProcessor data_processor;
		public Data first_data_entry;
			
		/*********
		 * CONSTRUCTOR
		 *********/

		public DataReader() {
			this.file_num = 0;
			this.file_is_registered = false;
			this.login_is_registered = false;
			this.ready_to_read = false;
			
			// initialize variables for reading data files
			Element.file_completed = 		false;
			Element.file_active = 			false;
			Element.file_read = 			false;
			Element.data_batch = 			0;
			Element.total_readings = 		0;
			Element.valid_readings = 		0;
			Element.file_name_current = 	"";
			Element.file_name_short = 		"";

			this.data_processor = new DataProcessor();			
		}
			

		public void register_file_name(String file_name_base, String[] file_params) {
		}
		
		public void register_username_password(String username, String password) {
		}
		
		public boolean open_file(String filename) {		
			return false;
		}

		public boolean open_next_file() {
			return false;
		}

		public String read_full_file() {
			return null;
		}

		public String read_partial_file(int number_of_lines) {
			return null;
		}
		
		protected void open_file_init() {
			Element.data_processor = 		this.data_processor;		
			Element.file_active = 			true;
			Element.file_completed = 		false;
			Element.file_read = 			true;
			Element.data_batch = 			0;
			Element.total_readings = 		0;
			Element.valid_readings = 		0;
		}
		public boolean read_file_old() {
			return true;
		}
		
		public void read_and_process() {
			// if file is active then continue loading data
			if (Element.file_active) {
				Element.file_active = this.read_file_old();
				// if file has been set to inactive then set file_completed to true
				if (!Element.file_active) Element.file_completed = true;
			}		
		}	
				

}

package com.julioterra.moodyjulio.dataload.datahandlers;

import java.io.BufferedReader;
import java.io.FileReader;
import processing.core.PApplet;
import com.julioterra.moodyjulio.dataload.basicelements.Element;

public class DataReader_BigFile extends DataReader {
		
	private BufferedReader file2read;
	int file_num_placeholders;
	String file_extension;
	int last_valid_file_num;

	public String file_num_seq;

	/******************************
	 ** CONSTRUCTOR
	 **/

	public DataReader_BigFile() {
		super();
		this.file_extension = "";
		this.file_num_placeholders = 0;
		this.file_num_seq = "";
		this.last_valid_file_num = this.file_num;
		this.new_file = false;
	} 
			
	@Override
	/************************************
	 * REGISTER_FILE_NAME()
	 * This method is used to set file_name parameters when using sequentially numbered files.
	 * For the DataReader_BigFile class the file_params argument needs to be a string array
	 * with two element. The first element needs to be transformable into a number, the second
	 * is the file extension for the files to be read (i.e. '.txt', '.csv').
	 *  
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public void register_file_name(String file_name_base, String[] file_params) {
		this.file_name_base = file_name_base;
		if (file_params.length == 3) {
			try { 
				this.file_num = Integer.parseInt(file_params[0]); 
				this.file_extension = file_params[1];
				this.file_num_placeholders = Integer.parseInt(file_params[2]);
			} 
			catch (NumberFormatException e) {
				System.out.println("DataReader_BigFile.register_file_name() registered file data cannot be converted into number " + file_params[0]);
			}
		}
		this.file_is_registered = true;
		if (Element.debug_code) System.out.println("DataReader_BigFile.register_file_name() - file name and params registered");		
	}
	
	@Override
	/************************************
	 * OPEN_NEXT_FILE()
	 * This method is used to open the next file in a group of sequentially labeled files. 
	 *  
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public boolean open_next_file() {
		if (this.file_is_registered) {
			this.file_num_seq = convert_seq_number(this.file_num);
			this.file_name_active = this.file_name_base + this.file_num_seq + "." + this.file_extension;

			if (Element.debug_code) System.out.println("DataReader_BigFile.open_next_file() - Creating New File Name " + this.file_name_active);		
			Element.file_name_current = this.file_name_active;
			String [] temp_string = PApplet.split(Element.file_name_current, '/');
			Element.file_name_short = temp_string[(temp_string.length-1)];

			this.file_num++;
			return open_file(this.file_name_active);	
		}
		return false;
	}
	
	/************************************
	 * OPEN_FILE_CHOOSER()
	 * This method is used to open a file using a dialog window. 
	 *  
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public void open_file_chooser() {		
		this.file_name_active = processing_app.selectInput();
		if (this.file_name_active != null) open_file(this.file_name_active);	
		else PApplet.println("\n\nNo file was selected...");

		Element.file_name_current = this.file_name_active;
		String [] temp_string = PApplet.split(Element.file_name_current, '/');
		Element.file_name_short = temp_string[(temp_string.length-1)];
	}
	
	@Override
	/************************************
	 * OPEN_FILE()
	 * This method is used to open a file from a location specified via a string variable.
	 *  
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public boolean open_file(String filename) {		
		this.open_file_init();	

		// Load file into a Buffered Reader using a try and catch to avoid errors
		try { 
			file2read = new BufferedReader (new FileReader(filename)); 
			this.last_valid_file_num = this.file_num-1;
			DataProcessor_Mobile.init_gsr_high_pass();
			if (Element.debug_code) System.out.println("DataReader_BigFile.open_file() Successfully openned file named: " + filename);		
			this.new_file = true;
			return true;
		} 
		catch(Exception e) {
			Element.file_active = 	false;
			Element.file_read = 	false;
			if (Element.debug_code) System.out.println("DataReader_BigFile.open_file() Error opening file named: " + filename + " -- more information: "+ e.getMessage()); 
			this.new_file = false;
		}
		return false;
	}

	@Override
	/************************************
	 * READ_PARTIAL_FILE()
	 * This method is used to read data from an part of a file. This instance of this method reads
	 * data until it encounters 6 different time readings. It then returns the contents that have been
	 * read so far from the file as a String. This method will return a null when the file is complete.
	 *  
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public String read_partial_file(int number_of_lines) {
		String filePart = "";
		String newLine = "";

		// if read_status is set to true then read data from the file
		if (Element.file_read && Element.file_active) {
			try {
				int while_loops = 0;
				while(while_loops < number_of_lines) {
					newLine = file2read.readLine();
					if (newLine == null) break;
					if (while_loops == 0) filePart = newLine;
					else filePart += "\n" + newLine;
					while_loops++;
				}
				if (Element.debug_code) System.out.println("DataReader_BigFile.read_partial_file(): file part loaded ");
				this.new_file = false;
				return filePart;
			} catch(Exception e) { 
				System.out.println("DataReader_BigFile.read_partial_file(): error reading file - more info: " + e +" - " + e.getMessage() + filePart);
			}
		}
		return null;
	}

	
	@Override
	/************************************
	 * READ_FULL_FILE()
	 * This method is used to read data from the full file. This methods should only be used when processing
	 * smaller files, as the application may crash if the file is too large (i.e. more than 50,000 or 100,000 lines).
	 *  
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public String read_full_file() {
		return read_partial_file(Integer.MAX_VALUE);
	}
	
	/************************************
	 * CONVERT_SEQ_NUMBER()
	 * This method takes a number and converts it into a string with the appropriate number of leading
	 * zeros based on the file_num_placeholders variable. You pass an integer as an argument and it returns
	 * a properly formatted string.
	 *  
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	protected String convert_seq_number(int number) {
		String number_text = String.valueOf(number);
		int diff = file_num_placeholders - number_text.length();
		if (diff > 0) 
			for (int i = 0; i < diff; i++) number_text = "0" + number_text;
		return number_text;
	}
	
}

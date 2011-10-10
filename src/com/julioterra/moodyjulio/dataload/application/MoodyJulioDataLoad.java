package com.julioterra.moodyjulio.dataload.application;

import com.julioterra.moodyjulio.dataload.basicelements.Date;
import com.julioterra.moodyjulio.dataload.basicelements.Element;
import com.julioterra.moodyjulio.dataload.basicelements.Time;
import com.julioterra.moodyjulio.dataload.datahandlers.DataProcessor;
import com.julioterra.moodyjulio.dataload.datahandlers.DataProcessor_Log;
import com.julioterra.moodyjulio.dataload.datahandlers.DataProcessor_Mobile;
import com.julioterra.moodyjulio.dataload.datahandlers.DataProcessor_Journal;
import com.julioterra.moodyjulio.dataload.view.Display;
import processing.core.PApplet;


@SuppressWarnings("serial")
public class MoodyJulioDataLoad extends PApplet {

	public Display display;
	public DataProcessor_Journal journal_processor;
	public DataProcessor_Mobile mobile_processor;
	public DataProcessor_Log log_processor;
	int previous_source = -1;
	
	/**********************************
	 ** START - STANDARD PROCESSING FUNCTIONS (set-up and draw
	 **/

	// SET-UP FUNCTION: Read data from file, load and analyze
	@Override
	public void setup() {
		Element.application_init(this);
		this.journal_processor = new DataProcessor_Journal();
		this.journal_processor.register_data_source(Element.database_journal);
		this.mobile_processor = new DataProcessor_Mobile();
		this.mobile_processor.register_data_source(Element.database_physio);
		this.log_processor = new DataProcessor_Log();
		this.log_processor.register_data_source(Element.database_log);
		
		// prepare the display
		this.display = new Display();
	} 
		
	@Override
	public void draw() {
		this.display.write_status();
	}
		
	@Override
	public void keyPressed () {
		if (key == '+') Element.auto_read = true;
		if (key == '-') Element.auto_read = false;
		if (key == 'r') Element.file_read = true;
		if (key == 'z') mobile_processor.read_raw_data();
		if (key == 'a') mobile_processor.transformDataByHour( 30);
		if (key == 'x') journal_processor.read_raw_data();
		if (key == 'c') log_processor.read_raw_data();
	}
	
}
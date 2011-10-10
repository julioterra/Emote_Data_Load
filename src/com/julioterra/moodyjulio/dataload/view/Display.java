package com.julioterra.moodyjulio.dataload.view;

import processing.core.PFont;

import com.julioterra.moodyjulio.dataload.basicelements.Element;

public class Display extends Element {

	PFont font;															// font variable for displaying data to screen
	int background_color;
	int font_color;
	
	public Display () {
		processing_app.size((processing_app.screenWidth/4), (processing_app.screenHeight/2)); 			
		background_color = 0xffffff;
		font = processing_app.loadFont("AdiraDisplaySSi-20.vlw");
		font_color = 0x000000;
		processing_app.background(background_color);
	}
	

	public void write_status () {
		String title_current_activity = 	"";
		String title_file_string = 			"";
		String file_string = 				"";
		String title_count_string = 		"";
		String count_string = 				"";
		String title_status_string =		"";
		String status_string = 				"";

		if (Element.data_source == 0) {
			title_current_activity = "MoodyJULIO File Reading App";
			title_status_string = "choose data source: ";
			status_string = "press \'1\' for mobile data \n" +
						    "press \'2\' for journal data \n";
		} else {
			title_status_string = "status: ";
			status_string = "";
			title_count_string = "total readings: \n" +
			  					 "valid readings: \n" +
			  					 "batch number: ";
			count_string = Element.total_readings + "\n" +
						   Element.valid_readings + "\n" +
						   Element.data_batch;
		}

		if (Element.data_source == 1) {
			title_current_activity = "MoodyJULIO - Mobile Data";
			title_file_string = "name of active file: ";
	 		file_string = Element.file_name_short;
			if (Element.file_active) { status_string += "reading file and processing data."; }
			else if (Element.file_completed) { status_string += "finished reading file. \n" + "press \'n\' to select new file."; }
			else { status_string += "press \'n\' to select a file to read."; }		
		}

		else if (Element.data_source == 2) {
			title_current_activity = "MoodyJULIO - Journal Data";
			title_file_string = "active page number: ";
	 		file_string = Element.file_name_short;
			if (Element.file_active) { status_string += "reading file and processing data."; }
			else if (Element.file_completed) { status_string += "finished reading file. \n" + "press \'n\' to read next page."; }
			else { status_string += "press \'n\' to read data from posterous."; }		
		}

		processing_app.background(background_color);
		processing_app.textFont(font, 16);

		processing_app.fill(0,0,0);
		processing_app.text(title_current_activity, 10, 20);

		processing_app.fill(0,0,0);
		processing_app.text(title_status_string, 10, 50);
		processing_app.fill(50,50,50);
		processing_app.text(status_string, 10, 70);

		processing_app.fill(0,0,0);
		processing_app.text(title_file_string, 10, 120);
		processing_app.fill(50,50,50);
		processing_app.text(file_string, 150, 120);

		processing_app.fill(0,0,0);
		processing_app.text(title_count_string, 10, 140);
		processing_app.fill(50,50,50);
		processing_app.text(count_string, 120, 140);
		
	}
	
	
}

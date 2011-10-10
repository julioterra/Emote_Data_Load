package com.julioterra.moodyjulio.dataload.datahandlers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import processing.core.PApplet;
import java.net.*;
import com.julioterra.moodyjulio.dataload.basicelements.Element;

public class DataReader_AuthHttp extends DataReader {

	/******************************
	 ** CONSTRUCTORS
	 **/

	public DataReader_AuthHttp() {
		super();
		this.file_num = 1;		
		this.login_is_registered = false;
		this.file_is_registered = false;
		this.ready_to_read = false;
	} 


	@Override
	/************************************
	 * REGISTER_FILE_NAME()
	 * This method is used to set file_name parameters when using sequentially numbered files.
	 *  
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public void register_file_name(String file_name_base, String[] file_params) {
		this.file_name_base = file_name_base;
		this.file_params = file_params;
		this.file_is_registered = true;
		if (Element.debug_code) System.out.println("DataReaderAuthHttp.register_file_name() - file name and params registered");		
	}
	
	@Override
	/************************************
	 * REGISTER_USERNAME_PASSWORD()
	 * This method is used to set file_name parameters when using sequentially numbered files.
	 *  
	 * Functions to call before: n/a
	 * 
	 * Functions to call after: n/a
	 * 
	 */	
	public void register_username_password(String username, String password) {
		this.username = username;
		this.password = password;
		this.login_is_registered = true;
		if (Element.debug_code) System.out.println("DataReaderAuthHttp.register_username_password() - username and password registered");		
	}


	@Override
	/************************************
	 * OPEN_NEXT_FILE()
	 * This method is used to open the next file in a group of sequentially labelled files.
	 *  
	 * Functions to call before: n/a
	 * 
	 * Functions to call after: n/a
	 * 
	 */	
	public boolean open_next_file() {
		if (this.file_is_registered) {
			this.file_name_active = this.file_name_base + this.file_num;
			for (int i = 0; i < this.file_params.length; i++) { this.file_name_active += this.file_params[i]; }
			if (Element.debug_code) System.out.println("DataReaderAuthHttp.open_next_file() - Creating New File Name " + this.file_name_active);		

			Element.file_name_current = this.file_name_active;
			Element.file_name_short = "page number " + this.file_num;

			this.file_num++;
			return open_file(this.file_name_active);	
			
		}
		return false;
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
		super.open_file_init();	
		if (this.login_is_registered) {
			try {
				if (Element.debug_code) System.out.println("DataReaderAuthHttp.open_file() - Openning URL: " + filename);		
			    make_auth_http_request(filename, this.username, this.password);
				this.ready_to_read = true;		
				return true;
			} catch(Exception e) {
				this.ready_to_read = false;			    
				if (Element.debug_code) System.out.println("DataReaderAuthHttp.open_file() - Error Opening URL at: " + filename + "\n More Information: "+ e.getMessage()); 
				return false;
			}
		} 
		if (Element.debug_code) System.out.println("DataReaderAuthHttp.open_file() - open file failed because log-in not registered");					
		return false;

	}
	
	@Override
	/************************************
	 * READ_FULL_FILE()
	 * This method is used to read data from an entire file. It returns the contents of the file as a String.
	 *  
	 * Functions to call before: n/a
	 * Functions to call after: n/a
	 * 
	 */	
	public String read_full_file() {
		String current_page;
		try { 
			current_page = read_auth_http_request(); 
			if (Element.debug_code) System.out.println("DataReaderAuthHttp.read_full_file() - new full file " + current_page);		
		} 
		catch (Exception e) {
			current_page = "";
			if (Element.debug_code) System.out.println("DataReaderAuthHttp.read_full_file() - error reading file " + e.getLocalizedMessage());		
		}
		return current_page;
	}
	
	

	
	/******************************
	 ** STATIC METHODS - READ DATA FROM POSTEROUS ACCOUNT
	 **/

	private static String inputLine, inputPage = "";
	private static BufferedReader data;
	  
	public static void make_auth_http_request(String web_address, String username, String password) throws Exception {
	    Authenticator.setDefault(new MyAuthenticator(username, password));
	    URL url = new URL(web_address);
	    URLConnection connection = url.openConnection();
		inputPage = "";
	    data = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
	}
	
	public static String read_auth_http_request() throws Exception {
	    while ((inputLine = data.readLine()) != null) inputPage += inputLine;
	    data.close();
	    return inputPage;
	}
	
	protected static class MyAuthenticator extends Authenticator {
	    private String username, password;
	    public MyAuthenticator(String user, String pwd) {
	        username = user;
	        password = pwd;
	    }
	    @Override
		protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(username, password.toCharArray()); }
	}

}

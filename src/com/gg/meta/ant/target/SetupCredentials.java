package com.gg.meta.ant.target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;

public class SetupCredentials {
	static Properties prop;
	static String targetFolder = GGUtils.getRootFolderURL() + "java\\src\\";
	public static Logger log = Logger.getRootLogger();
	
	public static void main(String[] args) throws Exception {
		System.out.println("SetupCredentials start");
		loadPropertyFile();
		setupSourceOrgProperty();
		setupTargetOrgProperty();
		System.out.println("SetupCredentials end");
	}
	/**
	 * Loads the properties from the "build.properties" file into memory.
	*/
	private static void loadPropertyFile()  {
		InputStream input = null;
		prop = new Properties();
		try {
			input = new FileInputStream(new File(GGUtils.getRootFolderURL() + "build.properties"));
			prop.load(input);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.error(e.getMessage());
					e.printStackTrace();
				}
			} 
		}
	}

	/**
	 * 
	 * Sets up the properties for the source organization by extracting the
	 * "sf.sourceusername", "sf.sourcepassword", and "sf.sourceapiendpoint" from the
	 * loaded properties and creates a new property file named "org-src.properties"
	 * in the target folder.
	 */
	private static void setupSourceOrgProperty() {
		String username = prop.getProperty("sf.sourceusername");
		String password = prop.getProperty("sf.sourcepassword");
		String endpoint = prop.getProperty("sf.sourceapiendpoint");
		createPropertyFile(new File(targetFolder + "org-src.properties"), username, password, endpoint);
	}
	
	/**
	 * Sets up the properties for the target organization by extracting the
	 * "sf.username", "sf.password", and "sf.apiendpoint" from the loaded properties
	 * and creates a new property file named "org-target.properties" in the target
	 * folder.
	 */
	private static void setupTargetOrgProperty() {
		String username = prop.getProperty("sf.username");
		String password = prop.getProperty("sf.password");
		String endpoint = prop.getProperty("sf.apiendpoint");
		createPropertyFile(new File(targetFolder + "org-target.properties"), username, password, endpoint);
	}
	
	private static void createPropertyFile(File f, String username, String password, String endpoint) {
		String data = "user=" + username + "\n" +
					"password=" + password + "\n" +
					"endpoint=" + endpoint;
		FileUtils.createFile(f, data);		
	}
}

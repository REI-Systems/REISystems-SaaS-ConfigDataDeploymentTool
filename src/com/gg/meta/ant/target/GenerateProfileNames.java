package com.gg.meta.ant.target;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.gg.config.util.AppUtils;
import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;
import com.lib.util.StringUtils;

/**
 * It generates the XML which is used to deploy the Internal Unique ID field for native objects
 * @author shahnavazk
 *
 */
public class GenerateProfileNames {
	static List<String> profileNames = new ArrayList<String>();
	static String packageXMLFilename;
	static Logger log = Logger.getRootLogger();
	private String profileStr;
	
	public static void main(String[] args) {
		log.info("GenerateProfileNames starts");
		profileNames = StringUtils.getList(args[0], ",", true, false);
		packageXMLFilename = args[1];
		new GenerateProfileNames().process();
		log.info("GenerateProfileNames ends");
	}
	
	private void process() {
		handleProfiles();
		createPackageXML();
	}
	/**
	 * Handles the processing of profile names and generates the corresponding 'members' elements for non-temporary profiles.
	 */
	private void handleProfiles() {
		List<String> profiles = new ArrayList<String>();
		for (String profileName : profileNames) {
			if (AppUtils.isNonTempFile(profileName)) {
				profiles.add("\t\t<members>" + profileName + "</members>");
			}
		}
		profileStr = StringUtils.getConcatenatedString(profiles, "\n");
	}
	/**
	 * Creates the Package.xml file by replacing the placeholder with the generated profile elements.
	 */
	private void createPackageXML() {
		String path = GGUtils.getSRCFolderURL();
		File f = new File(path + packageXMLFilename);
		String packageXMLBody = FileUtils.readFile(f, true);
		packageXMLBody = packageXMLBody.replace("{profiles}", profileStr);
		
		FileUtils.createFile(new File(path+"package.xml"), packageXMLBody);			
	}
}

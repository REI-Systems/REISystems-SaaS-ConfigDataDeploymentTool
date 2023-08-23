package com.gg.meta.helper;

import java.util.HashSet;
import java.util.Set;

import com.gg.common.Variables;
import com.lib.util.XMLUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.gg.config.util.AppUtils;
import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;


public class CleanLayoutHelper {
	static Logger log = Logger.getRootLogger();
	static String layoutFolderPath = GGUtils.getSRCFolderURL() + "profiles";
	private static String data;
	
	public static void main(String[] args) {
		log.info("Layout cleanup starts");
		layoutFolderPath = "C:\\Users\\nakul.kadam\\OneDrive - REI Systems Inc\\REI Projects\\REISystems-SaaS-GovGrants-MigrationTool-master(1)\\REISystems-SaaS-GovGrants-MigrationTool-master\\Main\\src\\profiles\\";
		System.out.println("*** Test " + layoutFolderPath);
		File layoutFolder = new File(layoutFolderPath);
		for (File layout : layoutFolder.listFiles()) {
			process(layout);
		}
		log.info("Layout cleanup ends");
	}
	/**
	 * Processes the specified layout file by performing the following actions:
	 * 1. Reads the content of the layout file.
	 * 2. Removes unwanted fields from the layout data.
	 * 3. Removes the standard layout information.
	 * 4. Saves the updated layout data back to the original file.
	 *
	 * @param layout The layout file to be processed.
	 */
	private static void process(File layout) {
		log.info("Processing Layout " + layout.getName());
		data = FileUtils.readFile(layout, true);
		removeUnwantedField();
		removeStandardLayout();
	
		FileUtils.createFile(layout, data);
		
	}
	/**
	 * Removes unwanted field permissions from the layout data.
	 */
	private static void removeUnwantedField() {
		Set<String> unwantedFieldsSet = new HashSet<String>();
		for(String fldName : Variables.unwantedFieldsList){
			unwantedFieldsSet.add(fldName.toLowerCase());
		}
		log.info("unwantedFieldsSet >>>>"+unwantedFieldsSet);
		for (String tagValue : XMLUtils.fetchTags(data, "fieldPermissions", true)) {
			String fieldName = XMLUtils.fetchTagValue(tagValue, "field", false);
			if (unwantedFieldsSet.contains(fieldName.toLowerCase())) {
				data = data.replace(tagValue, "");
			}
			for(String fld : unwantedFieldsSet){
				if(fieldName.toLowerCase().contains(fld)){
					data = data.replace(tagValue, "");
				}
			}
		}
	}
	/**
	 * Removes standard layout assignments from the layout data.
	 * It reads the layout data and identifies the layout assignment tags.
	 */
	private static void removeStandardLayout() {
		Set<String> unwantedLayoutSet = new HashSet<String>();
		for(String layout : Variables.unwantedLayoutsList){
			unwantedLayoutSet.add(layout.toLowerCase());
		}
		log.info("unwantedLayoutSet >>>>"+unwantedLayoutSet);
		for (String tagValue : XMLUtils.fetchTags(data, "layoutAssignments", true)) {
			String layoutName = XMLUtils.fetchTagValue(tagValue, "layout", false);
			//if (unwantedLayoutSet.contains(layoutName.toLowerCase())) {
			//	data = data.replace(tagValue, "");
			//}
			log.info("layoutName: "+layoutName);
			for(String fld : unwantedLayoutSet){
				if(layoutName.toLowerCase().startsWith(fld.toLowerCase())){
					log.info("**** Replaced ****");
					data = data.replace(tagValue, "");
				
				}
			}
		}
	}
	/**
	 * Removes layout assignments associated with specific managed record types identified by their names from the layout data.
	 */
	private static void removeManagedRecTypeTmp() {
		Set<String> unwantedLayoutSet = new HashSet<String>();
		String name1 = "GNT__PageBlockDetailConfig__c.tmp";
		unwantedLayoutSet.add(name1.toLowerCase());		
		/*for(String layout : Variables.unwantedLayoutsList){ // variables.unwantedlayoutsList is null here -- check why
			unwantedLayoutSet.add(layout.toLowerCase());
		}*/
		for (String tagValue : XMLUtils.fetchTags(data, "layoutAssignments", true)) {
			if (tagValue.contains("<recordType>")){			
				String recordType = XMLUtils.fetchTagValue(tagValue, "recordType", false); // throws null if the attribute is not found
				if (unwantedLayoutSet.contains(recordType.toLowerCase())){
					data = data.replace(tagValue, "");
				}	
			}
		}		
		
	}
}

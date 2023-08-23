package com.gg.meta.ant.target;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;
import com.lib.util.XMLUtils;

/**
 * Removes listViews tags from custom objects if those list views belong to queues.
 * If we don't remove, deploying was failing with duplicate list view error.
 * 
 * @author shahnavazk
 *
 */
public class CleanCustomObject {
	static Logger log = Logger.getRootLogger();
	
	public static void main(String[] args) {
		log.info("Custom object cleanup starts");
		// Delete Unwanted Objects
		for (File f : new File(GGUtils.getSRCFolderURL() + "objects").listFiles()) {
			if(f.getName().contains("__x") || f.getName().toUpperCase().equals("CONTENTVERSION")) {
				f.delete();
			}
		}
		
		for (File f : new File(GGUtils.getSRCFolderURL() + "objects").listFiles()) {			
			if (f.getName().endsWith(".object")) {
				process(f);
			}
		}
		log.info("Custom object cleanup ends");
		
	}

	/**
	 * Processes the given XML file to remove specific tags and their values related to queues in list views.
	 */
	private static void process(File f) {
		String data = FileUtils.readFile(f, true);
		List<String> tags = XMLUtils.fetchTags(data, "listViews", true);
		boolean changed = false;
		for (String tag : tags) {
			if (tag.contains("<queue>")) {
				data = data.replace(tag, "");
				changed = true;
			}
		}
		
		for(String tag : tags) {
			String subTag = XMLUtils.fetchTagValue(tag, "filterScope", false);
			if(subTag.toUpperCase().contains("QUEUE")){
				data = data.replace(tag, "");
				changed = true;
			}
		}
				
		
		// ERROR 2.  objects/Account.object (Account.IsPartner) -- Error: Could not resolve standard field's name. (line 728, column 13)
		for (String tagValue : XMLUtils.fetchTags(data, "fields", true)) {
			String fieldName = XMLUtils.fetchTagValue(tagValue, "fullName", false);
			String fieldType = XMLUtils.fetchTagValue(tagValue, "type", false);
			
			//System.out.println("Tag Value " + tagValue + " Field Name - " + fieldName + " Field Type - " + fieldType);
			if (fieldName != null) {
				if (!fieldName.startsWith("GNT__") && !fieldName.endsWith("__c") && (fieldType == null || (fieldType != null && !fieldType.equalsIgnoreCase("picklist") ))) {	// standard fields 
					data = data.replace(tagValue, "");
					log.info("fieldName >>>>>>"+fieldName+" <<-- fieldType -->>"+fieldType);
					changed = true;
				}
				if(fieldName.startsWith("APXTConga4__")) {
					data = data.replace(tagValue, "");
					log.info("fieldName >>>>>>"+fieldName+" <<-- fieldType -->>"+fieldType);
					changed = true;
				}
			}
			
		}
		
		for (String tagValue : XMLUtils.fetchTags(data, "webLinks", true)) {
			String fieldName = XMLUtils.fetchTagValue(tagValue, "fullName", false);
			//String fieldType = XMLUtils.fetchTagValue(tagValue, "type", false);
			
			//System.out.println("Tag Value " + tagValue + " Field Name - " + fieldName + " Field Type - " + fieldType);
			if (fieldName != null) {
				/*if (!fieldName.startsWith("GNT__") && !fieldName.endsWith("__c") && (fieldType == null || (fieldType != null && !fieldType.equalsIgnoreCase("picklist") ))) {	// standard fields 
					data = data.replace(tagValue, "");
					log.info("fieldName >>>>>>"+fieldName+" <<-- fieldType -->>"+fieldType);
					changed = true;
				}*/
				if(fieldName.startsWith("APXTConga4__")) {
					data = data.replace(tagValue, "");
					log.info("weblinks -- fieldName >>>>>>"+fieldName);
					changed = true;
				}
			}
			
		}
		
		// Error. AddCampaign is not a standard action and cannot be overridden
		
		for (String tagValue : XMLUtils.fetchTags(data, "actionOverrides", true)) {
			if (tagValue.toUpperCase().contains("ADDCAMPAIGN") || tagValue.toUpperCase().contains("SAVEEDIT")) {				
				data = data.replace(tagValue, "");
			}
		}
		// ERRROR 13.  objects/UserRegistration__c.object (UserRegistration__c.Reviewer_Additional_Information) -- Error: AvailableFields in FieldSet is not editable for your organization. (line 629, column 16)
	/*	for (String tagValue : XMLUtils.fetchTags(data, "availableFields", true)) {
			if (tagValue != null) {
				data = data.replace(tagValue, "");
				changed = true;
			}
		}
		// ERRROR 6. objects/User.object (User.Partner_Users) -- Error: Could not resolve list view column: CORE.USERS.IS_PARTNER (line 499, column 16)
		for (String tagValue : XMLUtils.fetchTags(data, "listViews", true)) {
			if (tagValue != null && tagValue.toUpperCase().contains("CORE.USERS.IS_PARTNER")) {	// standard fields
				data = data.replace(tagValue, "");
				changed = true;
			}
		}	*/			
		
		if (changed) {
			FileUtils.createFile(f, data);
		}
	}
}

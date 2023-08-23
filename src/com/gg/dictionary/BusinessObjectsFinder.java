package com.gg.dictionary;

import java.util.ArrayList;
import java.util.List;

import com.force.service.raw.ForceDelegateRaw;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;

/**
 * Prints out the names of business objects and non-business objects. It uses the API name and label to differentiate.
 * @author shahnavazk
 *
 */
public class BusinessObjectsFinder {
	private ForceDelegateRaw connRaw;
	private static List<String> ignoreObjects = new ArrayList<String>();
	
	static {
		ignoreObjects.add("ExternalInterfaces__c");
		ignoreObjects.add("GrantConnector__c");
		ignoreObjects.add("GroupTaskMapping__c");
		ignoreObjects.add("JXPrograms__c");
		ignoreObjects.add("ProfileAccess__c");
		ignoreObjects.add("Profiles__c");
	}

	public BusinessObjectsFinder(ForceDelegateRaw connRaw) {
		this.connRaw = connRaw;
	}
	
	public static void main(String[] args) {
		BusinessObjectsFinder dict = new BusinessObjectsFinder(ForceDelegateRaw.login("src"));
		dict.find();
	}
	/**
	 * Finds and categorizes Salesforce objects into business and non-business objects.
	 */
	public List<String> find() {
		List<String> businessObjects = new ArrayList<String>();
		List<String> nonBusinessObjects = new ArrayList<String>();
		
		DescribeGlobalResult gr = connRaw.describeGlobal();
		for (DescribeGlobalSObjectResult gs : gr.getSobjects()) {
			if (ignoreTotal(gs.getName(), gs.getLabel())) continue;
			
			if (ignore(gs.getName(), gs.getLabel()) == false) {		
				businessObjects.add(gs.getLabel() + " (" + gs.getName() + ")");
			}
			else {
				nonBusinessObjects.add(gs.getLabel() + " (" + gs.getName() + ")");
			}
		}
		
		System.out.println("===================");
		System.out.println("Business Objects:");
		System.out.println("===================");
		System.out.println(StringUtils.getConcatenatedString(businessObjects, "\n"));
		System.out.println("\n\n");

		System.out.println("===================");
		System.out.println("Non Business Objects:");
		System.out.println("===================");
		System.out.println(StringUtils.getConcatenatedString(nonBusinessObjects, "\n"));
		
		return businessObjects;
	}
	/**
	 * Determines whether the given Salesforce object should be ignored or not.
	 */
	private boolean ignoreTotal(String objectName, String objectLabel) {
		if (objectName.endsWith("__Feed") || objectName.endsWith("__Share") || objectName.endsWith("__History") ||
				objectName.startsWith("cwbtool__") || !objectName.endsWith("__c") || objectLabel.startsWith("Review ") || 
				objectLabel.startsWith("SamGov") || objectName.startsWith("APXTConga4__") || objectLabel.startsWith("Sample ") ||
				objectName.startsWith("Field_Trip__")) {
			return true;
		}
		return false;
	}
	/**
	 * Determines whether the given Salesforce object should be ignored or not.
	 * 
	 */
	private boolean ignore(String objectName, String objectLabel) {
		String objectLabelLower = objectLabel.toLowerCase();
		if (objectName.equals("Account") || objectName.equals("Contact") || objectName.equals("Task") || objectName.equals("User")) {
			return false;
		}
		
		if (objectName.endsWith("__c") == false) {  //ignore all standard objects except the above ones
			return true;
		}
		
		if (objectLabel.startsWith("MST:") || objectLabel.startsWith("IST:") || objectLabel.startsWith("STG:") || 
				objectLabel.startsWith("Config:") || objectLabel.equals("GG") || objectLabel.endsWith("Config") || objectLabel.startsWith("CFG:") ||
				objectLabelLower.contains("temp")) {
			return true;
		}
		
		if (isManagedObject(objectName) || ignoreObjects.contains(objectName)) {
			return true;
		}
		return false;
	}
	
	private boolean isManagedObject(String objectName) {
		objectName = objectName.replace("__c", "");
		return objectName.contains("__");
	}
}

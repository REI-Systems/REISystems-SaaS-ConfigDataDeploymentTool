package com.gg.meta.util.dictionary;

import java.util.ArrayList;
import java.util.List;

import com.force.service.raw.ForceDelegateRaw;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;

/**
 * For a given org, it extracts the objects and fields information and creates multiple CSV files out of it.
 * @author shahnavazk
 *
 */
public class CreateDataDictionary {
	private static String folder = "C:\\Projects\\Salesforce\\REI\\Projects\\GovGrants\\Data Dictionary\\";
	private ForceDelegateRaw src = ForceDelegateRaw.login("src");
	private static List<String> ignoreObjects = new ArrayList<String>();
	
	static {
		ignoreObjects.add("");
	}

	public static void main(String[] args) {
		CreateDataDictionary dict = new CreateDataDictionary();
		dict.generate();
	}
	/**
	 * Generates data dictionaries for Salesforce SObjects.
	 */
	private void generate() {
		DescribeGlobalResult gr = src.describeGlobal();
		for (DescribeGlobalSObjectResult gs : gr.getSobjects()) {
			if (ignore(gs.getName(), gs.getLabel()) == false) {
				DescribeSObjectResult res = src.describeSObject(gs.getName());
				new SObjectDataDictionary(res, folder, src).generate();
			}
		}
	}
	/**
	 * Checks if an SObject should be ignored for data dictionary generation.
	 * @param objectName
	 * @param objectLabel
	 * @return
	 */
	private boolean ignore(String objectName, String objectLabel) {
		if (objectName.equals("Account") || objectName.equals("Contact") || objectName.equals("Task") || objectName.equals("User")) {
			return false;
		}
		
		if (objectName.endsWith("__c") == false) {  //ignore all standard objects except the above ones
			return true;
		}
		
		if (objectLabel.startsWith("MST:") || objectLabel.startsWith("IST:") || objectLabel.startsWith("STG:") || 
				objectLabel.startsWith("Config:") || objectLabel.equals("GG") || objectLabel.endsWith("Config")) {
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

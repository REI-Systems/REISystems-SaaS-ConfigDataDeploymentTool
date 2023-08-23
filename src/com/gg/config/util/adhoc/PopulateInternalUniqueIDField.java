package com.gg.config.util.adhoc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;

import com.force.service.ForceDelegate;
import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.gg.common.Variables;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.sobject.SObject;
/**
 * It performs a fake update on all custom objects (excluding custom settings) so that the trigger
 * on those objects will fire and will set the "Internal Unique ID" field. Before running this class
 * make sure that all objects have the trigger logic to set the unique id value.
 * 
 * This class can be run any number of times as it populates the unique id field ONLY if that value is missing.
 * 
 * @author shahnavazk
 *
 */
public class PopulateInternalUniqueIDField {
	
	static final String ORG_NAME = "lctcsqa";
	static final String NAMESPACE = "";
	static final String UNIQUE_ID_FIELD = NAMESPACE + Variables.INTERNAL_UNIQUEID_FIELDNAME;

	static ForceDelegate gate;
	static ForceDelegateRaw gateRaw;
	static List<String> objectNames = new ArrayList<String>();
	
	public static void main(String[] args) {
		gate = ForceDelegate.login(ORG_NAME);
		gateRaw  = ForceDelegateRaw.login(ORG_NAME);
		//populateAllSObjects();
		populateSingleSObject("Classification__c");
	}
	/**
	 * Populates all custom sObjects in the Salesforce org with data.
	 * The method first retrieves a DescribeGlobalResult to obtain information about all sObjects in the org.
	 */
	private static void populateAllSObjects() {
		DescribeGlobalResult gs = gate.describeGlobal();
		for (DescribeGlobalSObjectResult res : gs.getSobjects()) {
			String objectName = res.getName();
			if (objectName.startsWith(NAMESPACE) && objectName.endsWith("__c") && !res.isCustomSetting()) {
				System.out.println(res.getName());
				populateSingleSObject(res.getName());
			}
		}
	}
	/**
	 * Populates a single custom sObject in the Salesforce org with unique data for the specified UNIQUE_ID_FIELD.
	 * The method queries the sObject to find records with null values in the UNIQUE_ID_FIELD.
	 * For each record found, it generates a unique token using the generateUniqueToken() method and sets it as the value of the UNIQUE_ID_FIELD.
	 */
	private static void populateSingleSObject(String objectName) {
		try {
			SObject[] records = gateRaw.queryMultiple("Select Id, Name, " + UNIQUE_ID_FIELD + " from " 
						+ objectName + " where " + UNIQUE_ID_FIELD + "=null", new Object[]{});
			List<SObject> recordsToUpdate = new ArrayList<SObject>();
			for (SObject record : records) {
				String uniqueIdValue = ForceUtils.getSObjectFieldValue(record, UNIQUE_ID_FIELD);
				if (StringUtils.isEmpty(uniqueIdValue)) {
					ForceUtils.setSObjectFieldValue(record, UNIQUE_ID_FIELD, generateUniqueToken());
					recordsToUpdate.add(record);
				}
			}
			gateRaw.updateMultiple(recordsToUpdate);
		}
		catch (Exception e) {
			System.out.println("error for " + objectName);
			e.printStackTrace();
		}
	}
	
	private static String generateUniqueToken() {
		return RandomStringUtils.randomAlphanumeric(32);
	}
}

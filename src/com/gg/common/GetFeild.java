package com.gg.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;
import com.lib.util.CSVUtils;
import com.lib.util.StringUtils;


public class GetFeild {
	public static final String SRC_ORG_NAME_ALIAS = "src";
	public static ForceDelegateRaw src;
	static String folder = "C:\\Users\\harshada.kale\\Workspace\\Data\\Version1\\";
	static List<String> ignoreFields = Arrays.asList("Id", "CreatedById", "CreatedDate", "OwnerId", "IsDeleted", "LastModifiedById", "LastModifiedDate","SystemModstamp");
	public static void main(String[] args) 
	{
		loginToOrg();
		Map<String, List<String>> exclusiveFieldMap = processSobject();
		processSobjectMetadata(exclusiveFieldMap);
	}

	/**
	 * Processes SObject metadata, excluding fields specified in the exclusiveFieldMap, and generates a CSV file for each SObject with the remaining fields.
	 * 
	 * @param exclusiveFieldMap A mapping of Salesforce object names to lists of field names that should be excluded from processing for each object.
	 */
	public static void processSobjectMetadata(Map<String, List<String>> exclusiveFieldMap) {
	    DescribeGlobalSObjectResult[] sobjs = src.describeGlobal().getSobjects();
	    for (DescribeGlobalSObjectResult descGlobalResult : sobjs) {
	        String objectName = descGlobalResult.getName();

	        if (exclusiveFieldMap.containsKey(objectName)) {
	            System.out.println(descGlobalResult);
	            System.out.println("***********************************************************************");
	            System.out.println("Object Name:-  " + objectName);
	            System.out.println("***********************************************************************");

	            Map<String, String> labelOverrideMap = fetchLabelOverride(objectName);

	            DescribeSObjectResult or = src.describeSObject(objectName);
	            List<String[]> result = new ArrayList<>();
	            List<String> excludedFields = exclusiveFieldMap.get(objectName);

	            for (Field f : or.getFields()) {
	                String fieldName = f.getName();
	                String fieldLabel = f.getLabel();

	                if (excludedFields.contains(fieldName) || fieldName.startsWith("Hide") || fieldName.startsWith("Disable")) {
	                    System.out.println("Exclusive field:- >>>>>>" + fieldLabel);
	                    continue;
	                }

	                String label = labelOverrideMap.getOrDefault(fieldName, fieldLabel);
	                String[] row = new String[]{label, labelOverrideMap.getOrDefault(fieldName, ""), fieldName};
	                result.add(row);
	            }

	            System.out.println("***********************************************************************");
	            CSVUtils.createFile(folder + objectName + ".csv", result);
	        }
	    }
	}

	/**
	 * Fetches field label overrides for a given Salesforce object from the database and returns them as a map.
	 */
	private static Map<String, String> fetchLabelOverride(String objectName) {
		SObject[]layoutFields = src.queryMultiple("select GNT__FieldAPIName__c, GNT__FieldLabelOverride__c from GNT__PageBlockDetailConfig__c"
				+ " Where GNT__PageBlockConfig__r.GNT__TabLayoutConfig__r.GNT__PageLayoutConfig__r.GNT__ObjectAPIName__c = ? "
				+ "AND  GNT__IsActive__c = true and GNT__FieldAPIName__c != null", 
				new Object[] {objectName});
		Map<String, String> fieldMap = new HashMap<String, String>();
		for (SObject record : layoutFields) {
			String label = ForceUtils.getSObjectFieldValue(record, "GNT__FieldLabelOverride__c");
			if (StringUtils.isNonEmpty(label)) {
				fieldMap.put(ForceUtils.getSObjectFieldValue(record, "GNT__FieldAPIName__c"), label);
			}
		}
		return fieldMap;
	}
	
	public static Map<String, List<String>> processSobject()
	{
		String soql = prepareQuery();
		SObject[] srcRecords = src.queryMultiple(soql, null);
		Map<String, List<String>> exclusiveFieldMap = new HashMap<String, List<String>>();
		List<String> exclusiveFields  = new ArrayList<String>();
		for(SObject var : srcRecords) {
			exclusiveFields = Arrays.asList(ForceUtils.getSObjectFieldValue(var, "Data_Migration_Exclusion_Fields__c", true).split(","));
			List<String> trimmedFields = new ArrayList<String>();
			for (String f : exclusiveFields) {
				trimmedFields.add(f.trim());
			}
			exclusiveFieldMap.put(ForceUtils.getSObjectFieldValue(var, "name", true), trimmedFields);
		}
		return exclusiveFieldMap;
	}
	public static void loginToOrg()
	{
		src = ForceDelegateRaw.login(SRC_ORG_NAME_ALIAS);
	}
	public static String prepareQuery() {
		String soql = "SELECT Name, Data_Migration_Eligible__c, Data_Migration_Exclusion_Fields__c FROM GNT__SobjectConfig__c Where Data_Migration_Eligible__c = true";
		System.out.println("SOQL: " + soql);		
		return soql;
	}
}

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


public class GetFeildsUsingPageLayoutId {
	public static final String SRC_ORG_NAME_ALIAS = "src";
	public static ForceDelegateRaw src;
	static String folder = "C:\\Users\\harshada.kale\\Workspace\\Data\\Test\\";
	public static List<String> ignoreFields = Arrays.asList("ID", "CreatedById", "CreatedDate", "OwnerId", "IsDeleted", "LastModifiedById", "LastModifiedDate","SystemModstamp");
	public static Map<String, String> SobjectList = new HashMap<String,String>()
			{{
				//dev5
				put("Announcement__c","'a0l36000004x6J9AAI'");
				put("Account","'a0l36000004x5uPAAQ'"); // Internal Organization (PageLayout config id for the Account record)
				put("Account","'a0l36000004x6LhAAI'"); // Recipient Organization
				put("Account","'a0l36000004x6KhAAI'"); //Federal Organization
				put("Application__c","'a0l36000004x6IvAAI'");
				put("Grant__c","'a0l36000004x6J3AAI'");
				put("Award__c","'a0l36000004x6J6AAI'"); //after Grant
				put("BudgetPeriod__c","'a0l36000004x6H8'"); // Before award
				put("BudgetCategory__c","'a0l36000004x6Ik'"); //Before budget 
				put("Program__c","'a0l36000004x6IxAAI'"); // Internal Program
				put("SiteVisit__c","'a0l36000004x6IpAAI'"); // After Grant
				put("Program__c","'a0l36000004x6IAAAY'"); // Federal Program (Grantee side)
				put("ServiceArea__c","'a0l36000004x6GtAAI'"); // It's in Grantee side
				put("GrantServiceArea__c","'a0l36000004x6Io'"); // Grantee side
				//Purto-roco
				
				
			}};
	public static void main(String[] args)
	{
		loginToOrg();
		for(String key : SobjectList.keySet())
		{
			Map<String, String> FieldMap = processSobject(SobjectList.get(key)); // pass layoutid as an argument
			processSobjectMetadata(FieldMap,key); //pass Sobject and FieldMap as an argument
		}
	}
	/**
	 * Processes metadata for a specific Salesforce sObject.
	 *
	 * @param fieldMap A mapping of field names to custom labels.
	 * @param sobject  The name of the Salesforce sObject to process metadata for.
	 */
	
	public static void processSobjectMetadata(Map<String, String> fieldMap, String sobject) {
	    DescribeGlobalSObjectResult[] sobjs = src.describeGlobal().getSobjects();
	    for (DescribeGlobalSObjectResult descGlobalResult : sobjs) {
	        if (descGlobalResult.getName().equals(sobject)) {
	            System.out.println(descGlobalResult);
	            System.out.println("----------------------------------------------------------------------");
	            System.out.println("Object Name:-  " + descGlobalResult.getName());
	            System.out.println("***********************************************************************");

	            DescribeSObjectResult or = src.describeSObject(descGlobalResult.getName());
	            List<String[]> result = new ArrayList<>();
	          
	            for (Field f : or.getFields()) {
	                String fieldName = f.getName();
	                String fieldLabel = f.getLabel();

	                if (fieldName.startsWith("Hide") || fieldName.startsWith("Disable") || ignoreFields.contains(fieldName)) {
	                    System.out.println("Exclusive field:- >>>>>>" + fieldLabel);
	                    continue;
	                }

	                if (fieldMap.containsKey(fieldName)) {
	                    String label = fieldMap.getOrDefault(fieldName, fieldLabel);
	                    String dataType = f.getType().toString();

	                    String[] row = new String[]{label, dataType, fieldName};
	                    result.add(row);
	                   
	                }
	            }
	            System.out.println("----------------------------------------------------------------------");
	            CSVUtils.createFile(folder + descGlobalResult.getName() + ".csv", result);
	        }
	    }
	}
    
	/**
	 * Processes Salesforce sObject fields and their custom labels based on a specified layout ID.
	 *
	 * @param layoutId The ID of the layout to retrieve fields and labels for.
	 * @return A map containing field names as keys and custom labels as values.
	 */	public static Map<String, String> processSobject(String layoutId)
	{
		String soql = prepareQuery(layoutId);
		SObject[] srcRecords = src.queryMultiple(soql, null);
		Map<String,String> fieldMap = new HashMap<String, String>();
		for(SObject var : srcRecords) {
			fieldMap.put(ForceUtils.getSObjectFieldValue(var, "GNT__FieldAPIName__c", true), ForceUtils.getSObjectFieldValue(var, "GNT__FieldLabelOverride__c", true));
		}
		return fieldMap;
	}
	public static void loginToOrg()
	{
		src = ForceDelegateRaw.login(SRC_ORG_NAME_ALIAS);
	}
	public static String prepareQuery(String layoutId) 
	{
			String soql = "select GNT__FieldAPIName__c, GNT__FieldLabelOverride__c from GNT__PageBlockDetailConfig__c "
					+ "Where GNT__PageBlockConfig__r.GNT__TabLayoutConfig__r.GNT__PageLayoutConfig__c = "+ layoutId + " and GNT__FieldAPIName__c != null";
			System.out.println("SOQL: " + soql);
		return soql;
	}
}

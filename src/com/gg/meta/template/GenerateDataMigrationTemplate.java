package com.gg.meta.template;

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


public class GenerateDataMigrationTemplate {
	public static final String SRC_ORG_NAME_ALIAS = "src";
	public static ForceDelegateRaw src;
	static String folder = "C:\\Projects\\Salesforce\\REI\\Projects\\GovGrants\\Peel\\Data Migration\\Templates\\";
	static List<String> ignoreFields = Arrays.asList("Id", "CreatedById", "CreatedDate", "OwnerId", "IsDeleted", "LastModifiedById", "LastModifiedDate","SystemModstamp");
	public static void main(String[] args) 
	{
		loginToOrg();
		Map<String, List<String>> exclusiveFieldMap = processSobject();
		processSobjectMetadata(exclusiveFieldMap);
	}
	/**
	 * Processes Salesforce object metadata to create CSV files with field information, excluding specified fields.
	 * @param exclusiveFieldMap
	 */
	public static void processSobjectMetadata(Map<String, List<String>> exclusiveFieldMap)
	{
		DescribeGlobalSObjectResult[] sobjs = src.describeGlobal().getSobjects();
		for(DescribeGlobalSObjectResult descGlobalResult : sobjs) {			
			if(exclusiveFieldMap.containsKey(descGlobalResult.getName()))
			{
				System.out.println(descGlobalResult);
				System.out.println("----------------------------------------------------------------------");
				System.out.println("Object Name:-  "+ descGlobalResult.getName());
				System.out.println("***********************************************************************");
				
				Map<String, String> labelOverrideMap = fetchLabelOverride(descGlobalResult.getName());
				
				DescribeSObjectResult or = src.describeSObject(descGlobalResult.getName());
				List<String[]> result = new ArrayList<String[]>();
				String[] cols = new String[or.getFields().length];
				String[] rows = new String[or.getFields().length];
				String[] row2 = new String[or.getFields().length];
				int i = 0;
				List<String> excludedFields = exclusiveFieldMap.get(descGlobalResult.getName());
				for (Field f : or.getFields()) {
					if (excludedFields.contains(f.getName()) || f.getName().startsWith("Hide") || f.getName().startsWith("Disable"))
					{
						System.out.println("Exclusive field:- >>>>>>" + f.getLabel());
						continue;
					}
					cols[i] =f.getLabel();
					//f.getReferenceTo() != null --> foreign key exist
					if (labelOverrideMap.containsKey(f.getName())) {
						cols[i] = labelOverrideMap.get(f.getName());
						rows[i] = labelOverrideMap.get(f.getName());
						row2[i] = f.getName();
					}
					i = i + 1;
				}
				result.add(cols);
				result.add(rows);
				result.add(row2);
				System.out.println("----------------------------------------------------------------------");
				CSVUtils.createFile(folder+descGlobalResult.getName() + ".csv", result);		
			}
		}
	}
	/**
	 * Fetches field label overrides for the specified Salesforce object.
	 * @param objectName The API name of the Salesforce object for which to fetch field label overrides.
	 * @return  A Map where keys are field API names as strings, and values are the corresponding field label overrides as strings.
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
	/**
	 * Processes SObject records to retrieve a map of exclusive fields for each SObject.
	 * @return A map where keys are SObject names as strings, and values are lists of exclusive field names as strings.
	 */
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
	/**
	 * Prepares the Salesforce Object Query Language (SOQL) query for retrieving SObject configuration records.
	 * @return The prepared SOQL query as a string.
	 */
	public static String prepareQuery() {
		String soql = "SELECT Name, Data_Migration_Eligible__c, Data_Migration_Exclusion_Fields__c FROM GNT__SobjectConfig__c Where Data_Migration_Eligible__c = true";
		System.out.println("SOQL: " + soql);		
		return soql;
	}
}

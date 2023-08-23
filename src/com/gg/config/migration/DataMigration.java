package com.gg.config.migration;

import java.io.IOException;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.gg.common.Variables;
import com.lib.util.CSVUtils;
import com.sforce.soap.ConfigDataService.Connector;
import com.sforce.soap.ConfigDataService.SField;
import com.sforce.soap.ConfigDataService.SObjectKey;
import com.sforce.soap.ConfigDataService.SObjectRequest;
import com.sforce.soap.ConfigDataService.SObjectResponse;
import com.sforce.soap.ConfigDataService.SObjectTable;
import com.sforce.soap.ConfigDataService.SRecord;
import com.sforce.soap.ConfigDataService.SoapConnection;
import com.sforce.soap.partner.sobject.SObject;

/**
 * A collection of sobjects are migrated from one org to another. If any of the sobjects fails, then subsequent execution
 * is stopped.
 * 
 *
 */
public class DataMigration extends Variables {
	public static final String SRC_ORG_NAME_ALIAS = "src";
	public ForceDelegateRaw src;
	
	public String fileName; //"C:\\Users\\harshada.kale\\Workspace\\Data\\version3\\Internal account.csv";

	public String sobjectName; // "Account";
	public static final int batchSize = 200;
	
	public static void main(String[] args) throws Exception {
		log.info("----------------------------------------------------------------------------");
		//new DataMigration().handle("Application__c", "C:\\Users\\harshada.kale\\Workspace\\Data\\DataMigrationToolTest\\Test\\Application_new.csv");
		//new DataMigration().handle("Announcement__c", "C:\\Users\\harshada.kale\\Workspace\\Data\\DataMigrationToolTest\\Test\\Annauncement_new.csv");
		new DataMigration().handle(args[0],args[1]);
	}
	
	/**
	 * Handles the data migration process for the given sobjectName and fileName.
	 */
	private void handle(String sobjectName, String fileName) throws Exception {
		this.sobjectName = sobjectName;
		this.fileName = fileName;
		
		loginToTargetSalesforceInstance();

		String headerMapping = getHeaderMappingFromSF();
		Map<String,String> oldHeadderToNewHeaderMap = parseMapping(headerMapping); //key->field label, field API name
		String[] header = getOriginalHeaderFromCSV();
		String [] targetHeader = generateNewHeader(header, oldHeadderToNewHeaderMap);
		List<Map<String, String>> data = getCSVDataWithNewHeader(targetHeader);
	
		List<String> foreignKeys = getForeignKey();
		
		callWebservice(data, foreignKeys);
	}
	/**
	 * Displays the total records in each batch from the given list of lists containing records as maps.
	 * The method iterates through each batch of records in 'TotalRecordsinBatch' and prints each record's key-value pairs.
	 */
	private void displayTotalRecordsBatch(List<List<Map<String, String>>> TotalRecordsinBatch) {
		int i = 0;
		for(List<Map<String, String>> list : TotalRecordsinBatch) {
			//System.out.println("Outer loop --->>>"+i);
			for(Map<String, String> l: list ) {
				for(String key: l.keySet()) {
					System.out.println(key + ":" + l.get(key));
				}
			}
			i++;
		}
	}
	
	private void loginToTargetSalesforceInstance(){
		log.info("-------- Login to Salesforce Org ---------");
		src = ForceDelegateRaw.login(SRC_ORG_NAME_ALIAS);
		log.info("-------- Login Completed ---------");
	}
	private boolean isForeignKey(String key, String Sobject, List<String> foreignKeys) {
		if(foreignKeys.contains(key)) {
			return true;
		}
		return false;
	}
	/**
	 * Generates a new header based on the provided 'header' array and the 'oldHeadderToNewHeaderMap' mapping.
	 */
	private String [] generateNewHeader(String[] header, Map<String,String> oldHeadderToNewHeaderMap) {
		String [] targetHeader = new String[header.length];
		int i = 0;
		for(String oldHeader : header) {	// Name, Parent Id, Status__c, City
			String newHeader = oldHeadderToNewHeaderMap.get(oldHeader);	// Name:Name, Parent Agency: Parent Id, Status: Status__c, City:City
			if(oldHeadderToNewHeaderMap.containsKey(oldHeader)) {
				//System.out.println("newHeader >>>> "+newHeader);
				targetHeader[i] = newHeader;
			}else {
				//System.out.println("oldHeader >>>> "+oldHeader);
				targetHeader[i] = oldHeader;
			}
			i += 1;
		}
		return targetHeader;
	}
	
	private String[] getOriginalHeaderFromCSV() throws IOException{
		String[] header = CSVUtils.getHeader(fileName);
		return header;
	}
	/**
	 * Parses the mapping data received as a JSON string and converts it into a map of field names and their corresponding values.
	 * The method uses the JSONParser to parse the mapping data and then iterates through the JSON object to populate the fieldMap.
	 * The parsed fieldMap is returned.
	 */
	private Map<String,String> parseMapping(String mapping){
		JSONParser parser = new JSONParser();
		Map<String,String> fieldMap = new HashMap<String, String>();
		try {
			Object obj = parser.parse(mapping);
			JSONObject jo = (JSONObject) obj;
			Map Fields = ((Map)jo.get("Field"));
			Iterator<Map.Entry> itr1 = Fields.entrySet().iterator();
	        while (itr1.hasNext()) {
	            Map.Entry pair = itr1.next();
	            //System.out.println(pair.getKey() + " : " + pair.getValue());
	            fieldMap.put(pair.getKey().toString(), pair.getValue().toString());
	        }
		}
		catch(Exception pe){
	         System.out.println(pe);
	         System.out.println("Please make sure that data Migration has Mapping and ExternalIdForRelatedObject is in json format");
	         log.error(pe.getMessage());
	         log.error(pe);
	         System.out.print("Exception Occured.!! Press Any Key to Exit....");
	          new Scanner(System.in).nextLine();
	          System.exit(-1);
	    }
		//System.out.println(fieldMap);
		return fieldMap;
	}
	/**
	 * Retrieves the 'mapping__c' data from Salesforce using a SOQL query and returns it as a string.
	 */
	private String getHeaderMappingFromSF(){
		String mapping = "";
		String soql = prepareQuery(sobjectName);
		SObject[] srcRecords  = src.queryMultiple(soql, null);
		for(SObject var : srcRecords) {
			mapping =  ForceUtils.getSObjectFieldValue(var, "mapping__c", true);
		}
		return mapping;
	}
	/**
	 * Retrieves the ExternalIdForRelatedObject__c data from Salesforce using a SOQL query and returns it as a string.
	 */
	private String getExternalIdForRelatedObjectFromSF() {
		String ExternalIdWithSobject = "";
		String soql = prepareQueryForExternalId(sobjectName);
		SObject[] srcRecords  = src.queryMultiple(soql, null);
		for(SObject var : srcRecords) {
			ExternalIdWithSobject =  ForceUtils.getSObjectFieldValue(var, "ExternalIdForRelatedObject__c", true);
		}
		return ExternalIdWithSobject;	
	}
	/**
	 * Parses the ExternalIdForRelatedObject__c data retrieved from Salesforce and returns it as a map.
	 * @return A map containing the ExternalIdForRelatedObject__c data parsed from Salesforce.
	 */
	private Map<String,String> parseExternalIdsForRelatedObject(){
		String ExternalIdWithSobject = getExternalIdForRelatedObjectFromSF();
		//System.out.println("ExternalIdWithSobject ---> " + ExternalIdWithSobject);
		Map<String,String> ExternalIdWithSobjectMap = parseMapping(ExternalIdWithSobject);
		return ExternalIdWithSobjectMap;
	}
	/**
	 * Retrieves the list of foreign key fields for the given sObjectName and returns them as a list.
	 * @return A list of foreign key fields for the specified sObjectName.
	 */
	private List<String> getForeignKey() {
		List<String> result = new ArrayList<String>();
		DescribeSObjectResult sobj = src.describeSObject(sobjectName);
		for (Field f : sobj.getFields()) {
			if (f.getReferenceTo()!= null) {
				//System.out.println("Foreign Key :- " + f.getName());
				result.add(f.getName());
			}
		}
		return result;
	}
	/**
	 * Prepares a SOQL query to fetch the 'mapping__c' field value from the 'DataMigrationConfig__c' object based on the provided sobjectName.
	 */
	private String prepareQuery(String sobjectName){
		String soql = "select mapping__c from DataMigrationConfig__c where sObjectConfig__r.name = '" + sobjectName + "'";
		//System.out.println(soql);
		return soql;
	}
	/**
	 * Prepares a SOQL query to fetch the ExternalIdForRelatedObject__c field value from the DataMigrationConfig__c object.
	 */
	private String prepareQueryForExternalId(String sobjectName){
		String soql = "select ExternalIdForRelatedObject__c	from DataMigrationConfig__c where sObjectConfig__r.name = '" + sobjectName + "'";
		//System.out.println(soql);
		return soql;
	}
	/**
	 * Reads data from a CSV file and processes it to create a list of records with a new header.
	 * The method reads the CSV file using CSVUtils.readFile(fileName) and receives a list of string arrays, 
	 * where each array represents a row from the CSV file, including the header row.
	 * It then converts the CSV data into a list of maps, where each map represents a record with the targetHeader.
	 */
	private List<Map<String, String>> getCSVDataWithNewHeader(String[] targetHeader) {
		List<String[]> recordsWithHeader = CSVUtils.readFile(fileName);
		List<Map<String, String>> processedDataList = new ArrayList<Map<String, String>>();
		if(recordsWithHeader.size() == 0) {
			System.out.println("No data found");
			return processedDataList;
		}
		int index = 0;
		
		for (String[] cols : recordsWithHeader) {
			if(index == 0) {
				index += 1;
				continue;
			}
			int j = 0;
			Map<String, String> dataValueMap = new HashMap<String, String>();  //key->field API name, value->field value
			for(String data : cols) {
				dataValueMap.put(targetHeader[j], data);
				j++;
			}
			//System.out.println();
			processedDataList.add(dataValueMap);
		}
		//System.out.println("Final data ----"); // just for display purpose
		for(Map<String, String> row : processedDataList) {
			for(String key : row.keySet()) {
				//System.out.print(key + ":" + row.get(key) + ",");
			}
			//System.out.println();
		}
		return processedDataList;		
	}
	/**
	 * Converts a list of records represented as maps (data) into batches of records with a specified batch size.
	 * Each batch contains a list of maps, where each map represents a single record.
	 */
	private List<List<Map<String, String>>> convertDataIntoBatchLimit( List<Map<String, String>> data){
		List<List<Map<String,String>>> totalRecords = new ArrayList(); // 1 list contain 200 records.
		List<Map<String, String>> processedData = new ArrayList();
		
		for (Map<String, String> row :data ) {	// 300
			processedData.add(row); //Adding record here

			if (processedData.size() == batchSize) {
				totalRecords.add(processedData);	// 200
				processedData = new ArrayList();
			}
		}
		if(processedData.isEmpty() == false) {
			totalRecords.add(processedData);
		}
		return totalRecords;
	}
	/**
	 * Calls a web service using SOAP to perform data migration with a batch of records.
	 * The method sets up a SOAP connection, prepares data for the request, and handles batch processing.
	 */
	private void callWebservice(List<Map<String, String>> data, List<String> foreignKeys) throws Exception {
		SoapConnection con = Connector.newConnection("",  "");
		con.setSessionHeader(src.getSessionId());
		SObjectRequest request = new SObjectRequest();
		Map<String,String> externalIdsForRelatedObjects = parseExternalIdsForRelatedObject();
		SObjectKey[] keyForExternalId = new SObjectKey[externalIdsForRelatedObjects.size()];
		int index = 0;
		for(String key : externalIdsForRelatedObjects.keySet() ) { //Key - sObject and Value - ExternalId	
			keyForExternalId[index] = new SObjectKey();
			keyForExternalId[index].setSobjectName(key);
			keyForExternalId[index].setUniqueFieldNames(new String[] {externalIdsForRelatedObjects.get(key)});
			index++;
		}
		request.setKeys(keyForExternalId);
		
		SObjectTable table1 = new SObjectTable();
		table1.setSobjectName(sobjectName);
		
		List<List<Map<String, String>>> totalRecordsWithBatch = convertDataIntoBatchLimit(data);
		//System.out.println("BtachSize==========================" + totalRecordsWithBatch.size());
		
		for(List<Map<String, String>> currBatch: totalRecordsWithBatch) {
			//200 records batch
			SRecord[] records = new SRecord[currBatch.size()];
			int recordIndex = 0;
			for(Map<String, String> row: currBatch) {
				//1 record
				int fieldIndex = 0;
				SField[] fields = new SField[row.keySet().size()];
				SRecord record = new SRecord();
				for(String key: row.keySet()){
					//Get fields of 1 record
					SField field = new SField();
					field.setFieldName(key);
					field.setFieldValue(row.get(key));
					if ( isForeignKey(key,sobjectName,foreignKeys) == true) {
						//System.out.println("Key >>>>>>>>>>>>>>>>>" + key);
						field.setFieldValue("[" + row.get(key) + "]");
					}
					fields[fieldIndex] = field;
					fieldIndex++;
				}
				record.setFieldValues(fields);
				records[recordIndex] = record;
				recordIndex = recordIndex +1;
			}
			table1.setInsertRecords(records);
			request.setTables(new SObjectTable[] {table1});
			SObjectResponse response = con.handle(request);
			//log.info("response: " + response);
			if ( response.getSuccess()) {
				System.out.println( currBatch.size() + " Records inserted successfully");
			}
			else {
				 log.error("Something went wong, please make sure related data is present in the system. "+ response.getErrorMessage());
		         log.error("Response: "+response);
		         System.out.print("Exception Occured.!! Press Any Key to Exit....");
		          new Scanner(System.in).nextLine();
		          System.exit(-1);
			}
		}
		System.out.println("Data Migration process completed.");
	}
}	

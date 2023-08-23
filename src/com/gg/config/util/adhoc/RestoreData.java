package com.gg.config.util.adhoc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.gg.common.Variables;
import com.lib.util.CSVUtils;
import com.lib.util.FileUtils;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Restores deleted records from recycle bin by comparing with the live records.
 * 
 * @author shahnavazk
 *
 */
public class RestoreData {
	static Logger log = Logger.getRootLogger();
	static String objectName = "ggsTempPack1__LayoutBusinessRuleConfig__c";
	static ForceDelegateRaw gate = ForceDelegateRaw.login("shah@ggp.dev2", "test@1234", false, 26.0);
	static Map<String, SObject> binRecordsMap = new HashMap<String, SObject>();  //key->internal unique id
	static Map<String, SObject> liveRecordsMap = new HashMap<String, SObject>();  //key->internal unique id
	static List<String[]> deletedRecords = new ArrayList<String[]>();
	static List<String[]> undeletedRecords = new ArrayList<String[]>();
	static int deleteSuccess=0, undeleteSuccess=0, deleteFailure=0, undeleteFailure=0;
	static String folder = "D:\\Projects\\Salesforce\\REI\\Projects\\GovGrants\\Product\\Config Migration Issue 2016-10-10\\";
	static boolean isCustomSetting = false;
	static DescribeSObjectResult or;
	static String uniqueIdFieldName;
	
	public static void main(String[] args) {
		try {
			init();
			analyze();
			restore();
			compareWithDemo3();
		}
		finally  {
			createAudit();
		}
	}
	
	private static void init() {
		deletedRecords.add(new String[]{"Id", "Unique Id", "Status", "Error"});
		undeletedRecords.add(new String[]{"Id", "Unique Id", "Status", "Error"});
	}
	/**
	 * Analyzes the Salesforce object with the specified objectName to identify and manage records in the live and bin states.
	 * The method fetches the sObject metadata using gate.describeSObject(objectName).
	 * It determines the uniqueIdFieldName based on whether the sObject is a custom setting or not.
	 * It then constructs and executes the SOQL query using the getSOQL() method to retrieve the relevant records.
	 */
	private static void analyze() {
		or = gate.describeSObject(objectName);
		uniqueIdFieldName = Variables.managedPackageNamespaceSrc + Variables.INTERNAL_UNIQUEID_FIELDNAME;
		if (or.isCustomSetting()) {
			uniqueIdFieldName = "Name";
			isCustomSetting = true;
		}
		
		String soql = getSOQL();
		Boolean duplicateLiveRecordsFound = false;
		SObject[] records = gate.queryAllMultiple(soql, null);
		if (records == null)  log.info("Success..!! Press Any Key to Exit.. ");System.exit(0);
		
		for (SObject record : records) {
			Boolean isDeleted = Boolean.valueOf(ForceUtils.getSObjectFieldValue(record, "IsDeleted"));
			String lastModifiedDate = ForceUtils.getSObjectFieldValue(record, "LastModifiedDate");
			String uniqueId = ForceUtils.getSObjectFieldValue(record, uniqueIdFieldName);
			String id = record.getId();
			
			if (lastModifiedDate.contains("2016-10-10") == false) continue;  //pull the most recent record from bin
			
			if (isDeleted) {
				binRecordsMap.put(uniqueId, record);
			}
			else {
				if (liveRecordsMap.containsKey(uniqueId)) {
					log.info("ERROR: Duplicate live unique key found: " + uniqueId + ". Record1: " + id + 
							", Record2: " + liveRecordsMap.get(uniqueId).getId());
					duplicateLiveRecordsFound = true;
					continue;
				}
				liveRecordsMap.put(uniqueId, record);
			}
		}
		
		if (duplicateLiveRecordsFound) {
			log.info("fix the above errors and rerun this code.");
			return;
		}		
		
		log.info("live records: " + liveRecordsMap.size());
		log.info("bin records: " + binRecordsMap.size());
	}
	/**
	 * Generates and returns the SOQL (Salesforce Object Query Language) query to fetch relevant records
	 * from the specified object based on the defined criteria.
	 *
	 * @return The SOQL query as a String.
	 */
	private static String getSOQL() {
		String soql = "Select Id, IsDeleted, LastModifiedDate, ";
				
		List<String> fields = new ArrayList<String>();
		for (Field f : or.getFields()) {
			if (f.isUpdateable()) {
				fields.add(f.getName());
			}
		}
		soql += StringUtils.getCommaSeparatedString(fields);
		soql += " from " + objectName + " where LastModifiedDate=THIS_MONTH order by " + uniqueIdFieldName + " asc, LastModifiedDate asc";
		
		return soql;
	}
	/**
	 * Restores the records from the bin (recycle bin) to the live state in Salesforce. This method iterates
	 * through the binRecordsMap and for each record, it checks if the corresponding live record exists.
	 */
	private static void restore() {
		int i=1;
		for (String uniqueId : binRecordsMap.keySet()) {
			log.info("------------------ Processing bin record " + i++ + "/" + binRecordsMap.size() + " unique id: " + uniqueId);
			SObject binRecord = binRecordsMap.get(uniqueId);
			SObject liveRecord = liveRecordsMap.get(uniqueId);
			
			try {
				if (liveRecord == null) {
					undeleteBinRecord(binRecord, uniqueId);
				}
				else {
					deleteLiveRecord(liveRecord, uniqueId);
					undeleteBinRecord(binRecord, uniqueId);
				}
			}
			catch (SkipException se) {
				//ignore
			}
		}
	}
	/**
	 * Undeletes the given record from the recycle bin in Salesforce. The method checks if the record is a
	 * custom setting or not and performs the corresponding undelete operation. For custom settings, it
	 * recreates the record using the `createSingle` method, whereas for other standard objects, it uses
	 * the `undelete` method to restore the record from the recycle bin. The process is logged,
	 */
	private static void undeleteBinRecord(SObject binRecord, String uniqueId) {
		if (isCustomSetting) {
			try {
				log.info("Recreating from recycle bin: " + binRecord.getId() + ", Unique Id: " + uniqueId);
				gate.createSingle(binRecord);
				undeletedRecords.add(new String[]{binRecord.getId(), uniqueId, "Success-Insert", ""});
				undeleteSuccess++;
			}
			catch (Exception e) {
				log.error(e.getMessage());
				undeletedRecords.add(new String[]{binRecord.getId(), uniqueId, "Error-Insert", e.getMessage()});
				undeleteFailure++;
			}
		}
		else {
			try {
				log.info("Undeleting from recycle bin: " + binRecord.getId() + ", Unique Id: " + uniqueId);
				gate.undelete(new String[]{binRecord.getId()});
				undeletedRecords.add(new String[]{binRecord.getId(), uniqueId, "Success-Undelete", ""});
				undeleteSuccess++;
			}
			catch (Exception e) {
				log.error(e.getMessage());
				undeletedRecords.add(new String[]{binRecord.getId(), uniqueId, "Error-Undelete", e.getMessage()});
				undeleteFailure++;
			}
		}
	}
	/**
	 * Deletes the given live record from Salesforce. If the record is a custom setting, it updates the
	 * record using the `updateLiveRecord` method before deletion. The process is logged, and the result
	 * of each operation is added to the `deletedRecords` list for later analysis. If an error occurs
	 * during deletion, a `SkipException` is thrown to signal that the deletion should be skipped.
	 */
	private static void deleteLiveRecord(SObject liveRecord, String uniqueId) {
		try {
			if (isCustomSetting) {
				updateLiveRecord(liveRecord, uniqueId);
			}
			
			log.info("Deleting live record: " + liveRecord.getId());
			List<String> liveIds = new ArrayList<String>();
			liveIds.add(liveRecord.getId());
			gate.delete(liveIds);
			deletedRecords.add(new String[]{liveRecord.getId(), uniqueId, "Success-Delete", ""});
			deleteSuccess++;
		}
		catch (Exception e) {
			log.error(e.getMessage());
			deletedRecords.add(new String[]{liveRecord.getId(), uniqueId, "Error-Delete", e.getMessage()});
			deleteFailure++;
			throw new SkipException();
		}
	}
	
	/**
	 * Updates the given live record in Salesforce to change its unique identifier. This method is
	 * specifically designed for custom settings. The record's "Name" field is modified to incorporate
	 * an "_X" suffix with the new unique identifier. 
	 */
	private static void updateLiveRecord(SObject liveRecord, String uniqueId) {
		try {
			log.info("Update live record to change unique id");
			ForceUtils.setSObjectFieldValue(liveRecord, "Name", uniqueId + "_X");
			gate.updateSingle(liveRecord);
		}
		catch (Exception e) {
			log.error(e.getMessage());
			deletedRecords.add(new String[]{liveRecord.getId(), uniqueId, "Error-Update", e.getMessage()});
			deleteFailure++;
			throw new SkipException();
		}
	}
	/**
	 * Creates audit CSV files for deleted and undeleted records. The deleted records are saved to a CSV file
	 * named "{objectName}_deletedRecords.csv", and the undeleted records are saved to a CSV file named
	 * "{objectName}_undeletedRecords.csv". 
	 */
	private static void createAudit() {
		CSVUtils.createFile(folder + objectName + "_deletedRecords.csv", deletedRecords);
		CSVUtils.createFile(folder + objectName + "_undeletedRecords.csv", undeletedRecords);
	}
	/**
	 * Compares the current records count in the target organization with demo3 and test1 organizations.
	 * The method first queries the total count of records for the specified `objectName` in the target organization.
	 */
	private static void compareWithDemo3() {
		String soql = "Select count() from " + objectName;
		Integer devCnt = gate.queryCount(soql, null);
		
		ForceDelegateRaw demo = ForceDelegateRaw.login("shah@ggp.demo3", "test@123", false, 26.0);
		Integer demoCnt = demo.queryCount(soql, null);

		ForceDelegateRaw test1 = ForceDelegateRaw.login("shah@ggp.test1", "test@123", false, 26.0);
		Integer testCnt = test1.queryCount(soql, null);

		String msg = "Total records deleted: " + deleteSuccess + "\n" +
				"Total records undeleted: " + undeleteSuccess + "\n" +
				"Total records failed to delete: " + deleteFailure + "\n" +
				"Total records failed to undelete: " + undeleteFailure + "\n\n" +				
				"After the above operations, following is the current metrics:\ndev2: " + devCnt + " records\ntest1: " + testCnt + " records\ndemo3: " + demoCnt + " records";
		log.info(msg);
		FileUtils.createFile(new File(folder + objectName + "_Summary.txt"), msg);		
	}
}

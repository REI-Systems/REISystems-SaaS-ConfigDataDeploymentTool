package com.gg.config.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.force.service.raw.ForceDelegateRaw;
import com.gg.common.Variables;
import com.gg.config.util.AppUtils;
import com.gg.config.vo.MigrationItem;
import com.lib.util.CSVUtils;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;

public class SObjectDiff extends Variables {
	private MigrationItem item;
	private Map<String, SObject> srcRecordMap; //key->unique id
	private Map<String, SObject> targetRecordMap;  //key->unique id
	private String[] headerRow;
	private List<String> compareFields = new ArrayList<String>();
	private List<String[]> differences = new ArrayList<String[]>();

	public SObjectDiff(MigrationItem item) {
		this.item = item;
	}

	public static void main(String[] args) {
		new SObjectDiff(new MigrationItem("ApprovalDecisionConfig__c", true)).compare();
	}

	public void compare() {
		log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		log.info("SObjectDiff starts for " + item.getSrcObjectName());
		init();
		execute();
		createResultFiles();
		log.info("SObjectDiff ends for " + item.getSrcObjectName());		
	}
	
	/* PRIVATE METHODS */
	
	private void init() {
		identifyComparableFields();
		String soql = prepareSOQL();
		SObject[] srcRecords = src.queryMultiple(soql, null);
		srcRecordMap = prepareRecordMap(srcRecords);
		
		SObject[] targetRecords = target.queryMultiple(soql, null);
		targetRecordMap = prepareRecordMap(targetRecords);
		prepareHeaderRow();		
	}
	
	private Map<String, SObject> prepareRecordMap(SObject[] records) {
		Map<String, SObject> recordMap = new HashMap<String, SObject>();  //key->unique id
		if (records != null) {
			for (SObject record : records) {
				String[] uniqueKeys = AppUtils.getUniqueIdFieldName(item.getObjectNameWithoutNamespace(), managedPackageNamespaceSrc, src);
				String uniqueId = AppUtils.findUniqueFieldValue(record, uniqueKeys, true);
				recordMap.put(uniqueId, record);
			}
		}
		return recordMap;
	}
	
	private void identifyComparableFields() {
		DescribeSObjectResult or = src.describeSObject(item.getSrcObjectName());
		for (Field f : or.getFields()) {
			if (f.isUpdateable() && !f.getName().equals("SetupOwnerId") && 
					!AppUtils.isFieldExcludedFromMigration(item, f) &&
					f.getReferenceTo() == null) {
				compareFields.add(f.getName());
			}
		}		
	}
	
	private String prepareSOQL() {
		String soql = "Select Id, CreatedDate, CreatedById, LastModifiedDate, LastModifiedById, ";
		soql += StringUtils.getCommaSeparatedString(compareFields) + " from " + item.getSrcObjectName();
		log.info("SOQL: " + soql);
		
		return soql;
	}
	
	private void prepareHeaderRow() {
		List<String> header = new ArrayList<String>();
		header.add("Org");
		header.add("Difference Type");
		header.add("Id");
		header.add("Internal Unique Id");

		for (String field : compareFields) {
			header.add(field);
		}
		
		header.add("Created By");
		header.add("Created Date");
		header.add("Last Modified By");
		header.add("Last Modified Date");
		
		this.headerRow = StringUtils.getStringArrayFromList(header);
	}
	
	private void execute() {
		String[] uniqueFieldNames = AppUtils.getUniqueIdFieldName(item.getObjectNameWithoutNamespace(), Variables.managedPackageNamespaceSrc, src);
		for (String uniqueId : srcRecordMap.keySet()) {
			SObject srcRecord = srcRecordMap.get(uniqueId);
			SObject targetRecord = targetRecordMap.get(uniqueId);
			RecordDiff diff = new RecordDiff(srcRecord, targetRecord, uniqueId, uniqueFieldNames, compareFields);
			List<String[]> diffRows = diff.compare();
			if (diffRows.size() > 0) {
				differences.addAll(diffRows);
			}
		}
	}
	
	private void createResultFiles() {
		if (differences.size() > 0) {
			List<String[]> result = new ArrayList<String[]>();
			result.add(headerRow);
			result.addAll(differences);
			CSVUtils.createFile(OrgDiff.result_folder + item.getSrcObjectName() + "-Diff.csv", result);
		}
		else {
			log.info("No differences found for " + item.getSrcObjectName());
		}
	}	
}

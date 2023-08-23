package com.gg.config.vo;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.force.service.ForceUtils;
import com.force.service.raw.FieldWrapper;
import com.force.service.raw.ForceDelegateRaw;
import com.force.service.raw.SObjectWrapper;
import com.gg.common.Variables;
import com.gg.config.util.AppUtils;
import com.gg.config.util.QueryTool;
import com.lib.util.CSVUtils;
import com.lib.util.DateUtils;
import com.lib.util.StringUtils;
import com.lib.util.UnCheckedException;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;

/**
 * For a given sobject name, it loads all (or filtered) records from database and keeps them in memory for later usage.
 * 
 * @author shahnavazk
 *
 */
public class SObjectDataHolder {
	private MigrationItem mitem;
	private boolean isSrcOrg = false;
	private ForceDelegateRaw gateRaw;
	private List<String> filterByRecordIds;  //if set, then only those records are loaded in the cache
	private Set<String> filterByUniqueIds;
	private List<FieldItem> excludeFields;
	private List<String> excludeFieldsWithNamespace = new ArrayList<String>();
	
	private Map<String, Field> fieldDescMap;
	private String[] uniqueFieldNamesWithNamespace;
	public QueryTool qtool;	
	
	private Logger log = Logger.getRootLogger();
	
	public SObjectDataHolder(MigrationItem mitem, boolean srcOrg, List<String> filterByRecordIds, Set<String> filterByUniquqIds, ForceDelegateRaw gateRaw) {
		this.mitem = mitem;
		this.isSrcOrg = srcOrg;
		this.filterByRecordIds = filterByRecordIds;
		this.filterByUniqueIds = filterByUniquqIds;
		this.gateRaw = gateRaw;
		init();
		fetchRecords();
	}
	
	public void merge(QueryTool otherQTool) {
		this.qtool.recordList.addAll(otherQTool.recordList);
		this.qtool.recordMap.putAll(otherQTool.recordMap);
		this.qtool.keyMap.putAll(otherQTool.keyMap);		
	}
	
	public SObjectWrapper getRecordUsingUniqueKeyArray(String[] uniqueKeys) {
		String key = StringUtils.getConcatenatedString(uniqueKeys, "#");
		return qtool.recordMap.get(key);
	}

	public SObjectWrapper getRecordUsingUniqueKeyString(String uniqueKeyString) {
		return qtool.recordMap.get(uniqueKeyString);
	}

	public boolean isStandardObject() {
		return mitem.getSrcObjectName().endsWith("__c") == false;
	}
	
	public SObjectWrapper getRecord(String recordId) {
		String key = qtool.keyMap.get(recordId);
		if (key == null) {
			log.error("Record not found for id " + recordId + " in sobject " + mitem.toString() + ", org: " + gateRaw.getOrgName());
			throw new UnCheckedException("Record not found for id " + recordId + " in sobject " + mitem.toString() + ", org: " + gateRaw.getOrgName());
		}
		return qtool.recordMap.get(key);
	}
	
	public List<String> getReferenceFieldValues(String fieldNameWithNamespace) {
		List<String> fieldValues = new ArrayList<String>();
		for (SObjectWrapper soWrapper : qtool.recordMap.values()) {
			FieldWrapper fw = soWrapper.getField(fieldNameWithNamespace);
			if (fw != null) {
				String fieldValue = fw.getValueAsString(false); 
				if (StringUtils.isNonEmpty(fieldValue) && fieldValues.contains(fieldValue) == false && qtool.keyMap.containsKey(fieldValue) == false) {
					fieldValues.add(fieldValue);
				}
			}
		}
		return fieldValues;
	}
	
	public Set<String> getUniqueFieldValues() {
		return qtool.recordMap.keySet();
	}
	
	public List<SObjectWrapper> getRecords() {
		return qtool.recordList;
	}
	
	public Map<String, SObjectWrapper> getRecordsMap() {
		return qtool.recordMap;
	}
	
	public int getTotalRecords() {
		return qtool.recordMap.size();
	}
	
	public String[] getUniqueFieldNamesWithNamespace() {
		return uniqueFieldNamesWithNamespace;
	}
	
	public void createBackupFile() {
		String sobjectName = null;
		if (isSrcOrg) {
			sobjectName = mitem.getSrcObjectName();
		}
		else {
			sobjectName = mitem.getTargetObjectName();
		}
		DescribeSObjectResult or = gateRaw.describeSObject(sobjectName);
		List<List<String>> rows = new ArrayList<List<String>>();
		List<String> headerValues = new ArrayList<String>();
		
		for (Field fd : or.getFields()) {
			headerValues.add(fd.getName());
		}
		rows.add(headerValues);
		
		if (qtool.recordList != null) {
			for (SObjectWrapper owrap : qtool.recordList) {
				SObject record = owrap.getRecord(); 
				
				List<String> fieldValues = new ArrayList<String>();
				for (Field fd : or.getFields()) {
					String data = ForceUtils.getSObjectFieldValue(record, fd.getName(), true);
					if (fd.getName().equalsIgnoreCase("Id")) {
						data = owrap.getId();
					}
					fieldValues.add(data);
				}
				
				rows.add(fieldValues);
			}
		}
		
		List<String[]> result = new ArrayList<String[]>();
		for (List<String> row : rows) {
			String[] cols = new String[or.getFields().length];
			int i=0;
			for (String col : row) {
				cols[i++] = col;
			}
			result.add(cols);
		}
		
		String folder = Variables.backUpFolder;
		if (new File(folder).exists()) {
			CSVUtils.createFile(folder + getFilename(), result);
		}//shubhangi added else block to auto create backup folder 
		else {
			boolean flag = new File(folder).mkdirs();
			if(flag) {
				CSVUtils.createFile(folder + getFilename(), result);
			}
		}
	}
	
	/* PRIVATE METHODS */
	
	private void init() {
		this.excludeFields = mitem.getExcludeFields();
		prepareExcludeFields();		

		this.fieldDescMap = gateRaw.getFieldsMetaData(mitem.getObjectName(isSrcOrg));
		
		//Remove excluded fields from the meta data
		Map<String, Field> tempFieldDescMap = new HashMap<String, Field>();
		for (Field f : fieldDescMap.values()) {
			if (AppUtils.isFieldExcludedFromMigration(mitem, f)) {
				if (excludeFieldsWithNamespace.contains(f.getName()) == false) {
					excludeFieldsWithNamespace.add(f.getName());
				}
			}
			else {
				tempFieldDescMap.put(f.getName(), f);
			}			
		}
		this.fieldDescMap = tempFieldDescMap;
	}
	
	private void fetchRecords() {
		//identify the unique field names for the sobject
		this.uniqueFieldNamesWithNamespace = getUniqueFields();
		String whereClause = mitem.getWhereClause();
		if (isSrcOrg == false) {
			whereClause = "";
		}
		this.qtool = new QueryTool(mitem.getObjectName(isSrcOrg), whereClause, excludeFieldsWithNamespace, filterByRecordIds, filterByUniqueIds, 
						gateRaw, fieldDescMap, uniqueFieldNamesWithNamespace);
		qtool.query();
	}

	private String[] getUniqueFields() {
		String ns = "";
		if (mitem.isPackaged()) {
			ns = isSrcOrg ? Variables.managedPackageNamespaceSrc : Variables.managedPackageNamespaceTarget; 
		}
		return AppUtils.getUniqueIdFieldName(mitem.getObjectNameWithoutNamespace(), ns, gateRaw);
	}	

	private void prepareExcludeFields() {
		if (excludeFields != null) {
			for (FieldItem field : excludeFields) {
				if (excludeFieldsWithNamespace.contains(field.getSrcFieldName()) == false) {
					excludeFieldsWithNamespace.add(field.getSrcFieldName());
				}
			}
		}
	}
	
	private String getFilename() {
		String fileName = mitem.getTargetObjectName();
		if (isSrcOrg) {
			fileName += ".src.";
		}
		else {
			fileName += ".target.";
		}
		fileName += DateUtils.convertDateToString(Calendar.getInstance().getTime(), "yyyy-MM-dd_hh-mm");
		fileName += ".csv";
		return fileName;
	}
	
	public static void main(String[] args) {

	}
	
}

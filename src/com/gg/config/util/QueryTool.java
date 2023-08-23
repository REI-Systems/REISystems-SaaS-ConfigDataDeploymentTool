package com.gg.config.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.force.service.raw.SObjectWrapper;
import com.gg.common.Variables;
import com.lib.util.StringUtils;
import com.lib.util.UnCheckedException;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;

public class QueryTool {
	private String sobjectName;
	private String whereClause;
	private List<String> excludeFields;
	private List<String> filterByRecordIds; 
	private Set<String> filterByUniqueIds;
	private ForceDelegateRaw gateRaw;	
	private Map<String, Field> fieldDescMap;
	private String[] uniqueFields;
	
	public List<SObjectWrapper> recordList = new ArrayList<SObjectWrapper>();
	public Map<String, SObjectWrapper> recordMap = new HashMap<String, SObjectWrapper>();  //key->unique key concatenated with # symbol
	public Map<String, String> keyMap = new HashMap<String, String>();  //key->record id, value->unique key concatenated with # symbol
	
	private Logger log = Logger.getRootLogger();

	public QueryTool(String sobjectName, String whereClause, List<String> excludeFields, List<String> filterByRecordIds, Set<String> filterByUniquqIds, 
					ForceDelegateRaw gateRaw, Map<String, Field> fieldDescMap, String[] uniqueFields) {
		this.sobjectName = sobjectName;
		this.whereClause = whereClause;
		this.excludeFields = excludeFields;
		this.filterByRecordIds = filterByRecordIds;
		this.filterByUniqueIds = filterByUniquqIds;
		this.gateRaw = gateRaw;
		this.fieldDescMap = fieldDescMap;
		this.uniqueFields = uniqueFields;
		init();
	}
	/**
	 * Queries records based on various filters such as filterByRecordIds, filterByUniqueIds, and sobjectName.
	 * It loads records in batches of 200 to optimize performance for large datasets.
	 * The fetched records are stored in the recordList.
	 * After querying, the method logs the number of records loaded for the specified sobjectName.
	 */
	public void query() {
		//load records by filter record ids
		if (filterByRecordIds != null && filterByRecordIds.size() > 0) {
			List<String> recordIds = new ArrayList<String>();
			for (String recordId : filterByRecordIds) {
				recordIds.add(recordId);
				if (recordIds.size() == 200) {
					fetchRecordsByRecordIds(recordIds);
					recordIds.clear();
				}
			}
			if (recordIds.size() > 0) {
				fetchRecordsByRecordIds(recordIds);
				recordIds.clear();
			}
		}
		else if (sobjectName.equalsIgnoreCase("RecordType")) {
			fetchRecordsForRecordType();
		}
		//load records by filter unique ids
		else if (filterByUniqueIds != null && filterByUniqueIds.size() > 0) {
			List<String> uniqueIds = new ArrayList<String>();
			for (String uniqueId : filterByUniqueIds) {
				uniqueIds.add(uniqueId);
				if (uniqueIds.size() == 200) {
					fetchRecordsByUniqueIds(uniqueIds);
					uniqueIds.clear();
				}
			}
			if (uniqueIds.size() > 0) {
				fetchRecordsByUniqueIds(uniqueIds);
				uniqueIds.clear();
			}
		}
		else {
			fetchRecordsByOtherFilter();
		}
		
		log.info(gateRaw.getOrgName() + " - " + sobjectName + " - Total records loaded: " + recordList.size());		
	}
	
	/* PRIVATE METHODS */
	/**
	 * Initializes the object instance by processing the whereClause property.
	 * If the whereClause is null, it is set to an empty string to prevent NullPointerExceptions.
	 */
	private void init() {
		if (whereClause == null) whereClause = "";
		if (StringUtils.isNonEmpty(whereClause)) {
			if (isTargetOrg()) {
				whereClause = whereClause.replace(Variables.SRC_NAMESPACE, Variables.TAR_NAMESPACE);
			}
		}
	}
	/**
	 * Fetches records from the "RecordType" object based on the specified filters in filterByUniqueIds.
	 * The method constructs a SOQL query to retrieve the required fields for the RecordType object.
	 */
	private void fetchRecordsForRecordType() {
		String soql = "Select Name, IsActive, DeveloperName, Id, SobjectType from RecordType";  
		if (filterByUniqueIds != null && filterByUniqueIds.size() > 0) {
			String sobjectType = null;
			List<String> developerNames = new ArrayList<String>();
			for (String filterByUniqueId : filterByUniqueIds) {
				String[] arr = filterByUniqueId.split("#");
				sobjectType = arr[0];
				developerNames.add(arr[1]);
			}
			soql += " where SObjectType='" + sobjectType + "' and DeveloperName in ('" + StringUtils.getConcatenatedString(developerNames, "','") + "')";
			log.info("sobjectType >>"+sobjectType);
			if(sobjectType.equalsIgnoreCase("account")){
				soql += " AND NameSpacePrefix != 'GNT' ";
				log.info("soql >>"+soql);
			}
		}
		
		soql = addOrderByClause(sobjectName, soql);
		queryIt(soql);
	}
	/**
	 * Fetches records from the specified sobjectName using a list of recordIds as a filter.
	 */
	private void fetchRecordsByRecordIds(List<String> recordIds) {
		String tempWhereClause = whereClause;
		if (StringUtils.isNonEmpty(tempWhereClause)) {
			tempWhereClause += " and ";
		}
		tempWhereClause += " Id in ('" + StringUtils.getConcatenatedString(recordIds, "','") + "')";

		String soql = ForceUtils.prepareSOQL(gateRaw, sobjectName, false, true, true, true, false, 
							excludeFields, tempWhereClause);
		soql = addOrderByClause(sobjectName, soql);
		queryIt(soql);
	}	
	/**
	 * Fetches records from the specified sobjectName using a list of uniqueIds as a filter.
	 * The method constructs a temporary whereClause by appending the uniqueIds to the existing whereClause.
	 */
	private void fetchRecordsByUniqueIds(List<String> uniqueIds) {
		String tempWhereClause = whereClause;
		if (StringUtils.isNonEmpty(tempWhereClause)) {
			tempWhereClause += " and ";
		}
		tempWhereClause += " " + uniqueFields[0] + " in ('" + StringUtils.getConcatenatedString(uniqueIds, "','") + "')";

		String soql = ForceUtils.prepareSOQL(gateRaw, sobjectName, false, true, true, true, false, 
							excludeFields, tempWhereClause);
		soql = addOrderByClause(sobjectName, soql);
		queryIt(soql);
	}
	/**
	 * Fetches records from the specified sobjectName based on other filters provided in the whereClause.
	 * The SOQL query is prepared using the sobjectName, excludeFields, and whereClause.
	 */
	private void fetchRecordsByOtherFilter() {
		String soql = ForceUtils.prepareSOQL(gateRaw, sobjectName, false, false, true, true, false, 
							excludeFields, whereClause);
		if (soql.toLowerCase().contains("order by") == false) {
			if (soql.contains("limit ")) {
				soql = soql.replace(" limit ", " order by CreatedDate limit ");
			}
			else {
				soql = addOrderByClause(sobjectName, soql);
			}
		}
		queryIt(soql);
	}
	/**
	 * Adds an "ORDER BY" clause to the SOQL query based on the sObjectName.
	 * If the sObjectName is not "UserRole" or "Profile", the method appends "ORDER BY CreatedDate" to the SOQL query.
	 */
	private String addOrderByClause(String sObjectName, String soql) {
		if (sobjectName.equalsIgnoreCase("UserRole") == false && sobjectName.equalsIgnoreCase("Profile") == false) {
			soql += " order by CreatedDate";
		}
		return soql;
	}
	/**
	 * Executes the SOQL query provided as a parameter using the gateRaw object to perform the query.
	 * The method queries the records in the Salesforce organization using the SOQL query and retrieves SObject records.
	 * It then wraps each SObject record into an SObjectWrapper and adds it to the recordMap and recordList.
	 */
	private void queryIt(String soql) {
		SObject[] records;
		try {
			records = gateRaw.queryMultiple(soql, new Object[]{});
		}
		catch (Exception e) {
			log.error("Query failed in org " + gateRaw.getOrgName() + ", query: " + soql);
			throw new UnCheckedException(e.getMessage() + ", query: " + soql);
		}
		
		if (records == null) return;
				
		for (SObject record : records) {
			SObjectWrapper recordWrapper = new SObjectWrapper(record, fieldDescMap);
			String uniqueKey = AppUtils.findUniqueFieldValue(recordWrapper.getRecord(), uniqueFields, isSourceOrg());
			
			if (uniqueKey != null && recordMap.containsKey(uniqueKey) == false) {
				recordMap.put(uniqueKey,  recordWrapper);
				recordList.add(recordWrapper);
				keyMap.put(record.getId(), uniqueKey);
			}
			/*if(isSourceOrg()){
				System.out.println("test sarat record - " + record);
				System.out.println("test sarat record wrapper  - " + recordWrapper);
				System.out.println("test sarat force " + ForceUtils.getSObjectFieldValue(record, "GNT__QuickSearchBehaviour__c", true));
			}*/
		}
	}

	private Boolean isSourceOrg() {
		return gateRaw.getOrgName().equalsIgnoreCase(Variables.src.getOrgName());
	}

	private Boolean isTargetOrg() {
		return gateRaw.getOrgName().equalsIgnoreCase(Variables.target.getOrgName());
	}

	
}

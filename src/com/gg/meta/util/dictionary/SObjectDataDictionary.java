package com.gg.meta.util.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.lib.util.CSVUtils;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;

public class SObjectDataDictionary {
	private DescribeSObjectResult obj;
	private String folder;
	private List<String[]> result = new ArrayList<String[]>();
	private String filename;
	private Map<String, Set<String>> labelMap = new HashMap<String, Set<String>>();  //key->field name
	private Map<String, Set<String>> helpMap = new HashMap<String, Set<String>>();  //key->field name
	private ForceDelegateRaw src;
	private static List<String> ignoreFields = new ArrayList<String>();
	
	static {
		ignoreFields.add("Id");
		ignoreFields.add("IsDeleted");
		ignoreFields.add("MasterRecordId");
		ignoreFields.add("RecordTypeId");
		ignoreFields.add("OwnerId");
		ignoreFields.add("CreatedDate");
		ignoreFields.add("CreatedById");
		ignoreFields.add("LastModifiedDate");
		ignoreFields.add("LastModifiedById");
		ignoreFields.add("SystemModstamp");
		ignoreFields.add("LastActivityDate");
		ignoreFields.add("Jigsaw");
		ignoreFields.add("JigsawCompanyId");
	}
	
	public SObjectDataDictionary(DescribeSObjectResult obj, String folder,ForceDelegateRaw src) {
		this.obj = obj;
		this.folder = folder; 
		this.src = src;
	}
	/**
	 * Checks if an SObject should be ignored for data dictionary generation.
	 */
	public void generate() {
		this.filename = obj.getLabel().replace("/", " ").replace(":", " ") + " - Data Dictionary.csv";
		result.add(new String[]{"Object Name", "Field Name", "Data Type", "Field Label - Native", "Field Label - Page", "Help Text - Native",
					"Help Text - Page"});
		fetchLayoutDetails();
		for (Field f : obj.getFields()) {
			handleField(f);
		}
		
		CSVUtils.createFile(folder+filename, result);
	}
	/**
	 * Fetches layout details for the SObject.
	 */
	private void fetchLayoutDetails() {
		String soql = "Select GNT__FieldAPIName__c, GNT__FieldLabelOverride__c, GNT__HelpTextLong__c, GNT__HelpText__c, "
				+ "GNT__HelpTextPost__c from GNT__PageBlockDetailConfig__c where "
				+ "GNT__PageBlockConfig__r.GNT__TabLayoutConfig__r.GNT__PageLayoutConfig__r.GNT__ObjectAPIName__c=?";
		SObject[] records = src.queryMultiple(soql, new Object[]{obj.getName()});
		if (records == null) return;
		
		for (SObject record : records) {
			String fieldName = ForceUtils.getSObjectFieldValue(record, "GNT__FieldAPIName__c");
			String label = ForceUtils.getSObjectFieldValue(record, "GNT__FieldLabelOverride__c");
			String help1 = ForceUtils.getSObjectFieldValue(record, "GNT__HelpTextLong__c");
			String help2 = ForceUtils.getSObjectFieldValue(record, "GNT__HelpText__c");
			String help3 = ForceUtils.getSObjectFieldValue(record, "GNT__HelpTextPost__c");
			
			Set<String> innerSet = labelMap.get(fieldName);
			if (innerSet == null) {
				innerSet = new HashSet<String>();
				labelMap.put(fieldName, innerSet);
			}
			if (StringUtils.isNonEmpty(label)) {
				innerSet.add(label);
			}
			
			innerSet = helpMap.get(fieldName);
			if (innerSet == null) {
				innerSet = new HashSet<String>();
				helpMap.put(fieldName, innerSet);
			}
			if (StringUtils.isNonEmpty(help1)) {
				innerSet.add(help1);
			}
			if (StringUtils.isNonEmpty(help2)) {
				innerSet.add(help2);
			}
			if (StringUtils.isNonEmpty(help3)) {
				innerSet.add(help3);
			}
		}
	}
	/**
	 * Handles a field of the SObject during the generation of the data dictionary.
       This method processes each field of the given SObject and collects relevant information to populate the data dictionary.
	 * @param f
	 */
	private void handleField(Field f) {
		if (ignoreFields.contains(f.getName())) return;
		
		List<String> cols = new ArrayList<String>();
		cols.add(obj.getName());
		cols.add(f.getName());
		cols.add(f.getType().getValue());
		
		cols.add(f.getLabel());
		Set<String> pageLabels = labelMap.get(f.getName());
		if (pageLabels == null) pageLabels = new HashSet<String>();
		cols.add(StringUtils.removeNull(StringUtils.getConcatenatedString(pageLabels, "\n")));
		
		cols.add(StringUtils.removeNull(f.getInlineHelpText()));
		Set<String> pageHelps = helpMap.get(f.getName());
		if (pageHelps == null) pageHelps = new HashSet<String>();
		cols.add(StringUtils.removeNull(StringUtils.getConcatenatedString(pageHelps, "\n")));
		
		result.add(StringUtils.getStringArrayFromList(cols));
		
	}
}


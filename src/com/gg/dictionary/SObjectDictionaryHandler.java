package com.gg.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.force.service.ForceDelegate;
import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;

public class SObjectDictionaryHandler {
	private String sobjectName;
	private DescribeSObjectResult obj;
	private ForceDelegateRaw connRaw;
	private ForceDelegate connCustom;
	private static List<String> ignoreFields = new ArrayList<String>();
	private DictionarySObjectVO header;
	private Map<String, DictionaryFieldVO> fieldMap;
	
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
	
	public SObjectDictionaryHandler(String sobjectName, ForceDelegateRaw connRaw, ForceDelegate connCustom) {
		this.sobjectName = sobjectName;
		this.obj = connRaw.describeSObject(sobjectName);
		this.connRaw = connRaw;
		this.connCustom = connCustom;
	}
	
	public void generate() {
		createHeader();
		createFields();
		createLayouts();
		deleteUnusedFields();
	}
	
	public static void main(String[] args) {
		ForceDelegateRaw connRaw = ForceDelegateRaw.login("src");
		ForceDelegate connCustom = ForceDelegate.login("src");
		new SObjectDictionaryHandler("BuildUpItem__c", connRaw, connCustom).generate();
	}
	
	/* PRIVATE METHODS */
	
	
	private void createHeader() {
		this.header = new DictionarySObjectVO();
		header.setName(sobjectName);
		header.setSObjectLabel__c(obj.getLabel());
		String headerId = connCustom.createSingle("Name, SObjectLabel__c", header);
		this.header.setId(headerId);
	}
	/**
	 * Creates dictionary fields based on the metadata of the Salesforce object.
	 */
	private void createFields() {
		List<DictionaryFieldVO> fields = new ArrayList<DictionaryFieldVO>();
		fieldMap = new HashMap<String, DictionaryFieldVO>();
		
		for (Field f : obj.getFields()) {
			if (ignoreFields.contains(f.getName())) continue;
			
			DictionaryFieldVO field = new DictionaryFieldVO();
			field.setDataType__c(f.getType().getValue());
			field.setDictionarySObject__c(header.getId());
			field.setName(f.getName());
			field.setNativeLabel__c(f.getLabel());
			field.setNativeHelpText__c(f.getInlineHelpText());
			
			fields.add(field);
			fieldMap.put(f.getName(), field);
		}
		
		connCustom.createMultiple("Name, DataType__c, DictionarySObject__c, NativeHelpText__c, NativeLabel__c", fields);
	}
	/**
	 * Creates dictionary layouts for the corresponding Salesforce object based on page layout configuration.
	 * 
	 */
	private void createLayouts() {
		String soql = "Select Id, GNT__PageLayoutId__c, GNT__FieldAPIName__c, GNT__FieldLabelOverride__c, GNT__HelpTextLong__c, GNT__HelpText__c "
				+  "from GNT__PageBlockDetailConfig__c where "
				+ "GNT__PageBlockConfig__r.GNT__TabLayoutConfig__r.GNT__PageLayoutConfig__r.GNT__ObjectAPIName__c=?";
		SObject[] records = connRaw.queryMultiple(soql, new Object[]{obj.getName()});
		if (records == null) return;
		
		List<DictionaryLayoutVO> layouts = new ArrayList<DictionaryLayoutVO>();
		for (SObject record : records) {
			String blockDetailId = ForceUtils.getSObjectFieldValue(record, "Id");
			String layoutId = ForceUtils.getSObjectFieldValue(record, "GNT__PageLayoutId__c");
			String fieldName = ForceUtils.getSObjectFieldValue(record, "GNT__FieldAPIName__c");
			String label = ForceUtils.getSObjectFieldValue(record, "GNT__FieldLabelOverride__c");
			String help1 = ForceUtils.getSObjectFieldValue(record, "GNT__HelpTextLong__c");
			String help2 = ForceUtils.getSObjectFieldValue(record, "GNT__HelpText__c");

			DictionaryFieldVO field = fieldMap.get(fieldName);
			if (field == null) continue;
			
			DictionaryLayoutVO layout = new DictionaryLayoutVO();
			layout.setField__c(field.getId());
			
			layout.setFieldLabel__c(label);
			if (StringUtils.isEmpty(label)) {
				layout.setFieldLabelType__c("Native");
				layout.setFieldLabel__c(field.getNativeLabel__c());
			}
			else {
				layout.setFieldLabelType__c("Custom");
			}
			
			layout.setHelpText__c(StringUtils.isNonEmpty(help1) ? help1 : help2);
			if (StringUtils.isEmpty(layout.getHelpText__c())) {
				layout.setHelpText__c(field.getNativeHelpText__c());
				if (StringUtils.isNonEmpty(layout.getHelpText__c())) {
					layout.setHelpTextType__c("Native");
				}
			}
			else {
				layout.setHelpTextType__c("Custom");
			}
			
			layout.setPageBlockDetail__c(blockDetailId);
			layout.setPageLayout__c(layoutId);
			layouts.add(layout);			
		}
		connCustom.createMultiple("Field__c, FieldLabel__c, FieldLabelType__c, HelpText__c, HelpTextType__c, PageBlockDetail__c, PageLayout__c", layouts);
	}
	
	private void deleteUnusedFields() {
		connCustom.delete("Select Id from DictionaryField__c where DictionarySObject__c=? and TotalLayoutsReferenced__c=0", new Object[]{header.getId()});
	}
}


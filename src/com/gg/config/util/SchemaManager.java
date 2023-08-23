package com.gg.config.util;

import java.util.HashMap;
import java.util.Map;

import com.force.service.raw.ForceDelegateRaw;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;

public abstract class SchemaManager {
	private Map<String, Map<String, Field>> sobjectFieldMap = new HashMap<String, Map<String, Field>>();  //key->object name, inner key->field name
	private Map<String, DescribeSObjectResult> sobjectMap = new HashMap<String, DescribeSObjectResult>(); //key->sobject name
	private ForceDelegateRaw gate;
	
	public SchemaManager(ForceDelegateRaw gate) {
		this.gate = gate;
		load(gate);
	}
	/**
	 * Retrieves the fields metadata for the specified Salesforce sObject.
	 * The method checks if the field metadata for the given sObject is already cached in the sobjectFieldMap.
	 */
	public Map<String, Field> getFields(String sobjectName) {
		Map<String, Field> fieldRes = sobjectFieldMap.get(sobjectName);
		if (fieldRes == null) {
			fieldRes = gate.getFieldsMetaData(sobjectName);
			sobjectFieldMap.put(sobjectName, fieldRes);
		}
		return fieldRes;
	}
	/**
	 * Retrieves the DescribeSObjectResult for the specified Salesforce sObject.
	 * The method checks if the DescribeSObjectResult for the given sObject is already cached in the sobjectMap.
	 */
	public DescribeSObjectResult getSObject(String sobjectName) {
		DescribeSObjectResult or = sobjectMap.get(sobjectName);
		if (or == null) {
			or = gate.describeSObject(sobjectName);
			sobjectMap.put(sobjectName, or);
		}
		return or;
	}
	
	protected void load(ForceDelegateRaw gate) {
	}
}

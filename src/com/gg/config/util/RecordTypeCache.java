package com.gg.config.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.force.service.raw.SObjectWrapper;
import com.sforce.soap.partner.Field;

public class RecordTypeCache {
	private static RecordTypeCache instance;
	private Map<String, Field> fieldDescMap;
	private Map<String, List<SObjectWrapper>> recordMap = new HashMap<String, List<SObjectWrapper>>();  //key->object name
	
	private RecordTypeCache() {
		init();
	}
	
	public static RecordTypeCache getInstance() {
		if (instance == null) {
			instance = new RecordTypeCache();
		}
		return instance;
	}
	
	public List<SObjectWrapper> getRecords(String sobjectName) {
		return recordMap.get(sobjectName);
	}
	
	private void init() {
		
	}
}

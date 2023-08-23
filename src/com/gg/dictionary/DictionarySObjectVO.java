package com.gg.dictionary;

import com.force.service.vo.CustomVO;

public class DictionarySObjectVO extends CustomVO {
	private String Name;
	private String SObjectLabel__c;
	
	public String getSObjectLabel__c() {
		return SObjectLabel__c;
	}

	public void setSObjectLabel__c(String sObjectLabel__c) {
		SObjectLabel__c = sObjectLabel__c;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}
	
}

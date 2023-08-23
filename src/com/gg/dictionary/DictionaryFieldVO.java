package com.gg.dictionary;

import com.force.service.vo.CustomVO;

public class DictionaryFieldVO extends CustomVO {
	private String DataType__c;
	private String DictionarySObject__c;
	private String NativeHelpText__c;
	private String NativeLabel__c;
	private String Name;
	
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getDictionarySObject__c() {
		return DictionarySObject__c;
	}
	public void setDictionarySObject__c(String dictionarySObject__c) {
		DictionarySObject__c = dictionarySObject__c;
	}
	public String getDataType__c() {
		return DataType__c;
	}
	public void setDataType__c(String dataType__c) {
		DataType__c = dataType__c;
	}
	public String getNativeHelpText__c() {
		return NativeHelpText__c;
	}
	public void setNativeHelpText__c(String nativeHelpText__c) {
		NativeHelpText__c = nativeHelpText__c;
	}
	public String getNativeLabel__c() {
		return NativeLabel__c;
	}
	public void setNativeLabel__c(String nativeLabel__c) {
		NativeLabel__c = nativeLabel__c;
	}
	
}

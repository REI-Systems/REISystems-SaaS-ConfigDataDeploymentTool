package com.gg.dictionary;

import java.util.ArrayList;
import java.util.List;

import com.force.service.ForceDelegate;
import com.force.service.raw.ForceDelegateRaw;

/**
 * For a given org, it extracts the objects and fields and layout information and dumps the details into the following objects:
 * 	DictionarySObject__c
 *  DictionaryField__c
 * 	DictionaryLayout__c
 * 
 * @author shahnavazk
 *
 */
public class DictionaryGenerator {
	private ForceDelegateRaw connRaw = ForceDelegateRaw.login("src");
	private ForceDelegate connCustom = ForceDelegate.login("src");
	private static List<String> ignoreObjects = new ArrayList<String>();

	
	static {
		ignoreObjects.add("");
	}

	public static void main(String[] args) {
		DictionaryGenerator dict = new DictionaryGenerator();
		dict.generate();
	}
	
	private void generate() {
		cleanup();
		
		List<String> businessObjects = new BusinessObjectsFinder(connRaw).find();
		for (String objectName : businessObjects) {
				new SObjectDictionaryHandler(objectName, connRaw, connCustom).generate();
		}
	}
	
	private void cleanup() {
		connCustom.delete("Select Id from DictionaryLayout__c", null);
		connCustom.delete("Select Id from DictionaryField__c", null);
		connCustom.delete("Select Id from DictionarySObject__c", null);
	}

}

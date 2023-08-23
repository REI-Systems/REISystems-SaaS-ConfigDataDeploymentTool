package com.gg.meta.ant.target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Migrates account & contact from source to target org during org initial setup.
 * For delta migration, this class should not be invoked as it may delete
 * existing records from target org.
 * 
 * @author shahnavazk
 *
 */
public class MigrateAccountContactShort extends Variables {
	Map<String, String> srcAcctMap = new HashMap<String, String>(); //key->acct Id, value->internal unique id
	Map<String, String> tarAcctMap = new HashMap<String, String>(); //key->internal unique id, value->acct id

	public static void main(String[] args) {
		log.info("MigrateUsers starts");
		if (isFirstTimeMigration()) {
			MigrateAccountContactShort u = new MigrateAccountContactShort();		
			u.migrateAccounts();
			u.migrateContacts();
		}
		else {
			log.info("MigrateAccountContactShort is skipped");
		}
		log.info("MigrateUsers ends");
	}
	/**
	 * Migrates Account records from the source to the target Salesforce environment based on their InternalUniqueId__c field.
	 */
	private void migrateAccounts() {
		log.info("account migration starts");
		SObject[] accts;
		
		accts = target.queryMultiple("Select Id, InternalUniqueId__c from Account where InternalUniqueId__c != null", null);
		if (accts != null) {
			for (SObject acct : accts) {
				String uniqueId = ForceUtils.getSObjectFieldValue(acct, "InternalUniqueId__c");
				String acctId = acct.getId();
				tarAcctMap.put(uniqueId, acctId);
			}
		}

		List<SObject> acctsToInsert = new ArrayList<SObject>();
		accts = src.queryMultiple("Select Name, InternalUniqueId__c from Account where InternalUniqueId__c != null", null);
		if (accts != null) {
			for (SObject acct : accts) {
				String uniqueId = ForceUtils.getSObjectFieldValue(acct, "InternalUniqueId__c");
				if (tarAcctMap.containsKey(uniqueId) == false) {
					acctsToInsert.add(acct);
				}
			}
		}
		target.createMultiple(acctsToInsert);
		
		accts = src.queryMultiple("Select Id, InternalUniqueId__c from Account where InternalUniqueId__c != null", null);
		if (accts != null) {
			for (SObject acct : accts) {
				String uniqueId = ForceUtils.getSObjectFieldValue(acct, "InternalUniqueId__c");
				String acctId = acct.getId();
				srcAcctMap.put(acctId, uniqueId);
			}
		}
		
		accts = target.queryMultiple("Select Id, InternalUniqueId__c from Account where InternalUniqueId__c != null", null);
		if (accts != null) {
			for (SObject acct : accts) {
				String uniqueId = ForceUtils.getSObjectFieldValue(acct, "InternalUniqueId__c");
				String acctId = acct.getId();
				tarAcctMap.put(uniqueId, acctId);
			}
		}
		log.info("account migration ends");
	}
	/**
	 * Migrates Contact records from the source to the target Salesforce environment based on their InternalUniqueId__c field.
	 */
	private void migrateContacts() {
		log.info("contact migration starts");
		SObject[] conts;
		
		Map<String, String> tarContMap = new HashMap<String, String> (); //key->internal unique id, value->contact id
		conts = target.queryMultiple("Select Id, InternalUniqueId__c from Contact where InternalUniqueId__c != null", null);
		if (conts != null) {
			for (SObject cont : conts) {
				String uniqueId = ForceUtils.getSObjectFieldValue(cont, "InternalUniqueId__c");
				String contId = cont.getId();
				tarContMap.put(uniqueId, contId);
			}
		}
		
		List<SObject> contsToInsert = new ArrayList<SObject>();
		conts = src.queryMultiple("Select Firstname, Lastname, InternalUniqueId__c, AccountId, Email from Contact where InternalUniqueId__c != null", null);
		if (conts != null) {
			for (SObject cont : conts) {
				String uniqueId = ForceUtils.getSObjectFieldValue(cont, "InternalUniqueId__c");
				if (tarContMap.containsKey(uniqueId)) continue;
				
				String acctId = ForceUtils.getSObjectFieldValue(cont, "AccountId");
				if (StringUtils.isNonEmpty(acctId)) {
					String acctUniqueId = srcAcctMap.get(acctId);
					String tarAcctId = tarAcctMap.get(acctUniqueId);
					ForceUtils.setSObjectFieldValue(cont, "AccountId", tarAcctId);
				}
				contsToInsert.add(cont);
			}
		}
		target.createMultiple(contsToInsert);
				
		log.info("contact migration ends");
	}

}

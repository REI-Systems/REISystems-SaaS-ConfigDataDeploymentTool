package com.gg.meta.ant.target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.sforce.soap.partner.sobject.SObject;

/**
 * User object contains a text field that stores the organization account id.
 * This class updates that account id to reflect the target org.
 * 
 * @author shahnavazk
 *
 */
public class UpdateUserAccountId extends Variables {
	
	private static List<String> acctUniqueIds = new ArrayList<String>();
	private static Map<String, String> acctIdMap = new HashMap<String, String>(); //key->old acct id, value->old acct id
	private static Map<String, String> oldAcctMap = new HashMap<String, String>(); //key->unique id, value->old acct id
	
	public static void main(String[] args) {
		log.info("UpdateUserAccountId starts");
		loadSourceUsers();
		loadTargetUsers();
		log.info("UpdateUserAccountId ends");
	}
	/**
	 * Loads the unique internal IDs of source organization users and corresponding accounts into memory for migration purposes.
	 */
	private static void loadSourceUsers() {
		String fieldName = managedPackageNamespaceSrc + "OrganizationId__c";
		SObject[] users = src.queryMultiple("Select Id, " + fieldName + " from User where " + fieldName + " != null", null);
		List<String> acctIds = new ArrayList<String>();
		for (SObject user : users) {
			String acctId = ForceUtils.getSObjectFieldValue(user, fieldName);
			if (acctIds.contains(acctId) == false) {
				acctIds.add(acctId);
			}
		}
		
		SObject[] accts = src.queryMultiple("Select Id, InternalUniqueID__c from Account where Id in ?", new Object[]{acctIds});
		for (SObject acct : accts) {
			String uniqueId = ForceUtils.getSObjectFieldValue(acct, "InternalUniqueID__c");
			if (acctUniqueIds.contains(uniqueId) == false) {
				acctUniqueIds.add(uniqueId);
			}
			oldAcctMap.put(uniqueId, acct.getId());
		}
	}
	/**
	 * Loads target organization account IDs for source organization users and updates the corresponding users with new account IDs.
	 */
	private static void loadTargetUsers() {
		SObject[] accts = target.queryMultiple("Select Id, InternalUniqueID__c from Account where InternalUniqueID__c in ?", new Object[]{acctUniqueIds});
		for (SObject acct : accts) {
			String uniqueId = ForceUtils.getSObjectFieldValue(acct, "InternalUniqueID__c");
			String newAcctId = acct.getId();
			String oldAcctId = oldAcctMap.get(uniqueId);
			if (oldAcctId != null) {
				acctIdMap.put(oldAcctId, newAcctId);
			}
		}

		List<SObject> userUpdateList = new ArrayList<SObject>();
		String fieldName = managedPackageNamespaceTarget + "OrganizationId__c";
		SObject[] users = target.queryMultiple("Select Id, " + fieldName + " from User where " + fieldName + " != null", null);
		for (SObject user : users) {
			String acctId = ForceUtils.getSObjectFieldValue(user, fieldName);
			String newAcctId = acctIdMap.get(acctId);
			if (newAcctId != null) {
				ForceUtils.setSObjectFieldValue(user, fieldName, newAcctId);
				userUpdateList.add(user);
			}
		}
		
		target.updateMultiple(userUpdateList);
		log.info("account id is updated for " + userUpdateList.size() + " users");
	}
	
}

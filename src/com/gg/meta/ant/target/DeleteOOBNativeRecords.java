package com.gg.meta.ant.target;

import java.util.ArrayList;
import java.util.List;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.sforce.soap.partner.sobject.SObject;

/**
 * When we generate a new org from partner portal, it automatically comes with few sample records from
 * following objects:
 * 		Case
 * 		Opportunity
 * 		Contact
 * 		Account
 * 		UserRole
 * 
 * @author shahnavazk
 *
 */
public class DeleteOOBNativeRecords extends Variables {
	static SObject[] records;
	
	public static void main(String[] args) {
		if (isFirstTimeMigration()) {
			process();
		}
		else {
			log.info("DeleteOOBNativeRecords is skipped");
		}
	}
	/**
	 * Deletes out-of-the-box native records from the Account, Contact, Case, and Opportunity objects in the target system,
	 * as well as deletes UserRoles. This method is designed to clean up and prepare the target system for data migration or
	 * integration tasks by removing unwanted default records.
	 */
	private static void process() {
		log.info("deleting out of box native records from account, contact and case objects");
		
		records = target.queryMultiple("Select Id from Case", null);
		delete(records);
		
		records = target.queryMultiple("Select Id from Opportunity", null);
		delete(records);
		
		records = target.queryMultiple("Select Id from Contact", null);
		delete(records);
		
		records = target.queryMultiple("Select Id from Account", null);
		delete(records);

		deleteUserRoles();
		
		log.info("OOB native record deletion completed");
	}
	/**
	 * Removes UserRoles from the target system. This method first removes the "UserRoleId" value from all User records
	 * where it is not null, effectively removing the association of users with their roles.
	 */
	private static boolean deleteUserRoles() {
		//Remove roleId value from all users
		SObject[] users = target.queryMultiple("Select Id from User where UserRoleId != null", null);
		if (users != null) {
			for (SObject user : users) {
				ForceUtils.setSObjectFieldValue(user, "UserRoleId", null);
			}
			target.updateMultiple(users);
		}
		
		//Now delete roles
		records = target.queryMultiple("Select Id from UserRole", null);
		try {
			delete(records);
			return true;
		}
		catch (Exception ignore) {
			log.info("Ignoring UserRole deletion exception");
			return false;
		}		
	}
	/**
	 * Deletes the specified records from the target system using their respective record IDs.
	 */
	private static void delete(SObject[] records) {
		if (records == null) return;
		
		List<String> ids = new ArrayList<String> ();
		for (SObject record : records) {
			ids.add(record.getId());
		}
		
		if (ids.size() > 0) {
			target.delete(ids);
		}
	}
}

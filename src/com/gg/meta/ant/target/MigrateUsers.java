package com.gg.meta.ant.target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.gg.config.migration.MigrateSObject;
import com.gg.config.vo.MigrationItem;
import com.gg.meta.helper.UserHolder;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Migrates user records from source to target org.
 * It includes both platform and community users.
 * 
 * @author shahnavazk
 *
 */
public class MigrateUsers extends Variables {
	Map<String, String> srcAcctMap = new HashMap(); //key->acct Id, value->internal unique id
	Map<String, String> tarAcctMap = new HashMap(); //key->internal unique id, value->acct id
	static List<String> profileNames;
	
	public static void main(String[] args) {
		log.info("MigrateUsers starts");
		profileNames = StringUtils.getList(args[0], ",", true, false);
		profileNames.add("System Administrator");
		
		MigrateUsers u = new MigrateUsers();		
		u.migrateCommunityGuestUser();
		u.migrateSiteGuestUser();
		u.migrateAdminUsers();
		u.migratePlatformUsers();
		//u.migrateCommunityUsers();
		u.updateTargetOrgUsers();
		log.info("MigrateUsers ends");
	}
	/**
	 * Migrates the Community Guest User record from the source to the target Salesforce environment.
	 */
	private void migrateCommunityGuestUser() {
		log.info("community guest user migration starts");
		SObject srcUser = UserHolder.getInstance().getSourceCommunityGuestUser();
		SObject tarUser = UserHolder.getInstance().getTargetCommunityGuestUser();
		String srcUniqueId = ForceUtils.getSObjectFieldValue(srcUser, Variables.INTERNAL_UNIQUEID_FIELDNAME);
		String tarUniqueId = ForceUtils.getSObjectFieldValue(tarUser, Variables.INTERNAL_UNIQUEID_FIELDNAME);
		
		if (srcUniqueId.equals(tarUniqueId) == false) {
			ForceUtils.setSObjectFieldValue(tarUser, Variables.INTERNAL_UNIQUEID_FIELDNAME, srcUniqueId);
			target.updateSingle(tarUser);
		}
		log.info("community guest user migration end");
	}
	/**
	 * Migrates the Site Guest User record from the source to the target Salesforce environment.
	 */
	private void migrateSiteGuestUser() {
		log.info("site guest user migration starts");
		SObject srcUser = UserHolder.getInstance().getSourceSiteGuestUser();
		SObject tarUser = UserHolder.getInstance().getTargetSiteGuestUser();
		String srcUniqueId = ForceUtils.getSObjectFieldValue(srcUser, Variables.INTERNAL_UNIQUEID_FIELDNAME);
		String tarUniqueId = ForceUtils.getSObjectFieldValue(tarUser, Variables.INTERNAL_UNIQUEID_FIELDNAME);

		if (srcUniqueId.equals(tarUniqueId) == false) {
			ForceUtils.setSObjectFieldValue(tarUser, Variables.INTERNAL_UNIQUEID_FIELDNAME, srcUniqueId);
			target.updateSingle(tarUser);
		}
		log.info("site guest user migration ends");
	}
	/**
	 * Migrates Salesforce User records with the "System Administrator" profile and additional specified conditions from the source to the target Salesforce environment.
	 */
	private void migrateAdminUsers() {		
		log.info("admin user migration starts");
		String condition = "Profile.Name='System Administrator'" + prepareMigrateUsernamesSOQL();
		MigrationItem item = new MigrationItem("User").excludeField("AccountId").excludeField("ContactId").where(condition)
				.setBatchSize(1).deleteTargetRecords(false);
				
		MigrateSObject mig = new MigrateSObject(item);
		try{
			mig.migrate();
		}
		catch(Exception ex) {
			log.error("Error caught : " + ex);
		}
		log.info("admin user migration ends");
	}
	/**
	 * Migrates Salesforce User records with specific profile names and conditions from the source to the target Salesforce environment.
	 */
	private void migratePlatformUsers() {	
		log.info("platform user migration starts");
		String condition = "UserType != 'Guest' and Profile.Name in ('" + StringUtils.getConcatenatedString(profileNames, "','") + "') and " +
					"ContactId=null" + prepareMigrateUsernamesSOQL();
		MigrationItem item = new MigrationItem("User").excludeField("AccountId").excludeField("ContactId").where(condition)
				.setBatchSize(1).deleteTargetRecords(false);
		MigrateSObject mig = new MigrateSObject(item);
		try{
			mig.migrate();
		}
		catch(Exception ex) {
			log.error("Error caught : " + ex);
		}
		log.info("platform user migration ends");
	}
	/**
	 * Migrates Salesforce User records with specific profile names and conditions from the source to the target Salesforce environment, considering only users associated with Contacts (community users).
	 */
	private void migrateCommunityUsers() {		
		log.info("community user migration starts");
		String condition = "UserType != 'Guest' and Profile.Name in ('" + StringUtils.getConcatenatedString(profileNames, "','") + "') and " +
					"ContactId!=null" + prepareMigrateUsernamesSOQL();
		MigrationItem item = new MigrationItem("User").where(condition).setBatchSize(1).deleteTargetRecords(false);
		MigrateSObject mig = new MigrateSObject(item);
		mig.migrate();
		log.info("community user migration ends");
	}

	private String prepareMigrateUsernamesSOQL() {
		String condition = "";
		if (migrateUsernames != null && migrateUsernames.size() > 0) {
			condition = " and Username in ('" + StringUtils.getConcatenatedString(migrateUsernames, "','") + "')";
		}
		return condition;
	}
	//this is needed to refresh the account id stored on user records
	private void updateTargetOrgUsers() {
		SObject[] users = target.queryMultiple("Select Id, Username from User", null);
		target.updateMultiple(users);
	}
}

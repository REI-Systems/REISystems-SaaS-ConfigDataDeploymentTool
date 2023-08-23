package com.gg.config.migration.helper;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.gg.config.migration.MigrateSObject;
import com.gg.config.vo.MigrationItem;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.sobject.SObject;

public class MigrateHierarchyCustomSettings extends Variables {
	
	public MigrateHierarchyCustomSettings() {
		migrateUserPrefCustomSetting();
		migreateGlobalConfigCustomSetting();
	}

	public static void main(String[] args) {
		new MigrateHierarchyCustomSettings();
	}
	/**
	 * Migrates user preference custom settings for all users, except the system-level user preference configuration.
	 * The migration involves selecting specific UserPreferences__c records based on a given global where condition,
	 * and then applying the migration process using the MigrateSObject class.
	 */
	private void migrateUserPrefCustomSetting() {
		String whereConditionForUser= "Name!='User Preference Config (User)'";
		if(!globalWhereCondition.isEmpty()) {
			whereConditionForUser = whereConditionForUser +" and "+globalWhereCondition;
		}
		MigrationItem item = new MigrationItem("UserPreferences__c", true).where(whereConditionForUser);
		MigrateSObject mig = new MigrateSObject(item);
		mig.migrate();
		
		fixSmallPhotoURL();
		createPublicProfileUserPreference();
	}
	/**
	 * Migrates custom settings for global configurations (GlobalConfig__c) with the option to include all fields.
	 * This method is responsible for selecting and migrating specific GlobalConfig__c records based on a given
	 * global where condition, if provided. It uses the MigrateSObject class to execute the migration process.
	 */
	private void migreateGlobalConfigCustomSetting() {
		String whereConditionForUser = "Name!='User Preference Config (User)'";
		if(!globalWhereCondition.isEmpty()) {
			whereConditionForUser = whereConditionForUser +" and "+globalWhereCondition;
		}
		MigrationItem item = new MigrationItem("GlobalConfig__c", true).where(whereConditionForUser);
		MigrateSObject mig = new MigrateSObject(item);
		mig.migrate();
	}
	/**
	 * Attempts to fix the SmallPhotoURL field on user preference custom settings by modifying the URL format.
	 * This method iterates through the UserPreferences__c records in the target org and checks for non-empty
	 * SmallPhotoURL values that start with "http". For such records, it adjusts the URL format to follow the
	 */
	//Fix the URL domain in 'SmallPhotoURL' field
	private void fixSmallPhotoURL() {
		log.info("trying to fix SmallPhotoURL on user preference custom setting");
		String fieldName = managedPackageNamespaceTarget + "SmallPhotoURL__c";
		String instancePrefix = targetOrgPrefix;
		SObject[] records = target.queryMultiple("Select Id, " + fieldName + " from " + managedPackageNamespaceTarget + "UserPreferences__c where " +
				fieldName + " != null", null);
		for (SObject record : records) {
			String url = ForceUtils.getSObjectFieldValue(record, fieldName);
			if (StringUtils.isNonEmpty(url) && url.startsWith("http")) {
				int i = url.indexOf("/", 10);
				if (i != -1) {
					url = "https://" + instancePrefix + ".salesforce.com" + url.substring(i);
					ForceUtils.setSObjectFieldValue(record, fieldName, url);
				}
			}
		}
		target.updateMultiple(records);	
		log.info("SmallPhotoURL has been fixed on all user preference records.");
	}
	/**
	 * Creates a user preference record for the public profile user with specific profile attributes.
	 * This method is responsible for finding a user in the target org with a Profile matching the UserType 'Guest'
	 * and a given profile name (communityGuestProfile). If such a user is found, it creates a user preference record
	 * associated with that user to store specific preferences or settings for the public profile.
	 */
	private void createPublicProfileUserPreference() {
		SObject user = target.querySingle("Select Id from User where Profile.UserType='Guest' and Profile.Name=? limit 1", new Object[]{communityGuestProfile});
		if (user == null) {
			log.error("Public profile user not found");
			throw new RuntimeException("Public profile user not found");
		}
		
		SObject existingUP = target.querySingle("Select Id from " + managedPackageNamespaceTarget + "UserPreferences__c where SetupOwnerId=?", new Object[]{user.getId()});
		if (existingUP != null) return;
		
		SObject up = target.querySingle("Select SetupOwnerId from " + managedPackageNamespaceTarget + "UserPreferences__c limit 1", new Object[]{});
		ForceUtils.setSObjectFieldValue(up, "SetupOwnerId", user.getId());
		target.createSingle(up);
	}
	
}

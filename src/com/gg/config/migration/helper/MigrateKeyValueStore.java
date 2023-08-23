package com.gg.config.migration.helper;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.gg.config.migration.MigrateSObject;
import com.gg.config.vo.MigrationItem;
import com.sforce.soap.partner.sobject.SObject;

public class MigrateKeyValueStore extends Variables {
	/**
	 * Performs the migration process for the KeyValueStore__c object if it's the first-time migration.
	 * If not, performs a delta migration. After migration, sets the community URL.
	 */
	public MigrateKeyValueStore() {
		if (isFirstTimeMigration()) {
			MigrationItem item = new MigrationItem("KeyValueStore__c", true);
			item.excludeField("SetupOwnerId");
			if(!globalWhereCondition.isEmpty()) {
				item.where(globalWhereCondition);
			}
			MigrateSObject mig = new MigrateSObject(item);
			mig.migrate();
		}
		else {
			deltaMigration();
		}

		setCommunityURL();
	}
	
	public static void main(String[] args) {
		new MigrateKeyValueStore();
	}
	
	/**
	 * Performs delta migration for the "KeyValueStore__c" object in the context of
	 * migrating data between organizations. Delta migration involves moving only
	 * the changed or updated records
	 */
	private void deltaMigration() {
	    // Create a MigrationItem with the required parameters
	    MigrationItem item = new MigrationItem("KeyValueStore__c", true);
	    item.excludeField("SetupOwnerId");

	    // Construct the WHERE condition based on delta migration and globalWhereCondition
	    String whereCondition = "";
	    if (isDeltaMigration() && !globalWhereCondition.isEmpty()) {
	        whereCondition = "OrgDependent__c = false AND " + globalWhereCondition;
	    } else if (!globalWhereCondition.isEmpty()) {
	        whereCondition = globalWhereCondition;
	    } else if (isDeltaMigration()) {
	        whereCondition = "OrgDependent__c = false";
	    }

	    item.where(whereCondition);

	    // Perform the migration using the MigrateSObject class
	    MigrateSObject mig = new MigrateSObject(item);
	    mig.migrate();
	}

	/**
	 * Sets the community URL for the "Recipient Portal" Chatter Network in the
	 * target organization. The method retrieves or creates a specific record in the
	 * "KeyValueStore__c" object to store the community's login URL.
	 */
	private void setCommunityURL() {
		SObject site = target.querySingle("SELECT Subdomain FROM Site where MasterLabel='Recipient Portal' and SiteType='ChatterNetwork' limit 1", null);
		String subdomain = ForceUtils.getSObjectFieldValue(site, "Subdomain");
		String url = "https://" + subdomain + "." + targetOrgPrefix + ".force.com";
		String keyValue = "ExternalLoginPage";
		
		String textFieldName = managedPackageNamespaceTarget + "TextValue__c";
		SObject cs = target.querySingle("Select Id, " + textFieldName + " from " + managedPackageNamespaceTarget + "KeyValueStore__c where Name=?", new Object[]{keyValue});
		if (cs != null) {
			ForceUtils.setSObjectFieldValue(cs, textFieldName, url);
			target.updateSingle(cs);
		}
		else {
			String adminQuestionField = managedPackageNamespaceTarget + "Admin_Question__c";
			String descField = managedPackageNamespaceTarget + "Description__c";
			cs = target.querySingle("Select Name, " + textFieldName + ", " + adminQuestionField + ", " + descField + 
						" from " + managedPackageNamespaceTarget + "KeyValueStore__c limit 1", null);
			ForceUtils.setSObjectFieldValue(cs, "Name", keyValue);
			ForceUtils.setSObjectFieldValue(cs, textFieldName, url);
			ForceUtils.setSObjectFieldValue(cs, adminQuestionField, "What is the community login URL?");
			ForceUtils.setSObjectFieldValue(cs, descField, "Community login URL");
			target.createSingle(cs);
		}
	}
}

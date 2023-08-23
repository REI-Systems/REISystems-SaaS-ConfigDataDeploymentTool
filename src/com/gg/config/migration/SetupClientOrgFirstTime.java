
package com.gg.config.migration;

import java.util.Scanner;

import com.gg.common.Variables;
import com.gg.config.migration.helper.MigrateHierarchyCustomSettings;
import com.gg.config.migration.helper.MigrateKeyValueStore;
import com.gg.config.util.MigrateAccountContactFull;
import com.gg.config.vo.MigrationItem;

/**
 * A collection of sobjects are migrated from one org to another. If any of the
 * sobjects fails, then subsequent execution is stopped.
 * 
 * @author shahnavazk , Shubhangi Shinde, Vijayalaxmi
 *
 */
public class SetupClientOrgFirstTime extends Variables {

	public static void main(String[] args) {
		System.out.print("You are trying to migrate data FROM  " + SRC_ORG_NAME_ALIAS + " (" + src.getUsername()
				+ ") TO " + TAR_ORG_NAME_ALIAS + " (" + target.getUsername()
				+ ").\nWould you like to proceed? (Hit Enter to continue)");
		new Scanner(System.in).nextLine();

		firstTimeMigration = false;
		SetupClientOrgFirstTime mig = new SetupClientOrgFirstTime();
		
		if (isKeyValueStoreMigrationRequired) {
			log.info("-----------Migration for Key Value Store Started... ----------");
			System.out.println("");
			new MigrateKeyValueStore();
			System.out.println("");
		}else {
			log.info("----Skipped Migration of Key Value store as isKeyValueStoreMigrationRequired flag is set as false.----\n");
		}
		
		
		if (isCustomSettingsRequired) {
			log.info("----------- Migration for Custom Settings Started... --------------");
			System.out.println("");
			new MigrateHierarchyCustomSettings();
			System.out.println("");
		} else {
			log.info("----skipped custom settings as it's a sandbox created from GG production----\n");
		}
		
		if (isAccountContactMigrationRequired) {
			log.info("------------------Migration for Account Started... --------------");
			System.out.println("");
			mig.migrateAccountContact();
			System.out.println("");
		}else {
			log.info("----Skipped Migration of Account as isAccountContactMigrationRequired flag is set as false.----\n");
		}
		
		
		if (configSobjects != null) {
			log.info("------------------Migration for ConfigSobjects Started...--------");
			System.out.println("");
			mig.migrateConfigSObjects();
			System.out.println("");
		}else {
			log.info("----Skipped Migration of configSobjects as object details are empty.----\n");
		}
		
		
		if (productMasterSobjects != null) {
			
			log.info("-----------Migration for ProductMasterSobjects Started...--------");
			System.out.println("");
			mig.migrateProductMasterSObjects();
		}else {
			log.info("----Skipped Migration of productMasterSobjects as object details are empty.----");
		}
		
		System.out.println("------------------------------------------------------------------------------------");
		System.out.println("\nMigration Completed. Press Any Key to Exit....");
        new Scanner(System.in).nextLine();
		

		// mig.deletePageTemplates();
	}

	private void migrateConfigSObjects() {
		for (MigrationItem item : configSobjects) {
			log.info("----------------------------------------------------------------------------");
			log.info("-------------- Migrating config sobject " + item.getSrcObjectName() + " -------------------");
			log.info("----------------------------------------------------------------------------");

			MigrateSObject m = new MigrateSObject(item);
			m.migrate();
		}
	}

	private void migrateProductMasterSObjects() {
		for (MigrationItem item : productMasterSobjects) {
			log.info("----------------------------------------------------------------------------");
			log.info("-------------- Migrating product master sobject " + item.getSrcObjectName()
					+ " -------------------");
			log.info("----------------------------------------------------------------------------");

			MigrateSObject m = new MigrateSObject(item);
			m.migrate();
		}
	}

	private void migrateReviewStepTemplates() {
		MigrationItem item = new MigrationItem("ReviewStepTemplate__c").excludeField("Announcement__c")
				.excludeField("ClonedFrom__c").excludeField("Program__c").where("DefaultStep__c=true");

		MigrateSObject m = new MigrateSObject(item);
		m.migrate();
	}

	// This method should be called before migrating page layouts to overcome delete
	// errors
	private void deletePageTemplates() {
		/*
		 * SObject[] records = target.queryMultiple("Select Id from " +
		 * managedPackageNamespaceTarget + "PageTemplate__c", new Object[]{}); if
		 * (records == null) return;
		 * 
		 * List<String> ids = new ArrayList<>(); for (SObject record : records) {
		 * ids.add(record.getId()); } target.delete(ids);
		 */
	}

	private void migrateAccountContact() {
		new MigrateAccountContactFull().process();
	}

}

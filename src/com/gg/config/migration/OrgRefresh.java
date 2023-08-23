package com.gg.config.migration;

import java.util.Scanner;

import com.gg.common.Variables;
import com.gg.config.migration.helper.MigrateKeyValueStore;
import com.gg.config.util.MigrateAccountContactFull;
import com.gg.config.migration.helper.MigrateHierarchyCustomSettings;
import com.gg.config.vo.MigrationItem;

/**
 * A collection of sobjects are migrated from one org to another. If any of the sobjects fails, then subsequent execution
 * is stopped.
 * 
 * @author shahnavazk
 *
 */
public class OrgRefresh extends Variables {
	
	public static void main(String[] args) {
		System.out.print("You are trying to migrate data FROM  " + SRC_ORG_NAME_ALIAS + " (" + src.getUsername() + ") TO " +
				TAR_ORG_NAME_ALIAS + " (" + target.getUsername() + ").\nWould you like to proceed? (Hit Enter to continue)");
		new Scanner(System.in).nextLine();
	
		firstTimeMigration = false;
		OrgRefresh mig = new OrgRefresh();
		new MigrateKeyValueStore();
		new MigrateHierarchyCustomSettings();
		mig.migrateSpecificConfigSObjects();
		mig.migrateProductMasterSObjects();
		log.info("----------------------------------------------------------------------------");
		log.info("-------------- Migrating config sobject Ends-------------------");
		log.info("----------------------------------------------------------------------------");
		//mig.deletePageTemplates(); -- Always have this commented out until we find a reason to get this back		
	}
	/**
	 * Migrates specific configuration SObjects specified in the list of MigrationItem objects.
	 * Iterates through each MigrationItem representing a specific config SObject and performs the migration.
	 */
	private void migrateSpecificConfigSObjects() {		
		for (MigrationItem item : specificConfigSobjects) {
			log.info("----------------------------------------------------------------------------");
			log.info("-------------- Migrating Specific config sobject " + item.getSrcObjectName() + " -------------------");
			log.info("----------------------------------------------------------------------------");
			
			MigrateSObject m = new MigrateSObject(item);
			m.migrate();
		}
	}
	
	/**
	 * Migrates configuration SObjects specified in the list of MigrationItem objects.
	 * Iterates through each MigrationItem representing a config SObject and performs the migration.
	 */

	private void migrateConfigSObjects() {		
		for (MigrationItem item : configSobjects) {
			log.info("----------------------------------------------------------------------------");
			log.info("-------------- Migrating config sobject " + item.getSrcObjectName() + " -------------------");
			log.info("----------------------------------------------------------------------------");
			
			MigrateSObject m = new MigrateSObject(item);
			m.migrate();
		}
	}
	/**
	 * Migrates Product Master SObjects specified in the list of MigrationItem objects.
	 * Iterates through each MigrationItem representing a Product Master SObject and performs the migration.
	 */
	private void migrateProductMasterSObjects() {
		for (MigrationItem item : productMasterSobjects) {
			log.info("----------------------------------------------------------------------------");
			log.info("-------------- Migrating product master sobject " + item.getSrcObjectName() + " -------------------");
			log.info("----------------------------------------------------------------------------");
			
			MigrateSObject m = new MigrateSObject(item);
			m.migrate();
		}
	}

	private void migrateAccountContact() {
		new MigrateAccountContactFull().process();
	}

}

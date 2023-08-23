package com.gg.config.migration;


import com.gg.common.Variables;
import com.gg.config.migration.helper.MigrateHierarchyCustomSettings;
import com.gg.config.migration.helper.MigrateKeyValueStore;
import com.gg.config.util.MigrateAccountContactFull;
import com.gg.config.vo.MigrationItem;
/**
 * A collection of sobjects are migrated from one org to another. If any of the sobjects fails, then subsequent execution
 * is stopped.
 * 
 * @author shahnavazk
 *
 */
public class SetupProductOrgFirstTime extends Variables {

	public static void main(String[] args) {
		System.out.print("You are trying to migrate data FROM  " + SRC_ORG_NAME_ALIAS + " (" + src.getUsername() + ") TO " +
				TAR_ORG_NAME_ALIAS + " (" + target.getUsername() + ").\nWould you like to proceed? (Hit Enter to continue)");
		//new Scanner(System.in).nextLine();

		firstTimeMigration = false;
		SetupProductOrgFirstTime mig = new SetupProductOrgFirstTime();
		new MigrateKeyValueStore();
		new MigrateHierarchyCustomSettings();
		mig.migrateAccountContact();
		mig.migrateConfigSObjects();
		mig.migrateProductMasterSObjects(); 
		
		//mig.deletePageTemplates(); // Commennted out until further notice 
	}
	
	/**
	 * Migrates Config SObjects specified in the list of MigrationItem objects.
	 * Iterates through each MigrationItem representing a Config SObject and performs the migration.
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
	/**
	 * Migrates Product Sample SObjects specified in the list of MigrationItem objects.
	 * Iterates through each MigrationItem representing a Product Sample SObject and performs the migration.
	 */
	private void migrateProductSampleSObjects() {
		for (MigrationItem item : productSampleSobjects) {
			log.info("----------------------------------------------------------------------------");
			log.info("-------------- Migrating product sample sobject " + item.getSrcObjectName() + " -------------------");
			log.info("----------------------------------------------------------------------------");
			
			MigrateSObject m = new MigrateSObject(item);
			m.migrate();
			System.out.println("End of the deployment");
		}
	}
	
	
	//This method should be called before migrating page layouts to overcome delete errors
/*	private void deletePageTemplates() {
		SObject[] records = target.queryMultiple("Select Id from " + managedPackageNamespaceTarget + "PageTemplate__c", new Object[]{});
		if (records == null) return;
		
		List<String> ids = new ArrayList<>();
		for (SObject record : records) {
			ids.add(record.getId());
		}
		target.delete(ids);
	}*/
	
	private void migrateAccountContact() {
		new MigrateAccountContactFull().process();
	}


}

package com.gg.config.util;
import com.gg.common.Variables;
import com.gg.config.migration.MigrateSObject;
import com.gg.config.vo.MigrationItem;

/**
 * Migrates account, contact & user records from source to target org.
 * It includes both platform and community users.
 * 
 * @author shahnavazk
 *
 */
public class MigrateAccountContactFull extends Variables {

	public static void main(String[] args) {
		log.info("MigrateUsers starts");
		MigrateAccountContactFull u = new MigrateAccountContactFull();
		u.process();
		log.info("MigrateUsers ends");
	}

	public void process() {
		migrateAccounts();
		//migrateContacts();
	}
	/**
	 * Migrates Account records based on different criteria and conditions.
	 * Three separate migration processes are executed for specific Account types:
	 * - Accounts with 'Name' equal to 'SYSTEM'
	 * - Accounts with RecordType.Name equal to 'Reviewers'
	 * - Accounts with RecordType.Name equal to 'External'
	 * Global where condition (if provided) is applied to each migration process.
	 */
	private void migrateAccounts() {
		log.info("account migration starts");
		/*MigrationItem item = new MigrationItem("Account").where("Name IN ('GMS Administrator')")
		.excludeField("PersonContactId").excludeField("DandbCompanyId").excludeField("UserRegistration__c")
		.excludeField("JigsawCompanyId").excludeField("MasterRecordId").excludeField("IsCustomerPortal")
		.excludeField("PointofContactUser__c").excludeField("GNT__Workspace__c").setBatchSize(50).deleteTargetRecords(false);
		MigrateSObject mig = new MigrateSObject(item);
		mig.migrate();
		*/
		String whereConditionForAccountNameInSystem = "Name IN ('SYSTEM')";
		String whereConditionForAccountNameInReviewers = "RecordType.Name in ('Reviewers')";
		String whereConditionForAccountNameInExternal = "RecordType.Name in ('External')";
		if(!globalWhereCondition.isEmpty()) {
			whereConditionForAccountNameInSystem = whereConditionForAccountNameInSystem+" and "+globalWhereCondition;
			whereConditionForAccountNameInReviewers = whereConditionForAccountNameInReviewers+" and "+globalWhereCondition;
			whereConditionForAccountNameInExternal = whereConditionForAccountNameInExternal+" and "+globalWhereCondition;
		}
		
		MigrationItem item = new MigrationItem("Account").where(whereConditionForAccountNameInSystem)
				.excludeField("PersonContactId").excludeField("DandbCompanyId").excludeField("UserRegistration__c")
				.excludeField("JigsawCompanyId").excludeField("MasterRecordId").excludeField("IsCustomerPortal")
				.excludeField("PointofContactUser__c").excludeField("GNT__Workspace__c").setBatchSize(50).deleteTargetRecords(false);
		MigrateSObject mig = new MigrateSObject(item);
		mig.migrate();
		
		item = new MigrationItem("Account").where(whereConditionForAccountNameInReviewers).setBatchSize(1)
				.excludeField("PersonContactId").excludeField("DandbCompanyId").excludeField("UserRegistration__c")
				.excludeField("JigsawCompanyId").excludeField("MasterRecordId").excludeField("IsCustomerPortal")
				.excludeField("PointofContactUser__c").excludeField("GNT__Workspace__c").setBatchSize(50).deleteTargetRecords(false); 
		mig = new MigrateSObject(item);
		mig.migrate();		
				
		item = new MigrationItem("Account").where(whereConditionForAccountNameInExternal).setBatchSize(1)
				.excludeField("PersonContactId").excludeField("DandbCompanyId").excludeField("UserRegistration__c")
				.excludeField("JigsawCompanyId").excludeField("MasterRecordId").excludeField("IsCustomerPortal")
				.excludeField("PointofContactUser__c").excludeField("GNT__Workspace__c").setBatchSize(50).deleteTargetRecords(false); 
		mig = new MigrateSObject(item);
		mig.migrate();		
				
		log.info("account migration ends");
		
	}
	
	/*private void migrateContacts() {
		log.info("contact migration starts");
		MigrationItem item = new MigrationItem("Contact").excludeField("MasterRecordId").excludeField("LastCUUpdateDate")
				.excludeField("LastCURequestDate").excludeField("JigsawContactId").excludeField("UserRegistration__c").excludeField("OwnerId")
				.deleteTargetRecords(false);
		MigrateSObject mig = new MigrateSObject(item);
		
		mig.migrate();
		log.info("contact migration ends");
	}*/
	

}

package com.gg.config.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.force.service.ForceUtils;
import com.force.service.raw.SObjectWrapper;
import com.gg.common.Variables;
import com.gg.config.util.AppUtils;
import com.gg.config.util.PrepareTargetRecord;
import com.gg.config.vo.MigrationItem;
import com.gg.config.vo.SObjectDataHolder;
import com.gg.meta.helper.UserHolder;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;
/**
 * For a given sobject, it migrates all records from one org to another.
 * 
 * @author shahnavazk
 *
 */
public class MigrateSObject extends Variables {
	private MigrationItem item;
	private SObjectDataHolder primarySrcDataHolder;
	private SObjectDataHolder primaryTargetDataHolder;
	private Map<String, SObjectDataHolder> srcDataCache = new HashMap<String, SObjectDataHolder>();  //key->sobject name without namespace (lowercase)
	private Map<String, SObjectDataHolder> tarDataCache = new HashMap<String, SObjectDataHolder>();  //key->sobject name without namespace (lowercase)
	
	private List<SObject> targetRecordsToInsert = new ArrayList<SObject>();
	private List<SObject> targetRecordsToUpdate = new ArrayList<SObject>();
	private boolean refFieldsNotFoundInTargetOrg = false;
	
	public MigrateSObject(MigrationItem item) {
		this.item = item;
		item.setRelationshipRecordsMustFoundInTargetOrg(false);
	}
	/**
	 * Initiates the data migration process.
	 * Loads all records, checks for source records availability, deletes obsolete records from the target org
	 * (if applicable), performs the actual migration, and handles additional migration if required for relationship records.
	 */
	public void migrate() {
		loadAllRecords();

		if (primarySrcDataHolder.getTotalRecords() == 0) {
			log.info("No source records found in " + item.getSrcObjectName());
			return;
		}
		
		//Delete obsolete records from target org. Don't delete hierarchy custom setting entries.
		if (AppUtils.isHierarchyCustomSetting(item.getSrcObjectName()) == false && primarySrcDataHolder.isStandardObject() == false &&
				item.isDeleteTargetRecords()) {
			//new DeleteObsoleteTargetRecords(primarySrcDataHolder.getRecordsMap(), primaryTargetDataHolder.getRecordsMap()).delete();
		}

		performMigration();
		
		if (refFieldsNotFoundInTargetOrg) {			
			//do migration one more time
			item.setRelationshipRecordsMustFoundInTargetOrg(true);
			converActiveUserToInactive = false;
			loadAllRecords();
			performMigration();
		}
	}
	
	/* PRIVATE METHODS */
	
	//fetches the current primary sobject records and all referenced records
	private void loadAllRecords() {
		srcDataCache.clear();
		tarDataCache.clear();
		
		loadPrimaryRecords();
		loadRelationshipRecords();
		
		log.info("existing source and target records loaded into cache");
	}
	/**
	 * Loads all records for the primary SObject from both the source and target orgs.
	 * Creates data holders for both the source and target records.
	 * Creates backup files for the data holders.
	 */
	private void loadPrimaryRecords() {
		//load all records from primary sobject from source org
		this.primarySrcDataHolder = new SObjectDataHolder(item, true, null, null, src);
		if (primarySrcDataHolder.getTotalRecords() == 0) return;
		srcDataCache.put(item.getSrcObjectName().toLowerCase(), primarySrcDataHolder);
		primarySrcDataHolder.createBackupFile();

		//load all records from primary sobject from target org
		this.primaryTargetDataHolder = new SObjectDataHolder(item, false, null, null, target);
		tarDataCache.put(item.getSrcObjectName().toLowerCase(), primaryTargetDataHolder);
		primaryTargetDataHolder.createBackupFile();
	}
	/**
	 * Loads relationship records for the primary SObject from both the source and target orgs.
	 * For each reference field of the primary SObject, loads reference record ids from the source org.
	 * If reference records are found in the source org, loads corresponding records from the target org using unique fields.
	 */
	private void loadRelationshipRecords() {
		if (item.getSrcObjectName().equalsIgnoreCase("User")) {
			UserHolder.getInstance().reset();
		}
		
		//load all reference records from both source and target orgs
		Map<String, Field> refFields = src.getReferenceFieldsMetaData(AppUtils.addNamespace(item.getSrcObjectName(), SRC_NAMESPACE), true, true, false);
		
		for (Field f : refFields.values()) {
			//Check if this relationship field is present in the exclusion list
			if (AppUtils.isFieldExcludedFromMigration(item, f) || AppUtils.isUniqueIdFieldName(item.getObjectNameWithoutNamespace(), Variables.managedPackageNamespaceSrc, f.getName(), src)) {
				continue;
			}
			log.info("Loading data for relationship field " + f.getName());
			
			//load reference record ids for each reference field from source org
 			List<String> refRecordIds = primarySrcDataHolder.getReferenceFieldValues(f.getName());
 			if (refRecordIds != null && refRecordIds.size() > 0) {
				String refSObjectNameWithoutNamespace = AppUtils.removeNamespace(f.getReferenceTo()[0]);
				MigrationItem miSrc = new MigrationItem(refSObjectNameWithoutNamespace);
				if (refSObjectNameWithoutNamespace.startsWith(Variables.managedPackageNamespaceSrc)) {
					miSrc.setPackaged(true);
				}
				SObjectDataHolder refSrcSObjectDataHolder = new SObjectDataHolder(miSrc, true, refRecordIds, null, src);
				storeCache(srcDataCache, refSObjectNameWithoutNamespace.toLowerCase(), refSrcSObjectDataHolder);
			
				//for the same reference field, load reference records from target org using unique fields
				Set<String> tarUniqueFieldValues = refSrcSObjectDataHolder.getUniqueFieldValues();
				tarUniqueFieldValues = applyTargetOrgNamespaceForUniqueFields(tarUniqueFieldValues);
				MigrationItem miTar = new MigrationItem(refSObjectNameWithoutNamespace);
				if (refSObjectNameWithoutNamespace.startsWith(Variables.managedPackageNamespaceSrc)) {
					miTar.setPackaged(true);
				}
				SObjectDataHolder refTargetSObjectDataHolder = new SObjectDataHolder(miTar, false, null, tarUniqueFieldValues, target);
				//tarDataCache.put(refSObjectNameWithoutNamespace.toLowerCase(), refTargetSObjectDataHolder);
				storeCache(tarDataCache, refSObjectNameWithoutNamespace.toLowerCase(), refTargetSObjectDataHolder);
 			}
		}		
	}
	
	private Set<String> applyTargetOrgNamespaceForUniqueFields(Set<String> uniqueFieldValues) {
		Set<String> newValues = new HashSet<String>();
		for (String value : uniqueFieldValues) {
			newValues.add(value.replace(managedPackageNamespaceSrc, managedPackageNamespaceTarget));
		}
		return newValues;
	}
	
	private void storeCache(Map<String, SObjectDataHolder> dataCache, String refSObjectNameWithoutNamespaceLC, SObjectDataHolder refSrcSObjectDataHolder) {
		if (dataCache.containsKey(refSObjectNameWithoutNamespaceLC)) {
			SObjectDataHolder cachedHolder = dataCache.get(refSObjectNameWithoutNamespaceLC);
			cachedHolder.merge(refSrcSObjectDataHolder.qtool);  //merge with existing cache
		}
		else {
			dataCache.put(refSObjectNameWithoutNamespaceLC, refSrcSObjectDataHolder);  //create new cache
		}
	}
	/**
	 * Performs the migration process for the primary SObject.
	 * Clears the targetRecordsToInsert and targetRecordsToUpdate lists.
	 * Iterates over each source record in the primarySrcDataHolder and prepares the corresponding target record using PrepareTargetRecord class.
	 * If any reference field is not found in the target org during preparation, sets refFieldsNotFoundInTargetOrg flag to true.
	 * Adds the target record to either targetRecordsToInsert or targetRecordsToUpdate based on whether it already has an Id or not.
	 * Pushes the records to the target org in batches as per the specified batch size.
	 *
	 */
	private void performMigration() {
		targetRecordsToInsert.clear();
		targetRecordsToUpdate.clear();
		
		int i=1, j=0;
		for (SObjectWrapper srcRecordWrapper : primarySrcDataHolder.getRecords()) {
			log.info("**************************************************************************************************");
			log.info("*********** Processing source record [" + i + "/" + primarySrcDataHolder.getTotalRecords() + "] " + 
									srcRecordWrapper.getId() + " (" + item.getSrcObjectName() + ") **************");
			PrepareTargetRecord mig = new PrepareTargetRecord(srcRecordWrapper, primaryTargetDataHolder, srcDataCache, tarDataCache, 
						primarySrcDataHolder.getUniqueFieldNamesWithNamespace(), item); 
			SObject targetRecord = mig.prepare();
			if (mig.isRefFieldNotFoundInTargetOrg()) {
				this.refFieldsNotFoundInTargetOrg = true;
			}
			
			if (StringUtils.isEmpty(targetRecord.getId())) {
				targetRecordsToInsert.add(targetRecord);
			}
			else {
				targetRecordsToUpdate.add(targetRecord);
			}
			i++;
			j++;
			if (j == item.getBatchSize()) {
				try {
					pushToTargetOrg();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.error(e);
					e.printStackTrace();
					
				}
				j=0;
			}
		}
		
		try {
			pushToTargetOrg();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e);
			e.printStackTrace();
			
		}
	} 
	/**
	 * Pushes the target records to the target org in batches.
	 */
	private void pushToTargetOrg() throws Exception {
		if (targetRecordsToInsert.size() > 0) {
			log.info("-------------------------------------------------------");
			log.info("creating new records: " + targetRecordsToInsert.size());
			log.info("creating new records: " + targetRecordsToInsert); // sarat test
			log.info(" test ");
			try {
				target.createMultiple(targetRecordsToInsert);
			}
			catch (Exception e) {
				if (item.getSrcObjectName().equals("User") && e.getMessage().contains("License Limit Exceeded") && userLicenseLimitIgnore) {
					converActiveUserToInactive = true;
					convertActiveUsersToInactive(targetRecordsToInsert);
					target.createMultiple(targetRecordsToInsert);
				}
				else {
					log.error(e.getMessage());
					throw  e ;
				}
			}
			targetRecordsToInsert.clear();
		}
		
		if (targetRecordsToUpdate.size() > 0) {
			log.info("-------------------------------------------------------");
			log.info("updating existing records: " + targetRecordsToUpdate.size());
			log.info("Record - test -  " + targetRecordsToUpdate);
			target.updateMultiple(targetRecordsToUpdate);
			targetRecordsToUpdate.clear();
		}
		
		System.gc();
	}
	/**
	 * Converts active users to inactive in the provided list of SObject records.
	 * For each SObject user record in the list, checks if the "IsActive" field is set to true.
	 * If the user is active (isActive = true), sets the "IsActive" field to false (inactive).
	 */
	private void convertActiveUsersToInactive(List<SObject> users) {
		for (SObject user : users) {
			Boolean isActive = Boolean.valueOf(ForceUtils.getSObjectFieldValue(user, "IsActive"));
			if (isActive) {
				ForceUtils.setSObjectFieldValue(user, "IsActive", "FALSE");
			}
		}
	}
	
	
	public static void main(String[] args) {
		System.out.print("You are trying to migrate data FROM  " + SRC_ORG_NAME_ALIAS + " (" + src.getUsername() + ") TO " +
				TAR_ORG_NAME_ALIAS + " (" + target.getUsername() + ").\nWould you like to proceed? (Hit Enter to continue)");
		//new Scanner(System.in).nextLine();
		
		firstTimeMigration = false;
		
		MigrationItem item = new MigrationItem("DataTableConfig__c", true).where("Name IN ('NegotiationApplicationsCompleted','NegotiationApplications','PendingNegotiations','completedNegotiations')").setBatchSize(1).deleteTargetRecords(false);
		MigrateSObject mig = new MigrateSObject(item);
		mig.migrate();
		MigrationItem item1 = new MigrationItem("DataTableAction__c", true).where("GNT__DataTableConfig__r.Name IN ('NegotiationApplicationsCompleted','NegotiationApplications','PendingNegotiations','completedNegotiations')").setBatchSize(1).deleteTargetRecords(false);
		MigrateSObject mig1 = new MigrateSObject(item1);
		mig1.migrate();
		MigrationItem item2 = new MigrationItem("DataTableDetailConfig__c", true).where("GNT__FlexTableConfig__r.Name IN ('NegotiationApplicationsCompleted','NegotiationApplications','PendingNegotiations','completedNegotiations')").setBatchSize(1).deleteTargetRecords(false);
		MigrateSObject mig2 = new MigrateSObject(item2);
		mig2.migrate();
		MigrationItem item3 = new MigrationItem("FlexTableFilterListViewConfig__c", true).where("GNT__FlexTableConfig__r.Name IN ('NegotiationApplicationsCompleted','NegotiationApplications','PendingNegotiations','completedNegotiations')").setBatchSize(1).deleteTargetRecords(false);
		MigrateSObject mig3 = new MigrateSObject(item3);
		mig3.migrate();
		MigrationItem item4 = new MigrationItem("FlexGridConfig__c", true).where("Id IN('a0k0b000007KGYV','a0k0b000007KGYU')").setBatchSize(1).deleteTargetRecords(false);
		MigrateSObject mig4 = new MigrateSObject(item4);
		mig4.migrate();
		
	}

}

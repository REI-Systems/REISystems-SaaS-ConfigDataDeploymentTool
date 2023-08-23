package com.gg.config.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.force.service.ForceUtils;
import com.force.service.raw.FieldWrapper;
import com.force.service.raw.SObjectWrapper;
import com.gg.common.Variables;
import com.gg.config.vo.MigrationItem;
import com.gg.config.vo.SObjectDataHolder;
import com.gg.meta.helper.OrganizationHolder;
import com.gg.meta.helper.ProfileHolder;
import com.gg.meta.helper.UserHolder;
import com.lib.util.StringUtils;
import com.lib.util.UnCheckedException;
import com.sforce.soap.partner.sobject.SObject;
/**
 * For a given source record, it identifies the matching record in target org (if found). 
 * If target org doesn't have the matching record (using unique id), then it prepares
 * a new target record by coping the fields from source record.
 * 
 * It does NOT perform DML operation to insert the record into target org
 * as it will be performed outside of this class in bulk.
 * 
 * @author shahnavazk
 *
 */
public class PrepareTargetRecord extends Variables {
	private SObjectWrapper srcWrapper;
	private SObject targetRecord;
	private Map<String, String> referencedIds = new HashMap<String, String>(); //key->source reference id, value->target reference id
	private SObjectDataHolder primaryTargetDataHolder;  //cache contains all primary target records from target org
	private Map<String, SObjectDataHolder> srcDataCache;  //key->sobject name without namespace (lowercase)
	private Map<String, SObjectDataHolder> tarDataCache;  //key->sobject name without namespace (lowercase)
	private String[] uniqueFieldNamesWithNamespace;
	private MigrationItem item;
	private boolean refFieldNotFoundInTargetOrg = false;

	//srcRecord should have all fields selected - only those fields will be migrated over
	public PrepareTargetRecord(SObjectWrapper srcWrapper, SObjectDataHolder primaryTargetDataHolder, 
			Map<String, SObjectDataHolder> srcDataCache, Map<String, SObjectDataHolder> tarDataCache, String[] uniqueFieldNamesWithNamespace,
			MigrationItem item) {			
		this.srcWrapper = srcWrapper;
		this.primaryTargetDataHolder = primaryTargetDataHolder;
		this.srcDataCache = srcDataCache;
		this.tarDataCache = tarDataCache;
		this.uniqueFieldNamesWithNamespace = uniqueFieldNamesWithNamespace;
		this.item = item;
	}
	/**
	 * Prepares the SObject for migration by finding the target record, fetching referenced records,
	 * and updating or creating the target record accordingly.
	 *
	 * @return The prepared target SObject.
	 * @throws UnCheckedException If an exception occurs during the preparation process.
	 */
	public SObject prepare() {
		try {
			findTargetRecord();
			fetchReferencedRecords();
			
			if (targetRecord != null) {  //target org already has the matching record - so just update it
				List<String> excludeFields = sobjectExclusionFields.get(targetRecord.getType());
				if (excludeFields == null) {
					excludeFields = new ArrayList<String>();
				}
				ForceUtils.copyAllFields(srcWrapper.getRecord(), targetRecord, true, excludeFields, managedPackageNamespaceSrc, managedPackageNamespaceTarget);
				changeReferenceFieldsOnTargetRecord();		
				massageExistingUser();
			}
			else {  //create new target reecord
				targetRecord = ForceUtils.changeNamespace(srcWrapper.getRecord(), true, managedPackageNamespaceSrc, managedPackageNamespaceTarget);
				changeReferenceFieldsOnTargetRecord();
				massageNewUser();
			}
			
			return targetRecord;
		}
		catch (UnCheckedException e) {
			log.error(e, e);
			throw new UnCheckedException("Migration context [" + srcWrapper.getObjectNameWithnamespace() + "=" + srcWrapper.getId() + "] " + e.getMessage());
		}
	}
	
	/* PRIVATE METHODS */
	/**
	 * Massage new User object by applying specific modifications for fields like username,
	 * firstname, lastname, nickname, and additional fields.
	 * (Applicable only for User object)
	 */
	private void massageNewUser() {
		//applicable only for User object
		if (targetRecord.getType().equalsIgnoreCase("User")) {
			massageUsernameForUserObject();  
			massageFirstnameForUserObject();
			massageLastnameForUserObject();
			massageNicknameForUserObject();  
			massageAdditionalFieldsForUserObject();
		}
	}
	/**
	 * Massage existing User object by applying specific modifications for fields like firstname,
	 * lastname, nickname, and additional fields.
	 * (Applicable only for User object)
	 */
	private void massageExistingUser() {
		if (targetRecord.getType().equalsIgnoreCase("User")) {
			massageFirstnameForUserObject();
			massageLastnameForUserObject();
			massageNicknameForUserObject();  
			massageAdditionalFieldsForUserObject();
		}
	}
	/**
	 * Massages the "Username" field for a User object by applying specific modifications,
	 * including converting to lowercase, replacing username suffixes, and appending
	 * the target organization's username suffix if not already present.
	 */
	//Since username for User object should be unique, we should massage it before it is copied over to target org.
	private void massageUsernameForUserObject() {
		String username = ForceUtils.getSObjectFieldValue(targetRecord, "Username");
		username = username.toLowerCase();
		for (String suffixFrom : usernameSuffixMap.keySet()) {
			String suffixTo = usernameSuffixMap.get(suffixFrom);
			username = username.replace(suffixFrom, suffixTo);
		}
		
		if (username.contains(Variables.targetOrgUsernameSuffix) == false) {
			username += "." + Variables.targetOrgUsernameSuffix;
		}
		
		ForceUtils.setSObjectFieldValue(targetRecord, "Username", username);
	}
	/**
	 * Massages the "FirstName" field for a User object by applying specific modifications,
	 * including replacing parts of the first name with their corresponding suffixes.
	 */
	private void massageFirstnameForUserObject() {
		String firstname = ForceUtils.getSObjectFieldValue(targetRecord, "FirstName");
		for (String suffixFrom : userFullNameSuffixMap.keySet()) {
			String suffixTo = userFullNameSuffixMap.get(suffixFrom);
			firstname = firstname.replace(suffixFrom, suffixTo);
		}
		
		ForceUtils.setSObjectFieldValue(targetRecord, "FirstName", firstname);
	}
	/**
	 * Massages the "LastName" field for a User object by applying specific modifications,
	 * including replacing parts of the last name with their corresponding suffixes.
	 */
	private void massageLastnameForUserObject() {
		String lastname = ForceUtils.getSObjectFieldValue(targetRecord, "LastName");
		for (String suffixFrom : userFullNameSuffixMap.keySet()) {
			String suffixTo = userFullNameSuffixMap.get(suffixFrom);
			lastname = lastname.replace(suffixFrom, suffixTo);
		}
		
		ForceUtils.setSObjectFieldValue(targetRecord, "LastName", lastname);
	}
	/**
	 * Massages the "CommunityNickname" field for a User object by applying specific modifications.
	 * Trims the nickname to a maximum of 25 characters and appends the target organization's
	 * username suffix if not already present.
	 */
	private void massageNicknameForUserObject() {
		String nickname = ForceUtils.getSObjectFieldValue(targetRecord, "CommunityNickname");
		nickname = StringUtils.trim(nickname, 25);
		if (nickname.contains(Variables.targetOrgUsernameSuffix) == false) {
			nickname += "." + Variables.targetOrgUsernameSuffix;
		}
		
		ForceUtils.setSObjectFieldValue(targetRecord, "CommunityNickname", nickname);
	}
	/**
	 * Massages additional fields for a User object by applying specific modifications.
	 */
	private void massageAdditionalFieldsForUserObject() {
		ForceUtils.setSObjectFieldValue(targetRecord, "UserPermissionsSFContentUser", "FALSE");
		Boolean isActive = Boolean.valueOf(ForceUtils.getSObjectFieldValue(targetRecord, "IsActive"));
		if (isActive && converActiveUserToInactive) {
			ForceUtils.setSObjectFieldValue(targetRecord, "IsActive", "FALSE");
		}
	}
	
	/**
	 * For the main source record, identify the matching record in target org
	 */
	private void findTargetRecord() {
		String uniqueKey = AppUtils.findUniqueFieldValue(srcWrapper.getRecord(), uniqueFieldNamesWithNamespace, true);
		SObjectWrapper targetWrapper = primaryTargetDataHolder.getRecordUsingUniqueKeyString(uniqueKey);
		if (targetWrapper != null) {
			this.targetRecord = targetWrapper.getRecord();
			log.info("existing target record found. source record id: " + srcWrapper.getId() + ", target record id: " + targetRecord.getId());
		}
	}
	
	/**
	 * If the source record has any lookup or master-detail fields, those reference records are identified in the target object for mapping.
	 */
	private void fetchReferencedRecords() {
		for (FieldWrapper srcFieldWrapper : srcWrapper.getReferenceFields(true, false, false)) {
			//Check if this relationship field is present in the exclusion list
			if (AppUtils.isFieldExcludedFromMigration(item, srcFieldWrapper.getFieldDesc())) {
				continue;
			}
						
			String relationshipRecordId = srcFieldWrapper.getValueAsString(false); //lookup or master-detail field value
			if (relationshipRecordId == null) continue;
			
			SObjectWrapper tarRefWrapper = null;
			
			if (relationshipRecordId.startsWith("00D")) { //organization record
				tarRefWrapper = findReferencedTargetOrganization(relationshipRecordId);
			}
			else if (relationshipRecordId.startsWith("00e")) {  //profile record
				tarRefWrapper = findReferencedTargetProfile(relationshipRecordId);
			}
			else if (relationshipRecordId.startsWith("005")) {  //user record
				tarRefWrapper = findReferencedTargetUser(relationshipRecordId);
			}
			else {
				tarRefWrapper = findReferencedTargetRegularRecord(srcFieldWrapper, relationshipRecordId);
			}
			
			if (tarRefWrapper == null || tarRefWrapper.getId() == null) {
				if (item.isRelationshipRecordsMustFoundInTargetOrg()) {
					log.error("Relationship record not found in target org. " + 
							"source org record: " + srcWrapper.getId() + 
							", sobject name: " + srcWrapper.getObjectNameWithnamespace() + ", relationship record: " + relationshipRecordId);
					throw new UnCheckedException("Relationship record not found in target org. " + 
							"source org record: " + srcWrapper.getId() + 
							", sobject name: " + srcWrapper.getObjectNameWithnamespace() + ", relationship record: " + relationshipRecordId);
				}
			}
			else {
				referencedIds.put(relationshipRecordId, tarRefWrapper.getId());
			}
		}
		
		log.info("referencedIds: " + referencedIds);
	}
	/**
	 * Finds the referenced target organization's SObjectWrapper based on the given relationship record ID.
	 * If the target organization is available, it returns a corresponding SObjectWrapper; otherwise, it returns null.
	 */
	private SObjectWrapper findReferencedTargetOrganization(String relationshipRecordId) {
		SObject targetRecord = OrganizationHolder.getInstance().getTargetOrg();
		if (targetRecord != null) {
			return new SObjectWrapper(targetRecord, TargetOrgSchema.getInstance().getFields("Organization"));
		}
		return null;
	}
	/**
	 * Finds the referenced target profile's SObjectWrapper based on the given relationship record ID.
	 * If the target profile is available, it returns a corresponding SObjectWrapper; otherwise, it returns null.
	 *
	 */
	private SObjectWrapper findReferencedTargetProfile(String relationshipRecordId) {
		SObject targetRecord = ProfileHolder.getInstance().getTargetProfileBySourceId(relationshipRecordId);
		if (targetRecord != null) {
			return new SObjectWrapper(targetRecord, TargetOrgSchema.getInstance().getFields("Profile"));
		}
		return null;
	}
	/**
	 * Finds the referenced target user's SObjectWrapper based on the given relationship record ID.
	 * If the target user is available, it returns a corresponding SObjectWrapper; otherwise, it returns null.
	 */
	private SObjectWrapper findReferencedTargetUser(String relationshipRecordId) {
		SObject targetRecord = UserHolder.getInstance().getTargetOrgUserBySourceId(relationshipRecordId);
		if (targetRecord != null) {
			return new SObjectWrapper(targetRecord, TargetOrgSchema.getInstance().getFields("User"));
		}
		return null;
	}
	/**
	 * Finds the referenced target regular record's SObjectWrapper based on the given relationship record ID
	 * and the source field wrapper.
	 * If the target regular record is available, it returns a corresponding SObjectWrapper; otherwise, it returns null.
	 */
	private SObjectWrapper findReferencedTargetRegularRecord(FieldWrapper srcFieldWrapper, String relationshipRecordId) {
		//Fetch target record for lookup or master-detail fields
		String srcRefObjectNameWithNamespace = srcFieldWrapper.getFieldDesc().getReferenceTo()[0];
		String srcRefObjectNameWithoutNamespaceLowercase = AppUtils.removeNamespace(srcRefObjectNameWithNamespace).toLowerCase();
		SObjectDataHolder srcRefDataHolder = srcDataCache.get(srcRefObjectNameWithoutNamespaceLowercase);
		SObjectWrapper srcRefWrapper = srcRefDataHolder.getRecord(relationshipRecordId);
		String[] uniqueFieldsWithNamespace = srcRefDataHolder.getUniqueFieldNamesWithNamespace();
		String srcUniqueFieldValue = AppUtils.findUniqueFieldValue(srcRefWrapper.getRecord(), uniqueFieldsWithNamespace, true);
		
		
		String tarRefObjectNameWithoutNamespaceLowercase = AppUtils.addNamespace(srcRefObjectNameWithoutNamespaceLowercase, TAR_NAMESPACE);
		SObjectDataHolder tarRefDataHolder = tarDataCache.get(AppUtils.removeNamespace(tarRefObjectNameWithoutNamespaceLowercase));
		String tarUniqueFieldValue = srcUniqueFieldValue.replace(managedPackageNamespaceSrc, managedPackageNamespaceTarget);
		SObjectWrapper tarRefWrapper = tarRefDataHolder.getRecordUsingUniqueKeyString(tarUniqueFieldValue);

		if (tarRefWrapper == null) {
			if (item.isRelationshipRecordsMustFoundInTargetOrg()) {
				throw new UnCheckedException("Relationship record not found in target org for Unique ID " + tarUniqueFieldValue + 
						"; source org record: " + srcWrapper.getId() + 
						", sobject name: " + srcWrapper.getObjectNameWithnamespace() + ", relationship record: " + relationshipRecordId);
			}
		}

		return tarRefWrapper;
	}
	/**
	 * Changes reference fields on the target record by updating their values based on the
	 * referenced IDs mapping. Handles special cases for the "OwnerId" field.
	 */
	private void changeReferenceFieldsOnTargetRecord() {
		List<FieldWrapper> srcRefFields = srcWrapper.getReferenceFields(true, false, false);
		for (FieldWrapper srcRefField : srcRefFields) {
			String tarRefFieldName = srcRefField.getFieldName().replace(managedPackageNamespaceSrc, managedPackageNamespaceTarget);
			String srcRefId = srcRefField.getValueAsString(false);
			String tarRefId = null;
			if (srcRefId != null) {
				tarRefId = referencedIds.get(srcRefId);
				if (StringUtils.isEmpty(tarRefId)) {
					refFieldNotFoundInTargetOrg = true;
				}
				else {
					if (tarRefFieldName.equalsIgnoreCase("OwnerId")) {
						Boolean isTargetUserActive = UserHolder.getInstance().isTargetUserActive(tarRefId);
						if (!isTargetUserActive) {
							tarRefId = targetOrgAlternateOwnerId;
						}
					}
				}
			}
			ForceUtils.setSObjectFieldValue(targetRecord, tarRefFieldName, tarRefId);
		}
	}

	public boolean isRefFieldNotFoundInTargetOrg() {
		return refFieldNotFoundInTargetOrg;
	}

	public static void main(String[] args) {
	}

}

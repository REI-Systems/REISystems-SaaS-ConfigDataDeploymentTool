package com.gg.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.gg.config.util.JSONFileReader;
import com.gg.config.vo.MigrationItem;
import com.gg.config.vo.SrcToTargetVO;
import com.gg.meta.util.GGUtils;
import com.lib.util.StringUtils;
import com.lib.util.UnCheckedException;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Contains all variables that can be changed for this application to run. Do
 * NOT modify any variables in this class.
 * 
 * @author shahnavazk
 *
 */
public class Variables {
	public static Boolean firstTimeMigration; // first time or delta migration?

	// Source and target org names (as they are defined in organization.xml file
	public static final String SRC_ORG_NAME_ALIAS = "src";
	public static final String TAR_ORG_NAME_ALIAS = "target";

	public static final String SRC_NAMESPACE = ""; // include double underscore at the end
	public static final String TAR_NAMESPACE = ""; // include double underscore at the end
	public static String managedPackageNamespaceSrc;
	public static String managedPackageNamespaceTarget;
	public static final String INTERNAL_UNIQUEID_FIELDNAME = "InternalUniqueID__c";

	public static Map<String, String> usernameSuffixMap;
	public static Map<String, String> userFullNameSuffixMap;
	public static Map<String, String> licenseMap;
	public static List<String> migrateUsernames;
	public static List<String> unwantedFieldsList; // added from nakul
	public static List<String> unwantedClassesList;
	public static List<String> unwantedPagesList;
	public static List<String> unwantedLayoutsList;
	public static String targetOrgPrefix;
	public static String communityGuestProfile = "Public Profile";
	public static String siteGuestProfile = "Site Profile";
	public static String targetOrgAlternateOwnerId  = ""; // Used to set the OwnerId field on target org if the existing
															// owner is inactive
	public static String targetOrgAlternateOwnerUsername;
	public static Boolean userLicenseLimitIgnore = true; // When user license limit is hit, should the code still
															// migrate them as inactive users to avoid the errors?
	public static Boolean converActiveUserToInactive = false; // DON'T CHANGE THIS VARIABLE

	public static Map<String, String[]> sobjectUniqueFields; // key->lookup or master-detail sobject name, value->list
																// of fields to form uniqueness
	public static MigrationItem[] specificConfigSobjects;
	public static MigrationItem[] configSobjects; // Identifies the list of sobjects (with a proper sequence) which will
													// be migrated. Do NOT include namespace.
	public static MigrationItem[] productMasterSobjects; // Identifies the list of sobjects (with a proper sequence)
															// which will be migrated. Do NOT include namespace.
	public static MigrationItem[] productSampleSobjects; // Identifies the list of sobjects (with a proper sequence)
															// which will be migrated. Do NOT include namespace.
	public static Set<String> globalExclusionFields; // Across all sobjects, which fields should never be migrated to
														// target org?
	public static Map<String, List<String>> sobjectExclusionFields; // key->sobject name without namespace
	public static boolean excludeUserRelationshipFields = false; // Should we exclude all User relationship fields since
																	// user records may not be there in target org?
	public static final String backUpFolder = GGUtils.getRootFolderURL() + "backup\\";
	public static Double apiVersion = 39.0;
	public static String targetOrgUsernameSuffix;
	public static List<String> hierarchyCustomSettings = new ArrayList<String>(); // without namespace
	public static Boolean ignoreUserMapping = false;
	//Shubhangi Addded below fields 
	public static Boolean isCustomSettingsRequired ;      //boolean check to specify custom setting required or not
	public static Boolean isAccountContactMigrationRequired; // boolean check to specify Account and contact migration required or not
	public static Boolean isMigrationRequiredFromAllUsers;  // boolean check to specify migration from all users or only from current user
	public static Boolean isKeyValueStoreMigrationRequired; // boolean check to specify key value store migration required or not
	public static String  globalWhereCondition;
	// Holds the API connection to salesforce orgs
	public static ForceDelegateRaw src;
	public static ForceDelegateRaw target;

	public static Logger log = Logger.getRootLogger();

	public static List<String> obsoleteObjects = new ArrayList<String>();

	static {

		/*
		 * Across all sobjects, which relationship fields should never be migrated to
		 * target org? Regular case is fine. NO NAMESPACE.
		 */
		globalExclusionFields = new HashSet<String>(Arrays.asList("ClonedFrom__c", "DelegatedApproverId",
				"CallCenterId", "ConnectionSentId", "ConnectionReceivedId", "JigsawCompanyId", "MasterRecordId",
				"UserRegistration__c", "Agency__c"));

		sobjectExclusionFields = new HashMap<String, List<String>>();
		List<String> excludeFields = new ArrayList<String>();
		excludeFields.add("Username");
		excludeFields.add("AccountId");
		excludeFields.add("OfflinePdaTrialExpirationDate");
		excludeFields.add("ContactId");
		excludeFields.add("OfflineTrialExpirationDate");
		excludeFields.add("PortalRole");
		excludeFields.add("UserPermissionsSFContentUser");
		excludeFields.add("UserPermissionsChatterAnswersUser");
		excludeFields.add("LastLoginDate");
		excludeFields.add("LastPasswordChangeDate");
		sobjectExclusionFields.put("User", excludeFields);

		/*
		 * All list custom settings are automatically handled in the code and Name field
		 * is treated as unique field. Hierarchy custom settings are not handled at this
		 * time. Any other objects that are NOT listed here, will be assumed that
		 * 'InternalUniqueID__c' field is the unique field.
		 * 
		 * **** Include namespace in object or field names here ***
		 */
		sobjectUniqueFields = new HashMap<String, String[]>(); // both key and values are case insensitive
		sobjectUniqueFields.put("RecordType", new String[] { "SObjectType", "DeveloperName" });
		sobjectUniqueFields.put("UserRole", new String[] { "Name" });
		sobjectUniqueFields.put("Profile", new String[] { "Name" });
		sobjectUniqueFields.put("Organization", new String[] { "Name" });
		sobjectUniqueFields.put("Group", new String[] { "DeveloperName", "Type" });

		src = ForceDelegateRaw.login(SRC_ORG_NAME_ALIAS);
		target = ForceDelegateRaw.login(TAR_ORG_NAME_ALIAS);

		initAppSpecificData();
		changeUniqueFieldsIntoLowercase();
		changeExclusionRelationshipFieldsIntoLowercase();

		if (StringUtils.isNonEmpty(managedPackageNamespaceSrc) && managedPackageNamespaceSrc.endsWith("__") == false) {
			log.error("MANAGED_PACKAGE_NAMESPACE variable should end with double underscore");
			throw new RuntimeException("MANAGED_PACKAGE_NAMESPACE variable should end with double underscore");
		}
	}

	/* PRIVATE METHODS */

	private static void initAppSpecificData() {
		// Shubhangi Added below changes for getting details from json instead properties
		// file
		SrcToTargetVO srcToTargetVO = JSONFileReader.readSrcToTargetJSON();
		firstTimeMigration = srcToTargetVO.getFirstTimeMigration();
		targetOrgPrefix = srcToTargetVO.getTargetOrgPrefix();
		targetOrgAlternateOwnerId = srcToTargetVO.getTargetOrgAlternateOwnerId();
		targetOrgUsernameSuffix = srcToTargetVO.getTargetOrgUsernameSuffix();
		usernameSuffixMap = srcToTargetVO.getUsernameSuffixMap();
		userFullNameSuffixMap = srcToTargetVO.getUserFullNameSuffixMap();
		licenseMap = srcToTargetVO.getLicenseMap();
		managedPackageNamespaceSrc = srcToTargetVO.getSrcManagedPackageNamespace();
		managedPackageNamespaceTarget = srcToTargetVO.getTargetManagedPackageNamespace();
		unwantedFieldsList = srcToTargetVO.getUnwantedFields(); // added from nakul
		unwantedClassesList = srcToTargetVO.getUnwantedClasses();
		unwantedLayoutsList = srcToTargetVO.getUnwantedLayouts();
		isAccountContactMigrationRequired = srcToTargetVO.getIsAccountContactMigrationRequired();
		isCustomSettingsRequired = srcToTargetVO.getIsCustomSettingsRequired();
		isMigrationRequiredFromAllUsers = srcToTargetVO.getIsMigrationRequiredFromAllUsers();
		isKeyValueStoreMigrationRequired = srcToTargetVO.getIsKeyValueStoreMigrationRequired();
		
		

		if (ignoreUserMapping == false) {
			try {
				SObject user = target.querySingle("Select Id, Username from User where Id=?",
						new Object[] { targetOrgAlternateOwnerId });
				targetOrgAlternateOwnerId = user.getId();
				targetOrgAlternateOwnerUsername = ForceUtils.getSObjectFieldValue(user, "Username");
			} catch (Exception e) {
				log.error("Userid " + targetOrgAlternateOwnerId + " not found in target org");
				throw new UnCheckedException("Userid " + targetOrgAlternateOwnerId + " not found in target org");
			}
		}

		String className = srcToTargetVO.getAppDataInitializer();
		try {
			AppDataInitializer initializer = (AppDataInitializer) Class.forName(className).newInstance();
			initializer.init();
		} catch (Exception e) {
			log.error(e.getMessage());;
			throw new UnCheckedException(e);
		}
	}

	// The following code converts the unique fields into lower case.
	protected static void changeUniqueFieldsIntoLowercase() {
		Map<String, String[]> tempUniqueFields = new HashMap<String, String[]>();
		for (String objectName : sobjectUniqueFields.keySet()) {
			String[] fields = sobjectUniqueFields.get(objectName);
			for (int i = 0; i < fields.length; i++) {
				fields[i] = fields[i].toLowerCase();
			}
			tempUniqueFields.put(objectName.toLowerCase(), fields);
		}
		sobjectUniqueFields = tempUniqueFields;
	}

	protected static void changeExclusionRelationshipFieldsIntoLowercase() {
		Set<String> tempFields = new HashSet<String>();
		for (String fieldName : globalExclusionFields) {
			tempFields.add(fieldName.toLowerCase());
		}
		globalExclusionFields = tempFields;
	}

	protected static boolean isFirstTimeMigration() {
		return firstTimeMigration;
	}

	protected static boolean isDeltaMigration() {
		return !isFirstTimeMigration();
	}

	protected static Map<String, String> convertToMap(List<String> values) {
		Map<String, String> result = new HashMap<String, String>();
		if (values != null) {
			for (String value : values) {
				String[] arr = value.split(":");
				result.put(arr[0], arr[1]);
			}
		}
		return result;
	}
}

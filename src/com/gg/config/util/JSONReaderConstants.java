package com.gg.config.util;
/**
 * 
 * @author Shubhangi Shinde, Vijayalaxmi
 *
 */
public class JSONReaderConstants {

	//JSON File Name
	public static final String JSON_FILE_NAME = "/config.json";
	
	//Parent Keys
	public static final String SRC_ORG_NAME_ALIAS = "src";
	public static final String TAR_ORG_NAME_ALIAS = "target";
	public static final String SRC_TO_TAR_ORG_NAME_ALIAS = "srcToTarget";
	public static final String DATE_TIME_WHERE_CLAUSE_FOR_MIGRATION="dateTimeWhereClauseForMigration";
	public static final String OBJECT_TYPE_SPECIFIC_CONFIG_SOBJECTS = "specificConfigSobjects";
	public static final String OBJECT_TYPE_CONFIG_SOBJECTS = "configSobjects";
	public static final String OBJECT_TYPE_PRODUCT_MASTER_SOBJECTS = "productMasterSobjects";
	
	//Child Keys
	public static final String KEY_FIRST_TIME_MIGRATION = "firstTimeMigration";
	public static final String KEY_TARGET_ORG_PREFIX = "targetOrgPrefix";
	public static final String KEY_TARGET_ORG_ALTERNATE_OWNER_ID = "targetOrgAlternateOwnerId";
	public static final String KEY_UNWANTED_FIELDS = "unwantedFields";
	public static final String KEY_SRC_MANAGED_PACKAGE_NAMESPACE = "srcManagedPackageNamespace";
	public static final String KEY_UNWANTED_CLASSES = "unwantedClasses";
	public static final String KEY_TARGET_MANAGED_PACKAGE_NAMESPACE = "targetManagedPackageNamespace";
	public static final String KEY_APP_DATA_INITIALIZER = "appDataInitializer";
	public static final String KEY_UNWANTED_LAYOUTS = "unwantedLayouts";
	public static final String KEY_TARGET_ORG_USERNAME_SUFFIX = "targetOrgUsernameSuffix";
	public static final String KEY_LICENSE_MAP = "licenseMap";
	public static final String KEY_USER_NAME_SUFFIX_MAP = "usernameSuffixMap";
	public static final String KEY_USER_FULL_NAME_SUFFIX_MAP = "userFullNameSuffixMap";
	public static final String KEY_IS_CUSTOM_SETTINGS_REQUIRED = "isCustomSettingsRequired";
	public static final String KEY_IS_ACCOUNT_CONTACT_MIGRATION_REQUIRED = "isAccountContactMigrationRequired";
	public static final String KEY_OBJECTNAME_WITHOUT_NAMESPACE = "objectNameWithoutNamespace";
	public static final String KEY_PACKAGED = "packaged";
	public static final String KEY_RELATIONSHIP_RECORD_MUST_FPUND_IN_TARGET_ORG = "relationshipRecordsMustFoundInTargetOrg";
	public static final String KEY_EXCLUDE_FIELDS = "excludeFields";
	public static final String KEY_WHERE_CLAUSE = "whereClause";
	public static final String KEY_BATCH_SIZE = "batchSize";
	public static final String KEY_DELETE_TARGET_RECORDS = "deleteTargetRecords";
	public static final String KEY_FIELD_API_NAME_WITHOUT_NAMESPACE = "fieldAPINameWithoutNamespace";
	public static final String KEY_EXCLUDE_FIELDS_PACKAGED = "package";
	public static final String KEY_IS_MIGRATION_REQUIRED_FROM_ALL_USERS= "isMigrationRequiredFromAllUsers";
	public static final String KEY_IS_KEY_VALUE_STORE_MIGRATION_REQUIRED= "isKeyValueStoreMigrationRequired";
	public static final String KEY_USER = "user";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_ENDPOINT = "endPoint";
	public static final String KEY_DATE_LITERAL_OR_DATETIME ="dateLiteralOrDateTime";
	public static final String KEY_OPERATOR="operator";
	
	public static final String LIST_SEPARATOR = ",";
	public static final String MAP_SEPARATOR = ":";
	
	
	
	


}

package com.gg.config.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.gg.common.Variables;
import com.gg.config.vo.MigrationItem;
import com.gg.config.vo.SchemaCache;
import com.gg.meta.helper.ProfileHolder;
import com.gg.meta.helper.UserHolder;
import com.lib.util.StringUtils;
import com.lib.util.UnCheckedException;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;

public class AppUtils {
	
	private static Map<String, SchemaCache> schemaMap = new HashMap<String, SchemaCache>(); //key->org name
	private static Logger log = Logger.getRootLogger();
	
	/**
	 * Retrieves the unique ID field name(s) for a given SObject name with or without a namespace.
	 * It also generates a lowercase version of the SObject name for case-insensitive comparison purposes.
	 * @param sobjectNameWithoutNamespace
	 * @param namespace
	 * @param gateRaw
	 * @return
	 */
	public static String[] getUniqueIdFieldName(String sobjectNameWithoutNamespace, String namespace, ForceDelegateRaw gateRaw) {
	    String sobjectNameWithNamespace = namespace + sobjectNameWithoutNamespace;
	    String sobjectNameWithNamespaceLower = sobjectNameWithNamespace.toLowerCase();

	    if (Variables.sobjectUniqueFields.containsKey(sobjectNameWithNamespaceLower)) {
	        return Variables.sobjectUniqueFields.get(sobjectNameWithNamespaceLower);
	    }

	    DescribeGlobalSObjectResult sObjectMetaData = AppUtils.getSObjectMetaDataByName(sobjectNameWithNamespace, gateRaw);
	    boolean isCustomSetting = sObjectMetaData.isCustomSetting();
	    boolean isManagedObject = StringUtils.isNonEmpty(namespace);

	    if (isCustomSetting) {
	        if (isHierarchyCustomSetting(sobjectNameWithNamespace)) {
	            return new String[]{"SetupOwnerId"};
	        } else {
	            return new String[]{"Name"};
	        }
	    } else {
	        String uniqueIdFieldName = Variables.INTERNAL_UNIQUEID_FIELDNAME;
	        if (isManagedObject) {
	            uniqueIdFieldName = namespace + uniqueIdFieldName;
	        }

	        return new String[]{uniqueIdFieldName.toLowerCase()};
	    }
	}

	/**
	 * Checks if the provided field name is a unique ID field for a given SObject name with or without a namespace.
	 * This method verifies whether the given fieldName is one of the unique ID field names associated with the provided SObject name, considering the namespace and SObject's custom setting status.
	 * @param sobjectNameWithoutNamespace
	 * @param namespace
	 * @param fieldName
	 * @param gateRaw
	 * @return
	 */
	public static boolean isUniqueIdFieldName(String sobjectNameWithoutNamespace, String namespace, String fieldName, ForceDelegateRaw gateRaw) {
		for (String uniqueIdField : getUniqueIdFieldName(sobjectNameWithoutNamespace, namespace, gateRaw)) {
			if (uniqueIdField.equalsIgnoreCase(fieldName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds the unique field value (key) for a given SObject record based on the specified unique field names.
	 * This method is responsible for generating a unique key representing the provided SObject record based on the given array of unique field names (uniqueFieldNamesWithNamespace).
     * The exception indicates that the uniqueness of the record cannot be determined accurately due to the missing value(s).
	 * @param record
	 * @param uniqueFieldNamesWithNamespace
	 * @param isSourceOrg
	 * @return
	 */
	public static String findUniqueFieldValue(SObject record, String[] uniqueFieldNamesWithNamespace, boolean isSourceOrg) {
	    List<String> uniqueFieldValues = new ArrayList<>(uniqueFieldNamesWithNamespace.length);
	    for (String uniqueFieldNameWithNamespace : uniqueFieldNamesWithNamespace) {
	        String fieldValue = ForceUtils.getSObjectFieldValue(record, uniqueFieldNameWithNamespace);
	        if (StringUtils.isEmpty(fieldValue)) {
	            String errorMessage = String.format("Unique field '%s' is empty on record '%s'. SObject name: '%s', Source org? %s",
	                    uniqueFieldNameWithNamespace, record.getId(), record.getType(), isSourceOrg);
	            log.error(errorMessage);
	            throw new UnCheckedException(errorMessage);
	        }
	        uniqueFieldValues.add(fieldValue);
	    }

	    String key = StringUtils.getConcatenatedString(uniqueFieldValues,"#");

	    // If it is a hierarchy custom setting, convert the unique key value (from SetupOwnerId field) into textual data (instead of record id)
	    if (isHierarchyCustomSetting(record.getType())) {
	        key = convertUniqueKeyForHierarchyCustomSetting(key, isSourceOrg);
	    }

	    return key;
	}
	
   /**
    * Removes the namespace from the provided object or field name.
    * This method is responsible for removing the namespace from the given objectOrFieldName by replacing it with an empty string. 
    * The method considers both the source (SRC) namespace and the target (TAR) namespace, if they are present.
    * @param objectOrFieldName
    * @return
    */
	public static String removeNamespace(String objectOrFieldName) {
		if (StringUtils.isNonEmpty(Variables.SRC_NAMESPACE)) {
			objectOrFieldName = objectOrFieldName.replace(Variables.SRC_NAMESPACE, "");
		}
		if (StringUtils.isNonEmpty(Variables.TAR_NAMESPACE)) {
			objectOrFieldName = objectOrFieldName.replace(Variables.TAR_NAMESPACE, "");
		}
		return objectOrFieldName;
	}
 
	
	/**
	 * Removes the managed package namespace from the provided object or field name.
	 * This method is responsible for removing the managed package namespace from the given objectOrFieldName by performing a search and replace operation.
	 * The method considers both the source (managedPackageNamespaceSrc) namespace and the target (managedPackageNamespaceTarget) namespace, if they are present.
	 * @param objectOrFieldName
	 * @return
	 */
	public static String removePackageNamespace(String objectOrFieldName) {
		objectOrFieldName = objectOrFieldName.replace(Variables.managedPackageNamespaceSrc, "");
		objectOrFieldName = objectOrFieldName.replace(Variables.managedPackageNamespaceTarget, "");
		return objectOrFieldName;
	}
  /**
   * Adds the specified namespace to the provided object or field name.
   * First, the method calls the removeNamespace method to remove any existing namespace from the objectOrFieldName. This ensures that any existing namespace is replaced with an empty string.
   * @param objectOrFieldName
   * @param namespace
   * @return
   */
	public static String addNamespace(String objectOrFieldName, String namespace) {
		objectOrFieldName = removeNamespace(objectOrFieldName);
		if (objectOrFieldName.endsWith("__c")) {
			objectOrFieldName = namespace + objectOrFieldName;
		}
		return objectOrFieldName;
	}
	
	/**
	 * Retrieves metadata for a Salesforce SObject based on the given record ID.
	 * @param recordId The record ID from which to extract the SObject prefix.
	 * @param gateRaw  The ForceDelegateRaw instance to interact with the Salesforce API.
	 * @return
	 */
	public static DescribeGlobalSObjectResult getSObjectMetaDataByRecordId(String recordId, ForceDelegateRaw gateRaw) {		
		String sobjectPrefix = recordId.substring(0, 3);
		SchemaCache schema = schemaMap.get(gateRaw.getOrgName());
		if (schema == null) {
			schema = new SchemaCache(gateRaw);
			schemaMap.put(gateRaw.getOrgName(), schema);
		}
		return schema.getSObjectMetaDataByPrefix(sobjectPrefix);
	}

	/**
	 * 
	 * @param sobjectNameWithNamespace
	 * @param gateRaw
	 * @return
	 */
	public static DescribeGlobalSObjectResult getSObjectMetaDataByName(String sobjectNameWithNamespace, ForceDelegateRaw gateRaw) {
		SchemaCache schema = schemaMap.get(gateRaw.getOrgName());
		if (schema == null) {
			schema = new SchemaCache(gateRaw);
			schemaMap.put(gateRaw.getOrgName(), schema);
		}
		return schema.getSObjectMetaDataByName(sobjectNameWithNamespace);
	}

	/**
	 * Checks whether a field is excluded from migration based on the provided MigrationItem and Field objects.
	 *
	 * @param item The MigrationItem containing information about excluded fields.
	 * @param f The Field object representing the field to be checked.
	 * @return True if the field is excluded from migration, false otherwise.
	 */
	public static boolean isFieldExcludedFromMigration(MigrationItem item, Field f) {
	    String currentFieldName = f.getName().toLowerCase();
	    String currentFieldNameWithoutNamespace = AppUtils.removeNamespace(currentFieldName);
	    String currentFieldNameWithoutPackageNamespace = AppUtils.removePackageNamespace(currentFieldNameWithoutNamespace);

	    if (Variables.globalExclusionFields.contains(currentFieldNameWithoutPackageNamespace) ||
	        currentFieldNameWithoutPackageNamespace.startsWith("tmp") ||
	        currentFieldNameWithoutPackageNamespace.startsWith("temp") ||
	        currentFieldNameWithoutPackageNamespace.contains("__tmp") ||
	        Variables.excludeUserRelationshipFields) {
	        return true;
	    }

	    return item.getExcludedFieldsAsList()
	            .stream()
	            .anyMatch(excludedFieldNameWithoutNamespace -> excludedFieldNameWithoutNamespace.equalsIgnoreCase(currentFieldNameWithoutPackageNamespace));
	}

	
	public static boolean isHierarchyCustomSetting(String sobjectNameWithnamespace) {
		String sobjectNameWithoutNamespace = removePackageNamespace(sobjectNameWithnamespace);
		return Variables.hierarchyCustomSettings.contains(sobjectNameWithoutNamespace);
	}
	/**
	 * Checks whether the given data represents a temporary file.
	 *
	 * @param data The data to be checked for temporary file representation.
	 * @return True if the data is associated with a temporary file, false otherwise.
	 */
	public static boolean isTempFile(String data) {
		data = data.toLowerCase();
		return (data.startsWith("tmp") || data.startsWith("__tmp") || data.startsWith("lightning") || 
				data.contains("__tmp") || data.contains("__temp") || data.contains(".tmp") ||
				data.contains("tempverified")) && !data.contains("__template");
	}

	public static boolean isNonTempFile(String fileName) {
		return !isTempFile(fileName);
	}
	/* PRIVATE METHODS */
	
	/**
	 * Converts a unique key for hierarchy custom settings into a human-readable representation.
	 *
	 * @param uniqueKey The unique key to be converted.
	 * @param isSourceOrg A boolean flag indicating whether the unique key is from the source organization.
	 * @return The human-readable representation of the unique key.
	 */
	private static String convertUniqueKeyForHierarchyCustomSetting(String uniqueKey, boolean isSourceOrg) {
		if (uniqueKey.startsWith("00D")) {  //means organization
			return "Organization";
		}
		else if (uniqueKey.startsWith("005")) {  //means user
			if (isSourceOrg) {
				return UserHolder.getInstance().getSourceInternalUniqueId(uniqueKey);
			}
			else {
				return UserHolder.getInstance().getTargetInternalUniqueId(uniqueKey);
			}
		}
		else {  //means profile
			if (isSourceOrg) {
				return ProfileHolder.getInstance().getSourceProfileName(uniqueKey);
			}
			else {
				return ProfileHolder.getInstance().getTargetProfileName(uniqueKey);
			}
		}
	}
	


	public static void main(String[] args) {
		ForceDelegateRaw raw = ForceDelegateRaw.login("ggint");
		DescribeGlobalSObjectResult result = getSObjectMetaDataByRecordId("a1t37000000MnrR", raw);
		System.out.println(result);
		
		result = getSObjectMetaDataByRecordId("a1t37000000MnrR", raw);
		log.info(result);
	}
}

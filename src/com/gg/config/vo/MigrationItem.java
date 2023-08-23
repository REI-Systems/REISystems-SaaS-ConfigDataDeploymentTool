package com.gg.config.vo;

import java.util.ArrayList;
import java.util.List;

import com.gg.common.Variables;

public class MigrationItem {
	private String objectNameWithoutNamespace;
	private boolean packaged = false;
	private boolean relationshipRecordsMustFoundInTargetOrg = true; // If the primary source record has a lookup or
																	// master-detail relationship field, should that
																	// relationship record be found in the target org
																	// using unique id value?
	private List<FieldItem> excludeFields = new ArrayList<FieldItem>(); // Which fields should NOT be migrated to target
																		// org? If not specified, all fields are
																		// migrated.
	private String whereClause;
	private int batchSize = 500;
	private boolean deleteTargetRecords = false; // delete non-matching target org records after migration?

	public MigrationItem(String objectNameWithoutNamespace, boolean packaged,
			boolean relationshipRecordsMustFoundInTargetOrg, List<FieldItem> excludeFields, String whereClause,
			int batchSize, boolean deleteTargetRecords) {
		super();
		this.objectNameWithoutNamespace = objectNameWithoutNamespace;
		this.packaged = packaged;
		this.relationshipRecordsMustFoundInTargetOrg = relationshipRecordsMustFoundInTargetOrg;
		this.excludeFields = excludeFields;
		this.whereClause = whereClause;
		this.batchSize = batchSize;
		this.deleteTargetRecords = deleteTargetRecords;
		init();
	}

	private List<String> excludeFieldListWithNamespace = new ArrayList<String>();
	private List<String> excludeFieldListWithNamespaceLowercase = new ArrayList<String>();

	public MigrationItem(String objectNameWithoutNamespace) {
		this.objectNameWithoutNamespace = objectNameWithoutNamespace;
		init();
	}

	public MigrationItem(String objectNameWithoutNamespace, boolean packaged) {
		this.objectNameWithoutNamespace = objectNameWithoutNamespace;
		this.packaged = packaged;
		init();
	}

	public MigrationItem excludeField(String fieldAPIName, boolean packaged) {
		excludeFields.add(new FieldItem(fieldAPIName, packaged));
		return this;
	}

	public MigrationItem excludeField(String fieldAPIName) {
		excludeFields.add(new FieldItem(fieldAPIName, false));
		return this;
	}

	public boolean isDeleteTargetRecords() {
		return deleteTargetRecords;
	}

	public MigrationItem deleteTargetRecords(boolean deleteTargetRecords) {
		this.deleteTargetRecords = deleteTargetRecords;
		return this;
	}

	public MigrationItem where(String condition) {
		this.whereClause = condition;
		return this;
	}

	public MigrationItem setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public MigrationItem excludeRelationshipFields() {
		this.relationshipRecordsMustFoundInTargetOrg = false;
		return this;
	}

	public boolean isPackaged() {
		return packaged;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public String getObjectName(boolean isSrcOrg) {
		if (isSrcOrg) {
			return getSrcObjectName();
		} else {
			return getTargetObjectName();
		}
	}
	/**
	 * Gets the source object name. If the object is part of a managed package,
	 * it ensures the object name includes the appropriate namespace from the source organization.
	 */
	public String getSrcObjectName() {
		if (packaged) {
			return objectNameWithoutNamespace.startsWith(Variables.managedPackageNamespaceSrc)
					? objectNameWithoutNamespace
					: Variables.managedPackageNamespaceSrc + objectNameWithoutNamespace;
		} else {
			return objectNameWithoutNamespace;
		}
	}
	/**
	 * Gets the target object name. If the object is part of a managed package,
	 * it ensures the object name includes the appropriate namespace from the target organization.
	 */
	public String getTargetObjectName() {
		objectNameWithoutNamespace = objectNameWithoutNamespace.replace(Variables.managedPackageNamespaceSrc,
				Variables.managedPackageNamespaceTarget);
		if (packaged) {
			if (objectNameWithoutNamespace.startsWith(Variables.managedPackageNamespaceTarget)) {
				return objectNameWithoutNamespace;
			} else {
				return Variables.managedPackageNamespaceTarget + objectNameWithoutNamespace;
			}
		} else {
			return objectNameWithoutNamespace;
		}
	}

	public void setPackaged(boolean packaged) {
		this.packaged = packaged;
	}

	public boolean isRelationshipRecordsMustFoundInTargetOrg() {
		return relationshipRecordsMustFoundInTargetOrg;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public List<FieldItem> getExcludeFields() {
		return excludeFields;
	}

	public List<String> getExcludedFieldsAsList() {
		return excludeFieldListWithNamespace;
	}

	public String getObjectNameWithoutNamespace() {
		return objectNameWithoutNamespace;
	}

	public void setObjectNameWithoutNamespace(String objectNameWithoutNamespace) {
		this.objectNameWithoutNamespace = objectNameWithoutNamespace;
	}

	public List<String> getExcludeFieldListWithNamespace() {
		return excludeFieldListWithNamespace;
	}

	public void setExcludeFieldListWithNamespace(List<String> excludeFieldListWithNamespace) {
		this.excludeFieldListWithNamespace = excludeFieldListWithNamespace;
	}

	public List<String> getExcludeFieldListWithNamespaceLowercase() {
		return excludeFieldListWithNamespaceLowercase;
	}

	public void setExcludeFieldListWithNamespaceLowercase(List<String> excludeFieldListWithNamespaceLowercase) {
		this.excludeFieldListWithNamespaceLowercase = excludeFieldListWithNamespaceLowercase;
	}

	public void setRelationshipRecordsMustFoundInTargetOrg(boolean relationshipRecordsMustFoundInTargetOrg) {
		this.relationshipRecordsMustFoundInTargetOrg = relationshipRecordsMustFoundInTargetOrg;
	}

	public void setExcludeFields(List<FieldItem> excludeFields) {
		this.excludeFields = excludeFields;
	}

	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	public List<String> getExcludedFieldsLowercaseAsList() {
		return excludeFieldListWithNamespaceLowercase;
	}

	public String toString() {
		return getSrcObjectName();
	}

	/* PRIVATE METHODS */

	private void init() {
		if (excludeFields != null) {
			for (FieldItem excludeField : excludeFields) {
				excludeFieldListWithNamespace.add(excludeField.getSrcFieldName());
				excludeFieldListWithNamespaceLowercase.add(excludeField.getSrcFieldName().toLowerCase());
			}
		}
	}
}

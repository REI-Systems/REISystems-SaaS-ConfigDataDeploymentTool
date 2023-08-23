package com.gg.config.vo;

import java.util.List;

public class MigrationItemVO {

	private String objectNameWithoutNamespace;
	private Boolean packaged;
	private Boolean relationshipRecordsMustFoundInTargetOrg;
	private List<FieldItem> excludeFields;
	private String whereClause;
	private Integer batchSize;
	private Boolean deleteTargetRecords;

	public String getObjectNameWithoutNamespace() {
		return objectNameWithoutNamespace;
	}

	public void setObjectNameWithoutNamespace(String objectNameWithoutNamespace) {
		this.objectNameWithoutNamespace = objectNameWithoutNamespace;
	}

	public boolean isPackaged() {
		return packaged;
	}

	public void setPackaged(boolean packaged) {
		this.packaged = packaged;
	}

	public boolean isRelationshipRecordsMustFoundInTargetOrg() {
		return relationshipRecordsMustFoundInTargetOrg;
	}

	public void setRelationshipRecordsMustFoundInTargetOrg(boolean relationshipRecordsMustFoundInTargetOrg) {
		this.relationshipRecordsMustFoundInTargetOrg = relationshipRecordsMustFoundInTargetOrg;
	}

	public List<FieldItem> getExcludeFields() {
		return excludeFields;
	}

	public void setExcludeFields(List<FieldItem> excludeFields) {
		this.excludeFields = excludeFields;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public boolean isDeleteTargetRecords() {
		return deleteTargetRecords;
	}

	public void setDeleteTargetRecords(boolean deleteTargetRecords) {
		this.deleteTargetRecords = deleteTargetRecords;
	}
}

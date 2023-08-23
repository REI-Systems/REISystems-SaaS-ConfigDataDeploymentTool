package com.gg.config.vo;

import com.gg.common.Variables;

public class FieldItem {
	private String fieldAPINameWithoutNamespace;
	private boolean packaged = false;
	
	public FieldItem(String fieldAPINameWithoutNamespace, boolean packaged) {
		this.fieldAPINameWithoutNamespace = fieldAPINameWithoutNamespace;
		this.packaged = packaged;
	}
	

	/**
	 * Gets the appropriate field name based on the specified `isSrcOrg` parameter.
	 */
	public String getFieldName(boolean isSrcOrg) {
		if (isSrcOrg) {
			return getSrcFieldName();
		}
		else {
			return getTargetFieldName();
		}
	}
	/**
	 * Gets the field name for the source organization. If the field is part of a managed package,
	 * it ensures the field name includes the appropriate namespace from the source organization.
	 */
	public String getSrcFieldName() {
		if (packaged) {
			return fieldAPINameWithoutNamespace.startsWith(Variables.managedPackageNamespaceSrc) ? fieldAPINameWithoutNamespace : Variables.managedPackageNamespaceSrc + fieldAPINameWithoutNamespace;
		}
		else {
			return fieldAPINameWithoutNamespace;
		}
	}
	/**
	 * Gets the field name for the target organization. If the field is part of a managed package,
	 * it ensures the field name includes the appropriate namespace from the target organization.
	 */
	public String getTargetFieldName() {
		if (packaged) {
			return fieldAPINameWithoutNamespace.startsWith(Variables.managedPackageNamespaceSrc) ? fieldAPINameWithoutNamespace : Variables.managedPackageNamespaceSrc + fieldAPINameWithoutNamespace;
		}
		else {
			return fieldAPINameWithoutNamespace;
		}
	}

	public boolean isPackaged() {
		return packaged;
	}

	public void setPackaged(boolean packaged) {
		this.packaged = packaged;
	}

	
}

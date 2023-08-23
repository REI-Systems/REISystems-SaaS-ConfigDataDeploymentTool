package com.gg.config.vo;

import java.util.List;
import java.util.Map;
/**
 * 
 * @author Shubhangi Shinde, Vijayalaxmi
 *
 */
public class SrcToTargetVO {

	private String targetOrgPrefix;
	private String targetOrgAlternateOwnerId;
	private Boolean firstTimeMigration;
	private String targetOrgUsernameSuffix;
	private Map<String, String> usernameSuffixMap;
	private Map<String, String> userFullNameSuffixMap;
	private String appDataInitializer;
	private String srcManagedPackageNamespace;
	private String targetManagedPackageNamespace;
	private List<String> unwantedLayouts;
	private List<String> unwantedFields;
	private Map<String, String> licenseMap;
	private List<String> unwantedClasses;
	private Boolean isCustomSettingsRequired;
	private Boolean isAccountContactMigrationRequired;
	private Boolean isMigrationRequiredFromAllUsers;
	private Boolean isKeyValueStoreMigrationRequired;

	public String getTargetOrgPrefix() {
		return targetOrgPrefix;
	}

	public void setTargetOrgPrefix(String targetOrgPrefix) {
		this.targetOrgPrefix = targetOrgPrefix;
	}

	public String getTargetOrgAlternateOwnerId() {
		return targetOrgAlternateOwnerId;
	}

	public void setTargetOrgAlternateOwnerId(String targetOrgAlternateOwnerId) {
		this.targetOrgAlternateOwnerId = targetOrgAlternateOwnerId;
	}

	public Boolean getFirstTimeMigration() {
		return firstTimeMigration;
	}

	public void setFirstTimeMigration(Boolean firstTimeMigration) {
		this.firstTimeMigration = firstTimeMigration;
	}

	public String getTargetOrgUsernameSuffix() {
		return targetOrgUsernameSuffix;
	}

	public void setTargetOrgUsernameSuffix(String targetOrgUsernameSuffix) {
		this.targetOrgUsernameSuffix = targetOrgUsernameSuffix;
	}

	public Map<String, String> getUsernameSuffixMap() {
		return usernameSuffixMap;
	}

	public void setUsernameSuffixMap(Map<String, String> usernameSuffixMap) {
		this.usernameSuffixMap = usernameSuffixMap;
	}

	public Map<String, String> getUserFullNameSuffixMap() {
		return userFullNameSuffixMap;
	}

	public void setUserFullNameSuffixMap(Map<String, String> userFullNameSuffixMap) {
		this.userFullNameSuffixMap = userFullNameSuffixMap;
	}

	public String getAppDataInitializer() {
		return appDataInitializer;
	}

	public void setAppDataInitializer(String appDataInitializer) {
		this.appDataInitializer = appDataInitializer;
	}

	public String getSrcManagedPackageNamespace() {
		return srcManagedPackageNamespace;
	}

	public void setSrcManagedPackageNamespace(String srcManagedPackageNamespace) {
		this.srcManagedPackageNamespace = srcManagedPackageNamespace;
	}

	public String getTargetManagedPackageNamespace() {
		return targetManagedPackageNamespace;
	}

	public void setTargetManagedPackageNamespace(String targetManagedPackageNamespace) {
		this.targetManagedPackageNamespace = targetManagedPackageNamespace;
	}

	public List<String> getUnwantedLayouts() {
		return unwantedLayouts;
	}

	public void setUnwantedLayouts(List<String> unwantedLayouts) {
		this.unwantedLayouts = unwantedLayouts;
	}

	public List<String> getUnwantedFields() {
		return unwantedFields;
	}

	public void setUnwantedFields(List<String> unwantedFields) {
		this.unwantedFields = unwantedFields;
	}

	public Map<String, String> getLicenseMap() {
		return licenseMap;
	}

	public void setLicenseMap(Map<String, String> licenseMap) {
		this.licenseMap = licenseMap;
	}

	public List<String> getUnwantedClasses() {
		return unwantedClasses;
	}

	public void setUnwantedClasses(List<String> unwantedClasses) {
		this.unwantedClasses = unwantedClasses;
	}

	public Boolean getIsCustomSettingsRequired() {
		return isCustomSettingsRequired;
	}

	public void setIsCustomSettingsRequired(Boolean isCustomSettingsRequired) {
		this.isCustomSettingsRequired = isCustomSettingsRequired;
	}

	public Boolean getIsAccountContactMigrationRequired() {
		return isAccountContactMigrationRequired;
	}

	public void setIsAccountContactMigrationRequired(Boolean isAccountContactMigrationRequired) {
		this.isAccountContactMigrationRequired = isAccountContactMigrationRequired;
	}

	public Boolean getIsMigrationRequiredFromAllUsers() {
		return isMigrationRequiredFromAllUsers;
	}

	public void setIsMigrationRequiredFromAllUsers(Boolean isMigrationRequiredFromAllUsers) {
		this.isMigrationRequiredFromAllUsers = isMigrationRequiredFromAllUsers;
	}

	public Boolean getIsKeyValueStoreMigrationRequired() {
		return isKeyValueStoreMigrationRequired;
	}

	public void setIsKeyValueStoreMigrationRequired(Boolean isKeyValueStoreMigrationRequired) {
		this.isKeyValueStoreMigrationRequired = isKeyValueStoreMigrationRequired;
	}

	

	

}

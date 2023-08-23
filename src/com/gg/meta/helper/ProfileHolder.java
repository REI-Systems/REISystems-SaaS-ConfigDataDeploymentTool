package com.gg.meta.helper;

import java.util.HashMap;
import java.util.Map;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.sforce.soap.partner.sobject.SObject;

public class ProfileHolder extends Variables {
	private Map<String, SObject> srcProfiles = new HashMap<String, SObject>(); //key->profile id
	private Map<String, SObject> tarProfiles = new HashMap<String, SObject>(); //key->profile id
	private Map<String, SObject> tarProfileMap = new HashMap<String, SObject>(); //key->profile name
	private static ProfileHolder instance;
	
	private ProfileHolder() {
		loadProfiles();
	}
	
	public static ProfileHolder getInstance() {
		if (instance == null) {
			instance = new ProfileHolder();
		}
		return instance;
	}
	
	public SObject getSourceProfile(String srcProfileId) {
		return srcProfiles.get(srcProfileId);
	}

	public SObject getTargetProfile(String tarProfileId) {
		return tarProfiles.get(tarProfileId);
	}

	public SObject getTargetProfileBySourceId(String srcProfileId) {
		SObject srcProfile = srcProfiles.get(srcProfileId);
		String profileName = ForceUtils.getSObjectFieldValue(srcProfile, "Name");
		return tarProfileMap.get(profileName);
	}

	public String getSourceProfileName(String profileId) {
		SObject profile = srcProfiles.get(profileId);
		if (profile == null) {
			return null;
		}
		return ForceUtils.getSObjectFieldValue(profile, "Name");
	}

	public String getTargetProfileName(String profileId) {
		return ForceUtils.getSObjectFieldValue(tarProfiles.get(profileId), "Name");
	}

	public static void main(String[] args) {
		new ProfileHolder();
	}
	
	private void loadProfiles() {
		String soql = "Select Id, Name from Profile";
		SObject[] records = src.queryMultiple(soql, null);
		for (SObject record : records) {
			srcProfiles.put(record.getId(), record);
		}

		records = target.queryMultiple(soql, null);
		for (SObject record : records) {
			tarProfiles.put(record.getId(), record);
			String profileName = ForceUtils.getSObjectFieldValue(record, "Name");
			tarProfileMap.put(profileName, record);
		}
	}

}

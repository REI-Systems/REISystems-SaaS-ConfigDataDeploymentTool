package com.gg.meta.vo;

import com.force.service.vo.CustomVO;

public class UserVO extends CustomVO {
	private String username;
	private String InternalUniqueID__c;
	private String UserType;
	private ProfileVO Profile = new ProfileVO();
	private String ProfileId;
	
	public ProfileVO getProfile() {
		return Profile;
	}
	public void setProfile(ProfileVO profile) {
		Profile = profile;
	}
	public String getProfileId() {
		return ProfileId;
	}
	public void setProfileId(String profileId) {
		ProfileId = profileId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getInternalUniqueID__c() {
		return InternalUniqueID__c;
	}
	public void setInternalUniqueID__c(String internalUniqueID__c) {
		InternalUniqueID__c = internalUniqueID__c;
	}
	public String getUserType() {
		return UserType;
	}
	public void setUserType(String userType) {
		UserType = userType;
	}
	
	
}

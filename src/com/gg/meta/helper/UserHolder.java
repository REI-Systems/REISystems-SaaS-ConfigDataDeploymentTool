package com.gg.meta.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Holds all user records from both source and target orgs so that we can easily locate the target 
 * org user for each source org user.
 * 
 * @author shahnavazk
 *
 */
public class UserHolder extends Variables {
	private Map<String, String> usernameMap = new HashMap<String, String>();  //key->source username, value->target username
	private Map<String, SObject> targetUserUniqueIdMap = new HashMap<String, SObject>(); //key->unique id
	private Map<String, SObject> srcUserMap = new HashMap<String, SObject>(); //key->source org user id
	private Map<String, SObject> targetUserMap = new HashMap<String, SObject>(); //key->target org user id
	private static UserHolder instance;
	
	private String srcOrgWideEmailAddress;
	private String targetOrgWideEmailAddress;
	
	private UserHolder() {
		loadUsers();
		loadOrgWideEmailAddress();
	}
	
	public static UserHolder getInstance() {
		if (instance == null) {
			instance = new UserHolder();
		}
		return instance;
	}
	
	public static void main(String[] args) {
		System.out.println(new UserHolder().getTargetOrgUsername("shah@ggp.test1"));
	}
		
	public void reset() {
		instance = null;
	}

	public SObject getSourceOrgUser(String sourceUserId) {
		return srcUserMap.get(sourceUserId);
	}

	public SObject getTargetOrgUser(String targetUserId) {
		return targetUserMap.get(targetUserId);
	}
	/**
	 * Retrieves the corresponding target organization user based on the provided source user ID.
	 *
	 * @param sourceUserId The unique identifier of the user in the source organization.
	 * @return
	 */
	public SObject getTargetOrgUserBySourceId(String sourceUserId) {
		SObject srcUser = srcUserMap.get(sourceUserId);
		String uniqueId = ForceUtils.getSObjectFieldValue(srcUser, Variables.INTERNAL_UNIQUEID_FIELDNAME);
		SObject targetUser = targetUserUniqueIdMap.get(uniqueId);
		if (targetUser == null) {
			targetUser = targetUserMap.get(targetOrgAlternateOwnerId);
		}
		return targetUser;
	}
	/**
	 * Retrieves the internal unique identifier of a user in the source organization based on the provided source user ID.
	 *
	 * @param sourceUserId The unique identifier of the user in the source organization.
	 * @return The internal unique identifier (a string value) associated with the user in the source organization.
	 */
	public String getSourceInternalUniqueId(String sourceUserId) {
		SObject user = srcUserMap.get(sourceUserId);
		return ForceUtils.getSObjectFieldValue(user, Variables.INTERNAL_UNIQUEID_FIELDNAME);
	}
	/**
	 * Retrieves the internal unique identifier of a user in the target organization based on the provided target user ID.
	 *
	 * @param targetUserId The unique identifier of the user in the target organization.
	 * @return The internal unique identifier (a string value) associated with the user in the target organization.
	 */
	public String getTargetInternalUniqueId(String targetUserId) {
		SObject user = targetUserMap.get(targetUserId);
		return ForceUtils.getSObjectFieldValue(user, Variables.INTERNAL_UNIQUEID_FIELDNAME);
	}
	/**
	 * Retrieves the corresponding username of a user in the target organization based on the provided source organization username.
	 *
	 * @param sourceOrgUsername The username of the user in the source organization.
	 * @return The corresponding username of the user in the target organization.
	 */
	public String getTargetOrgUsername(String sourceOrgUsername) {
		String targetOrgUsername = usernameMap.get(sourceOrgUsername);
		if (targetOrgUsername == null) {
			targetOrgUsername = targetOrgAlternateOwnerUsername;
		}
		return targetOrgUsername;
	}
	/**
	 * Retrieves the corresponding user ID in the target organization based on the provided user ID from the source organization.
	 *
	 * @param sourceOrgUserId The user ID of the user in the source organization.
	 * @return The corresponding user ID in the target organization.
	 */
	public String getTargetOrgUserId(String sourceOrgUserId) {
		SObject srcUser = srcUserMap.get(sourceOrgUserId);
		String srcUniqueId = ForceUtils.getSObjectFieldValue(srcUser, Variables.INTERNAL_UNIQUEID_FIELDNAME);
		for (SObject tarUser : targetUserMap.values()) {
			String tarUniqueId = ForceUtils.getSObjectFieldValue(tarUser, Variables.INTERNAL_UNIQUEID_FIELDNAME);
			if (srcUniqueId.equals(tarUniqueId)) {
				return tarUser.getId();
			}
		}
				
		return targetOrgAlternateOwnerId;
	}
	/**
	 * Retrieves the Site Guest User in the source organization.
	 *
	 * @return The SObject representing the Site Guest User in the source organization, or null if not found.
	 */
	public SObject getSourceSiteGuestUser() {
		for (SObject user : srcUserMap.values()) {
			String profileId = ForceUtils.getSObjectFieldValue(user, "ProfileId");
			String profileName = ProfileHolder.getInstance().getSourceProfileName(profileId);
			if (Variables.siteGuestProfile.equals(profileName)) {
				return user;
			}
		}
		return null;
	}
	/**
	 * Retrieves the Site Guest User in the target organization.
	 *
	 * @return The SObject representing the Site Guest User in the target organization, or null if not found.
	 */
	public SObject getTargetSiteGuestUser() {
		for (SObject user : targetUserMap.values()) {
			String profileId = ForceUtils.getSObjectFieldValue(user, "ProfileId");
			String profileName = ProfileHolder.getInstance().getTargetProfileName(profileId);
			if (Variables.siteGuestProfile.equals(profileName)) {
				return user;
			}
		}
		return null;
	}
	/**
	 * Retrieves the Community Guest User in the source organization.
	 *
	 * @return The SObject representing the Community Guest User in the source organization, or null if not found.
	 */
	public SObject getSourceCommunityGuestUser() {
		for (SObject user : srcUserMap.values()) {
			String profileId = ForceUtils.getSObjectFieldValue(user, "ProfileId");
			String profileName = ProfileHolder.getInstance().getSourceProfileName(profileId);
			if (Variables.communityGuestProfile.equals(profileName)) {
				return user;
			}
		}
		return null;
	}
	/**
	 * Retrieves the Community Guest User in the target organization.
	 *
	 * @return
	 */
	public SObject getTargetCommunityGuestUser() {
		for (SObject user : targetUserMap.values()) {
			String profileId = ForceUtils.getSObjectFieldValue(user, "ProfileId");
			String profileName = ProfileHolder.getInstance().getTargetProfileName(profileId);
			if (Variables.communityGuestProfile.equals(profileName)) {
				return user;
			}
		}
		return null;
	}

	public String getSourceOrgWideEmailAddress() {
		return srcOrgWideEmailAddress;
	}
	
	public String getTargetOrgWideEmailAddress() {
		return targetOrgWideEmailAddress;
	}
	/**
	 * Checks if the source user with the given ID is active or not.
	 *
	 * @param srcUserId The ID of the source user to check for active status.
	 * @return Boolean value representing the user's active status. True if the user is active, false otherwise.
	 * @throws
	 */
	public Boolean isSourceUserActive(String srcUserId) {
		SObject srcUser = srcUserMap.get(srcUserId);
		if (srcUser == null) {
			log.error("Source user not found using id " + srcUserId);
			throw new RuntimeException("Source user not found using id " + srcUserId);
		}
		String active = ForceUtils.getSObjectFieldValue(srcUser, "IsActive");
		return Boolean.valueOf(active);
	}
	/**
	 * Checks if the target user with the given ID is active or not.
	 *
	 * @param tarUserId The ID of the target user to check for active status.
	 * @return Boolean value representing the user's active status. True if the user is active, false otherwise.
	 * @throws
	 */
	public Boolean isTargetUserActive(String tarUserId) {
		SObject tarUser = targetUserMap.get(tarUserId);
		if (tarUser == null) {
			log.error("Target user not found using id " + tarUserId);
			throw new RuntimeException("Target user not found using id " + tarUserId);
		}
		String active = ForceUtils.getSObjectFieldValue(tarUser, "IsActive");
		return Boolean.valueOf(active);
	}

	/* PRIVATE USERNAME */
	/**
	 * Loads user data from both the source and target Salesforce organizations.
	 */
	private void loadUsers() {
		//Considering public profile and site profile as guest usre's added where clause in the below query.
		String soql = "Select Id, Username, IsActive, " + Variables.INTERNAL_UNIQUEID_FIELDNAME + ", UserType, ProfileId from User Where Profile.name Not In ( 'Site Profile' ,'Public Profile' )";
		SObject[] srcUsers = src.queryMultiple(soql, new Object[]{});
		SObject[] targetUsers = target.queryMultiple(soql, new Object[]{});
		
		Map<String, String> targetUsernameMap = new HashMap<String, String>(); //key->internal id, value->username
		for (SObject tarUser : targetUsers) {
			String uniqueId = ForceUtils.getSObjectFieldValue(tarUser, Variables.INTERNAL_UNIQUEID_FIELDNAME);
			String username = ForceUtils.getSObjectFieldValue(tarUser, "Username");
			targetUsernameMap.put(uniqueId, username);
			targetUserMap.put(tarUser.getId(), tarUser);
			targetUserUniqueIdMap.put(uniqueId, tarUser);
		}
		
		for (SObject srcUser : srcUsers) {
			String uniqueId = ForceUtils.getSObjectFieldValue(srcUser, Variables.INTERNAL_UNIQUEID_FIELDNAME);
			String username = ForceUtils.getSObjectFieldValue(srcUser, "Username");
			String targetUsername = targetUsernameMap.get(uniqueId);
			usernameMap.put(username, targetUsername);
			srcUserMap.put(srcUser.getId(), srcUser);
		}
	}
	/**
	 * Loads the organization-wide email addresses from both the source and target Salesforce organizations.
	 */
	private void loadOrgWideEmailAddress() {
		SObject[] srcOrgWideList = src.queryMultiple("Select Address FROM OrgWideEmailAddress", null);
		if (srcOrgWideList == null) {
			log.error("Orgwide email address not defined in source org");
			System.out.print("Exception Occured.!! Press Any Key to Exit....");
	        new Scanner(System.in).nextLine();
			System.exit(-1);
		}
		//Commenting below condition as per sarat's request -To remove the org-wide email address dependency here
		/*else if (srcOrgWideList.length > 1) {
			log.error("Multiple Orgwide email address found in the source org but only one is expected. Please remove the additional ones.");
			System.out.print("Exception Occured.!! Press Any Key to Exit....");
	        new Scanner(System.in).nextLine();
			System.exit(-1);
		}*/
		srcOrgWideEmailAddress = ForceUtils.getSObjectFieldValue(srcOrgWideList[0], "Address");
		
		
		SObject[] targetOrgWideList = target.queryMultiple("Select Address FROM OrgWideEmailAddress", null);
		if (targetOrgWideList == null) {
			log.error("Orgwide email address not defined in target org");
			System.out.print("Exception Occured.!! Press Any Key to Exit....");
	        new Scanner(System.in).nextLine();
			System.exit(-1);
		}
		//Commenting below condition as per sarat's request -To remove the org-wide email address dependency here
	/*	else if (targetOrgWideList.length > 1) {
			log.error("Multiple Orgwide email address found in the target org but only one is expected. Please remove the additional ones.");
			System.out.print("Exception Occured.!! Press Any Key to Exit....");
	        new Scanner(System.in).nextLine();
			System.exit(-1);
		}*/
		targetOrgWideEmailAddress = ForceUtils.getSObjectFieldValue(targetOrgWideList[0], "Address");
	}
}

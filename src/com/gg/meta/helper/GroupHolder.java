package com.gg.meta.helper;

import java.util.HashMap;
import java.util.Map;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Holds all public groups and queues from both source and target orgs
 * 
 * @author shahnavazk
 *
 */
public class GroupHolder extends Variables {
	private Map<String, SObject> srcGroupMap = new HashMap<String, SObject>(); //key->source org group id
	private Map<String, SObject> tarGroupMap = new HashMap<String, SObject>(); //key->target org group id
	private static GroupHolder instance;
	
	private GroupHolder() {
		loadGroups();
	}
	
	public static GroupHolder getInstance() {
		if (instance == null) {
			instance = new GroupHolder();
		}
		return instance;
	}
	
	public static void main(String[] args) {
		
	}
	/**
	 * Retrieves the target group ID that corresponds to the given source group ID.
	 */
	public String getTargetGroupId(String srcGroupId) {
		SObject srcGroup = srcGroupMap.get(srcGroupId);
		if (srcGroup == null) return null;
		String srcDeveloperName = ForceUtils.getSObjectFieldValue(srcGroup, "DeveloperName");
		String srcType = ForceUtils.getSObjectFieldValue(srcGroup, "Type");		
		
		for (SObject tarGroup : tarGroupMap.values()) {
			String tarDeveloperName = ForceUtils.getSObjectFieldValue(tarGroup, "DeveloperName");
			String tarType = ForceUtils.getSObjectFieldValue(tarGroup, "Type");		
			if (tarDeveloperName.equals(srcDeveloperName) && tarType.equals(srcType)) {
				return tarGroup.getId();
			}
		}
		return null;
	}
	
	public SObject getSourceGroup(String srcGroupId) {
		return srcGroupMap.get(srcGroupId);
	}

	public SObject getTargetGroup(String tarGroupId) {
		return tarGroupMap.get(tarGroupId);
	}

	/* PRIVATE USERNAME */
	/**
	 * Loads groups from the source and target orgs into separate maps for easy access and comparison.
	 */
	private void loadGroups() {
		SObject[] srcGroups = src.queryMultiple("Select Id, DeveloperName, Type from Group", null);
		if (srcGroups != null) {
			for (SObject group : srcGroups) {
				srcGroupMap.put(group.getId(), group);
			}
		}
		
		SObject[] tarGroups = target.queryMultiple("Select Id, DeveloperName, Type from Group", null);
		if (tarGroups != null) {
			for (SObject group : tarGroups) {
				tarGroupMap.put(group.getId(), group);
			}
		}
	}
	

}

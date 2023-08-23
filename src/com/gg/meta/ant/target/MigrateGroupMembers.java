package com.gg.meta.ant.target;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.gg.meta.helper.GroupHolder;
import com.gg.meta.helper.UserHolder;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Migrates public group members from source to target org. 
 * Before running this class make sure the groups are created 
 * in the target org.
 * 
 * @author shahnavazk
 *
 */
public class MigrateGroupMembers extends Variables {

	public static void main(String[] args) {
		log.info("MigrateGroupMembers starts");
		List<SObject> tarMembers = new ArrayList();
		
		SObject[] srcMembers = src.queryMultiple("Select UserOrGroupId, GroupId from GroupMember " +
					"where Group.Type in ('Queue', 'Regular')", null);
		for (SObject srcMember : srcMembers) {
			//Handle GroupId field
			String srcGroupId = ForceUtils.getSObjectFieldValue(srcMember, "GroupId");
			String tarGroupId = GroupHolder.getInstance().getTargetGroupId(srcGroupId);
			if (tarGroupId == null) {
				log.error("Group not found in target org. Source org group id: " + srcGroupId);
				System.out.print("Exception Occured.!! Press Any Key to Exit....");
		        new Scanner(System.in).nextLine();
				System.exit(-1);
			}
			ForceUtils.setSObjectFieldValue(srcMember, "GroupId", tarGroupId);
			
			//Handle UserOrGroupId field
			String tarUserOrGroupId = null;
			String srcUserOrGroupId = ForceUtils.getSObjectFieldValue(srcMember, "UserOrGroupId");
			if (srcUserOrGroupId.startsWith("005")) {
				tarUserOrGroupId = UserHolder.getInstance().getTargetOrgUserId(srcUserOrGroupId);
				if (tarUserOrGroupId == null) {
					log.error("User not found in target org. Source org user id: " + srcUserOrGroupId);
					System.out.print("Exception Occured.!! Press Any Key to Exit....");
			        new Scanner(System.in).nextLine();
					System.exit(-1);
				}
			}
			else {
				tarUserOrGroupId = GroupHolder.getInstance().getTargetGroupId(srcUserOrGroupId);
				if (tarUserOrGroupId == null) {
					log.error("Group not found in target org. Source org group id: " + srcUserOrGroupId);
					System.out.print("Exception Occured.!! Press Any Key to Exit....");
			        new Scanner(System.in).nextLine();
					System.exit(-1);
				}
			}
			ForceUtils.setSObjectFieldValue(srcMember, "UserOrGroupId", tarUserOrGroupId);
			
			tarMembers.add(srcMember);
		}
		
		log.info("Trying to create group members: " + tarMembers.size());
		target.createMultiple(tarMembers);
		log.info("MigrateGroupMembers ends");
	}
}

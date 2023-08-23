package com.gg.config.migration.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.gg.config.compare.UserCache.Data;
import com.gg.config.util.JSONFileReader;
import com.gg.config.vo.MigrationItem;
import com.gg.config.vo.MigrationItemVO;
import com.gg.config.vo.SrcVO;
import com.sforce.soap.partner.sobject.SObject;

/**
 * 
 * @author Shubhangi Shinde,Vijayalaxmi
 *
 */
public class MigrationItemHelper extends Variables {

	/**
	 * Returns an array of MigrationItem objects containing the objects to be migrated based on the provided list of MigrationItemVO and a given WHERE condition.
	 * This method takes a list of MigrationItemVO objects, each representing the parameters for a specific object's migration. 
	 * It iterates through the list, filters out any invalid objects with empty or null object names, and creates a MigrationItem array with the valid objects for migration.
	 * The MigrationItem array will be used to perform the data migration between organizations.
	 * @param objectList
	 * @param whereCondition
	 * @return
	 */
	
	public static MigrationItem[] getListOfObjectsToMigrate(List<MigrationItemVO> objectList, String whereCondition) {
	    if (objectList == null || objectList.isEmpty()) {
	        return null;
	    }

	    MigrationItem[] objectArray = new MigrationItem[objectList.size()];
	    int k = 0;
	    for (MigrationItemVO migrationItemVO : objectList) {
	        String objectNameWithoutNamespace = migrationItemVO.getObjectNameWithoutNamespace().trim();
	        if (!objectNameWithoutNamespace.isEmpty()) {
	            String whereClause = migrationItemVO.getWhereClause();
	            if (!whereCondition.isEmpty()) {
	                whereClause = (whereClause == null || whereClause.trim().isEmpty())
	                        ? whereCondition
	                        : whereClause + " and " + whereCondition;
	            }

	            objectArray[k] = new MigrationItem(
	                    objectNameWithoutNamespace,
	                    migrationItemVO.isPackaged(),
	                    migrationItemVO.isRelationshipRecordsMustFoundInTargetOrg(),
	                    migrationItemVO.getExcludeFields(),
	                    whereClause,
	                    migrationItemVO.getBatchSize(),
	                    migrationItemVO.isDeleteTargetRecords()
	            );

	            k++;
	        }
	    }

	    return Arrays.copyOf(objectArray, k); // Copy the non-null elements to a new array of appropriate size
	}

	/**
	 * Retrieves the user ID of the current user from the Data object using the username provided in the SrcVO object.
	 * This method retrieves the SrcVO object using JSONFileReader.getSrcVo() and obtains the username of the current user from it. It then loops through the user map in the Data object and directly
     *   accesses the key (user ID) and value (SObject representing user data) using entry.getKey() and entry.getValue() within the loop. 
     *  The method compares the username of each user with the current user's username obtained from the SrcVO object. If a match is found, the method returns the user ID.
	 * @return
	 */
	public static String getCurrentUserId() {
	    SrcVO srcVO = JSONFileReader.getSrcVo();
	    String currentUser = srcVO.getUser();

	    Data data = new Data(src);
	    for (Map.Entry<String, SObject> entry : data.getUserMap().entrySet()) {
	        String userId = entry.getKey();
	        SObject userObject = entry.getValue();
	        String userName = ForceUtils.getSObjectFieldValue(userObject, "Username");
	        
	        if (userName.equalsIgnoreCase(currentUser)) {
	            return userId;
	        }
	    }
	    return ""; // Return an empty string if the current user is not found
	}

    /**
     * Generates a WHERE condition for querying records owned by the current user based on their user ID.
     * @return
     */
	public static String getWhereConditionForCurrentUser() {
		String currentUserId = getCurrentUserId();
		String whereCondition = "LastModifiedById=\'" + currentUserId + "\'";
		return whereCondition;
	}
}

package com.gg.common;

import java.util.Arrays;
import java.util.List;

import com.gg.config.migration.helper.MigrationItemHelper;
import com.gg.config.util.JSONFileReader;
import com.gg.config.util.JSONReaderConstants;
import com.gg.config.vo.DateTimeVO;
import com.gg.config.vo.MigrationItem;
import com.gg.config.vo.MigrationItemVO;


/**
 * Contains data for GovGrants product. Please modify the contents of this class
 * before running any of the following classes:
 * 
 * MigrateOrgDelta MigrateSObject SetupClientOrgFirstTime
 * SetupProductOrgFirstTime
 * 
 * @author shahnavazk, Shubhangi Shinde, Vijayalaxmi
 *
 */
public class GovGrantsInitializer extends Variables implements AppDataInitializer {

	@Override
	public void init() {
	
		//changed added  hierarchyCustomSettings in list 
		hierarchyCustomSettings.addAll(Arrays.asList("GlobalConfig__c", "SystemMaintenance__c", "UserPreferences__c"));

		/*----Getting values from JSON - config.json-----------*/
		
		List<MigrationItemVO> specificConfigSobjectsList = JSONFileReader
				.migrationItemsReader(JSONReaderConstants.OBJECT_TYPE_SPECIFIC_CONFIG_SOBJECTS);
		
		List<MigrationItemVO> configSobjectsList = JSONFileReader
				.migrationItemsReader(JSONReaderConstants.OBJECT_TYPE_CONFIG_SOBJECTS);
		List<MigrationItemVO> productMasterSobjectsList = JSONFileReader
				.migrationItemsReader(JSONReaderConstants.OBJECT_TYPE_PRODUCT_MASTER_SOBJECTS);
 
		// changed whereCondation if statement added in single line 
	    String whereCondition = getWhereConditionForMigration(!isMigrationRequiredFromAllUsers);

		
		
	    //changed above method added new optimized method  
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		DateTimeVO dateTimeVO = JSONFileReader.getDateTimeVO();
		if (dateTimeVO != null && !dateTimeVO.getDateLiteralOrDateTime().isEmpty()) {
			whereCondition = addConditionToWhereClause(whereCondition,
					"LastModifiedDate " + dateTimeVO.getOperator() + " " + dateTimeVO.getDateLiteralOrDateTime());
		}
		    
		    
		
		   globalWhereCondition = whereCondition;
		// skip custom settings if it's a sandbox created from GG production.
	//	configSobjects = configSobjectsArray;

		// The following are business master data
	//	productMasterSobjects = productMasterSobjectsArray;
		
	// change in 	
	specificConfigSobjects = getListOfObjectsToMigrate(specificConfigSobjectsList, whereCondition);
	configSobjects = getListOfObjectsToMigrate(configSobjectsList, whereCondition);
	productMasterSobjects = getListOfObjectsToMigrate(productMasterSobjectsList, whereCondition);
	}

	//added new method to get where Condition for Migration 
	private String getWhereConditionForMigration(boolean isMigrationRequiredFromAllUsers) {
	    return isMigrationRequiredFromAllUsers ? "" : MigrationItemHelper.getWhereConditionForCurrentUser();
	}
	// added new method to check  condationTo whereClause 
	private String addConditionToWhereClause(String whereClause, String condition) {
	    return whereClause.isEmpty() ? condition : whereClause + " and " + condition;
	}
	// added new method to get List of Objects To Migrate
	private MigrationItem[] getListOfObjectsToMigrate(List<MigrationItemVO> migrationItemVOList, String whereCondition) {
	    if (migrationItemVOList != null && !migrationItemVOList.isEmpty()) {
	        return MigrationItemHelper.getListOfObjectsToMigrate(migrationItemVOList, whereCondition);
	    }
	    return null;
	}
}

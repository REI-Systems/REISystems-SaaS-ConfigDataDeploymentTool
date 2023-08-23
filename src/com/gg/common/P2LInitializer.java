package com.gg.common;

import com.gg.config.vo.MigrationItem;

/**
 * Contains data for Projects2Link product.
 * Please modify the contents of this class before running any of the following classes:
 * 	
 *	 	MigrateOrgDelta
 *		MigrateSObject
 *		SetupClientOrgFirstTime
 *		SetupProductOrgFirstTime
 * 
 * @author shahnavazk
 *
 */
public class P2LInitializer extends Variables implements AppDataInitializer  { 
	
	@Override
	public void init() {

		/*
		 * Which objects to be migrated? (regular case is fine) - NO NAMESPACE for sobject names. But where clause custom fields SHOULD have
		 * source org namespace.   
		 */
		configSobjects = new MigrationItem[]{
				//The following are business config data
				new MigrationItem("ApprovalDecisionConfig__c", true).excludeField("SetupOwnerId"), 
		};    
		
		//The following are business master data   
		productMasterSobjects = new MigrationItem[]{  
				new MigrationItem("KeyPerformanceIndicator__c"),
		};     
		        
		//The following are business sample data
		productSampleSobjects = new MigrationItem[]{
				new MigrationItem("Program__c").setBatchSize(50).where("RecordType.DeveloperName='ExternalProgram'"), 
		};  
	}
}

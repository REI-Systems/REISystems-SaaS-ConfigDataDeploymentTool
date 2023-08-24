# REISystems-SaaS-ConfigDataDeploymentTool

                                                               Migration Tool
Introduction:
Migration Tool is the java based .exe application. We use this to migrate config data from one salesforce org to another salesforce org having GovGrants package installed. This tool is developed using java version 8(JDK 1.8).The .exe is bundled with JRE hence we do not require any IDE or java installed in our systems to use the tool.


Steps To Use the Tool: 
1.	Files to be included in the location of .exe file.
a.	JRE folder
b.	Exe file
c.	Config.json
d.	Jar file
e.	Xml document
2.	Perform changes in config.json
3.	Double click on the .exe file. (If double click doesn't work then you can do right click and click on run as Administrator.)
4.	After successful migration press any key to exit.


Suggested Changes for Config.json:
The config.json has below key fields - 
1.	src    				
2.	target 				
3.	srcToTarget
4.	dateTimeWhereClauseForMigration			
5.	configSobjects 			
6.	productMasterSobjects 		

SRC: 
It contains credentials for source org.
Description: 
user  : This key refers to a string value in which you can enter username.
password: This key refers to a string value in which you can enter password.
endpoint: This key refers to a string value in which you can enter endpoint.

TARGET:
It contains credentials for target org.
Description: 
user: This key refers to a string value in which you can enter username.
password: This key refers to a string value in which you can enter password.
endpoint: This key refers to a string value in which you can enter endpoint.

SRC-TO-TARGET:
It contains required input for migration.
Description: 
firstTimeMigration: This key refers to a Boolean value. A true or false value can be entered based on the requirement. 
targetOrgPrefix: This key refers to a String value. Used to configure target org instance name. It can be obtained from company 
                 information under setup of the target Salesforce org.
targetOrgAlternateOwnerId: This key refers to a String value. Used to configure target org owner Salesforce Id.
appDataInitializer: This key refers to a String value. This value contains app initializer class name along with package
srcManagedPackageNamespace:	This key refers to a String value. This value contains source org managed package namespace.
targetManagedPackageNamespace: This key refers to a String value. This value contains target org managed package namespace.
unwantedLayouts: This key refers to a list of String values separated by comma. It contains list of layouts that need to be excluded from 		 migration. 
unwantedFields:	This key refers to a list of String values separated by comma. It contains list of fields that need to be excluded from 
                migration.
unwantedClasses: This key refers to a list of String values separated by comma. It contains list of classes that need to be excluded from 
                 migration.
isCustomSettingsRequired: This key refers to a Boolean value. A true or false value can be entered for custom setting required. 
isAccountContactMigrationRequired: This key refers to a Boolean value. A true or false value can be entered for account contact migration 
                                   required. 
isKeyValueStoreMigrationRequired: This key refers to a Boolean value. A true or false value can be entered for key value store migration 
                                  required. 
isMigrationRequiredFromAllUsers: This key refers to a Boolean value. A true or false value can be entered for migration required from all 
                                 users. 

DATE-TIME-WHERE-CLAUSE-FOR-MIGRATION :
It contains information for date-time where condition for data to be migrated. This will be applicable to all the config-SObjects added in the config.json
Description: 
dateLiteralOrDateTime: This key refers to a String value. This value used to set where clause for all objects to filter data by date 
                       literal.
operator: This key refers to a String value. This value used to set operator for above dateLiteralOrDateTime where condition

Note – If we do not want any date time criteria please use the below syntax –  
"dateTimeWhereClauseForMigration":[
		{
			"dateLiteralOrDateTime"     : "",
			"operator"        		       : "<"
		}
	],
 
CONFIG-SOBJECTS AND PRODUCT-MASTER-SOBJECTS:
It contains information for List of config and Product Master SObjects respectively.
Description: 
objectNameWithoutNamespace: This key refers to a String value. This field is used to enter the object name without namespace.
packaged: This key refers to a Boolean value. This identifies if the object mentioned is from packaged source or not.	
relationshipRecordsMustFoundInTargetOrg: This key refers to a Boolean value. If the primary source record has a lookup or master-detail 
                                         relationship field, should that relationship record be found in the target org using unique id 
                                         value?
excludeFields: This key refers to List of excluded Fields for migration.It includes values for fieldAPINameWithoutNamespace and 
               packaged. It specifies list of fields that should NOT be migrated to target. If not specified, all fields are migrated.
whereClause: This key refers to a String value. This value used to set where clause. Any standard soql query where clause format can be 
             followed as the format.
batchSize: This key refers to a Integer value. This value used to set batch size. By default the value is set to 500 if not specified in 
           config.json
deleteTargetRecords: This key refers to a Boolean value. True value deletes non-matching target org records after migration.


  

package com.gg.config.migration;

import com.gg.config.vo.MigrationItem;


public class MigrateFlexTable {
                private String tableName;
                
                public MigrateFlexTable(String tableName) {
                                this.tableName = tableName;
                }
                /**
                 * Performs data migration for multiple MigrationItem objects, each representing a different SObject type.
                 * Each MigrationItem corresponds to migrating data for a specific SObject type with the specified conditions.
                 */
				public void migrate() {
                                //
                                MigrationItem item1 = new MigrationItem("GNT__DataTableConfig__c", true).setBatchSize(200)
								.where("Name IN('" + tableName + "') ").deleteTargetRecords(false);
								MigrateSObject mig1 = new MigrateSObject(item1);
								mig1.migrate();	
                               MigrationItem item2 = new MigrationItem("GNT__DataTableAction__c", true).setBatchSize(200)
								.where("GNT__DataTableConfig__r.Name IN('" + tableName + "') ").deleteTargetRecords(false);
								MigrateSObject mig2 = new MigrateSObject(item2);
								mig2.migrate();

                               MigrationItem item3 = new MigrationItem("GNT__DataTableDetailConfig__c", true).setBatchSize(200)
								.where("GNT__FlexTableConfig__r.Name IN('" + tableName + "') ").deleteTargetRecords(false);
								MigrateSObject mig3 = new MigrateSObject(item3);
								mig3.migrate();

                                MigrationItem item4 = new MigrationItem("GNT__FlexTableFilterListViewConfig__c", true).setBatchSize(200)
								.where("GNT__FlexTableConfig__r.Name IN('" + tableName + "') ").deleteTargetRecords(false);
								MigrateSObject mig4 = new MigrateSObject(item4);
								mig4.migrate();
								MigrationItem item5 = new MigrationItem("GNT__FlexGridConfig__c", true).setBatchSize(200)
								.where("GNT__ParentFlexTable__r.Name IN('" + tableName + "') ").deleteTargetRecords(false);
								MigrateSObject mig5 = new MigrateSObject(item5);
								mig5.migrate();	

                }

		public static void main(String[] args) {
			
		//new Scanner(System.in).nextLine();
			//firstTimeMigration = false;
            String[] tableNames = new String[]{"ManageAllUsers","ManagePublicGroup"};
            for (String tableName : tableNames) {
                      MigrateFlexTable mig = new MigrateFlexTable(tableName);
                      mig.migrate();
                                }
                }
}
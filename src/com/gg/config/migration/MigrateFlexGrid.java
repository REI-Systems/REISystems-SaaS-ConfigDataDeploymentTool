package com.gg.config.migration;

import com.gg.config.vo.MigrationItem;


public class MigrateFlexGrid {
                private String pageLayout;
                
                public MigrateFlexGrid(String pageLayout) {
                                this.pageLayout = pageLayout;
                }
                /**
                 * Performs data migration for specific MigrationItem objects, each representing a different SObject type.
                 */
				public void migrate() {
                                MigrationItem item1 = new MigrationItem("GNT__FlexGridConfig__c", true).setBatchSize(200)
								.where("GNT__ParentFlexTable__r.Name IN('" + pageLayout + "') ").deleteTargetRecords(false);
								MigrateSObject mig1 = new MigrateSObject(item1);
								mig1.migrate();	
                              /* MigrationItem item2 = new MigrationItem("GNT__TabConfig__c", true).setBatchSize(200)
								.where("GNT__DataTableConfig__r.Name IN('" + pageLayout + "') ").deleteTargetRecords(false);
								MigrateSObject mig2 = new MigrateSObject(item2);
								mig2.migrate();

                               MigrationItem item3 = new MigrationItem("GNT__PageBlockConfig__c", true).setBatchSize(200)
								.where("GNT__TabLayoutConfig__r.GNT__PageLayoutConfig__r.Name IN('" + pageLayout + "') ").deleteTargetRecords(false);
								MigrateSObject mig3 = new MigrateSObject(item3);
								mig3.migrate();

                                MigrationItem item4 = new MigrationItem("GNT__PageBlockDetailConfig__c", true).setBatchSize(200)
								.where("GNT__PageBlockConfig__r.GNT__TabLayoutConfig__r.GNT__PageLayoutConfig__r.Name IN('" + pageLayout + "') ").deleteTargetRecords(false);
								MigrateSObject mig4 = new MigrateSObject(item4);
								mig4.migrate();
								MigrationItem item5 = new MigrationItem("GNT__PageLayoutActionConfig__c", true).setBatchSize(200)
								.where("GNT__PageLayoutConfig__r.Name IN ('" + pageLayout + "')").deleteTargetRecords(false);
									MigrateSObject mig5 = new MigrateSObject(item5);*/

                }

		public static void main(String[] args) {
            String[] pageLayouts = new String[]{"Attachment", "KeyContacts123"};
            for (String pageLayout : pageLayouts) {
                        MigrateFlexGrid mig = new MigrateFlexGrid(pageLayout);
                        mig.migrate();
				}	
        }
}
package com.gg.config.migration;


import java.util.HashSet;
import java.util.Set;
import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.gg.config.vo.MigrationItem;
import com.sforce.soap.partner.sobject.SObject;



public class MigratePageTemplate extends Variables {
	
	private String pageTemplateName;
	
	public MigratePageTemplate(String pageTemplateName) {
        this.pageTemplateName = pageTemplateName;
								
    }
	/**
	 * Performs data migration for various related objects based on the specified layout.
	 * The method migrates data for different related objects associated with the given layout.
	 */
	public void migrate(String layout) {
		System.out.println("Layout-->"+layout);
		MigrationItem item1 = new MigrationItem("GNT__PageLayoutConfig__c", true).setBatchSize(200)
								.where("Name IN('" + layout + "') ").deleteTargetRecords(false);
		MigrateSObject mig1 = new MigrateSObject(item1);
			mig1.migrate();	
        MigrationItem item2 = new MigrationItem("GNT__TabConfig__c", true).setBatchSize(200)
								.where("GNT__PageLayoutConfig__r.Name IN('" + layout + "') ").deleteTargetRecords(false);
		MigrateSObject mig2 = new MigrateSObject(item2);
			mig2.migrate();

        MigrationItem item3 = new MigrationItem("GNT__PageBlockConfig__c", true).setBatchSize(200)
								.where("GNT__TabLayoutConfig__r.GNT__PageLayoutConfig__r.Name IN('" + layout + "') ").deleteTargetRecords(false);
		MigrateSObject mig3 = new MigrateSObject(item3);
			mig3.migrate();

        MigrationItem item4 = new MigrationItem("GNT__PageBlockDetailConfig__c", true).setBatchSize(200)
								.where("GNT__PageBlockConfig__r.GNT__TabLayoutConfig__r.GNT__PageLayoutConfig__r.Name IN('" + layout + "') ").deleteTargetRecords(false);
		MigrateSObject mig4 = new MigrateSObject(item4);
			mig4.migrate();
		MigrationItem item5 = new MigrationItem("GNT__PageLayoutActionConfig__c", true).setBatchSize(200)
								.where("GNT__PageLayoutConfig__r.Name IN ('" + layout + "')").deleteTargetRecords(false);
		MigrateSObject mig5 = new MigrateSObject(item5);
			mig5.migrate();
		MigrationItem item6 = new MigrationItem("GNT__MessageConfig__c", true).setBatchSize(200)
								.where("GNT__PageBlockConfig__r.GNT__TabLayoutConfig__r.GNT__PageLayoutConfig__r.Name IN('" + layout + "') ").deleteTargetRecords(false);
		MigrateSObject mig6 = new MigrateSObject(item6);
			mig6.migrate();
			MigrationItem item7 = new MigrationItem("GNT__PageAttachmentConfig__c", true).setBatchSize(200)
								.where("GNT__PageBlockConfig__r.GNT__TabLayoutConfig__r.GNT__PageLayoutConfig__r.Name IN('" + layout + "') ").deleteTargetRecords(false);
		MigrateSObject mig7 = new MigrateSObject(item7);
			mig7.migrate();
		
    }
	/**
	 * Migrates layout data by fetching layout IDs associated with the given page template name,
	 * and then performing data migration for each layout.
	 */
	private void migrateLayout(){
		SObject query1 = src.querySingle("select GNT__ViewLayoutConfig__c,GNT__PrintLayoutConfig__c,GNT__EditLayoutConfig__c from GNT__PageTemplate__c where Name =\'"+pageTemplateName+"\' limit 1",null);
		String layoutId1 = ForceUtils.getSObjectFieldValue(query1, "GNT__ViewLayoutConfig__c",true);
		String layoutId2 = ForceUtils.getSObjectFieldValue(query1, "GNT__PrintLayoutConfig__c",true);
		String layoutId3 = ForceUtils.getSObjectFieldValue(query1, "GNT__EditLayoutConfig__c",true);
		Set<String> layoutIds = new HashSet<String>();
		layoutIds.add(layoutId1);
		layoutIds.add(layoutId2);
		layoutIds.add(layoutId3);
		for(String layoutId: layoutIds){
			SObject LayoutNames = src.querySingle("select Name from GNT__PageLayoutConfig__c where Id =\'"+layoutId+"\' limit 1",null);
			String LayoutName = ForceUtils.getSObjectFieldValue(LayoutNames, "Name");
			migrate(LayoutName);
		}
		MigrationItem item = new MigrationItem("GNT__PageTemplate__c", true).setBatchSize(200)
								.where("Name IN('" + pageTemplateName + "') ").deleteTargetRecords(false);
		MigrateSObject mig = new MigrateSObject(item);
			mig.migrate();
	}
	
	public static void main(String[] args) {
        String[] pageTemplateNames = new String[]{"Step2: SME Review"};
        for (String pageTemplateName : pageTemplateNames) {
                MigratePageTemplate mig = new MigratePageTemplate(pageTemplateName);
                mig.migrateLayout();
		}	
    }
}
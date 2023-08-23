package com.gg.meta.util;

import java.io.File;

import com.lib.util.FileUtils;

public class DuplicateProfile {

	static String folder = "D:\\Projects\\Salesforce\\REI\\Projects\\GovGrants\\REISystems-SaaS-GovGrants-MigrationTool\\Meta data\\Ant\\temp - record type deletion\\retrieveUnpackaged\\profiles\\";
	static String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
"<Profile xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n" +
    "<recordTypeVisibilities>\n" +
        "<default>false</default>\n" +
        "<recordType>ggsTempPack1__FlexGridConfig__c.test</recordType>\n" +
        "<visible>false</visible>\n" +
    "</recordTypeVisibilities>\n" +
    "<recordTypeVisibilities>\n" +
    "<default>true</default>\n" +
    "<recordType>ggsTempPack1__FlexGridConfig__c.ggsTempPack1__N2G</recordType>\n" +
    "<visible>true</visible>\n" +
    "</recordTypeVisibilities>\n" +
"</Profile>";
		
	public static void main(String[] args) {
		for (File f : new File(folder).listFiles()) {
			FileUtils.createFile(f, body);
		}
	}

}

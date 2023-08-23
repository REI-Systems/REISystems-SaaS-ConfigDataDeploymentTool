package com.gg.meta.ant.target;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.gg.meta.util.GGUtils;
import com.gg.meta.util.PackageXMLGenerator;
import com.lib.util.FileUtils;
import com.sforce.soap.partner.sobject.SObject;

public class GenerateReportXML extends Variables {
	static Logger log = Logger.getRootLogger();
	static String packageXMLFilename;
	private PackageXMLGenerator gen;
	Map<String, String> folderMap = new HashMap<String, String>(); //key->folder name, value->folder name
	
	public static void main(String[] args) {
		log.info("GenerateReportXML starts");
		packageXMLFilename = args[0];
		new GenerateReportXML().generateXML();
		log.info("GenerateReportXML ends");
	}
	/**
	 * Generates a package XML file containing metadata information for Salesforce reports and report folders.
	 */
	public void generateXML() {
		gen = new PackageXMLGenerator();
		loadFolders();
		loadReports();
		gen.addElement("ReportType", "*");
		
		File f = new File(GGUtils.getSRCFolderURL() + "\\" + packageXMLFilename);
		FileUtils.createFile(f, gen.generateXML());
	}
	/**
	 * Loads information about Salesforce report folders and adds relevant reports to the package XML generator.
	 */
	private void loadFolders() {
		SObject[] folders = src.queryMultiple("Select Id, DeveloperName from Folder where Type='Report' and DeveloperName NOT IN ('Field_Trip_Reports','External_Review_Reports') AND DeveloperName != null", null);
		for (SObject folder : folders) {
			String folderName = ForceUtils.getSObjectFieldValue(folder, "DeveloperName");
			if (folderName == null) continue;
			if (folderName.toLowerCase().contains("conga")) continue;
			folderMap.put(folder.getId(), folderName);
			gen.addElement("Report", folderName);
		}
	}
	/**
	 * Loads information about Salesforce reports and adds relevant reports to the package XML generator.
	 */
	private void loadReports() {
		SObject[] reports = src.queryMultiple("Select Id, DeveloperName, OwnerId, Owner.Name from Report where Owner.Name NOT IN ('Field Trip Reports') and DeveloperName != 'Closed_Lost_by_Reason' AND DeveloperName != null", null);
		for (SObject report : reports) {
			String reportName = ForceUtils.getSObjectFieldValue(report, "DeveloperName");
			if (reportName.toLowerCase().contains("conga")) continue;
			String folderId = ForceUtils.getSObjectFieldValue(report, "OwnerId");
			String folderName = folderMap.get(folderId);
			if (folderName == null) continue;
			gen.addElement("Report", folderName + "/" + reportName);
		}		
	}

}

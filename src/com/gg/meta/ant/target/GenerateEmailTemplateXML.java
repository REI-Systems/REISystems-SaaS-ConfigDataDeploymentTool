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

public class GenerateEmailTemplateXML extends Variables {
	static Logger log = Logger.getRootLogger();
	static String packageXMLFilename;
	private PackageXMLGenerator gen;
	Map<String, String> folderMap = new HashMap<String, String>(); //key->folder id, value->folder name
	
	public static void main(String[] args) {
		log.info("GenerateEmailTemplateXML starts");
		packageXMLFilename = args[0];
		new GenerateEmailTemplateXML().generateXML();
		log.info("GenerateEmailTemplateXML ends");
	}
	/**
	 * Generates a Package.xml file based on the loaded folders and document names.
	 */
	public void generateXML() {
		gen = new PackageXMLGenerator();
		loadFolders();
		loadEmailTemplates();
		
		File f = new File(GGUtils.getSRCFolderURL() + "\\" + packageXMLFilename);
		FileUtils.createFile(f, gen.generateXML());
	}
	/**
	 * Loads document folders from Salesforce and populates the folderMap.
	 */
	private void loadFolders() {
		SObject[] folders = src.queryMultiple("Select Id, DeveloperName from Folder where Type='Email' and DeveloperName != 'Field_Trip_Postcards'", null);
		for (SObject folder : folders) {
			String folderName = ForceUtils.getSObjectFieldValue(folder, "DeveloperName");
			if (folderName.toLowerCase().contains("conga")) continue;
			folderMap.put(folder.getId(), folderName);
			gen.addElement("EmailTemplate", folderName);
		}
	}
	/**
	 * Loads email template names from Salesforce and adds them to the Package.xml using the folderMap.
	 * The method queries the 'EmailTemplate' object in Salesforce, filtering by folders of type 'Email'.
	 */
	private void loadEmailTemplates() {
		SObject[] emTemps = src.queryMultiple("SELECT DeveloperName, FolderId FROM EmailTemplate where Folder.Type='Email' and DeveloperName != 'Processing_Completed'", null);
		for (SObject em : emTemps) {
			String emName = ForceUtils.getSObjectFieldValue(em, "DeveloperName");
			if (emName.toLowerCase().contains("conga")) continue;
			String folderId = ForceUtils.getSObjectFieldValue(em, "FolderId");
			String folderName = folderMap.get(folderId);
			gen.addElement("EmailTemplate", folderName + "/" + emName);
		}
		
	}

}

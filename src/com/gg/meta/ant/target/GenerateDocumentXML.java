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

public class GenerateDocumentXML extends Variables {
	static Logger log = Logger.getRootLogger();
	static String packageXMLFilename;
	private PackageXMLGenerator gen;
	Map<String, String> folderMap = new HashMap<String, String>(); //key->folder id, value->folder name
	
	public static void main(String[] args) {
		log.info("GenerateDocumentXML starts");
		packageXMLFilename = args[0];
		new GenerateDocumentXML().generateXML();
		log.info("GenerateDocumentXML ends");
	}
	/**
	 * Generates a Package.xml file based on the loaded folders and document names.
	 */
	public void generateXML() {
		gen = new PackageXMLGenerator();
		loadFolders();
		loadDocumentNames();
		
		File f = new File(GGUtils.getSRCFolderURL() + "\\" + packageXMLFilename);
		FileUtils.createFile(f, gen.generateXML());
	}
	/**
	 * Loads document folders from Salesforce and populates the folderMap.
	 */
	private void loadFolders() {
		SObject[] folders = src.queryMultiple("Select Id, DeveloperName from Folder where Type='Document'", null);
		for (SObject folder : folders) {
			String folderName = ForceUtils.getSObjectFieldValue(folder, "DeveloperName");
			if (folderName.toLowerCase().contains("conga")) continue;
			folderMap.put(folder.getId(), folderName);
			gen.addElement("Document", folderName);
		}
	}
	/**
	 * Loads document names from Salesforce and adds them to the Package.xml using the folderMap.
	 */
	private void loadDocumentNames() {
		SObject[] documents = src.queryMultiple("SELECT DeveloperName, FolderId FROM Document where Folder.Type='Document'", null);
		for (SObject doc : documents) {
			String devName = ForceUtils.getSObjectFieldValue(doc, "DeveloperName");
			if (devName.toLowerCase().contains("conga")) continue;
			String folderId = ForceUtils.getSObjectFieldValue(doc, "FolderId");
			String folderName = folderMap.get(folderId);
			if (folderName == null) continue;
			gen.addElement("Document", folderName + "/" + devName);
		}
		
	}

}

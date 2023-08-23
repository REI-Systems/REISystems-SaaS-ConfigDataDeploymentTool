package com.gg.meta.ant.target;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.gg.common.Variables;
import com.gg.config.util.AppUtils;
import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;
import com.lib.util.XMLUtils;

public class RemoveTempFiles {
	static Logger log = Logger.getRootLogger();
	static List<String> allowedFolders = new ArrayList<String>();
	
	public static void main(String[] args) {		
		log.info("RemoveTempFiles starts");
		String folders = args[0];
		for (String folder : folders.split(",")) {
			allowedFolders.add(folder.trim());
		}
		
		RemoveTempFiles p = new RemoveTempFiles();
		p.deleteTempFile("objects", ".object");
		p.deleteTempFile("classes", ".cls");
		p.deleteTempFile("triggers", ".trigger");
		p.deleteTempFile("pages", ".page");
		p.deleteTempFile("components", ".component");
		p.deleteTempFile("approvalProcesses", ".approvalProcess");
		p.deleteTempFile("layouts", ".layout");
		p.deleteTempFile("reports", ".report");
		p.deleteTempFile("reportTypes", ".reportType");
		p.deleteTempFile("sharingRules", ".sharingRules");
		p.deleteTempFile("staticresources", ".resource");
		p.deleteTempFile("tabs", ".tab");
		p.deleteTempFile("workflows", ".workflow");
		p.deleteTempFile("roles", ".role");
		p.deleteTempFields();
		p.deleteUnwantedLayouts("layouts", ".layout");
		log.info("RemoveTempFiles ends");
	}
	/**
	 * Deletes temporary fields from Salesforce object metadata files.
	 */
	private void deleteTempFields() {
		if (allowedFolders.contains("objects") == false) return;
		
		for (File f : new File(GGUtils.getSRCFolderURL() + "objects\\").listFiles()) {
			boolean changed = false;
			String data = FileUtils.readFile(f, true);
			List<String> fields = XMLUtils.fetchTags(data, "fields", true);	
			for (String field : fields) {
				String fullName = XMLUtils.fetchTagValue(field, "fullName");
				//System.out.println("Full name - " + fullName);
				if(fullName != null) {
					if (AppUtils.isTempFile(fullName)) {
						data = data.replace(field, "");
						changed = true;
					}
				}				
			}
			
			if (changed) {
				FileUtils.createFile(f, data);
			}
		}
	}
	/**
	 * Deletes temporary files and their corresponding meta files from a specified folder with the given file extension, if allowed.
	 */
	private void deleteTempFile(String folderName, String fileExtension) {
		if (allowedFolders.contains(folderName) == false) return;
		
		for (File f : new File(GGUtils.getSRCFolderURL() + folderName).listFiles()) {
			if (f.getName().endsWith(fileExtension)) {
				if (AppUtils.isTempFile(f.getName())) {
					f.delete();
					
					File metaFile = new File(GGUtils.getSRCFolderURL() + folderName + "\\" + f.getName() + "-meta.xml");
					if (metaFile.exists()) {
						metaFile.delete();
					}
				}
			}
		}
	}
	/**
	 * Deletes unwanted layout files and their corresponding meta files from a specified folder with the given file extension.
	 */
	private void deleteUnwantedLayouts(String folderName, String fileExtension) {
		//if (allowedFolders.contains(folderName) == false) return;
		
		Set<String> unwantedLayoutSet = new HashSet<String>();
		for(String layout : Variables.unwantedLayoutsList){
			unwantedLayoutSet.add(layout.toLowerCase());
		}
		//log.info("Layout cleanup: >>>>");
		//String path = "C:\\Users\\nakul.kadam\\OneDrive - REI Systems Inc\\REI Projects\\REISystems-SaaS-GovGrants-MigrationTool-master(1)\\REISystems-SaaS-GovGrants-MigrationTool-master\\Main\\src\\";
		for (File f : new File(GGUtils.getSRCFolderURL() + folderName).listFiles()) {
			//log.info("Layout cleanup: >>>>" + f.getName());
			if (f.getName().endsWith(fileExtension)) {
				if (!f.getName().contains("__c-")) {
					log.info("Layout Removed: >>>>"+f.getName());
					f.delete();
					
					File metaFile = new File(GGUtils.getSRCFolderURL() + folderName + "\\" + f.getName() + "-meta.xml");
					if (metaFile.exists()) {
						metaFile.delete();
					}
				}
			}
		}
	}

}

package com.gg.meta.ant.target;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.gg.meta.util.GGUtils;
import com.gg.meta.util.PackageXMLGenerator;
import com.lib.util.FileUtils;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.sobject.SObject;

public class GenerateProfilePackageXML extends Variables {
	private String profiles;
	private String layoutNamesFile;
	private String packageXMLFilename = "package.xml";
	static Logger log = Logger.getRootLogger();
	PackageXMLGenerator xml = new PackageXMLGenerator();
	private String managedPackageNSNoUnderscore = managedPackageNamespaceSrc.replace("__", "");

	public static void main(String[] args) {
		log.info("GenerateProfilesXML starts");
		new GenerateProfilePackageXML(args[0], args[1]).generateXML();
		log.info("GenerateProfilesXML ends");
	}

	public GenerateProfilePackageXML(String profiles, String layoutNamesFile) {
		this.profiles = profiles;
		this.layoutNamesFile = layoutNamesFile;
	}
	/**
	 * Generates the Package.xml file based on the defined elements for customizations.
	 */
	public void generateXML() {
		prepareApexClass();
		prepareApexPage();
		xml.addElement("CustomTab", "*");
		xml.addElement("CustomApplication", "GovGrants");
		prepareLayoutTag();

		List<String> objectNames = getObjectNames();
		for (String objectName : objectNames) {
			xml.addElement("CustomObject", objectName);
		}

		String[] profileArr = profiles.split(",");
		for (String profile : profileArr) {
			profile = profile.trim();
			xml.addElement("Profile", profile);
		}
		
		String data = xml.generateXML();
		FileUtils.createFile(new File(GGUtils.getSRCFolderURL() + packageXMLFilename), data);
	}
	/**
	 * Prepares ApexClass elements for the Package.xml based on the managed package namespace and wildcard.
	 */
	private void prepareApexClass() {
		SObject[] records = src.queryMultiple("SELECT Name FROM ApexClass where NamespacePrefix=?", new Object[]{managedPackageNSNoUnderscore});
		for (SObject record : records) {
			String className = managedPackageNamespaceSrc + ForceUtils.getSObjectFieldValue(record, "Name");
			xml.addElement("ApexClass", className);
		}
		xml.addElement("ApexClass", "*");
	}
	
	private void prepareApexPage() {
		SObject[] records = src.queryMultiple("SELECT Name FROM ApexPage where NamespacePrefix=?", new Object[]{managedPackageNSNoUnderscore});
		for (SObject record : records) {
			String pageName = managedPackageNamespaceSrc + ForceUtils.getSObjectFieldValue(record, "Name");
			xml.addElement("ApexPage", pageName);
		}
		xml.addElement("ApexPage", "*");
	}
	/**
	 * Reads layout names from a file, extracts valid layout names, and adds them as "Layout" elements to the XML representation.
	 * The layout names are extracted from the file whose path is specified in the 'layoutNamesFile' variable. Each line in the file
	 * is expected to have a format like "FileName: layout_name", where 'layout_name' is the name of the layout.
	 */
	private void prepareLayoutTag() {
		String data = FileUtils.readFile(new File(GGUtils.getRootFolderURL() + layoutNamesFile), true);
		String[] lines = data.split("\n");
		for (String line : lines) {
			if (line.startsWith("FileName:")) {
				String layoutName = findLayoutName(line);
				if (layoutName != null) {
					xml.addElement("Layout", layoutName);
				}
			}
		}
		
		xml.addElement("Layout", "*");
	}
	/**
	 * Extracts and returns the layout name from a given input line in the format "FileName: layout_name".
	 * @param line The input line in the format "FileName: layout_name"
	 * @return The extracted layout name with the managed package namespace, or null if the layout is not valid or lacks a namespace.
	 */
	private String findLayoutName(String line) {
		String layoutName = line.replace("FileName: layouts/", "");
		layoutName = layoutName.replace(".layout", "");
		if (layoutName.startsWith(managedPackageNamespaceSrc)) { //means managed package layout
			int i = layoutName.indexOf("-");
			layoutName = layoutName.substring(0, i+1) + managedPackageNamespaceSrc + layoutName.substring(i+1);
			return layoutName;
		}
		else {
			return null;
		}
	}
	/**
	 * Retrieves a list of Salesforce object names that are relevant for the current application context.
	 */
	private List<String> getObjectNames() {
		List<String> objList = new ArrayList<String>();
		DescribeGlobalSObjectResult[] sobjs = src.describeGlobal().getSobjects();
		for (DescribeGlobalSObjectResult descGlobalResult : sobjs) {
			if (!descGlobalResult.getName().startsWith("APXTConga4__") && !descGlobalResult.getName().startsWith("tmp") 
					&& !descGlobalResult.getName().startsWith("temp") && !descGlobalResult.getName().contains("__tmp")
					&& !descGlobalResult.getName().contains("__temp")
					&& descGlobalResult.getName().endsWith("__c") && !descGlobalResult.getName().endsWith("__Share")
					&& !descGlobalResult.getName().endsWith("__History")) {
				objList.add(descGlobalResult.getName());
			}
		}
		
		objList.add("Account");
		objList.add("Contact");
		objList.add("Case");
		objList.add("Activity");
		objList.add("Task");
		objList.add("User");
		objList.add("ContentVersion");
		return objList;
	}

}

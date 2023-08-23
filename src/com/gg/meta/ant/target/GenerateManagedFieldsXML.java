package com.gg.meta.ant.target;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import com.gg.common.Variables;
import com.gg.config.util.AppUtils;
import com.gg.meta.util.GGUtils;
import com.gg.meta.util.PackageXMLGenerator;
import com.lib.util.FileUtils;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
/**
 * Generates XML file for the following fields:
 * 	1. Unmanaged fields which are found in managed objects.
 * 	2. Managed picklist fields
 * 
 * @author shahnavazk
 *
 */
public class GenerateManagedFieldsXML extends Variables {
	PackageXMLGenerator gen = new PackageXMLGenerator();
	static List<String> profileNames = new ArrayList<String>();
	static String packageXMLFilename;
	static Logger log = Logger.getRootLogger();
	
	public static void main(String[] args) {
		log.info("GenerateManagedFieldsXML starts");
		profileNames = StringUtils.getList(args[0], ",", true, false);
		packageXMLFilename = args[1];
		log.info("Trying to generate " + packageXMLFilename + " for unmanaged fields from managed objects");
		new GenerateManagedFieldsXML().process();
		log.info("GenerateManagedFieldsXML ends"); 
	}
	/**
	 * Processes the Salesforce objects and generates the Package.xml file.
	 */
	private void process() {
		DescribeGlobalSObjectResult[] sobjs = src.describeGlobal().getSobjects();
		for(DescribeGlobalSObjectResult descGlobalResult : sobjs) {
			if (isObsoleteObject(descGlobalResult.getName())) continue;
			
			if(descGlobalResult.getName().startsWith(managedPackageNamespaceSrc) && descGlobalResult.getName().endsWith("__c")
					&& AppUtils.isNonTempFile(descGlobalResult.getName())) {
				handleSObject(descGlobalResult.getName());
			}
		}
		
		gen.addElement("Profile", profileNames);
		String xml = gen.generateXML();
		FileUtils.createFile(new File(GGUtils.getSRCFolderURL() + packageXMLFilename), xml);
	}
	/**
	 * Checks if the given Salesforce object name is considered obsolete based on the configured list of obsolete objects.
	 */
	private boolean isObsoleteObject(String sobjectName) {
		int i = sobjectName.indexOf("__");
		if (i != -1) {
			sobjectName = sobjectName.substring(i+2);
		}
		return obsoleteObjects.contains(sobjectName);
	}
	/**
	 * Handles the processing of a specific Salesforce SObject (sobjectName) and generates CustomField elements for non-managed package custom fields.
	 */
	private void handleSObject(String sobjectName) {
		DescribeSObjectResult or = src.describeSObject(sobjectName);
		for (Field f : or.getFields()) {
			if (AppUtils.isTempFile(f.getName())) continue;

			if ((f.getName().startsWith(managedPackageNamespaceSrc) == false && f.isCustom())
					) { // || 
//				(f.getPicklistValues() != null && f.getPicklistValues().length > 0)
				String nodeValue = sobjectName + "." + f.getName();
				gen.addElement("CustomField", nodeValue);
			}
		}
	}

}

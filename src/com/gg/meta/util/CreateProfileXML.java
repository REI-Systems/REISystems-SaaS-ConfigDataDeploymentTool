package com.gg.meta.util;

import java.io.File;

import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.lib.util.FileUtils;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.sobject.SObject;

public class CreateProfileXML {
	
	static String folder = "C:\\Projects\\Salesforce\\REI\\Projects\\GovGrants\\San Diego\\Ant\\profile extraction2\\retrieveUnpackaged\\";
	static ForceDelegateRaw gate = ForceDelegateRaw.login("target");
	static PackageXMLGenerator xml = new PackageXMLGenerator(37.0);
	
	public static void main(String[] args) {
		SObject[] classes = gate.queryMultiple("Select Name, NamespacePrefix from ApexClass", null);
		for (SObject myclass : classes) {
			String ns = ForceUtils.getSObjectFieldValue(myclass, "NamespacePrefix");
			String className = ForceUtils.getSObjectFieldValue(myclass, "Name");
			if (StringUtils.isNonEmpty(ns)) {
				className = ns + "__" + className;
			}
			xml.addElement("ApexClass", className);
		}

		SObject[] pages = gate.queryMultiple("Select Name, NamespacePrefix from ApexPage", null);
		for (SObject page : pages) {
			String ns = ForceUtils.getSObjectFieldValue(page, "NamespacePrefix");
			String pageName = ForceUtils.getSObjectFieldValue(page, "Name");
			if (StringUtils.isNonEmpty(ns)) {
				pageName = ns + "__" + pageName;
			}
			xml.addElement("ApexPage", pageName);
		}

		DescribeGlobalResult gr = gate.describeGlobal();
		for (DescribeGlobalSObjectResult gsr : gr.getSobjects()) {
			String sobjectName = gsr.getName();
			if (sobjectName.endsWith("Share") || sobjectName.endsWith("History") || sobjectName.endsWith("__Feed") ||
					sobjectName.equals("SocialPost")) {
				continue;
			}
			xml.addElement("CustomObject", sobjectName);
		}

		xml.addElement("Layout", "*");

		xml.addElement("Profile", "SPA");
		xml.addElement("Profile", "SPI");
		
		FileUtils.createFile(new File(folder+"package.xml"), xml.generateXML());
	}

}

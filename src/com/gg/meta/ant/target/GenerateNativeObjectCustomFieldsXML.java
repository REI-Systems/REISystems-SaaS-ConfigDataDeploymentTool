package com.gg.meta.ant.target;

import java.io.File;

import com.gg.common.Variables;
import com.gg.meta.util.GGUtils;
import com.gg.meta.util.PackageXMLGenerator;
import com.lib.util.FileUtils;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;

/**
 * This class should be executed only when a fresh org is setup for the first time.
 * Typically fresh orgs comes with some unwanted custom fields on native objects (Account, Contact, Task etc).
 * We need to delete those fields in target org as we don't need them. This class helps with preparing the 
 * destructiveChanges XML file for such fields.
 * 
 * @author shahnavazk
 *
 */
public class GenerateNativeObjectCustomFieldsXML extends Variables {
	static PackageXMLGenerator gen = new PackageXMLGenerator(33.0);
	
	public static void main(String[] args) {
		log.info("GenerateNativeObjectCustomFieldsXML starts");
		handleSObject("Account");
		handleSObject("Task");
		handleSObject("Case");
		handleSObject("Contact");
		handleSObject("User");
		
		String data = gen.generateXML();
		if (data != null) {			
			FileUtils.createFile(new File(GGUtils.getSRCFolderURL() + "destructiveChanges.xml"), data);
		}
		log.info("GenerateNativeObjectCustomFieldsXML ends");
	}
	
	private static void handleSObject(String sobjectName) {
		DescribeSObjectResult or = target.describeSObject(sobjectName);
		for (Field f : or.getFields()) {
			if (f.isCustom() && !f.getName().startsWith(Variables.managedPackageNamespaceSrc)) {
				gen.addElement("CustomField", sobjectName + "." + f.getName());
			}
		}
	}
}

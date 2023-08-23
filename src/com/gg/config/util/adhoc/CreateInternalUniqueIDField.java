package com.gg.config.util.adhoc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.force.service.ForceDelegate;
import com.gg.common.Variables;
import com.lib.util.FileUtils;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
/**
 * This class creates package.xml and contents for objects folder to create the "Internal Unique ID" field on all custom objects
 * excluding custom settings. After this class runs, manually run Ant using the generated package.xml so that the fields will be
 * created on Salesforce.
 * 
 * @author shahnavazk
 *
 */
public class CreateInternalUniqueIDField {
	//Modify the contents for the below variables ONLY
	static final String ORG_NAME = "ggint";
	static final String NAMESPACE = "ggsInt__c";
	static final String folder = "C:\\_Projects\\Salesforce\\REI\\REI Internal\\Projects\\GovGrants\\Ant\\ggint deploy\\retrieveUnpackaged\\";  //This is where the deploy package is created
	
	//Do NOT modify anyting below
	static ForceDelegate gate = ForceDelegate.login(ORG_NAME);
	static List<String> objectNames = new ArrayList<String>();
	
	public static void main(String[] args) {
		File objectsFolder = new File(folder+"objects");
		if (objectsFolder.exists() == false) {
			objectsFolder.mkdir();
		}
		
		DescribeGlobalResult gs = gate.describeGlobal();
		for (DescribeGlobalSObjectResult res : gs.getSobjects()) {
			String objectName = res.getName();
			if (objectName.startsWith(NAMESPACE) && objectName.endsWith("__c") && !res.isCustomSetting()) {
				System.out.println(res.getName());
				createObjectFile(res.getName());
			}
		}
		createPackageXML();
	}
	/**
	 * Creates a Salesforce Custom Object metadata file with the specified objectName.
	 * The method processes the objectName, adds it to the objectNames list, and removes the NAMESPACE prefix.
	 * It then generates the XML content for the CustomObject metadata file, including the Internal Unique ID field.
	 */
	private static void createObjectFile(String objectName) {
		objectName = objectName.replace(NAMESPACE, "");
		objectNames.add(objectName);

		String data = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<CustomObject xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n" + 
				"    <enableEnhancedLookup>true</enableEnhancedLookup>\n" + 
				"    <fields>\n" + 
				"        <fullName>" + Variables.INTERNAL_UNIQUEID_FIELDNAME + "</fullName>\n" + 
				"        <caseSensitive>true</caseSensitive>\n" + 
				"        <description>Uniquely identifies each record for data migration purpose only. It is auto populated by trigger in before insert context.</description>\n" + 
				"        <externalId>true</externalId>\n" + 
				"        <label>Internal Unique ID</label>\n" + 
				"        <length>32</length>\n" + 
				"        <required>false</required>\n" + 
				"        <trackHistory>false</trackHistory>\n" + 
				"        <trackTrending>false</trackTrending>\n" + 
				"        <type>Text</type>\n" + 
				"        <unique>true</unique>\n" + 
				"    </fields>\n" + 
				"</CustomObject>";
		
		String filename = folder+"objects\\"+objectName+".object";
		FileUtils.createFile(new File(filename), data);
	}
	/**
	 * Creates the package.xml file required for Salesforce metadata deployment.
	 * The method generates the content for the package.xml file based on the list of objectNames
	 */
	private static void createPackageXML() {
		StringBuffer b = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n" + 
"    <types>\n");
		
		for (String objectName : objectNames) {
			b.append("        <members>");
			b.append(objectName);
			b.append("." + Variables.INTERNAL_UNIQUEID_FIELDNAME + "</members>\n");
		}
		
		b.append("        <name>CustomField</name>\n" + 
				"    </types>\n" + 
				"    <version>31.0</version>\n" + 
				"</Package>");
		FileUtils.createFile(new File(folder+"package.xml"), b.toString());
	}

}

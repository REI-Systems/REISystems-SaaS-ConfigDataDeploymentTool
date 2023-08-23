package com.gg.meta.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.lib.util.CSVUtils;
import com.lib.util.FileUtils;

public class MetaDataScannerUpdate {

	static String folder = "C:\\Users\\shahnavazk\\Downloads\\ConfigDataScan\\";
	static List<String> fields = new ArrayList<String>();
	
	public static void main(String[] args) {
		for (File f : new File(folder).listFiles()) {
			if (f.isFile()) {
				processSObject(f);
			}
		}
		
		PackageXMLGenerator xml = new PackageXMLGenerator();
		for (String field : fields) {
			xml.addElement("CustomField", field);
		}
		String body = xml.generateXML();
		FileUtils.createFile(new File("C:\\Projects\\Salesforce\\REI\\Projects\\GovGrants\\San Diego\\Ant\\custom fields deploy\\retrieveUnpackaged\\package.xml"), body);
	}

	/**
	 * Processes a CSV file containing field information for a specific Salesforce
	 * SObject.
	 * 
	 * @param f The CSV file to be processed.
	 */
	private static void processSObject(File f) {
		System.out.println("Processing file " + f.getName());
		List<String[]> rows = CSVUtils.readFile(f, true);
		String sobjectName = f.getName().replace(".csv", "");
		
		for (String[] cols : rows) {
			String id = cols[0];
			String fieldName = cols[1];
			String element = sobjectName + "." + fieldName;
			if (fields.contains(element) == false) {
				fields.add(element);
			}
		}
	}
}

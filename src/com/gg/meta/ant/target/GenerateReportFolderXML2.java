package com.gg.meta.ant.target;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import com.gg.meta.util.GGUtils;

public class GenerateReportFolderXML2 {
	static Logger log = Logger.getRootLogger();

	public static void main(String[] args) throws IOException {
		log.info("Creating reports folder starts");
		new GenerateReportFolderXML2().generateXML();
		log.info("Creating reports folder ends");
	}

	public void generateXML() throws IOException {
		String path = GGUtils.getSRCFolderURL() + "reports\\";
		generateFolderXML(path);
	}
	/**
	 * Generates a list of unique folder names within the specified URL directory and creates package XML files for each folder.
	 */
	private static List<String> generateFolderXML(String url) throws IOException {
		List<String> fileSet = new ArrayList<String>();
		File folder = new File(url);
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				if (!fileSet.contains(fileEntry.getName())) {
					fileSet.add(fileEntry.getName());
					createReportFolderPackageXML(fileEntry.getName());
				}
			}
		}

		return fileSet;
	}
	/**
	 * Creates a package XML file for a report folder with the specified folder name.
	 */
	private static void createReportFolderPackageXML(String foldername) throws IOException {
		log.info("Creating folder meta xml for " + foldername);
		String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <ReportFolder xmlns=\"http://soap.sforce.com/2006/04/metadata\"> <accessType>PublicInternal</accessType> <name>"
				+ foldername + "</name><publicFolderAccess>ReadWrite</publicFolderAccess></ReportFolder>";
		String path = GGUtils.getSRCFolderURL() + "reports\\";
		File file = new File(path + foldername + "-meta.xml");
		file.createNewFile();
		FileWriter writer = new FileWriter(path + foldername + "-meta.xml", false);
		writer.write(xmlStr);
		writer.close();
	}

}

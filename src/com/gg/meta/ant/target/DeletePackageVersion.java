package com.gg.meta.ant.target;
import java.io.File;

import org.apache.log4j.Logger;

import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;
import com.lib.util.XMLUtils;

/**
 * It removes 'packageVersions' tag which allows us to deploy code when the managed package version is 
 * different between source and target orgs
 * 
 * @author shahnavazk
 *
 */
public class DeletePackageVersion {
	static Logger log = Logger.getRootLogger();
	
	public static void main(String[] str) {
		log.info("Package version deletion starts");
		String baseSourceFolderURL = GGUtils.getSRCFolderURL();
		removeVersion(baseSourceFolderURL + "pages\\");
		removeVersion(baseSourceFolderURL + "components\\");
		removeVersion(baseSourceFolderURL + "classes\\");
		removeVersion(baseSourceFolderURL + "triggers\\");
		log.info("Package version deletion ends");
	}
	/**
	 * Deletes the specified records from the target system using their respective record IDs.
	 */
	public static void removeVersion(String path) {
		File folder = new File(path);
		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".xml")) {
				String data = FileUtils.readFile(fileEntry);
				data = XMLUtils.removeTag(data, "packageVersions");
				FileUtils.createFile(fileEntry, data);
			}
		}

	}
}

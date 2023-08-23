package com.gg.meta.ant.target;

import java.io.File;
import org.apache.log4j.Logger;
import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;
import com.lib.util.XMLUtils;

public class CleanAllFields {

	static Logger log = Logger.getRootLogger();
	
	public static void main(String[] args) {
		log.info("Custom object cleanup starts" + GGUtils.getSRCFolderURL());
		String path = "C:\\Users\\nakul.kadam\\OneDrive - REI Systems Inc\\REI Projects\\REISystems-SaaS-GovGrants-MigrationTool-master(1)\\REISystems-SaaS-GovGrants-MigrationTool-master\\Main\\src\\";
		// GGUtils.getSRCFolderURL() 
		for (File f : new File(path + "objects").listFiles()) {			
			if (f.getName().endsWith(".object")) {
				process(f);
			}
		}
		log.info("Custom object cleanup ends");
	}

	/**
	 * Processes the given XML file to remove specific tags and their values.
	 */
	private static void process(File f) {
		String data = FileUtils.readFile(f, true);
		boolean changed = false;

		for (String tagValue : XMLUtils.fetchTags(data, "fields", true)) {
			data = data.replace(tagValue, "");
			changed = true;
		}
		for (String tagValue : XMLUtils.fetchTags(data, "webLinks", true)) {
			data = data.replace(tagValue, "");
			changed = true;
		}
		if (changed) {
			FileUtils.createFile(f, data);
		}
	}

}

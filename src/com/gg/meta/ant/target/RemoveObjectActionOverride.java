package com.gg.meta.ant.target;

import java.io.File;
import java.util.List;

import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;
import com.lib.util.StringUtils;
import com.lib.util.XMLUtils;
/**
 * Since we deploy objects first without any classes and pages, we need to temporarily remove
 * the action overrides. Objects will be redeployed again at a later time when the classes
 * and pages are deployed.
 * 
 * @author shahnavazk
 *
 */
public class RemoveObjectActionOverride {

	public static void main(String[] args) {
		String root = GGUtils.getSRCFolderURL();
		for (File f : new File(root + "objects").listFiles()) {
			process(f);
		}
	}
	/**
	 * Processes a given XML file to modify specific actionOverride tags.
	 */
	private static void process(File f) {
		Boolean changed = false;
		String data = FileUtils.readFile(f, true);
		List<String> overrideTags = XMLUtils.fetchTags(data, "actionOverrides", true);
		for (String overrideTag : overrideTags) {
			String content = XMLUtils.fetchTagValue(overrideTag, "content");
			if (StringUtils.isNonEmpty(content) && overrideTag.contains("Visualforce")) {
				String overrideTagUpdated = XMLUtils.removeTag(overrideTag, "content");
				overrideTagUpdated = XMLUtils.removeTag(overrideTagUpdated, "skipRecordTypeSelect");
				overrideTagUpdated = overrideTagUpdated.replace("Visualforce", "Default");
				data = data.replace(overrideTag, overrideTagUpdated);
				changed = true;
			}
		}
		
		if (changed) {
			FileUtils.createFile(f, data);
		}
	}
}

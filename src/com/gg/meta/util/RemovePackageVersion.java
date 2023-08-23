package com.gg.meta.util;

import java.io.File;

import com.lib.util.FileUtils;
import com.lib.util.XMLUtils;

public class RemovePackageVersion {

	static String folder = "D:\\Projects\\Salesforce\\REI\\Projects\\GovGrants\\Product\\Ant\\user registration changes\\retrieveUnpackaged\\classes\\";
	public static void main(String[] args) {
		for (File f : new File(folder).listFiles()) {
			if (f.getName().endsWith(".xml")) {
				String data = FileUtils.readFile(f, true);
				data = XMLUtils.removeTag(data, "packageVersions");
				FileUtils.createFile(f, data);
			}
		}

	}

}

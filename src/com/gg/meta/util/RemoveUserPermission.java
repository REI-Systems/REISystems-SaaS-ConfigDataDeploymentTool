package com.gg.meta.util;

import java.io.File;

import com.lib.util.FileUtils;
import com.lib.util.XMLUtils;

public class RemoveUserPermission {

	static String folder = "D:\\Projects\\Salesforce\\REI\\Projects\\GovGrants\\Product\\Ant\\user registration changes\\retrieveUnpackaged\\profiles\\";
	public static void main(String[] args) {
		for (File f : new File(folder).listFiles()) {
			String body = FileUtils.readFile(f, true);
			body = XMLUtils.removeTag(body, "userPermissions");
			FileUtils.createFile(f, body);
		}

	}

}

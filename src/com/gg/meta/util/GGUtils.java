package com.gg.meta.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GGUtils {

	public static String getRootFolderURL() {
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		return s + "\\";
	}

	public static String getSRCFolderURL() {
		return getRootFolderURL() + "src\\";
	}

}

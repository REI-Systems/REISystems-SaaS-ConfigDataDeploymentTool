package com.gg.meta.ant.target;

import java.io.File;

import org.apache.log4j.Logger;

import com.gg.meta.util.GGUtils;

/**
 * Removes listViews tags from custom objects if those list views belong to queues.
 * If we don't remove, deploying was failing with duplicate list view error.
 * 
 * @author shahnavazk
 *
 */
public class DeleteCaseObject {
	static Logger log = Logger.getRootLogger();
	
	public static void main(String[] args) {
		log.info("DeleteCaseObject starts");
		File f = new File(GGUtils.getSRCFolderURL() + "objects\\Case.object");
		if (f.exists()) {
			f.delete();
		}
		log.info("DeleteCaseObject ends");
	}
}

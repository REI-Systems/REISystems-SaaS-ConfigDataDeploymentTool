package com.gg.meta.ant.target;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import com.gg.meta.helper.UserHolder;
import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;
import com.lib.util.RegexUtils;

/**
 * Replace approval process username to refer to target org
 * 
 * @author shahnavazk
 *
 */
public class ReplaceApprovalUsername {
	private static String srcFolder = GGUtils.getSRCFolderURL();
	private static Logger log = Logger.getRootLogger();	
	
	public static void main(String[] args) {
		log.info("ReplaceApprovalUsername starts");
		replaceUserName();
		log.info("ReplaceApprovalUsername ends");
	}
	/**
	 * Replaces source org usernames with their corresponding target org usernames in approval process files.
	 */
	public static void replaceUserName() {
		for (File f : new  File(srcFolder + "approvalProcesses\\").listFiles()) {
			log.info("ReplaceApprovalUsername starts");
			log.info("ReplaceApprovalUsername - processing approval process file: " + f.getName());
			String data = FileUtils.readFile(f, true);
			List<String> userNameList = RegexUtils.match(">[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z0-9]{1,})<", data);
			boolean changed = false;
			for (String userName : userNameList) {
				userName = userName.replace(">", "");
				userName = userName.replace("<", "");
				data = data.replace(userName, UserHolder.getInstance().getTargetOrgUsername(userName));
				changed = true;
			}
			
			if (changed) {
				FileUtils.createFile(f, data);
			}
		}
	}

}

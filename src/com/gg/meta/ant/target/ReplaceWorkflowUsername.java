package com.gg.meta.ant.target;


import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import com.gg.meta.helper.UserHolder;
import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;
import com.lib.util.RegexUtils;
import com.lib.util.XMLUtils;


public class ReplaceWorkflowUsername {
	private static Logger log = Logger.getRootLogger();	
	private static String srcFolder = GGUtils.getSRCFolderURL();
	
	public static void main(String[] args) {
		log.info("ReplaceWorkflowUsername starts");
		replaceUserName();
		log.info("ReplaceWorkflowUsername ends");
	}
	/**
	 * Replaces source org usernames with their corresponding target org usernames in workflow files.
	 */
	public static void replaceUserName() {
		for (File f : new File(srcFolder + "workflows\\").listFiles()) {
			log.info("ReplaceWorkflowUsername - processing workflow file: " + f.getName());
			String data = FileUtils.readFile(f, true);
			if (data.length() < 120) {
				f.delete();
			}
			else {
				boolean changed = false;
				List<String> alertTags = XMLUtils.fetchTags(data, "alerts", true);
				for (String alertTag : alertTags) {						
					String alertTagNew = handleAlertTag(alertTag);
					if (alertTag.equals(alertTagNew) == false) {
						data = data.replace(alertTag, alertTagNew);
						changed = true;
					}
				}

				List<String> taskTags = XMLUtils.fetchTags(data, "tasks", true);
				for (String taskTag : taskTags) {						
					String taskTagNew = handleTaskTag(taskTag);
					if (taskTag.equals(taskTagNew) == false) {
						data = data.replace(taskTag, taskTagNew);
						changed = true;
					}
				}

				if (changed) {
					FileUtils.createFile(f, data);
				}
			}			
		}
	}
	
	private static String handleAlertTag(String alertTag) {
		alertTag = handleOrgwideEmailAddress(alertTag);
		alertTag = handleRecipients(alertTag);
		
		return alertTag;
	}
	/**
	 * Handles the modification of the "assignedTo" tag in a given task tag to replace the source org username with its corresponding target org username.
	 */
	private static String handleTaskTag(String taskTag) {
		List<String> userLines = RegexUtils.match("<assignedTo>.*</assignedTo>", taskTag);
		for (String userLine : userLines) {
			int i = userLine.indexOf(">");
			int j = userLine.indexOf("</", i+2);			
			String sourceOrgUsername = userLine.substring(i+1, j);
			if (sourceOrgUsername.contains("@")) {
				String userLineNew = userLine.replace(sourceOrgUsername, UserHolder.getInstance().getTargetOrgUsername(sourceOrgUsername));
				taskTag = taskTag.replace(userLine, userLineNew);
			}
		}
		
		return taskTag;
	}
	
	//handle org wide email address
	private static String handleOrgwideEmailAddress(String alertTag) {
		String senderAddress = XMLUtils.fetchTagValue(alertTag, "senderAddress");
		String senderType = XMLUtils.fetchTagValue(alertTag, "senderType");
		
		if (senderAddress != null && senderType != null && senderType.equals("OrgWideEmailAddress")) {			
			alertTag = replaceOrgwideSenderEmailAddress(alertTag);
		}
		return alertTag;
	}
	/**
	 * Replaces the source org-wide sender email address with the corresponding target org-wide sender email address in an alert tag.
	 */
	private static String replaceOrgwideSenderEmailAddress(String alertTag) {
		List<String> userLines = RegexUtils.match("<senderAddress>.*</senderAddress>", alertTag);
		for (String userLine : userLines) {
			int i = userLine.indexOf(">");
			int j = userLine.indexOf("</", i+2);			
			String sourceOrgwideEmail = userLine.substring(i+1, j);
			String userLineNew = userLine.replace(sourceOrgwideEmail, UserHolder.getInstance().getTargetOrgWideEmailAddress());
			alertTag = alertTag.replace(userLine, userLineNew);
		}
		
		return alertTag;
	}
	
	//handle recipients email address
	private static String handleRecipients(String alertTag) {
		List<String> userLines = RegexUtils.match("<recipient>.*</recipient>", alertTag);
		for (String userLine : userLines) {
			int i = userLine.indexOf(">");
			int j = userLine.indexOf("</", i+2);			
			String sourceOrgUsername = userLine.substring(i+1, j);
			if (sourceOrgUsername.contains("@")) {
				String userLineNew = userLine.replace(sourceOrgUsername, UserHolder.getInstance().getTargetOrgUsername(sourceOrgUsername));
				alertTag = alertTag.replace(userLine, userLineNew);
			}
		}
		return alertTag;
	}

}

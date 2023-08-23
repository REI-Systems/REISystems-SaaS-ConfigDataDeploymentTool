package com.gg.meta.ant.target;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.gg.config.util.AppUtils;
import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.sobject.SObject;

public class GenerateRolesXML extends Variables {
	static List<String> profileNames = new ArrayList<String>();
	static String packageXMLFilename;
	
	private String roleStr;
	private String profileStr;
	
	public static void main(String[] args) {
		log.info("Roles XML generation starts");
		profileNames = StringUtils.getList(args[0], ",", true, false);
		packageXMLFilename = args[1];
		new GenerateRolesXML().process();
		log.info("Roles XML generation ends");
	}
	
	private void process() {
		handleRoles();
		handleProfiles();
		createPackageXML();
	}
	/**
	 * Handles Salesforce User Roles and generates a string containing members of non-temporary roles for the package XML.
	 */
	private void handleRoles() {
		String soql = "SELECT DeveloperName FROM UserRole where PortalAccountId=null";
		SObject[] rolesArr = src.queryMultiple(soql, null);
		List<String> roles = new ArrayList<String>();
		for (SObject role : rolesArr) {
			String roleName = ForceUtils.getSObjectFieldValue(role, "DeveloperName");
			if (AppUtils.isNonTempFile(roleName)) {
				roles.add("\t\t<members>" + roleName + "</members>");
			}
		}
		roleStr = StringUtils.getConcatenatedString(roles, "\n");
	}
	/**
	 * Handles Salesforce profiles and generates a string containing members of non-temporary profiles for the package XML.
	 */
	private void handleProfiles() {
		List<String> profiles = new ArrayList<String>();
		for (String profileName : profileNames) {
			if (AppUtils.isNonTempFile(profileName)) {
				profiles.add("\t\t<members>" + profileName + "</members>");
			}
		}
		profileStr = StringUtils.getConcatenatedString(profiles, "\n");
	}
	/**
	 * Creates the package.xml file by merging the generated content for roles and profiles.
	 */
	private void createPackageXML() {
		String path = GGUtils.getSRCFolderURL();
		String packageXMLBody = FileUtils.readFile(new File(path+packageXMLFilename), true);
		packageXMLBody = packageXMLBody.replace("{roles}", roleStr);
		packageXMLBody = packageXMLBody.replace("{profiles}", profileStr);
		
		FileUtils.createFile(new File(path+"package.xml"), packageXMLBody);		
	}
}

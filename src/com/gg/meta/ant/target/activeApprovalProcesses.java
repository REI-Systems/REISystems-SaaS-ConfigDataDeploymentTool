package com.gg.meta.ant.target;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.sobject.SObject;

public class activeApprovalProcesses extends Variables {
	static String packageXMLFilename;
	
	private String roleStr;
	public static void main(String[] args) {
		log.info("Approval process XML generation starts");
		packageXMLFilename = args[0];
		new activeApprovalProcesses().process();
		log.info("Approval process XML generation ends");
	}
	
	private void process() {
		getActiveApprovalProcesses();
		createPackageXML();
	}
	/**
	 * Retrieves the active approval processes from the source Salesforce org.
	 */
	private void getActiveApprovalProcesses() {
             String soql = "SELECT TableEnumOrId, DeveloperName FROM ProcessDefinition Where State =\'Active\'";
             SObject[] approvalProcessesArr = src.queryMultiple(soql, null);
             List<String> approvalProcesses = new ArrayList<String>();
             for (SObject approvalDefinition : approvalProcessesArr) {
                    String processName = ForceUtils.getSObjectFieldValue(approvalDefinition, "DeveloperName");
                    String sobjectName = ForceUtils.getSObjectFieldValue(approvalDefinition, "TableEnumOrId");
                    approvalProcesses.add("\t\t<members>" + sobjectName + "." + processName + "</members>");
             }
             roleStr = StringUtils.getConcatenatedString(approvalProcesses, "\n");
       }
	/**
	 * Creates the package.xml file with the required metadata components.
	 */
	private void createPackageXML() {
		String path = GGUtils.getSRCFolderURL();
		String packageXMLBody = FileUtils.readFile(new File(path+packageXMLFilename), true);
		packageXMLBody = packageXMLBody.replace("{approvalProcesses}", roleStr);
		
		FileUtils.createFile(new File(path+"package.xml"), packageXMLBody);		
	}
}
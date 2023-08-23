package com.gg.meta.helper;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.gg.common.Variables;
import com.gg.config.util.AppUtils;
import com.lib.util.FileUtils;
import com.lib.util.XMLUtils;
import org.apache.log4j.Logger;

public class ProfileCleanupHelper {
	private String data;
	private String profileName;
	public static Logger log = Logger.getRootLogger();
	
	public ProfileCleanupHelper(String data, String profileName) {
		this.data = data;
		this.profileName = profileName;
	}
	
	public String process() {
		removeTmpApexClass();
		removeTmpCustomField();
		removeTmpLayout();
		removeTmpCustomObject();
		removeTmpPage();
		removeTmpRecordType();
		removeTmpTab();
		removeUserPermission();
		massageUserLicense();
		removeUnwantedField();
		removeStandardLayout();
		removeUnwantedClasses();
		removeFieldTrip(); // remove these as they are not useful on other environments
		deleteLightningReport(); // http://salesforce.stackexchange.com/questions/156801/the-user-license-doesnt-allow-the-permission-subscribetolightningreports
		removeManagedRecTypeTmp(); // added to remove tmp record type created on dev5
		return data;		
	}
	
	private void removeTmpApexClass() {
		for (String tagValue : XMLUtils.fetchTags(data, "classAccesses", true)) {
			if (tagValue.contains("<apexClass>tmp")) {
				data = data.replace(tagValue, "");
			}
		}
		//System.out.println(" *** Profile Name 1" + profileName);
	}
	
	private void removeFieldTrip() {
		for(String tagValue : XMLUtils.fetchTags(data, "fieldPermissions", true)){
			String fieldName = XMLUtils.fetchTagValue(tagValue, "field",false);
			if(fieldName.contains("Field_Trip")){
				data = data.replace(tagValue, "");
			}			
		}
		for(String tagValue : XMLUtils.fetchTags(data, "objectPermissions", true)){
			String fieldName = XMLUtils.fetchTagValue(tagValue, "object",false);
			if(fieldName.contains("Field_Trip")){
				data = data.replace(tagValue, "");
			}			
		}
	}
	
	private void removeTmpCustomField() {
		for (String tagValue : XMLUtils.fetchTags(data, "fieldPermissions", true)) {
			String fieldName = XMLUtils.fetchTagValue(tagValue, "field", false);
			if (AppUtils.isTempFile(fieldName)) {
				data = data.replace(tagValue, "");
			}
		}
		//System.out.println(" *** Profile Name 2" + profileName);
	}
	
	private void removeTmpLayout() {
		for (String tagValue : XMLUtils.fetchTags(data, "layoutAssignments", true)) {
			String fieldName = XMLUtils.fetchTagValue(tagValue, "layout", false);
			if (AppUtils.isTempFile(fieldName)) {
				data = data.replace(tagValue, "");
			}
		}
		//System.out.println(" *** Profile Name 3" + profileName);
	}

	private void removeTmpCustomObject() {
		for (String tagValue : XMLUtils.fetchTags(data, "objectPermissions", true)) {
			String fieldName = XMLUtils.fetchTagValue(tagValue, "object", false);
			if (AppUtils.isTempFile(fieldName)) {
				data = data.replace(tagValue, "");
			}
		}
		//System.out.println(" *** Profile Name 4" + profileName);
	}

	private void removeTmpPage() {
		for (String tagValue : XMLUtils.fetchTags(data, "pageAccesses", true)) {
			
			String fieldName = XMLUtils.fetchTagValue(tagValue, "apexPage", false);
			if (AppUtils.isTempFile(fieldName)) {
				data = data.replace(tagValue, "");
			}
		}
		//System.out.println(" *** Profile Name 5" + profileName);
	}

	private void removeTmpRecordType() {
		for (String tagValue : XMLUtils.fetchTags(data, "recordTypeVisibilities", true)) {
			String fieldName = XMLUtils.fetchTagValue(tagValue, "recordType", false);
			if (AppUtils.isTempFile(fieldName)) {
				data = data.replace(tagValue, "");
			}
		}
		//System.out.println(" *** Profile Name 6" + profileName);
	}

	private void removeTmpTab() {
		for (String tagValue : XMLUtils.fetchTags(data, "tabVisibilities", true)) {
			String fieldName = XMLUtils.fetchTagValue(tagValue, "tab", false);
			if (AppUtils.isTempFile(fieldName)) {
				data = data.replace(tagValue, "");
			}
		}
		//System.out.println(" *** Profile Name 7" + profileName);
	}

	private void removeUserPermission() {
		data = XMLUtils.removeTag(data, "userPermissions");
		//System.out.println(" *** Profile Name 8" + profileName);
	}

	private void massageUserLicense() {
		if (profileName.contains("Public Profile") || profileName.contains("Site Profile")) {
			data = XMLUtils.removeTag(data, "userLicense");
			//System.out.println(" *** Profile Name 9" + profileName);
		}
		else {
			if (Variables.licenseMap != null) {
				String license = XMLUtils.fetchTagValue(data, "userLicense");
				String newLicense = Variables.licenseMap.get(license);
				if (newLicense != null) {
					data = data.replace("<userLicense>" + license + "</userLicense>",
								"<userLicense>" + newLicense + "</userLicense>");
				}
				//System.out.println(" *** Profile Name 10" + profileName);
			}
		}
	}
	
	private void removeUnwantedField() {
		Set<String> unwantedFieldsSet = new HashSet<String>();
		for(String fldName : Variables.unwantedFieldsList){
			unwantedFieldsSet.add(fldName.toLowerCase());
		}
		log.info("unwantedFieldsSet >>>>"+unwantedFieldsSet);
		for (String tagValue : XMLUtils.fetchTags(data, "fieldPermissions", true)) {
			String fieldName = XMLUtils.fetchTagValue(tagValue, "field", false);
			if (unwantedFieldsSet.contains(fieldName.toLowerCase())) {
				data = data.replace(tagValue, "");
			}
			for(String fld : unwantedFieldsSet){
				if(fieldName.toLowerCase().contains(fld)){
					data = data.replace(tagValue, "");
				}
			}
		}
	}
	
	private void removeStandardLayout() {
		Set<String> unwantedLayoutSet = new HashSet<String>();
		for(String layout : Variables.unwantedLayoutsList){
			unwantedLayoutSet.add(layout.toLowerCase());
		}
		log.info("unwantedLayoutSet >>>>"+unwantedLayoutSet);
		for (String tagValue : XMLUtils.fetchTags(data, "layoutAssignments", true)) {
			String layoutName = XMLUtils.fetchTagValue(tagValue, "layout", false);
			//if (unwantedLayoutSet.contains(layoutName.toLowerCase())) {
			//	data = data.replace(tagValue, "");
			//}
			//log.info("layoutName: "+layoutName);
			if(layoutName.toLowerCase().contains("__mdt-")){
				data = data.replace(tagValue, "");
			}
			for(String fld : unwantedLayoutSet){
				if(layoutName.toLowerCase().startsWith(fld.toLowerCase())){
					//log.info("**** Replaced ****");
					data = data.replace(tagValue, "");
				}
			}
		}
	}
	private void removeManagedRecTypeTmp() {
		Set<String> unwantedLayoutSet = new HashSet<String>();
		String name1 = "GNT__PageBlockDetailConfig__c.tmp";
		unwantedLayoutSet.add(name1.toLowerCase());		
		/*for(String layout : Variables.unwantedLayoutsList){ // variables.unwantedlayoutsList is null here -- check why
			unwantedLayoutSet.add(layout.toLowerCase());
		}*/
		for (String tagValue : XMLUtils.fetchTags(data, "layoutAssignments", true)) {
			if (tagValue.contains("<recordType>")){			
				String recordType = XMLUtils.fetchTagValue(tagValue, "recordType", false); // throws null if the attribute is not found
				if (unwantedLayoutSet.contains(recordType.toLowerCase())){
					data = data.replace(tagValue, "");
				}	
			}
		}		
		
	}
	
	private void deleteLightningReport() {
		if (!profileName.contains("Admin")) {
			//System.out.println("Here is my profile " + profileName);
			String str = "<userPermissions>" +
							"<enabled>false</enabled>" + 
							"<name>SubscribeToLightningReports</name>" +
							"</userPermissions>" +
						"</Profile>" ;
			data = data.replace("</Profile>",str);
		}
	}
	
	private void removeUnwantedClasses() {
		Set<String> unwantedClsSet = new HashSet<String>();
		for(String cls : Variables.unwantedClassesList){
			unwantedClsSet.add(cls.toLowerCase());
		}
		for (String tagValue : XMLUtils.fetchTags(data, "classAccesses", true)) {
			String fieldName = XMLUtils.fetchTagValue(tagValue, "apexClass", false);
			if (unwantedClsSet.contains(fieldName.toLowerCase())) {
				data = data.replace(tagValue, "");
			}
		}
	}

	public static void main(String[] args) {
		String folder = "C:\\Users\\nakul.kadam\\OneDrive - REI Systems Inc\\REI Projects\\REISystems-SaaS-GovGrants-MigrationTool-master(1)\\REISystems-SaaS-GovGrants-MigrationTool-master\\Main\\src\\profiles\\";
		String fileName = "Admin - Copy.profile";
		String data = FileUtils.readFile(new File(folder + fileName), true);
		ProfileCleanupHelper p = new ProfileCleanupHelper(data, fileName);
		p.process();
	}
}

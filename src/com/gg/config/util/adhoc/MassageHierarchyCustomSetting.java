package com.gg.config.util.adhoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lib.util.CSVUtils;

/**
 * This is a temporary class helps with massaging the reference ids for hierarchy custom setting. Use this class until the main code base
 * is upgraded to support hierarchy custom setting.
 * 
 * Before running this class, download the following files from source and target orgs
 * 			Profile
 * 			Organization
 * 			User Preference Config
 * 			Global Config
 * @author shahnavazk
 *
 */
public class MassageHierarchyCustomSetting {
	static String folder = "C:\\Users\\shahnavazk\\Downloads\\Salesforce Files\\GG\\";
	static Map<String, String> profileMapSrc = new HashMap<String, String>();  //key->id, value->name
	static Map<String, String> profileMapTarget = new HashMap<String, String>();  //key->name, value->id
	static String orgIdSrc;
	static String orgIdTarget;
	static List<String[]> result = new ArrayList<String[]>();
	
	public static void main(String[] args) {
		List<String[]> rows = CSVUtils.readFile(folder+"profile test.csv", true);
		for (String[] cols : rows) {
			profileMapSrc.put(cols[0], cols[1]);
		}

		rows = CSVUtils.readFile(folder+"profile demo.csv", true);
		for (String[] cols : rows) {
			profileMapTarget.put(cols[1], cols[0]);
		}

		rows = CSVUtils.readFile(folder+"organization test.csv", true);
		orgIdSrc = rows.get(0)[0];

		rows = CSVUtils.readFile(folder+"organization demo.csv", true);
		orgIdTarget = rows.get(0)[0];

		rows = CSVUtils.readFile(folder+"user preference test.csv", false);
		for (String[] cols : rows) {
			if (result.isEmpty()) {
				result.add(cols);
				continue;
			}
			
			String name = cols[2];
			if (name.endsWith("(User)")) continue;
			String setupOwnerId = cols[3];
			if (setupOwnerId.startsWith("00D")) {
				cols[3] = orgIdTarget;
			}
			else if (setupOwnerId.startsWith("00e")) {
				String setupOwnerName = profileMapSrc.get(setupOwnerId);
				cols[3] = profileMapTarget.get(setupOwnerName); 
			}
			
			result.add(cols);
		}
		
		CSVUtils.createFile(folder+"user preference demo.csv", result);
	}

}

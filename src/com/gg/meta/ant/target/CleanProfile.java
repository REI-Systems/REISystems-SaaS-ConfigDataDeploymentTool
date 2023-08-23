package com.gg.meta.ant.target;

import java.io.File;

import org.apache.log4j.Logger;

import com.gg.common.Variables;
import com.gg.meta.helper.ProfileCleanupHelper;
import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;

/**
 * It removes 'userPermissions' tag from all profiles as it causes
 * errors sometimes when source and target org have different salesforce versions
 * 
 * @author shahnavazk
 *
 */
public class CleanProfile extends Variables {
	static Logger log = Logger.getRootLogger();
	static String profilesFolder = GGUtils.getSRCFolderURL() + "profiles";
	
	public static void main(String[] args) {
		log.info("Profile cleanup starts");
		profilesFolder = "C:\\Users\\nakul.kadam\\OneDrive - REI Systems Inc\\REI Projects\\REISystems-SaaS-GovGrants-MigrationTool-master(1)\\REISystems-SaaS-GovGrants-MigrationTool-master\\Main\\src\\profiles\\";
		//System.out.println("*** Test " + profilesFolder);
		File profileFolder = new File(profilesFolder);
		log.info("Profile path "+ profilesFolder);
		for (File profile : profileFolder.listFiles()) {
			process(profile);
		}
		log.info("Profile cleanup ends");
	}
	/**
	 * Processes the given profile file to remove unwanted or redundant permissions and cleanup the profile's XML content.
	 */
	private static void process(File profile) {
		log.info("Processing profile " + profile.getName());
		String data = FileUtils.readFile(profile, true);
		ProfileCleanupHelper helper = new ProfileCleanupHelper(data, profile.getName());
		String newdata = helper.process();
		if (data.equals(newdata) == false) {
			FileUtils.createFile(profile, newdata);
		}
	}
	
	
}

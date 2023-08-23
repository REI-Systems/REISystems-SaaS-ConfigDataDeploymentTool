package com.gg.config.compare;

import java.util.Scanner;

import com.gg.common.Variables;
import com.gg.config.vo.MigrationItem;

/**
 * Compares source and target orgs and identifies the mismatches in the config records.
 * It performs the comparison both at record level and field level and spits out CSV
 * files with results.
 * 
 *  This list of objects to be compared is controlled by ChangeMe.java
 *  
 * @author shahnavazk
 *
 */
public class OrgDiff extends Variables {
	public static final String result_folder = "D:\\Projects\\Salesforce\\REI\\Projects\\GovGrants\\Product\\Config Migration Issue 2016-10-10\\Comparison\\";
	
	public static void main(String[] args) {
		System.out.print("You are trying to compare " + SRC_ORG_NAME_ALIAS + " (" + src.getUsername() + ") with " +
					TAR_ORG_NAME_ALIAS + " (" + target.getUsername() + ").\nWould you like to proceed? (Hit Enter to continue)");
		try (Scanner scanner = new Scanner(System.in)) {
			scanner.nextLine();
		}
		new OrgDiff().compare();
	}

	public void compare() {
		prepare();
	}
	
	/* private methods */
	
	private void prepare() {
		for (MigrationItem item : configSobjects) {
			new SObjectDiff(item).compare();
		}
	}

}

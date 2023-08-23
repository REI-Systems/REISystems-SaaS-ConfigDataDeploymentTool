package com.gg.config.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.axis.message.MessageElement;

import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.lib.util.CSVUtils;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.sobject.SObject;

public class ConfigDataScanner {

	static ForceDelegateRaw gate;
	static List<String> searchStrings;
	static List<Match> matches = new ArrayList<Match>();
	static List<String> skipSObjects = new ArrayList<String>();
	
	static {
		skipSObjects.add("Attachment");
		skipSObjects.add("Note");
	}
	
	//args[0] - org name, args[1] - search string, args[2] - result file
	public static void main(String[] args) {
		gate = ForceDelegateRaw.login(args[0]);
		fetchSearchStrings(args[1]);
		
		DescribeGlobalResult gr = gate.describeGlobal();
		for (DescribeGlobalSObjectResult gsr : gr.getSobjects()) {
			try {
				String sobjectName = gsr.getName();
				System.out.println("------------------------------------------------ " + sobjectName + "------------------------------------------------");
				if (sobjectName.endsWith("__c")) {
					String soql = ForceUtils.prepareSOQL(gate, sobjectName, null);
					SObject[] rows = gate.queryMultiple(soql, null);
					if (rows != null) {
						System.out.println("total records fetched from " + sobjectName + "is " + rows.length);
						for (SObject row : rows) {
							searchRecord(row);
						}
					}
					else {
						System.out.println("no records found in " + sobjectName);
					}
				}
			}
			catch (Exception e) {
				System.out.println(e);
			}
			finally {
				createResultFile(gsr.getName(), args[2]);
			}
		}		
	}

	/**
	 * Creates a result CSV file containing matches for a specific SObject.
	 *
	 * @param sobjectName The name of the SObject.
	 * @param folder The folder where the result file should be created.
	 */
	private static void createResultFile(String sobjectName, String folder) {
	    if (matches.isEmpty()) {
	        return; // No need to create a result file if there are no matches.
	    }

	    List<String[]> result = new ArrayList<>();
	    result.add(new String[]{"Record Id", "Field Name", "Field Value", "Search String Found"});

	    for (Match m : matches) {
	        result.add(new String[]{m.recordId, m.fieldName, m.fieldValue, m.searchStringFound});
	    }

	    // Use the Path class to handle file path concatenation.
	    String file = folder.endsWith("/") || folder.endsWith("\\") ?
	            folder + sobjectName + ".csv" : folder + "/" + sobjectName + ".csv";

	    CSVUtils.createFile(file, result);
	    matches.clear();
	}

	/**
	 * Searches for specific text patterns in the fields of an SObject record.
	 *
	 * @param record The SObject record to be searched.
	 */
	private static void searchRecord(SObject record) {
		for (MessageElement me : record.get_any()) {
			String fieldValue = ForceUtils.getSObjectFieldValue(record, me.getName(), true);
			if (StringUtils.isNonEmpty(fieldValue)) {
				searchField(fieldValue, record.getType(), me.getName(), record.getId());
			}
		}		
	}
	/**
	 * Searches a field value for specific text patterns and stores matches in the 'matches' list.
	 *
	 * @param fieldValue The value of the field to be searched.
	 * @param sobjectName The name of the SObject to which the field belongs.
	 * @param fieldName The name of the field being searched.
	 * @param recordId The ID of the record containing the field.
	 */
	private static void searchField (String fieldValue, String sobjectName, String fieldName, String recordId) {
		for (String searchStr : searchStrings) {
			if (fieldValue.contains(searchStr)) {
				matches.add(new Match(sobjectName, fieldName, fieldValue, searchStr, recordId));
			}
		}
	}
	/**
	 * Fetches and stores the search strings from a comma-separated input string.
	 * Each search string is trimmed and added to the 'searchStrings' list.
	 *
	 * @param searchString The comma-separated input string containing search strings.
	 */
	private static void fetchSearchStrings(String searchString) {
		searchStrings = new ArrayList<String>();
		
		String[] arr = searchString.split(",");
		for (String ele : arr) {
			searchStrings.add(ele.trim());
		}
	}
	
	public static class Match {
		private String sobjectName;
		private String fieldName;
		private String fieldValue;
		private String searchStringFound;
		private String recordId;
		
		public Match (String sobjectName, String fieldName, String fieldValue, String searchStringFound, String recordId) {
			this.sobjectName = sobjectName;
			this.fieldName = fieldName;
			this.fieldValue = fieldValue;
			this.searchStringFound = searchStringFound;
			this.recordId = recordId;
		}
	}
}

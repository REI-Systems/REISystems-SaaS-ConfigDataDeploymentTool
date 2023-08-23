package com.gg.config.migration.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.force.service.raw.SObjectWrapper;
import com.gg.common.Variables;
/**
 * It deletes the records found in target org but not found in source org. 
 * Deletion is performed only on target org.
 *   
 * @author shahnavazk
 *
 */
public class DeleteObsoleteTargetRecords extends Variables {
	private Map<String, SObjectWrapper> srcRecords, tarRecords;
	
	public DeleteObsoleteTargetRecords(Map<String, SObjectWrapper> srcRecords, Map<String, SObjectWrapper> tarRecords) {
		this.srcRecords = srcRecords;
		this.tarRecords = tarRecords;
	}

	/**
	 * Deletes obsolete records from the target organization based on a unique ID
	 * field.
	 */
	public void delete() {
		List<String> idsToDelete = new ArrayList<String>();
		for (String uniqueIdFieldValue : tarRecords.keySet()) {
			if (srcRecords.containsKey(uniqueIdFieldValue) == false) {
				String tarRecordId = tarRecords.get(uniqueIdFieldValue).getId();
				idsToDelete.add(tarRecordId);
			}
		}
		
		log.info("Deleting " + idsToDelete.size() + " obsolete records from target org");
		target.delete(idsToDelete);
	}

}

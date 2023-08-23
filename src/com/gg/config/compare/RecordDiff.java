package com.gg.config.compare;

import java.util.ArrayList;
import java.util.List;

import com.force.service.ForceUtils;
import com.gg.common.Variables;
import com.gg.config.util.AppUtils;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Compares two records from the same sobject (From two different orgs) having the same internal unique id
 * 
 * @author shahnavazk
 *
 */
public class RecordDiff extends Variables {
	private SObject srcRecord;
	private SObject tarRecord;
	private String uniqueIdValue;
	private List<String> uniqueIdFieldNames;
	private List<String[]> diffRows = new ArrayList<String[]>();
	private List<String> fields;
	private List<String> srcFieldValues = new ArrayList<String>();
	private List<String> targetFieldValues = new ArrayList<String>();
			
	public RecordDiff(SObject srcRecord, SObject targetRecord, String uniqueIdValue, String[] uniqueIdFieldNamesArr, List<String> fields) {
		this.srcRecord = srcRecord;
		this.tarRecord = targetRecord;
		this.uniqueIdValue = uniqueIdValue;
		this.fields = fields;

		this.uniqueIdFieldNames = new ArrayList<String>();		
		for (String uf : uniqueIdFieldNamesArr) {
			uniqueIdFieldNames.add(uf);
		}
	}
	
	public List<String[]> compare() {
		init();
		compareRecords();
		compareFields();
		return diffRows;		
	}
	
	/* PRIVATE METHODS */
	
	private void init() {
		srcFieldValues.addAll(getFieldValues(srcRecord));
		targetFieldValues.addAll(getFieldValues(tarRecord));
	}
	
	/**
	 * Compares two records and generates a list of differences for missing records.
	 */
	private void compareRecords() {
		
		 if (srcRecord == null && tarRecord == null) {
		        // Both records are null, nothing to compare
		        return;
		    }
		List<String> columns = new ArrayList<String>();
		// srcRecord is present, but tarRecord is missing
		if (srcRecord != null && tarRecord == null) {
			columns.add(SRC_ORG_NAME_ALIAS);
			columns.add("Record missing in " + TAR_ORG_NAME_ALIAS);
			
			columns.addAll(srcFieldValues);
			
			columns.add(UserCache.getInstance(src).getUsername(ForceUtils.getSObjectFieldValue(srcRecord, "CreatedById", true)));
			columns.add(ForceUtils.getSObjectFieldValue(srcRecord, "CreatedDate", true));
			columns.add(UserCache.getInstance(src).getUsername(ForceUtils.getSObjectFieldValue(srcRecord, "LastModifiedById", true)));
			columns.add(ForceUtils.getSObjectFieldValue(srcRecord, "LastModifiedDate", true));
			
			diffRows.add(StringUtils.getStringArrayFromList(columns));
		}
		 // tarRecord is present, but srcRecord is missing
		else if (srcRecord == null && tarRecord != null) {
			columns.add(TAR_ORG_NAME_ALIAS);
			columns.add("Record missing in " + SRC_ORG_NAME_ALIAS);
			
			columns.addAll(targetFieldValues);
			
			columns.add(UserCache.getInstance(target).getUsername(ForceUtils.getSObjectFieldValue(tarRecord, "CreatedById", true)));
			columns.add(ForceUtils.getSObjectFieldValue(tarRecord, "CreatedDate", true));
			columns.add(UserCache.getInstance(target).getUsername(ForceUtils.getSObjectFieldValue(tarRecord, "LastModifiedById", true)));
			columns.add(ForceUtils.getSObjectFieldValue(tarRecord, "LastModifiedDate", true));
			
			diffRows.add(StringUtils.getStringArrayFromList(columns));
		}
	}
	
	/**
	 * Compares the fields of the source (srcRecord) and target (tarRecord) records
	 * and generates a list of differences for each field.
	 */
	private void compareFields() {
		if (srcRecord != null && tarRecord != null) {
			List<String> srcColumns = new ArrayList<String>();
			srcColumns.add(SRC_ORG_NAME_ALIAS);
			srcColumns.add("Field Difference");
			srcColumns.add(srcRecord.getId());
			srcColumns.add(uniqueIdValue);

			List<String> tarColumns = new ArrayList<String>();
			tarColumns.add(TAR_ORG_NAME_ALIAS);
			tarColumns.add("Field Difference");
			tarColumns.add(tarRecord.getId());
			tarColumns.add(uniqueIdValue);

			boolean diffFound = false;
			for (String fieldName : fields) {
				if (isUniqueIdField(fieldName)) continue;
				
				String srcFieldValue = ForceUtils.getSObjectFieldValue(srcRecord, fieldName, true);
				srcFieldValue = AppUtils.removeNamespace(srcFieldValue);
				
				String tarFieldValue = ForceUtils.getSObjectFieldValue(tarRecord, fieldName, true);
				tarFieldValue = AppUtils.removeNamespace(tarFieldValue);
				
				if (srcFieldValue.equals(tarFieldValue) == false) {
					srcColumns.add(srcFieldValue);
					tarColumns.add(tarFieldValue);
					diffFound = true;
				}
				else {
					srcColumns.add("");
					tarColumns.add("");
				}
			}
			
			if (diffFound == false) return;
			
			srcColumns.add(UserCache.getInstance(src).getUsername(ForceUtils.getSObjectFieldValue(srcRecord, "CreatedById", true)));
			srcColumns.add(ForceUtils.getSObjectFieldValue(srcRecord, "CreatedDate", true));
			srcColumns.add(UserCache.getInstance(src).getUsername(ForceUtils.getSObjectFieldValue(srcRecord, "LastModifiedById", true)));
			srcColumns.add(ForceUtils.getSObjectFieldValue(srcRecord, "LastModifiedDate", true));

			tarColumns.add(UserCache.getInstance(target).getUsername(ForceUtils.getSObjectFieldValue(tarRecord, "CreatedById", true)));
			tarColumns.add(ForceUtils.getSObjectFieldValue(tarRecord, "CreatedDate", true));
			tarColumns.add(UserCache.getInstance(target).getUsername(ForceUtils.getSObjectFieldValue(tarRecord, "LastModifiedById", true)));
			tarColumns.add(ForceUtils.getSObjectFieldValue(tarRecord, "LastModifiedDate", true));

			diffRows.add(StringUtils.getStringArrayFromList(srcColumns));
			diffRows.add(StringUtils.getStringArrayFromList(tarColumns));
		}
	}

	private List<String> getFieldValues(SObject record) {
		List<String> values = new ArrayList<String>();
		if (record != null) {
			values.add(record.getId());
			values.add(uniqueIdValue);
			for (String fieldName : fields) {
				String fieldValue = ForceUtils.getSObjectFieldValue(record, fieldName);
				if (fieldValue == null) {
					fieldValue = "";
				}
				values.add(fieldValue);
			}
		}		
		return values;
	}
	
	private boolean isUniqueIdField(String fieldName) {
		return uniqueIdFieldNames.contains(fieldName);
	}
}

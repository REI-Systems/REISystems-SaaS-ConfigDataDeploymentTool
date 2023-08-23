package com.gg.config.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import javax.xml.namespace.QName;
 
import org.apache.axis.message.MessageElement;

import com.force.service.raw.ForceDelegateRaw;
import com.lib.util.CSVUtils;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.sobject.SObject;

public class ConfigDataScannerUpdate {

	static ForceDelegateRaw gate;
	static String folder = "C:\\Users\\shahnavazk\\Downloads\\ConfigDataScan\\";
	
	public static void main(String[] args) {
		gate = ForceDelegateRaw.login("target");
		for (File f : new File(folder).listFiles()) {
			processSObject(f);
		}
		
	}

	private static void processSObject(File f) {
		System.out.println("Processing file " + f.getName());
		/*List<String> processedIdFields = new ArrayList<>();
		Map<String, SObject> recordMap = new HashMap<>();		
		List<String[]> rows = CSVUtils.readFile(f, true);
		String sobjectName = f.getName().replace(".csv", "");
		
		for (String[] cols : rows) {
			String id = cols[0];
			String fieldName = cols[1];
			String fieldValue = cols[2];
			
			if (processedIdFields.contains(id + fieldName)) continue;
			
			if (fieldValue.contains("SPI") && fieldValue.contains("SPIP") == false) {
				fieldValue = fieldValue.replace("SPI", "SPIP");
			}
			
			SObject record = recordMap.get(id);
			if (record == null) {
				record = new SObject();
				record.setType(sobjectName);
				record.setId(id);
			}
			
			MessageElement[] arr = record.get_any();
			List<MessageElement> eleList = new ArrayList<>();
			if (arr != null) {
				for (MessageElement ele : arr) {
					eleList.add(ele);
				}
			}

			MessageElement ele = new MessageElement(new QName(fieldName), StringUtils.removeInvalidXMLChars(fieldValue));
			eleList.add(ele);
			record.set_any(convertToArray(eleList));
			
			recordMap.put(id, record);
			
			processedIdFields.add(id + fieldName);
		}
		
		List<SObject> updateRecords = new ArrayList<>();
		for (SObject record : recordMap.values()) {
			updateRecords.add(record);
		}
		System.out.println("updating records: " + updateRecords.size());
		gate.updateMultiple(updateRecords);*/

	}
	/**
	 * Converts a List of MessageElement objects to an array of MessageElement.
	 *
	 * @param fieldsList The List of MessageElement objects to be converted.
	 * @return An array of MessageElement containing the elements from the input List.
	 */
	private static MessageElement[] convertToArray(List<MessageElement> fieldsList) {
		MessageElement[] fieldsArr = new MessageElement[fieldsList.size()];
		int i=0;
		for (MessageElement field : fieldsList) {
			fieldsArr[i++] = field;
		}
		return fieldsArr;
	}
}

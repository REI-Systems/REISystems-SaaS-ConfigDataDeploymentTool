package com.gg.config.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.gg.config.vo.FieldItem;
import com.gg.meta.util.GGUtils;
/**
 * A JSON File util
 * 
 * @author Shubhangi Shinde
 * 
 */

public class JSONReaderUtil {
	
	public static Logger log = Logger.getRootLogger();
	/**
	 * Retrieves a JSONArray from the JSON file using the specified key field name.
	 *
	 * @param keyFieldName The key field name used to retrieve the JSONArray from the JSON file.
	 * @return A JSONArray containing the data associated with the specified key field name, or null if the key field is not found.
	 */
	public static  JSONArray getJsonArray(String keyFieldName) {
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(GGUtils.getRootFolderURL() + JSONReaderConstants.JSON_FILE_NAME));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(obj.toString());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		JSONArray keyFieldArray = null;
		try {
			if (jsonObject.has(keyFieldName)) {
				keyFieldArray = (JSONArray) jsonObject.getJSONArray(keyFieldName);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return keyFieldArray;
	}
	/**
	 * Retrieves the string data associated with the specified key field name from a JSONObject.
	 *
	 * @param jsonObject   The JSONObject from which to retrieve the string data.
	 * @param keyFieldName The key field name used to retrieve the string data from the JSONObject.
	 * @return The string data associated with the specified key field name, or an empty string if the key field is not found or the value is null.
	 */
	public  String getStringData(JSONObject jsonObject, String keyFieldName) {
		String value = "";
		if(jsonObject != null) {
			try {
				if (jsonObject.has(keyFieldName)) {
					try {
						if (jsonObject.get(keyFieldName) != null) {
							value = jsonObject.getString(keyFieldName);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return value;
	}
	/**
	 * Retrieves an Integer value from a JSONObject based on the provided key field name.
	 *
	 * @param jsonObject   The JSONObject from which to extract the Integer value.
	 * @param keyFieldName The key field name to look up the Integer value.
	 * @return The Integer value associated with the given key, or a default value of 500
	 *         if the key is not found or there's an exception during the extraction process.
	 */
	public Integer getIntegerData(JSONObject jsonObject, String keyFieldName) {
	    if (jsonObject != null && jsonObject.has(keyFieldName)) {
	        try {
	            return jsonObject.getInt(keyFieldName);
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }
	    }
	    return 500; // Default value if key is not found or there's an exception
	}

	/**
	 * Retrieves a Boolean value from a JSONObject based on the provided key field name.
	 *
	 * @param jsonObject   The JSONObject from which to extract the Boolean value.
	 * @param keyFieldName The key field name to look up the Boolean value.
	 * @param defaultValue The default Boolean value to return if the key is not found or there's an exception.
	 * @return The Boolean value associated with the given key, or the defaultValue if the key is not found
	 *         or there's an exception during the extraction process.
	 */
	public Boolean getBooleanData(JSONObject jsonObject, String keyFieldName, Boolean defaultValue) {
	    if (jsonObject != null && jsonObject.has(keyFieldName)) {
	        try {
	            return jsonObject.getBoolean(keyFieldName);
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }
	    }
	    return defaultValue;
	}

	/**
	 * Retrieves a List of String values from a JSONObject based on the provided key field name,
	 * splitting the values using the specified separator.
	 *
	 * @param jsonObj     The JSONObject from which to extract the List of String values.
	 * @param keyFieldName The key field name to look up the String values.
	 * @param separator    The separator to split the values in the JSON string.
	 * @return A List of String values associated with the given key. If the key is not found,
	 *         an empty list will be returned.
	 */
	public List<String> getListDataUsingSeparator(JSONObject jsonObj, String keyFieldName, String separator) {
	    List<String> listData = new ArrayList<>();
	    if (jsonObj != null && jsonObj.has(keyFieldName)) {
	        try {
	            String values = jsonObj.getString(keyFieldName);
	            String[] valueArray = values.split(separator);
	            listData.addAll(Arrays.asList(valueArray));
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }
	    }
	    return listData;
	}
	/**
	 * Retrieves a Map of String key-value pairs from a JSONObject based on the provided key field name,
	 * using the specified separators for splitting the keys and values.
	 */
	public  Map<String, String> getMapData(JSONObject jsonObj, String keyFieldName) {

		Map<String, String> mapData = new HashMap<String, String>();
		if(jsonObj != null) {
			try {
				if (jsonObj.has(keyFieldName)) {
					if (jsonObj.get(keyFieldName) != null) {
						String values = jsonObj.getString(keyFieldName);
						String[] licenseMapArray = values.split(JSONReaderConstants.LIST_SEPARATOR);
						for (String val : licenseMapArray) {
							String[] valArray = val.split(JSONReaderConstants.MAP_SEPARATOR);
							mapData.put(valArray[0], valArray[1]);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return mapData;

	}
	/**
	 * Retrieves a List of FieldItems from a JSONObject containing information about excluded fields.
	 *
	 * @param jsonObj The JSONObject from which to extract the List of FieldItems.
	 * @return A List of FieldItems containing field API names and packaging information
	 *         for excluded fields. If the JSON key is not found or there's an exception,
	 *         an empty list will be returned.
	 */
	public  List<FieldItem> getExcludeFieldData(JSONObject jsonObj) {
		List<FieldItem> fieldItems = new ArrayList<FieldItem>();
		if(jsonObj!= null) {
			if (jsonObj.has(JSONReaderConstants.KEY_EXCLUDE_FIELDS)) {
				JSONArray childArray;
				try {
					childArray = jsonObj.getJSONArray(JSONReaderConstants.KEY_EXCLUDE_FIELDS);
					for (int j = 0; j < childArray.length(); j++) {
						JSONObject childObj = childArray.optJSONObject(j);
						String fieldAPINameWithoutNamespace = getStringData(childObj,
								JSONReaderConstants.KEY_FIELD_API_NAME_WITHOUT_NAMESPACE);
						boolean packaged = getBooleanData(childObj, JSONReaderConstants.KEY_EXCLUDE_FIELDS_PACKAGED, false);
						FieldItem fieldItem = new FieldItem(fieldAPINameWithoutNamespace, packaged);
						fieldItems.add(fieldItem);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return fieldItems;
	}
}

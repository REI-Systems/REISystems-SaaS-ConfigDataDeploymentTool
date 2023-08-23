
package com.gg.config.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gg.config.vo.DateTimeVO;
import com.gg.config.vo.MigrationItemVO;
import com.gg.config.vo.SrcToTargetVO;
import com.gg.config.vo.SrcVO;
import com.gg.config.vo.TargetVO;


/**
 * A JSON File reader
 * 
 * @author Shubhangi Shinde, VijayaLaxmi
 * 
 */
public class JSONFileReader {
	public static Logger log = Logger.getRootLogger();
	/**
	 * Reads and retrieves a List of MigrationItemVO objects for a specific type of object from a JSON file.
	 *
	 * @param typeOfObject The type of object for which migration items are to be read.
	 * @return A List of MigrationItemVO objects representing the migration items read from the JSON file.
	 */
	public static List<MigrationItemVO> migrationItemsReader(String typeOfObject) {
		List<MigrationItemVO> migrationItemVOList = readMigrationItemsVO(JSONReaderUtil.getJsonArray(typeOfObject));
		log.info("loading "+typeOfObject+ " from JSON File.");
		return migrationItemVOList;
	}
	/**
	 * Reads and retrieves the source-to-target organization mapping details from a JSON file.
	 *
	 * @return A SrcToTargetVO object containing the source-to-target organization mapping details.
	 */
	public static SrcToTargetVO readSrcToTargetJSON() {
		SrcToTargetVO srcToTargetVO = readSrcToTargetVO(
				JSONReaderUtil.getJsonArray(JSONReaderConstants.SRC_TO_TAR_ORG_NAME_ALIAS));
		log.info("loading require details for source to target org from JSON File.");
		return srcToTargetVO;
	}
	/**
	 * Retrieves the required details of the source organization from a JSON file.
	 *
	 * @return A SrcVO object containing the required details of the source organization.
	 */
	public static SrcVO getSrcVo() {
		SrcVO srcVO = readSrcData(JSONReaderUtil.getJsonArray(JSONReaderConstants.SRC_ORG_NAME_ALIAS));
		log.info("loading require details for source org from JSON File.");
		return srcVO;
	}
	/**
	 * Retrieves the required details of the target organization from a JSON file.
	 *
	 * @return A TargetVO object containing the required details of the target organization.
	 */
	public static TargetVO getTargetVo() {
		TargetVO targetVO = readTargetData(JSONReaderUtil.getJsonArray(JSONReaderConstants.TAR_ORG_NAME_ALIAS));
		log.info("loading require details for target org from JSON File.");
		return targetVO;
	}
	/**
	 * Retrieves the required details related to date time where clause for migration from a JSON file.
	 *
	 * @return A DateTimeVO object containing the required date time details for migration.
	 */
	public static DateTimeVO getDateTimeVO() {
		JSONArray dateTimejsonArray = JSONReaderUtil.getJsonArray(JSONReaderConstants.DATE_TIME_WHERE_CLAUSE_FOR_MIGRATION);
		DateTimeVO dateTimeVO = null ;
		if(dateTimejsonArray != null) {
			 dateTimeVO = readDateTimeData(dateTimejsonArray);
		}else{
			log.error(JSONReaderConstants.DATE_TIME_WHERE_CLAUSE_FOR_MIGRATION +" key field is missing ! ");
		}
		log.info("loading require details for where clause related to date time from JSON File.");
		return dateTimeVO;
	}
	/**
	 * Reads and retrieves the date time related data from a JSONArray and constructs a DateTimeVO object.
	 *
	 * @param dateTimejsonArray The JSONArray containing date time related data.
	 * @return A DateTimeVO object containing the date time related data.
	 */
	private static DateTimeVO readDateTimeData(JSONArray dateTimejsonArray) {
		DateTimeVO dateTimeVO = new DateTimeVO();
		JSONObject jsonObj = dateTimejsonArray.optJSONObject(0);
		JSONReaderUtil jsonUtil = new JSONReaderUtil();
		if(jsonObj != null) {
			dateTimeVO.setDateLiteralOrDateTime(jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_DATE_LITERAL_OR_DATETIME));
			dateTimeVO.setOperator(jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_OPERATOR));
			if(!dateTimeVO.getDateLiteralOrDateTime().isEmpty() && dateTimeVO.getOperator().isEmpty()) {
				dateTimeVO.setOperator("=");
			}
		}else {
			log.error("dateLiteralOrDateTime and operator key fields are missing in confing.json!");
		}
		return dateTimeVO;
	}
	/**
	 * Reads and retrieves the target organization data from a JSONArray and constructs a TargetVO object.
	 *
	 * @param targetJsonArray The JSONArray containing target organization data.
	 * @return A TargetVO object containing the target organization data.
	 */
	private static TargetVO readTargetData(JSONArray targetJsonArray) {
		TargetVO targetVo = new TargetVO();
		JSONObject jsonObj = targetJsonArray.optJSONObject(0);
		JSONReaderUtil jsonUtil = new JSONReaderUtil();
		targetVo.setUser(jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_USER));
		targetVo.setPassword(jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_PASSWORD));
		targetVo.setEndPoint(jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_ENDPOINT));
		return targetVo;
	}
	/**
	 * Reads and retrieves the source-to-target organization mapping data from a JSONArray and constructs a SrcToTargetVO object.
	 *
	 * @param srcToTargetJsonArray The JSONArray containing source-to-target organization mapping data.
	 * @return A SrcToTargetVO object containing the source-to-target organization mapping data.
	 */
	private static SrcToTargetVO readSrcToTargetVO(JSONArray srcToTargetJsonArray) {

		JSONObject jsonObj = srcToTargetJsonArray.optJSONObject(0);
		JSONReaderUtil jsonUtil = new JSONReaderUtil();
		SrcToTargetVO srcToTargetVO = new SrcToTargetVO();
		srcToTargetVO.setFirstTimeMigration(
				jsonUtil.getBooleanData(jsonObj, JSONReaderConstants.KEY_FIRST_TIME_MIGRATION, false));
		srcToTargetVO.setTargetOrgPrefix(jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_TARGET_ORG_PREFIX));
		srcToTargetVO.setTargetOrgAlternateOwnerId(
				jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_TARGET_ORG_ALTERNATE_OWNER_ID));
		srcToTargetVO.setSrcManagedPackageNamespace(
				jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_SRC_MANAGED_PACKAGE_NAMESPACE));
		srcToTargetVO.setTargetManagedPackageNamespace(
				jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_TARGET_MANAGED_PACKAGE_NAMESPACE));
		srcToTargetVO
				.setAppDataInitializer(jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_APP_DATA_INITIALIZER));
		srcToTargetVO.setTargetOrgUsernameSuffix(
				jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_TARGET_ORG_USERNAME_SUFFIX));
		srcToTargetVO.setIsCustomSettingsRequired(
				jsonUtil.getBooleanData(jsonObj, JSONReaderConstants.KEY_IS_CUSTOM_SETTINGS_REQUIRED, false));
		srcToTargetVO.setIsAccountContactMigrationRequired(
				jsonUtil.getBooleanData(jsonObj, JSONReaderConstants.KEY_IS_ACCOUNT_CONTACT_MIGRATION_REQUIRED, false));
		srcToTargetVO.setIsMigrationRequiredFromAllUsers(
				jsonUtil.getBooleanData(jsonObj, JSONReaderConstants.KEY_IS_MIGRATION_REQUIRED_FROM_ALL_USERS, true));
		srcToTargetVO.setUnwantedFields(jsonUtil.getListDataUsingSeparator(jsonObj,
				JSONReaderConstants.KEY_UNWANTED_FIELDS, JSONReaderConstants.LIST_SEPARATOR));
		srcToTargetVO.setUnwantedClasses(jsonUtil.getListDataUsingSeparator(jsonObj,
				JSONReaderConstants.KEY_UNWANTED_CLASSES, JSONReaderConstants.LIST_SEPARATOR));
		srcToTargetVO.setUnwantedLayouts(jsonUtil.getListDataUsingSeparator(jsonObj,
				JSONReaderConstants.KEY_UNWANTED_LAYOUTS, JSONReaderConstants.LIST_SEPARATOR));
		srcToTargetVO.setLicenseMap(jsonUtil.getMapData(jsonObj, JSONReaderConstants.KEY_LICENSE_MAP));
		srcToTargetVO.setUsernameSuffixMap(jsonUtil.getMapData(jsonObj, JSONReaderConstants.KEY_USER_NAME_SUFFIX_MAP));
		srcToTargetVO.setUserFullNameSuffixMap(
				jsonUtil.getMapData(jsonObj, JSONReaderConstants.KEY_USER_FULL_NAME_SUFFIX_MAP));
		srcToTargetVO.setIsKeyValueStoreMigrationRequired(jsonUtil.getBooleanData(jsonObj, JSONReaderConstants.KEY_IS_KEY_VALUE_STORE_MIGRATION_REQUIRED, true));
		return srcToTargetVO;

	}
	/**
	 * Reads and retrieves the source organization data from a JSONArray and constructs a SrcVO object.
	 *
	 * @param srcJsonArray The JSONArray containing source organization data.
	 * @return A SrcVO object containing the source organization data.
	 */
	public static SrcVO readSrcData(JSONArray srcJsonArray) {
		SrcVO srcVo = new SrcVO();
		JSONReaderUtil jsonUtil = new JSONReaderUtil();
		JSONObject jsonObj = srcJsonArray.optJSONObject(0);
		srcVo.setUser(jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_USER));
		srcVo.setPassword(jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_PASSWORD));
		srcVo.setEndPoint(jsonUtil.getStringData(jsonObj, JSONReaderConstants.KEY_ENDPOINT));
		return srcVo;

	}
	/**
	 * Reads and retrieves a List of MigrationItemVO objects from a JSONArray containing migration items data.
	 *
	 * @param migrationItemsJsonArray The JSONArray containing migration items data.
	 * @return A List of MigrationItemVO objects representing the migration items data.
	 */
	private static List<MigrationItemVO> readMigrationItemsVO(JSONArray migrationItemsJsonArray) {

		List<MigrationItemVO> migrationVoList = new ArrayList<MigrationItemVO>();
		JSONReaderUtil jsonUtil = new JSONReaderUtil();
		if (migrationItemsJsonArray != null) {
			for (int i = 0; i < migrationItemsJsonArray.length(); i++) {

				JSONObject childObject;
				try {
					childObject = migrationItemsJsonArray.getJSONObject(i);
					String sObjectName = jsonUtil.getStringData(childObject,
							JSONReaderConstants.KEY_OBJECTNAME_WITHOUT_NAMESPACE);
					if (!sObjectName.isEmpty()) {
						MigrationItemVO migrationItemVO = new MigrationItemVO();
						migrationItemVO.setObjectNameWithoutNamespace(sObjectName);
						migrationItemVO.setPackaged(
								jsonUtil.getBooleanData(childObject, JSONReaderConstants.KEY_PACKAGED, false));
						migrationItemVO.setRelationshipRecordsMustFoundInTargetOrg(jsonUtil.getBooleanData(childObject,
								JSONReaderConstants.KEY_RELATIONSHIP_RECORD_MUST_FPUND_IN_TARGET_ORG, true));
						migrationItemVO.setWhereClause(
								jsonUtil.getStringData(childObject, JSONReaderConstants.KEY_WHERE_CLAUSE));
						migrationItemVO
								.setBatchSize(jsonUtil.getIntegerData(childObject, JSONReaderConstants.KEY_BATCH_SIZE));
						migrationItemVO.setDeleteTargetRecords(jsonUtil.getBooleanData(childObject,
								JSONReaderConstants.KEY_DELETE_TARGET_RECORDS, false));
						migrationItemVO.setExcludeFields(jsonUtil.getExcludeFieldData(childObject));
						migrationVoList.add(migrationItemVO);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}
		return migrationVoList;

	}

}

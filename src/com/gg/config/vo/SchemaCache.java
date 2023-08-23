package com.gg.config.vo;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.force.service.raw.ForceDelegateRaw;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;

/**
 * Caches all sobjects meta data information for a given org. 
 * @author shahnavazk
 *
 */
public class SchemaCache {
	private String orgName;
	private Map<String, DescribeGlobalSObjectResult> sobjectMetaDataMap = new HashMap<String, DescribeGlobalSObjectResult>();  //key->sobject prefix 
	private Map<String, String> sobjectNameMap = new HashMap<String, String>();  //key->sobject name with namespace, value->sobject prefix
	private static Logger log = Logger.getRootLogger();

	public SchemaCache(ForceDelegateRaw gateRaw) {
		this.orgName = gateRaw.getOrgName();
		loadMetaData(gateRaw);
	}
	
	public DescribeGlobalSObjectResult getSObjectMetaDataByName(String sobjectNameWithNamespace) {
		String keyPrefix = sobjectNameMap.get(sobjectNameWithNamespace.toLowerCase());
		return getSObjectMetaDataByPrefix(keyPrefix);
	}

	public DescribeGlobalSObjectResult getSObjectMetaDataByPrefix(String keyPrefix) {
		return sobjectMetaDataMap.get(keyPrefix);
	}

	/* PRIVATE METHODS */
	
	private void loadMetaData(ForceDelegateRaw gateRaw) {
		DescribeGlobalResult gr = gateRaw.describeGlobal();
		for (DescribeGlobalSObjectResult sobjectResult : gr.getSobjects()) {
			sobjectMetaDataMap.put(sobjectResult.getKeyPrefix(), sobjectResult);
			sobjectNameMap.put(sobjectResult.getName().toLowerCase(), sobjectResult.getKeyPrefix());
		}
		log.info("schema cache loaded for " + orgName);
	}
}

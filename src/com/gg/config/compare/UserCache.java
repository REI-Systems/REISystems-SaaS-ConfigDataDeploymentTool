package com.gg.config.compare;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.sforce.soap.partner.sobject.SObject;

public class UserCache {
	private static UserCache instance;
	private Map<String, Data> dataMap = new HashMap<String, Data>(); //key->org name
	public static Logger log = Logger.getRootLogger();
	private UserCache() {		
	}

	public static Data getInstance(ForceDelegateRaw gate) {
		if (instance == null) {
			instance = new UserCache();
		}
		
		Data d = instance.dataMap.get(gate.getOrgName());
		if (d == null) {
			d = new Data(gate);
			instance.dataMap.put(gate.getOrgName(), d);
		}
		
		return d;
	}	

	public static class Data {
		private ForceDelegateRaw gate;
		private Map<String, SObject> userMap = new HashMap<String, SObject>();  //key->user id
		
		public Data(ForceDelegateRaw gate) {
			this.gate = gate;
			loadUsers();
		}

		private void loadUsers() {
			SObject[] records = gate.queryMultiple("Select Id, Username, Firstname, Lastname from User", null);
			for (SObject record : records) {
				userMap.put(record.getId(), record);
			}
		}
		
		public String getUsername(String userId) {
			SObject user = userMap.get(userId);
			if (user == null) {
				log.error("User not found by record id " + userId);
				throw new RuntimeException("User not found by record id " + userId);
			}
			return ForceUtils.getSObjectFieldValue(user, "Username");
		}
		
		public Map<String, SObject> getUserMap(){
			return this.userMap;
		}

	}
	
}

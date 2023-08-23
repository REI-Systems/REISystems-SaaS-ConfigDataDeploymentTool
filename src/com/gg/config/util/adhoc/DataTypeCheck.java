package com.gg.config.util.adhoc;

import com.gg.common.Variables;
import com.sforce.soap.partner.sobject.SObject;

public class DataTypeCheck extends Variables {

	public static void main(String[] args) {
		SObject record = src.querySingle("Select Id, Name, ggsTempPack1__OMBExpiration__c from ggsTempPack1__PageTemplate__c where Id='a0s36000002eJk8AAE'", null);
		System.out.println(record);
		
		src.updateSingle(record);

	}

}

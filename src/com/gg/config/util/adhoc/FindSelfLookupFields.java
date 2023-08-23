package com.gg.config.util.adhoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gg.common.Variables;
import com.lib.util.StringUtils;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.Field;

public class FindSelfLookupFields extends Variables {

	public static void main(String[] args) {
		List<String> result = new ArrayList<String>();
		DescribeGlobalResult res = src.describeGlobal();
		for (DescribeGlobalSObjectResult gr : res.getSobjects()) {
			if (gr.isCustomSetting() == false && gr.isCustom() == true) {
				Map<String, Field> allFields = src.getFieldsMetaData(gr.getName());
				for (Field f : allFields.values()) {
					if (f.getName().contains("ClonedFrom__c")) continue;
					if (f.getReferenceTo() != null) {
						String referenceObjectName = f.getReferenceTo()[0];
						if (referenceObjectName.equals(gr.getName())) {
							result.add(gr.getName() + " has self lookup through field " + f.getName());
						}
					}
				}
			}
		}
		System.out.println(StringUtils.getConcatenatedString(result, "\n"));
	}

}

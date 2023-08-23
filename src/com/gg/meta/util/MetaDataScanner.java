package com.gg.meta.util;

import com.force.service.raw.ForceDelegateRaw;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;

public class MetaDataScanner {
	
	static ForceDelegateRaw gate = ForceDelegateRaw.login("target");
	public static void main(String[] args) {
		DescribeGlobalResult gr = gate.describeGlobal();
		for (DescribeGlobalSObjectResult gsr : gr.getSobjects()) {
			DescribeSObjectResult sr = gate.describeSObject(gsr.getName());
			for (Field f : sr.getFields()) {
				
			}
		}

	}

}

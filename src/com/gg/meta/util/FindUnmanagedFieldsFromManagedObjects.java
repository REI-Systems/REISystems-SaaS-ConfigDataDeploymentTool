package com.gg.meta.util;

import java.util.ArrayList;
import java.util.List;

import com.force.service.ForceDelegate;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;

public class FindUnmanagedFieldsFromManagedObjects {
	
	static final String NAMESPACE = "ggsTempPack1__";
	
	public static void main(String[] args) {
		List<String> result = new ArrayList<String>();
		ForceDelegate gate = ForceDelegate.login("shah@ggp.test1", "test@123", false, 25.0);
		DescribeGlobalResult gr = gate.describeGlobal();
		for (DescribeGlobalSObjectResult grs : gr.getSobjects()) {
			DescribeSObjectResult or = gate.describeSObject(grs.getName());
			System.out.println(or.getName());
			if (or.isCustom() && or.getName().startsWith(NAMESPACE)) {
				for (Field f : or.getFields()) {
					if (f.isCustom() && f.getName().startsWith(NAMESPACE) == false) {
						String entry = or.getName() + "." + f.getName();
						result.add(entry);	
						System.out.println("unmanaged field found: " + entry);
					}
				}
			}
		}

		System.out.println("==========================================");
		for (String entry : result) {
			System.out.println("<members>" + entry + "</members>");
		}
	}

}

package com.gg.config.util;

import com.gg.common.Variables;

public class TargetOrgSchema extends SchemaManager {
	private static TargetOrgSchema instance;
	
	private TargetOrgSchema() {
		super(Variables.src);
	}
	
	public static TargetOrgSchema getInstance() {
		if (instance == null) {
			instance = new TargetOrgSchema();
		}
		return instance;
	}
}

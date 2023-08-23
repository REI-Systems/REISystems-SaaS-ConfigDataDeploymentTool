package com.gg.config.util;

import com.gg.common.Variables;

public class SourceOrgSchema extends SchemaManager {
	private static SourceOrgSchema instance;
	
	private SourceOrgSchema() {
		super(Variables.src);
	}
	
	public static SourceOrgSchema getInstance() {
		if (instance == null) {
			instance = new SourceOrgSchema();
		}
		return instance;
	}
}

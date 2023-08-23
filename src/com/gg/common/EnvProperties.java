package com.gg.common;

import com.lib.util.CustomProperties;

public class EnvProperties extends CustomProperties {
	
	@Override
	protected String getFileName() {
		return new AppProperties().getEnvPropFilename();
	}
}

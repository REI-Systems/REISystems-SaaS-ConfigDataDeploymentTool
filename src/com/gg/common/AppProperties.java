package com.gg.common;

import com.lib.util.CustomProperties;

public class AppProperties extends CustomProperties {
	
	@Override
	protected String getFileName() {
		return "app.properties";
	}
	
	public String getEnvPropFilename() {
		return getString("environment.property.filename");
	}
	public static void main(String[] args) {
		AppProperties app = new AppProperties();
		System.out.println(app.getEnvPropFilename());
	}

}

package com.gg.meta.helper;

import com.gg.common.Variables;
import com.sforce.soap.partner.sobject.SObject;

public class OrganizationHolder extends Variables{
	private SObject srcOrg;
	private SObject tarOrg;
	private static OrganizationHolder instance;
	
	private OrganizationHolder() {
		loadOrganization();
	}
	
	public static OrganizationHolder getInstance() {
		if (instance == null) {
			instance = new OrganizationHolder();
		}
		return instance;
	}
	
	public SObject getSourceOrg() {
		return srcOrg;
	}

	public SObject getTargetOrg() {
		return tarOrg;
	}

	public static void main(String[] args) {
		new OrganizationHolder();
	}

	private void loadOrganization() {
		String soql = "Select Id, Name from Organization limit 1";
		SObject[] records = src.queryMultiple(soql, null);
		for (SObject record : records) {
			srcOrg = record;
		}

		records = target.queryMultiple(soql, null);
		for (SObject record : records) {
			tarOrg = record;
		}
	}
	
}

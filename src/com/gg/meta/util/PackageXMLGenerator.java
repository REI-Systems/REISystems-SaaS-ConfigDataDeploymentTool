package com.gg.meta.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gg.common.Variables;

public class PackageXMLGenerator {
	
	private Map<String, List<String>> nodeMap = new HashMap<String, List<String>>();
	private Double version;
	
	public PackageXMLGenerator() {
		version = Variables.apiVersion;
	}
	
	public PackageXMLGenerator(Double apiVersion) {
		this.version = apiVersion;
	}
	
	public static void main(String[] args) {
		PackageXMLGenerator gen = new PackageXMLGenerator();
		gen.addElement("ApexClass", "*");
		gen.addElement("CustomObject", "ggsTempPack1__Package__c");
		gen.addElement("CustomObject", "Task");
		gen.generateXML();
	}
	
	//Example: nodeType: ApexClass, nodeValue: *
	/**
	 * Adds a node value to the node map.
	 * @param nodeType
	 * @param nodeValue
	 */
	public void addElement(String nodeType, String nodeValue) {
		List<String> values = nodeMap.get(nodeType);
		if (values == null) {
			values = new ArrayList<String>();
			nodeMap.put(nodeType, values);
		}
		values.add(nodeValue);
	}
   /**
    * Adds a list of node values to the node map.
    * @param nodeType
    * @param nodeValues
    */
	public void addElement(String nodeType, List<String> nodeValues) {
		List<String> values = nodeMap.get(nodeType);
		if (values == null) {
			values = new ArrayList<String>();
			nodeMap.put(nodeType, values);
		}
		values.addAll(nodeValues);		
	}
     /**
      * Generates an XML representation of the node map.
      * @return The XML string representing the node map.
      */
	public String generateXML() {
		if (nodeMap.isEmpty()) {
			return null;
		}
		
		StringBuffer buf = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buf.append("<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n");
		for (String nodeType : nodeMap.keySet()) {
			buf.append("\t<types>\n");
			for (String nodeValue : nodeMap.get(nodeType)) {
				buf.append("\t\t<members>" + nodeValue + "</members>\n");
			}
			buf.append("\t\t<name>" + nodeType + "</name>\n");
			buf.append("\t</types>\n");
		}
		buf.append("\t<version>" + version + "</version>\n");
		buf.append("</Package>\n");
		
		return buf.toString();
	}
}

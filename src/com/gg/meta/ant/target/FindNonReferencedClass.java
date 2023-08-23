
package com.gg.meta.ant.target;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.force.service.ForceUtils;
import com.force.service.raw.ForceDelegateRaw;
import com.gg.meta.util.GGUtils;
import com.gg.meta.util.PackageXMLGenerator;
import com.lib.util.FileUtils;
import com.sforce.soap.partner.sobject.SObject;

public class FindNonReferencedClass {
	public static final String SRC_ORG_NAME_ALIAS = "src";
	public static ForceDelegateRaw src;
	private static String flexTableActionHanlder = "GNT.FlexTableAction";
	private static String flexTableActionObjectName = "GNT__DataTableAction__c";
	private static String pageLayoutActionHanlder = "GNT.DynamicLayoutAction";
	private static String pageLayoutActionObjectName = "GNT__PageLayoutActionConfig__c";
	public static void main(String[] args) {
		src = ForceDelegateRaw.login("src");
		
		FindNonReferencedClassWrapper fWrapper = new FindNonReferencedClassWrapper(flexTableActionHanlder, flexTableActionObjectName);
		List<String> nonReferencedFlexTableActionClasses = fetchNonReferencedClasses(fWrapper);
		
		FindNonReferencedClassWrapper pWrapper = new FindNonReferencedClassWrapper(pageLayoutActionHanlder, pageLayoutActionObjectName);
		List<String> nonReferencedPageLayoutActionClasses = fetchNonReferencedClasses(pWrapper);
		
		List<String> nonReferencedClasses = new ArrayList<String>();
		for(String cls : nonReferencedFlexTableActionClasses){
			nonReferencedClasses.add(cls);
		}
		for(String cls : nonReferencedPageLayoutActionClasses){
			nonReferencedClasses.add(cls);
		}
		nonReferencedClasses = fetchRelatedTestClasses(nonReferencedClasses);
		
		PackageXMLGenerator gen = new PackageXMLGenerator(42.0);
		gen.addElement("ApexClass", nonReferencedClasses);
		String xml = gen.generateXML();
		System.out.println("Package xml ----->>"+xml);
	}
	/**
	 * Prepares and returns an SOQL query string for the given object name.
	 *
	 */
	private static String prepareQuery(String objName){
		String soql = "select Id, GNT__ActionClass__c from "+ objName +" Where GNT__ActionClass__c IN ?";
		System.out.println("SOQL: " + soql);
		return soql;
	}
	/**
	 * Fetches a list of non-referenced classes based on the given FindNonReferencedClassWrapper.
	 * Non-referenced classes are classes that are not referred to in the action configurations.
	 *
	 * @param wrapper The FindNonReferencedClassWrapper containing the action handler class and action object name.
	 * @return A list of class names that are not referenced in the action configurations.
	 */
	private static List<String> fetchNonReferencedClasses(FindNonReferencedClassWrapper wrapper){
		List<String> classNames = new ArrayList<String>();
		classNames = findClassNames(wrapper.actionHandlerClass);

		Set<String> classNamesReferredInConfig = new HashSet<String>();
		SObject[] actionConfigs = src.queryMultiple(prepareQuery(wrapper.actionObjectName),  new Object[]{classNames});

		for(SObject actionConfig : actionConfigs){
			classNamesReferredInConfig.add(ForceUtils.getSObjectFieldValue(actionConfig, "GNT__ActionClass__c", true));
		}

		List<String> nonReferencedClasses = new ArrayList<String>();
		for(String cls : classNames){
			if(!containsIgnoreCase(classNamesReferredInConfig, cls)){
				nonReferencedClasses.add(cls);
			}
		}
		return nonReferencedClasses;
	}
	/**
	 * Checks if the given set contains a string ignoring case sensitivity.
	 *
	 * @param src The set of strings to be checked.
	 * @param o   The object to be searched in the set (converted to a string).
	 * @return True if the set contains the string (ignoring case), false otherwise.
	 */
	private static boolean containsIgnoreCase(Set<String> src, Object o) {
        String paramStr = (String)o;
        for (String s : src) {
            if (paramStr.equalsIgnoreCase(s)) return true;
        }
        return false;
    }
	/**
	 * Finds and returns a list of class names in the Salesforce project that contain references to the given abstract class.
	 *
	 * @param abstractClassName The name of the abstract class to search for references.
	 * @return A list of class names that reference the specified abstract class.
	 */
	private static List<String> findClassNames(String abstractClassName){
		List<String> classNames = new ArrayList<String>();
		for (File f : new File(GGUtils.getSRCFolderURL() + "classes").listFiles()) {
			if(f.getName().contains(".cls")){
				String data = FileUtils.readFile(f, true);
				if(data.contains(abstractClassName)){
					classNames.add(f.getName().substring(0, f.getName().length() - 4));
					System.out.println(f.getName());
				}
			}
		}
		return classNames;
	}
	/**
	 *
     * Fetches and returns a list of test class names related to the given list of classNames.
     *
	 * @param classNames The list of class names for which related test classes are to be fetched.
	 * @return
	 */
	private static List<String> fetchRelatedTestClasses(List<String> classNames){
		List<String> testClassNames = new ArrayList<String>();
		for (File f : new File(GGUtils.getSRCFolderURL() + "classes").listFiles()) {
			if(f.getName().endsWith(".cls") && f.getName().contains("Test")){ //  && f.getName().contains(cls)
				System.out.println("Processing ---" + f.getName());
				for(String cls : classNames){
					String data = FileUtils.readFile(f, true).toLowerCase();
					if(data.contains("@istest") && data.contains(cls.toLowerCase())){ //&& data.contains("@istest")
						testClassNames.add(f.getName().substring(0, f.getName().length() - 4));
						System.out.println(cls + "---" + f.getName());
						break;
					}
				}
			}
		}
		for(String cls : testClassNames){
			classNames.add(cls);
		}
		return classNames;
	}

}

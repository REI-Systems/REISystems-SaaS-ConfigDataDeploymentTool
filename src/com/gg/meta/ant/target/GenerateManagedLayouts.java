package com.gg.meta.ant.target;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.gg.meta.util.GGUtils;
import com.lib.util.FileUtils;
import com.lib.util.StringUtils;
import org.apache.log4j.Logger;

/**
 * This class prepares the XML file for deploying managed package layouts
 * @author smahavratayajula   
 */

public class GenerateManagedLayouts {	
	 
	static String packageXMLFilename; // XML Definition file for Managed Package Layouts
	static String pkgFileName; 	// Generated Raw Managed Package File Name
	static String path = GGUtils.getSRCFolderURL(); // Absolute path where files exist
	static ArrayList<String> unwantedLayoutNames = new ArrayList<String>();
	static Logger log = Logger.getRootLogger();

	public static void main(String[] args) throws IOException {
		log.info("ManagedLayouts XML generation starts"); 
		packageXMLFilename = args[0];
		pkgFileName = args[1];
		unwantedLayoutNames = StringUtils.getList(args[2], ",", true, false);
		new GenerateManagedLayouts().process();
		log.info("ManagedLayouts XML generation ends");
	}
	
	private void process() throws IOException {
		ArrayList<String> layoutNames = cleanRawLayoutNames();
		generateManagedPkgLayouts(layoutNames);
	}
	
	
	/**
	 * This method takes the raw input file generated from ant target list-layouts 
	 * and prepares the data for further processing 
	 * @return listLayoutNames
	 * @throws IOException
	 */
	private ArrayList<String> cleanRawLayoutNames() throws IOException {
		ArrayList<String> listLayoutNames = new ArrayList<String>();
		try{					
			FileInputStream fstream = new FileInputStream(path + "\\"+ pkgFileName);			
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {				 								
				if (strLine.contains("FileName: layouts/GNT__") ) {
					String pattern = "(GNT__)*[a-z,A-Z,\\.,\\-,__,\\s,1-9,\\%]*(layout)$";				
					Pattern r = Pattern.compile(pattern);
					Matcher m = r.matcher(strLine);
					String regex = "__c-";
					if (m.find()) {							
						listLayoutNames.add(m.group().substring(0, m.group().length() - 7).replaceAll(regex, "__c-GNT__"));						
					}					
				}
			}			
			in.close();
		} 
		catch(FileNotFoundException e) {	
			log.error(e.getMessage());
			e.getMessage();			
		}
		
		ArrayList<String> listLayoutNamesNew = new ArrayList<String>();
		for(String layout : listLayoutNames){
			if(!layout.contains("__mdt-")){
				listLayoutNamesNew.add(layout);
			}
		}
		return listLayoutNamesNew;
	}
	
	/**
	 * 1. This method Filters unwanted Layouts as defined in the build.properties file. 
	 * Should be an exact match to filter.
	 * 2. Generates the package.xml file for consumption.
	 * @param layoutNames
	 */
	
	private void generateManagedPkgLayouts(ArrayList<String> layoutNames) {
		String managedPkgLayoutName ="";
		
		if (unwantedLayoutNames != null && unwantedLayoutNames.size() > 0) {
			layoutNames.removeAll(unwantedLayoutNames);
		}
		
		ArrayList<String> addMembers = new ArrayList<String>(); 		
		for(String name: layoutNames) {
			addMembers.add("\t\t<members>" + name + "</members>\n");			
		}		
		managedPkgLayoutName = StringUtils.getConcatenatedString(addMembers,"");
		
		String packageXMLBody = FileUtils.readFile(new File(path+packageXMLFilename), true);
		packageXMLBody = packageXMLBody.replace("{managed_roles}", managedPkgLayoutName);
		FileUtils.createFile(new File(path+"package.xml"), packageXMLBody);		
	}	
}

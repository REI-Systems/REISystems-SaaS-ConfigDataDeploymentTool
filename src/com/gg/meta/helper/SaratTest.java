package com.gg.meta.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SaratTest {

	public static void main(String[] args) throws IOException {
		File file = new File("C:\\Users\\smahavratayajula\\Desktop\\deploy7-profiles\\profiles\\CTR.Profile");
		if (file.exists()) {
			System.out.println("File exists");
			  BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\smahavratayajula\\Desktop\\deploy7-profiles\\profiles\\CTR.Profile"));
			  String data = null;  
			  for (String line; (line = br.readLine()) != null;) {
			      data  = data + line;  
				 // System.out.print(line);
			  }
			    
			System.out.println(data);
			ProfileCleanupHelper helper = new ProfileCleanupHelper(data, "CTR");
			//helper.massageUserLicense();
			System.out.println("End");
		}
		else {
			System.out.println("Not found");
		}

	}

}

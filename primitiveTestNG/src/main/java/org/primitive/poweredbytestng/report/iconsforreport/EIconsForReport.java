package org.primitive.poweredbytestng.report.iconsforreport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public enum EIconsForReport {
	ERROR("error.png"),
	FINE("fine.png"),
	SUCCESS("success.png"),
	WARNING("warning.png");
	
	private String fileName;
	
	private EIconsForReport(String fileName)
	{
		this.fileName = fileName;
	}
	
	//copy icon to some output folder 
	public String getPath(String folder)
	{
		File iconFile = new File(folder + "/" + fileName);
		if (iconFile.exists()) //if icon has been already copied
		{
			return iconFile.getAbsolutePath();
		}
		
		//if icon is not copied works code that below 
		File folderForIcons = new File(folder + "/");
		if (!folderForIcons.exists())
		{
			folderForIcons.mkdirs();
		}
		
		InputStream inputStream = getClass().getResourceAsStream(fileName);
		try {
			FileOutputStream outputStream = new FileOutputStream(iconFile);
			int data = inputStream.read();
			
			while(data != -1) {
				outputStream.write(data);
				data = inputStream.read();
	        }
			outputStream.close();
		}	
		catch (Exception e) {	
			throw new RuntimeException(e);
		}			
		return iconFile.getAbsolutePath();
	}
}

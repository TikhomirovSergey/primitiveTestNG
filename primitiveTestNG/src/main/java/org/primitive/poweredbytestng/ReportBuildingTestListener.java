package org.primitive.poweredbytestng;

import java.util.Calendar;
import java.util.logging.Level;
import org.primitive.logging.Log;
import org.primitive.logging.Photographer;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ReportBuildingTestListener implements ITestListener {
	private static final int defaultStatusOnWarning = ITestResult.SUCCESS_PERCENTAGE_FAILURE;

	
	private static ConverterToTestNGReport converter = new ConverterToTestNGReport();
	
	public void onFinish(ITestContext arg0)
	{
		Log.removeConverter(converter);
	}


	public void onStart(ITestContext arg0)
	{
		Log.addConverter(converter);
	}

	public void onTestFailedButWithinSuccessPercentage(ITestResult arg0)
	{	
		synchronizeTestResults(arg0);
	}

	public void onTestFailure(ITestResult arg0) 
	{
		Log.error("Test has failed");
	}

	public void onTestSkipped(ITestResult arg0) 
	{
		Log.warning("Is skipped");
	}

	public void onTestStart(ITestResult arg0) 
	{
		//it initiates the new result container
		ResultStore.get(arg0);
		TestResultThreadLocal.set(arg0);
		//screnshots for each test will be saved to specified directory
		String picFolder = arg0.getTestContext().getOutputDirectory();
		picFolder = picFolder + "/" + Calendar.getInstance().getTime().toString().replace(":", " ");
		Photographer.setOutputFolder(picFolder + "/");
	}

	public void onTestSuccess(ITestResult arg0) 
	{
		synchronizeTestResults(arg0);
	}
	
	//sets real status according to Log information
	protected void synchronizeTestResults(ITestResult resultForSync)
	{
		Level resultLevel = ResultStore.getLevel(resultForSync);
		if (resultLevel==Level.SEVERE)
		{
			resultForSync.setStatus(ITestResult.FAILURE);
			Log.error("Test has failed");
		}
		else if (resultLevel == Level.WARNING)
		{
			resultForSync.setStatus(defaultStatusOnWarning);
		}	
		
	}

}

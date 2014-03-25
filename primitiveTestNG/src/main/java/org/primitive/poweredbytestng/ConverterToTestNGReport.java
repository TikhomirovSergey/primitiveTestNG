/**
 * 
 */
package org.primitive.poweredbytestng;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.primitive.logging.ILogConverter;
import org.primitive.logging.Log;
import org.primitive.logging.Photographer;
import org.primitive.logging.eLogColors;
import org.primitive.logging.Log.LogRecWithAttach;
import org.primitive.poweredbytestng.report.htmlpatterns.EHtmlPatterns;
import org.primitive.poweredbytestng.report.iconsforreport.EIconsForReport;
import org.testng.Reporter;

/**
 * @author s.tihomirov It is the basic implementation of ISender for using by
 *         TESTNG framework. It posts log record in report
 */
class ConverterToTestNGReport implements ILogConverter {

	private final String debugColor = eLogColors.DEBUGCOLOR
			.getHTMLColorDescription();
	private final String errorColor = eLogColors.SEVERESTATECOLOR
			.getHTMLColorDescription();

	private final String successColor = eLogColors.CORRECTSTATECOLOR
			.getHTMLColorDescription();
	private final String warnColor = eLogColors.WARNSTATECOLOR
			.getHTMLColorDescription();

	private final String expressionOfFilePath = "#filepath";
	private final String expressionOfComment = "#Comment";

	private final String expressionOfColorPattern = "#Color";
	private final String expressionOfTimePattern = "#Time";
	private final String expressionOfMessagePattern = "#Message";

	private final String htmlPatternString = EHtmlPatterns.HTMLPATTERN
			.getHtmlCode();
	private final String htmlFileMaskString = EHtmlPatterns.FILEMASK
			.getHtmlCode();
	private final String attachedFolder = "/attached/";

	// for posting screenshots as pictures
	// private final String textPatternForPicture =
	// "<p><img src=\"file:///FilePath\" alt=\"Comment\"></p>";
	// for posting files as links
	// private final String textPatternForAnyFile =
	// "<a href= \"FilePath\" type=\"file\">Comment> </a>";
	private String formatWithStackTrace(String original, LogRecWithAttach rec) {
		String formatted = null;

		StringBuilder stackBuilder = new StringBuilder();
		stackBuilder.append(original + "\n");
		// if there is a stack trace
		if (rec.getThrown() != null) {
			StackTraceElement stack[] = rec.getThrown().getStackTrace();
			int stackLength = stack.length;

			for (int i = 0; i < stackLength; i++) {
				stackBuilder.append(stack[i].toString() + "\n");
			}

			stackBuilder.append("...");
		}
		formatted = stackBuilder.toString();
		return formatted;
	}
	
	private File makeACopy(File original){
		File dirs = new File(getOutPutDir() + attachedFolder + "/");
		dirs.mkdirs();
		File destination = new File(getOutPutDir() + attachedFolder + "/" + UUID.randomUUID().toString() + "_"
		+ original.getName());
		try {
			FileUtils.copyFile(original, destination);
			return destination;
		} catch (IOException e) {
			Log.warning("There is a problem with attaching file" + original.getName(), e);
			throw new RuntimeException(e);
		}
	}

	private String returnLogMessage(LogRecWithAttach rec) {
		String formattedMessage = null;
		File attachment = rec.getAttachedFile();
		if (attachment == null) // if there is no attached file
		{
			formattedMessage = rec.getMessage();
		} else {
			String pattern = null;
			File fileToAttach = attachment;
			pattern = htmlFileMaskString;
			if (!fileToAttach.getName().endsWith("." + Photographer.format)){ // if there is not a
															// picture				
				fileToAttach = makeACopy(fileToAttach);
			}
			formattedMessage = pattern.replace(expressionOfComment,
					rec.getMessage());
			formattedMessage = formattedMessage.replace(expressionOfFilePath,
					fileToAttach.getParentFile().getName() + "/"
							+ fileToAttach.getName());
		}
		return formatWithStackTrace(formattedMessage, rec);
	}
	
	private String getOutPutDir(){
		return TestResultThreadLocal.get().getTestContext()
				.getOutputDirectory();
	}

	private String returnHtmlString(LogRecWithAttach rec) {
		String outputDir = getOutPutDir();

		Level level = rec.getLevel();
		String color = null;
		String icon = null;
		if (level == Level.SEVERE) {
			color = errorColor;
			icon = EIconsForReport.ERROR.getPath(outputDir);
		} else if (level == Level.WARNING) {
			color = warnColor;
			icon = EIconsForReport.WARNING.getPath(outputDir);
		} else if (level == Level.INFO) {
			color = successColor;
			icon = EIconsForReport.SUCCESS.getPath(outputDir);
		} else {
			color = debugColor;
			icon = EIconsForReport.FINE.getPath(outputDir);
		}

		Date date = new Date(rec.getMillis());
		String turnedString = htmlPatternString;
		turnedString = htmlPatternString.replace(expressionOfColorPattern,
				color);
		turnedString = turnedString.replace(expressionOfFilePath, icon);
		turnedString = turnedString.replace(expressionOfTimePattern,
				date.toString());
		turnedString = turnedString.replace(expressionOfMessagePattern,
				returnLogMessage(rec));

		return turnedString;
	}

	private void setToReport(String htmlInjection) {
		Reporter.setEscapeHtml(false);
		Reporter.log(htmlInjection);
	}

	public void convert(LogRecWithAttach record) {
		setToReport(returnHtmlString(record));
		;
		ResultStore store = ResultStore.get(TestResultThreadLocal.get());
		store.addLogRecord(record);
	}
}

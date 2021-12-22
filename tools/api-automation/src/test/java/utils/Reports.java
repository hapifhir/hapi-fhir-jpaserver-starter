package utils;

import com.relevantcodes.extentreports.ExtentReports;
import config.ConfigProperties;

public class Reports {

    private static ExtentReports extent;

    public static void startReport() {
        extent = new ExtentReports(System.getProperty("user.dir") + ConfigProperties.htmlReportPath, true);
    }

    public static ExtentReports getExtentReport() {
        if (extent != null) {
            return extent;
        } else {
            throw new IllegalStateException("Extent Report object not initialized");
        }
    }
    public static String getReportConfigPath(){
        String reportConfigPath = System.getProperty("user.dir")+"/extend-config2.xml";
        if(reportConfigPath!= null) return reportConfigPath;
        else throw new RuntimeException("Report Config Path not specified in the Configuration.properties file for the Key:reportConfigPath");
    }

}

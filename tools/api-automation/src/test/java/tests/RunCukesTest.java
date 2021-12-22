package tests;

import config.ConfigProperties;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import general.APIException;
import general.SendEmailAfterExecution;
import general.TestRail;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import utils.Reports;


import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;



import static config.ConfigProperties.sendEmail;


@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/feature" },
        glue = {"stepdefs"},
        plugin = { "pretty", "html:target/cucumber" },
        tags={"@ManagingOrganization,@LocationOrganization,@HealthCareService,@Practitioner,@PractitionerRole,@Patient,@RelatedPerson,@Encounter,@Condition,@Observation,@Goal,@CareTeam,@CarePlan,@FacilityLocation,@FacilityOrganization"})

public  class RunCukesTest
{
    static Integer passedCount = 0;
    static Integer failedCount = 0;
    static Integer skippedCount = 0;
    public static Integer getPassCount()
    {
        return passedCount;
    }
    public static Integer getFailCount()
    {
        return failedCount;
    }
    public static Integer getSkippedCount()
    {
        return skippedCount;
    }
    public static void setPassCount(Integer passCount)
    {
        passedCount=passCount;
    }
    public static void setFailCount(Integer failCount)
    {
        failedCount = failCount;
    }
    public static void setSkippedCount(Integer skipCount)
    {
        skippedCount = skipCount;
    }
    public static ArrayList<String> automationSteps;
    public static ArrayList<String> expectedResults;
    @BeforeClass
    public static void  beforeClass() throws SQLException {
        if(ConfigProperties.isReportingEnable.toLowerCase().equals("true"))
            Reports.startReport();
        automationSteps = new ArrayList<String>();
        expectedResults=new ArrayList<String>();
    }

    @AfterClass
    public static void AfterClass() throws IOException, MessagingException, APIException {
        //Reporter.loadXMLConfig(new File(Reports.getReportConfigPath()));
        if(ConfigProperties.isReportingEnable.toLowerCase().equals("true")) {
            Reports.getExtentReport().flush();
            Reports.getExtentReport().close();
        }
        if(ConfigProperties.logTestRail.toLowerCase().equals("true")) {
            TestRail.createSuite();
            TestRail.updateTestRail();

        }
        if (sendEmail.toLowerCase().equals("true")) {
            SendEmailAfterExecution.sendReportAfterExecution(passedCount, failedCount, skippedCount);
        }

    }



}


package config;


import ru.qatools.properties.Property;
import ru.qatools.properties.PropertyLoader;
import ru.qatools.properties.Resource.Classpath;


@Classpath({"application.properties"})

public class ApplicationConfigReader {


    @Property("baseUrl")
    private String baseUrl;
    @Property("htmlReportPath")
    private String htmlReportPath;
    @Property("LogTestRail")
    private String LogTestRail;
    @Property("TestRailurl")
    private String TestRailurl;
    @Property("TestRailusername")
    private String TestRailusername;
    @Property("TestRailpassword")
    private String TestRailpassword;
    @Property("TestRailprojectid")
    private String TestRailprojectid;
    @Property("isEnableReporting")
    private String isEnableReporting;
    @Property("SendEmailAfterExecution")
    private String SendEmailAfterExecution;
    @Property("From")
    private String From;
    @Property("FromPassword")
    private String FromPassword;
    @Property("To")
    private String[] To;
    @Property("ProjectDescription")
    private String ProjectDescription;

    @Property("Project")
    private String Project;
    @Property("Platform")
    private String Platform;
    @Property("Environment")
    private String Environment;

    @Property("UpdateCase")
    private String UpdateCase;

    public ApplicationConfigReader() {
        PropertyLoader.newInstance().populate(this);
    }


    public String getBaseUrl() {
        return this.baseUrl;
    }

    public String getHtmlReportPath() {
        return this.htmlReportPath;
    }

    public String getLogTestRail() {
        return this.LogTestRail;
    }

    public String getTestRailurl() {
        return this.TestRailurl;
    }

    public String getTestRailusername() {
        return this.TestRailusername;
    }

    public String getTestRailpassword() {
        return this.TestRailpassword;
    }


    public String getIsEnableReporting() {
        return this.isEnableReporting;
    }


    public String getSendEmailAfterExecution() {
        return this.SendEmailAfterExecution;
    }

    public String getFrom() {
        return this.From;
    }

    public String getFromPassword() {
        return this.FromPassword;
    }

    public String[] getTo() {
        return this.To;
    }


    public String getProject() {
        return this.Project;
    }

    public String getPlatform() {
        return this.Platform;
    }

    public String getEnviroment() {
        return this.Environment;
    }

    public String getUpdateTestRail() {
        return this.UpdateCase;
    }
}
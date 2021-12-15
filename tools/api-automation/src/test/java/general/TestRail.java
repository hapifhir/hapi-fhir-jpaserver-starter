package general;

import com.google.common.base.Throwables;
import config.ConfigProperties;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.runtime.ScenarioImpl;
import org.apache.commons.lang3.reflect.FieldUtils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

public class TestRail {

    public static String TestRunName;
    public static Long suiteId;
    static JSONParser jsonParser;
    static APIClient client;

    public static void getCaseIdandResultBDD(Scenario scenario, String CaseId, Integer beforeAddingStepsLength, Integer afterAddingStepsLength, ArrayList<String> automationSteps, Integer beforeAddingExpectedResultLength, Integer afterAddingExpectedResultLength, ArrayList<String> expectedResults, String customPreconds) throws IOException, IllegalAccessException {
        if (CaseId != null && CaseId != "") {
            DataList.caseid.add(CaseId);
        }

        DataList.result.add(getCaseResultBdd(scenario));
        DataList.updateData.add(updatetestcaseBdd(scenario, beforeAddingStepsLength, afterAddingStepsLength, automationSteps, beforeAddingExpectedResultLength, afterAddingExpectedResultLength, expectedResults, customPreconds));
    }
    static {
        TestRunName = "API"+ "-Automation-Test-" + LocalDate.now();
        jsonParser = new JSONParser();
        if (!Stream.of(ConfigProperties.testRailUrl, ConfigProperties.testRailUsername, ConfigProperties.testRailPassword).anyMatch(Objects::isNull) && !Stream.of(ConfigProperties.testRailUrl, ConfigProperties.testRailUsername, ConfigProperties.testRailPassword).anyMatch((i) -> {
            return i.isEmpty();
        })) {
            try {
                client = new APIClient(ConfigProperties
                        .testRailUrl);
                client.setUser(ConfigProperties.testRailUsername);
                client.setPassword(ConfigProperties.testRailPassword);
            } catch (Exception var1) {
                System.out.println(Throwables.getStackTraceAsString(var1));
            }
        } else {
            System.out.println("Failed to read credentials for Test Rail. Please make sure the credentials are present in either config.properties or environmental variables");
        }

    }


    public static Map getCaseResultBdd(Scenario scenario) throws IllegalAccessException {
        Map data = new HashMap();
        if (scenario.getStatus() == Result.Type.PASSED) {
            data.put("status_id", 1);
        } else if (scenario.getStatus() == Result.Type.SKIPPED) {
            data.put("status_id", 3);
        } else {
            data.put("status_id", 5);
            Field field = FieldUtils.getField(((ScenarioImpl)scenario).getClass(), "stepResults", true);
            field.setAccessible(true);
            ArrayList<Result> results = (ArrayList)field.get(scenario);
            Iterator var4 = results.iterator();

            while(var4.hasNext()) {
                Result result = (Result)var4.next();
                if (result.getError() != null) {
                    data.put("comment", result.getError().toString());
                }
            }

           // data.put("defects", JiraServiceProvider.issueName);
        }

        return data;
    }

    public static Map updatetestcaseBdd(Scenario scenario, Integer beforeAddingStepsLength, Integer afterAddingStepsLength, ArrayList<String> automationSteps, Integer beforeAddingExpectedResultLength, Integer afterAddingExpectedResultLength, ArrayList<String> expectedResults, String customPreconds) {
        ArrayList<HashMap<String, String>> dataset = new ArrayList();
        Map data1 = new HashMap();

        for(int i = beforeAddingExpectedResultLength; i < afterAddingExpectedResultLength; ++i) {
            HashMap<String, String> stepData = new HashMap();
            if (automationSteps.get(i) == null) {
                stepData.put("content", "");
            } else {
                stepData.put("content", (String)automationSteps.get(i));
            }

            stepData.put("expected", (String)expectedResults.get(i));
            dataset.add(stepData);
        }

        System.out.println("\n \n" + dataset);
        data1.put("custom_steps_separated", dataset);
        data1.put("custom_description", "" + scenario.getName());
        data1.put("custom_preconds", "" + customPreconds);
        return data1;
    }


    public static void createSuite() throws IOException {
        Map data = new HashMap();
        data.put("include_all", false);
        System.out.println(DataList.caseid);
        data.put("case_ids", DataList.caseid);
        data.put("name", TestRunName);
        JSONObject c = null;
        if (client != null) {
            try {
                c = (JSONObject) client.sendPost("add_run/" + 2, data);   //2 replace by project id
                suiteId = (Long) c.get("id");
            } catch (Exception var3) {
                System.out.println(Throwables.getStackTraceAsString(var3));
            }
        }
    }
    public static void updateTestRail() throws IOException, APIException {
        if (client != null) {
            System.out.println(DataList.caseid);

            for(int i = 0; i < DataList.caseid.size(); ++i) {
                System.out.println(DataList.caseid.get(i) + "" + DataList.result.get(i));
//                if (ConfigProperties.UpdateCase.toLowerCase().equals("true")) {
//                    try {
//                        client.sendPost("update_case/" + DataList.caseid.get(i), DataList.updateData.get(i));
//                    } catch (Exception var3) {
//                        System.out.println(Throwables.getStackTraceAsString(var3));
//                    }
//                }

                try {
                    client.sendPost("add_result_for_case/" + suiteId + "/" + DataList.caseid.get(i), DataList.result.get(i));
                } catch (Exception var2) {
                    System.out.println(Throwables.getStackTraceAsString(var2));
                }
            }
        }

    }

}

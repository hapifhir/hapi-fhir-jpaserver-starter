package general;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

public class ReusableFunctions {
    public static RequestSpecification REQUEST;

    public ReusableFunctions() {
    }

    public static ArrayList responseList(String key) {
        return (ArrayList)((ValidatableResponse) general.EnvGlobals.response.then()).extract().path(key, new String[0]);
    }

    public static int getResponseLength() {
        return (Integer) general.EnvGlobals.response.body().path("list.size()", new String[0]);
    }

    public static void printResponse() {
        if (general.EnvGlobals.response != null) {
            System.out.println(general.EnvGlobals.response.getBody().asString());
        }

    }

    public static String getResponse() {
        return general.EnvGlobals.response != null ? general.EnvGlobals.response.getBody().asString() : null;
    }


    private static JSONArray sortApiResponse(JSONArray jsonArr, final String sortBy, boolean sortOrder) {
        JSONArray sortedJsonArray = new JSONArray();
        List<JSONObject> jsonValues = new ArrayList();

        for(int i = 0; i < jsonArr.length(); ++i) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }

        final Boolean SORT_ORDER = sortOrder;
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            public int compare(JSONObject a, JSONObject b) {
                Integer valA = new Integer(0);
                Integer valB = new Integer(0);

                try {
                    valA = (Integer)a.get(sortBy);
                    valB = (Integer)b.get(sortBy);
                } catch (JSONException var6) {
                }

                return SORT_ORDER ? valA.compareTo(valB) : -valA.compareTo(valB);
            }
        });

        for(int i = 0; i < jsonArr.length(); ++i) {
            sortedJsonArray.put(jsonValues.get(i));
        }

        return sortedJsonArray;
    }


    public static String getResponsePath(String key) {
        return general.EnvGlobals.response.getBody().path(key, new String[0]).toString();
    }



    public static JSONArray getResponseJson(String... params) {
        JsonPath jsonPathEvaluator = general.EnvGlobals.response.jsonPath();
        JSONArray jArray = new JSONArray();
        ArrayList<Object> list1 = (ArrayList)jsonPathEvaluator.get(params[0]);
        ArrayList<Object> list2 = (ArrayList)jsonPathEvaluator.get(params[1]);

        for(int i = 0; i < getResponseLength(); ++i) {
            JSONObject obj = new JSONObject();

            for(int j = 0; j < params.length; ++j) {
                if (j == 0) {
                    obj.put(params[j], list1.get(i));
                } else {
                    obj.put(params[j], list2.get(i));
                }
            }

            jArray.put(obj);
        }

        return sortApiResponse(jArray, "id", true);
    }

    private static void contentType(String contentType) {
        REQUEST = RestAssured.given().contentType(contentType);
    }

    public static void given() {
        contentType("application/json");
       general.EnvGlobals.requestSpecification = REQUEST.given();
    }




    public static void givenHeaders() {
        contentType("application/json");
        general.EnvGlobals.requestSpecification = REQUEST.given();
    }
    public static void givenHeaderPayload(Map<String, String> headers, String payload) {
        contentType("application/json");
        general.EnvGlobals.requestSpecification = REQUEST.given();
        if (headers == null) {
            general.EnvGlobals.requestSpecification = REQUEST.given().body(payload);
        } else if (payload == null) {
            general.EnvGlobals.requestSpecification = REQUEST.given().headers(headers);
        } else {
            general.EnvGlobals.requestSpecification = REQUEST.given().headers(headers).body(payload);
        }

    }



    public static void whenFunction(String requestType, String endPoint) {
        byte var3 = -1;
        switch(requestType.hashCode()) {
            case -1335458389:
                if (requestType.equals("delete")) {
                    var3 = 2;
                }
                break;
            case 102230:
                if (requestType.equals("get")) {
                    var3 = 1;
                }
                break;
            case 111375:
                if (requestType.equals("put")) {
                    var3 = 3;
                }
                break;
            case 3446944:
                if (requestType.equals("post")) {
                    var3 = 0;
                }
                break;
            case 106438728:
                if (requestType.equals("patch")) {
                    var3 = 4;
                }
        }

        switch(var3) {
            case 0:
               general.EnvGlobals.response = (Response)((RequestSpecification) general.EnvGlobals.requestSpecification.when().log().all()).post(endPoint, new Object[0]);
                break;
            case 1:
                general.EnvGlobals.response = (Response)((RequestSpecification) general.EnvGlobals.requestSpecification.when().log().all()).get(endPoint, new Object[0]);
                break;
            case 2:
                general.EnvGlobals.response = (Response)((RequestSpecification) general.EnvGlobals.requestSpecification.when().log().all()).delete(endPoint, new Object[0]);
                break;
            case 3:
                general.EnvGlobals.response = (Response)((RequestSpecification) general.EnvGlobals.requestSpecification.when().log().all()).put(endPoint, new Object[0]);
                break;
            case 4:
                general.EnvGlobals.response = (Response)((RequestSpecification) general.EnvGlobals.requestSpecification.when().log().all()).patch(endPoint, new Object[0]);
        }

    }

    public static void thenFunction(int statusCode) {
        ((ValidatableResponse)((ValidatableResponse) general.EnvGlobals.response.then()).log().all()).statusCode(statusCode);
    }

    public static <K, V> Map<K, V> headers(Object... keyValues) {
        Map<K, V> map = new HashMap();

        for(int index = 0; index < keyValues.length / 2; ++index) {
            map.put((K)keyValues[index * 2], (V)keyValues[index * 2 + 1]);
        }

        return map;
    }

    public static <K, V> Map<K, V> form_data(Object... keyValues) {
        Map<K, V> map = new HashMap();

        for(int index = 0; index < keyValues.length / 2; ++index) {
            map.put((K)keyValues[index * 2], (V)keyValues[index * 2 + 1]);
        }

        return map;
    }

    public static <K, V> Map<K, V> params(Object... keyValues) {
        Map<K, V> map = new HashMap();

        for(int index = 0; index < keyValues.length / 2; ++index) {
            map.put((K)keyValues[index * 2], (V)keyValues[index * 2 + 1]);
        }

        return map;
    }


    private static int WorkingDays(Date startDate, Date endDate, int SpecialHolidays) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        int workDays = 0;
        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            return 1;
        } else {
            if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
                startCal.setTime(endDate);
                endCal.setTime(startDate);
            }

            do {
                if (startCal.get(7) != 7 && startCal.get(7) != 1) {
                    ++workDays;
                }

                startCal.add(5, 1);
            } while(startCal.getTimeInMillis() <= endCal.getTimeInMillis());

            if (SpecialHolidays != 0) {
                return workDays - SpecialHolidays;
            } else {
                return workDays;
            }
        }
    }
}

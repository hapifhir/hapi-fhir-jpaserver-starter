package general;

import java.io.File;
import java.util.ArrayList;

public class DataClass {
    public String summary;
    public String Description;
    String Method;
    String methodName;
    String csvRow;
    File imageFile;
    ArrayList<String> automationSteps;
    Integer lengthBeforeTestCase;
    Integer lengthAfterTestCase;

    public DataClass() {
    }
}
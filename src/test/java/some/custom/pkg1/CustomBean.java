package some.custom.pkg1;

import org.springframework.stereotype.Component;

@Component
public class CustomBean {

	private String initFlag;

	public CustomBean() {
		initFlag = "I am alive";
	}

	public String getInitFlag() {
		return initFlag;
	}

}

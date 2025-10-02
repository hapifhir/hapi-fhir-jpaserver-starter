package ca.uhn.fhir.jpa.starter.auth.models;

public class AuthApiResponse {
	private Boolean success;
	private String message;
	private VerifyTokenResponse data;

	public AuthApiResponse(Boolean success, String message, VerifyTokenResponse data) {
		this.success = success;
		this.message = message;
		this.data = data;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public VerifyTokenResponse getData() {
		return data;
	}

	public void setData(VerifyTokenResponse data) {
		this.data = data;
	}


	public static class VerifyTokenResponse {
		private String username;

		public VerifyTokenResponse(String username) {
			this.username = username;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

	}
}
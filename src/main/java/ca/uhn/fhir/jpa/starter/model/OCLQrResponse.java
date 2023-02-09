package ca.uhn.fhir.jpa.starter.model;

public class OCLQrResponse {
	String oclId;
	byte[] qrBase64String;

	public OCLQrResponse(String oclId, byte[] qrBase64String) {
		this.oclId = oclId;
		this.qrBase64String = qrBase64String;
	}

	public String getOclId() {
		return oclId;
	}

	public void setOclId(String oclId) {
		this.oclId = oclId;
	}

	public byte[] getQrBase64String() {
		return qrBase64String;
	}

	public void setQrBase64String(byte[] qrBase64String) {
		this.qrBase64String = qrBase64String;
	}
}

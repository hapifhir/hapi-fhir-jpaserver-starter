package ca.uhn.fhir.jpa.starter.interceptors;

public enum ActionEnum {
    READ,
    CREATE,
    UPDATE,
    DELETE,
    ACCESS;

    public String getConsentAction() {
        switch (this) {
            case READ: return "read";
            case CREATE: return "collect";
            case UPDATE: return "disclose";
            case DELETE: return "delete";
            case ACCESS:
            default:
                return "access";
        }
    }
}

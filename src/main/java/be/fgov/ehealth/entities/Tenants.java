package be.fgov.ehealth.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name = "tenants")
public class Tenants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_tenant;

    private String tenant_label;
    private String tenant_description;
    private String tenant_ehbox_sender_id;

	public String getTenant_api_key() {
		return tenant_api_key;
	}

	public void setTenant_api_key(String tenant_api_key) {
		this.tenant_api_key = tenant_api_key;
	}

	private String tenant_api_key;

    public Integer getId_tenant() { return id_tenant; }

    public void setId_tenant(Integer id_tenant) {
        this.id_tenant = id_tenant;
    }

    public String getTenant_description() {
        return tenant_description;
    }

    public void setTenant_description(String tenant_description) {
        this.tenant_description = tenant_description;
    }

    public String getTenant_label() {
        return tenant_label;
    }

    public void setTenant_label(String tenant_label) {
        this.tenant_label = tenant_label;
    }

    public String getTenant_ehbox_sender_id() {
        return tenant_ehbox_sender_id;
    }

    public void setTenant_ehbox_sender_id(String tenant_ehbox_sender_id) { this.tenant_ehbox_sender_id = tenant_ehbox_sender_id; }

}


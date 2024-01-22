package ch.ahdis.matchbox.gazelle.models.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Objects;

/**
 * Service metadata used for registration in service-discovery.
 */
@JsonTypeName("service")
@JsonPropertyOrder({"name", "version", "instanceId", "replicaId", "providedInterfaces", "consumedInterfaces"})
public class Service {

	@JsonProperty(value = "name")
	private String name;

	@JsonProperty(value = "version")
	private String version;

	@JsonProperty(value = "instanceId")
	private String instanceId;

	@JsonProperty(value = "replicaId")
	private String replicaId;

	@JsonProperty(value = "providedInterfaces")
	private List<Interface> providedInterfaces;

	@JsonProperty(value = "consumedInterfaces")
	private List<Interface> consumedInterfaces;


	/**
	 * Get the unique id of this service instance (usually an OID).
	 *
	 * @return the service id.
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * Set the unique id of this service instance (usually an OID).
	 *
	 * @param instanceId the id of the service.
	 *
	 * @return the current service instance.
	 */
	public Service setInstanceId(String instanceId) {
		this.instanceId = instanceId;
		return this;
	}

	/**
	 * Get the list of interfaces provided by this service.
	 *
	 * @return the list of provided interfaces.
	 */
	public List<Interface> getProvidedInterfaces() {
		return providedInterfaces;
	}

	/**
	 * Set the list of interfaces provided by this service.
	 *
	 * @param providedInterfaces the list of provided interfaces.
	 *
	 * @return the current service instance.
	 */
	public Service setProvidedInterfaces(List<Interface> providedInterfaces) {
		this.providedInterfaces = (providedInterfaces);
		return this;
	}

	/**
	 * Get the list of interfaces consumed by this service.
	 *
	 * @return the list of consumed interfaces.
	 */
	public List<Interface> getConsumedInterfaces() {
		return consumedInterfaces;
	}

	/**
	 * Set the list of interfaces consumed by this service.
	 *
	 * @param consumedInterfaces the list of consumed interfaces.
	 *
	 * @return the current service instance.
	 */
	public Service setConsumedInterfaces(List<Interface> consumedInterfaces) {
		this.consumedInterfaces = consumedInterfaces;
		return this;
	}

	/**
	 * Get the replica id of this service instance.
	 *
	 * @return the replicaId of the service.
	 */
	public String getReplicaId() {
		return replicaId;
	}

	/**
	 * Set the replica id of this service instance.
	 *
	 * @param replicaId the replica id of the service.
	 *
	 * @return the current service instance.
	 */
	public Service setReplicaId(String replicaId) {
		this.replicaId = replicaId;
		return this;
	}

	/**
	 * Get the name of the service.
	 *
	 * @return the name of the service.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the service.
	 *
	 * @param name name of the service.
	 *
	 * @return the current service instance.
	 */
	public Service setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Get the version of the service.
	 *
	 * @return the version of the service.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Set the version of the service.
	 *
	 * @param version version number of the service (ex: 1.2.0)
	 *
	 * @return the current service instance.
	 */
	public Service setVersion(String version) {
		this.version = version;
		return this;
	}

	@JsonIgnore
	public  boolean isInstanceIdValid() {
		return instanceId == null || !instanceId.isBlank();
	}

	@JsonIgnore
	public  boolean isReplicaIdValid() {
		return replicaId == null || !replicaId.isBlank();
	}

	@JsonIgnore
	public  boolean isNameValid() {
		return name != null && !name.isBlank();
	}

	@JsonIgnore
	public  boolean isVersionValid() {
		return version != null && !version.isBlank();
	}

	@JsonIgnore
	public boolean isProvidedInterfacesValid() {
		return providedInterfaces == null || !providedInterfaces.isEmpty();
	}

	@JsonIgnore
	public boolean isConsumedInterfacesValid() {
		return consumedInterfaces == null || !consumedInterfaces.isEmpty();
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Service service)) {
			return false;
		}
		return Objects.equals(name, service.name) && Objects.equals(version, service.version);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, version);
	}
}



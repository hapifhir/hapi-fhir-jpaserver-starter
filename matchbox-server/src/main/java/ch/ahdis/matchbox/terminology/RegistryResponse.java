
package ch.ahdis.matchbox.terminology;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "formatVersion",
    "registry-url",
    "candidates"
})
public class RegistryResponse {

    @JsonProperty("formatVersion")
    private String formatVersion;
    @JsonProperty("registry-url")
    private String registryUrl;
    @JsonProperty("candidates")
    private List<Candidate> candidates;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public RegistryResponse() {
    }

    /**
     * 
     * @param registryUrl
     * @param candidates
     * @param formatVersion
     */
    public RegistryResponse(String formatVersion, String registryUrl, List<Candidate> candidates) {
        super();
        this.formatVersion = formatVersion;
        this.registryUrl = registryUrl;
        this.candidates = candidates;
    }

    @JsonProperty("formatVersion")
    public String getFormatVersion() {
        return formatVersion;
    }

    @JsonProperty("formatVersion")
    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }

    @JsonProperty("registry-url")
    public String getRegistryUrl() {
        return registryUrl;
    }

    @JsonProperty("registry-url")
    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    @JsonProperty("candidates")
    public List<Candidate> getCandidates() {
        return candidates;
    }

    @JsonProperty("candidates")
    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.registryUrl == null)? 0 :this.registryUrl.hashCode()));
        result = ((result* 31)+((this.candidates == null)? 0 :this.candidates.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.formatVersion == null)? 0 :this.formatVersion.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RegistryResponse) == false) {
            return false;
        }
        RegistryResponse rhs = ((RegistryResponse) other);
        return (((((this.registryUrl == rhs.registryUrl)||((this.registryUrl!= null)&&this.registryUrl.equals(rhs.registryUrl)))&&((this.candidates == rhs.candidates)||((this.candidates!= null)&&this.candidates.equals(rhs.candidates))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.formatVersion == rhs.formatVersion)||((this.formatVersion!= null)&&this.formatVersion.equals(rhs.formatVersion))));
    }

}

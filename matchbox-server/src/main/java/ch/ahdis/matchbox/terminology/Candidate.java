
package ch.ahdis.matchbox.terminology;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "server-name",
    "url",
    "open"
})
public class Candidate {

    @JsonProperty("server-name")
    private String serverName;
    @JsonProperty("url")
    private String url;
    @JsonProperty("open")
    private Boolean open;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Candidate() {
    }

    /**
     * 
     * @param serverName
     * @param url
     * @param open
     */
    public Candidate(String serverName, String url, Boolean open) {
        super();
        this.serverName = serverName;
        this.url = url;
        this.open = open;
    }

    @JsonProperty("server-name")
    public String getServerName() {
        return serverName;
    }

    @JsonProperty("server-name")
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("open")
    public Boolean getOpen() {
        return open;
    }

    @JsonProperty("open")
    public void setOpen(Boolean open) {
        this.open = open;
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
        result = ((result* 31)+((this.serverName == null)? 0 :this.serverName.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.url == null)? 0 :this.url.hashCode()));
        result = ((result* 31)+((this.open == null)? 0 :this.open.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Candidate) == false) {
            return false;
        }
        Candidate rhs = ((Candidate) other);
        return (((((this.serverName == rhs.serverName)||((this.serverName!= null)&&this.serverName.equals(rhs.serverName)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.url == rhs.url)||((this.url!= null)&&this.url.equals(rhs.url))))&&((this.open == rhs.open)||((this.open!= null)&&this.open.equals(rhs.open))));
    }

}

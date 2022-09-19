package ca.uhn.fhir.jpa.starter.mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;

@Component
public class ResourceMapperRegistry {
  Map<String, IResourceMapper> registry;
  
  @Autowired
  public ResourceMapperRegistry(List<IResourceMapper> mappers) {
    registry = new HashMap<>();
    mappers.forEach(mapper -> registry.put(mapper.getResourceName(), mapper));
  }

  public IResourceMapper getMapper(String resourceName) {
    if (registry.keySet().contains(resourceName)) {
      return registry.get(resourceName);
    }
    throw new InvalidRequestException(
        Msg.code(572) + "Unable to process request, this server does not know how to handle resources of type "
            + resourceName + " - Can handle: " + registry.keySet());
  }
}

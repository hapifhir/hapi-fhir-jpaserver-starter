package ch.ahdis.matchbox.modelcontextprotocol;

import java.io.IOException;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r5.utils.EOperationOutcome;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.MatchboxEngine.MatchboxEngineBuilder;

@Service
public class ValidationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidationService.class);

    private MatchboxEngine matchboxEngine;

    @Tool(name= "get_validation_result", description = "Validates the given FHIR resource against the specified FHIR profile and returns the result.")
    public String getValidationResult(Resource resource, String profile) {
        matchboxEngine = new MatchboxEngineBuilder().getEngine();
        Resource validationResult = null;
        try {
            validationResult = matchboxEngine.validate(resource, profile);
        } catch (FHIRException | IOException | EOperationOutcome e) {
            log.error(e.getMessage());
        }
        return validationResult.toString();
    }
    
}

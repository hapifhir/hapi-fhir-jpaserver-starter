package ch.ahdis.matchbox;

import org.hl7.fhir.r5.context.IWorkerContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EngineLoggingService implements IWorkerContext.ILoggingService {

  protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EngineLoggingService.class);

  private final boolean debug;

  public EngineLoggingService() {
    this(false);
  }

  @Override
  public void logMessage(String message) {
    log.info(message);
  }

  @Override
  public void logDebugMessage(LogCategory category, String message) {
    if (debug) {
        log.debug(" -" + category.name().toLowerCase() + ": " + message);
    }
  }
}

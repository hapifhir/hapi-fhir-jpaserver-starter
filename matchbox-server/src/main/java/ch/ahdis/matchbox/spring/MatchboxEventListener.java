package ch.ahdis.matchbox.spring;

import ch.ahdis.matchbox.packages.MatchboxImplementationGuideProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * A Spring event listener for Matchbox.
 *
 * @author Quentin Ligier
 **/
@Component
public class MatchboxEventListener implements ApplicationListener<ApplicationReadyEvent> {
	private static final Logger log = LoggerFactory.getLogger(MatchboxEventListener.class);

	private final MatchboxImplementationGuideProvider igProvider;

	public MatchboxEventListener(final MatchboxImplementationGuideProvider igProvider) {
		this.igProvider = requireNonNull(igProvider);
	}

	/**
	 * Handle an application event.
	 *
	 * @param ignored the event to respond to
	 */
	@Override
	public void onApplicationEvent(final ApplicationReadyEvent ignored) {
		log.debug("Loading all ImplementationGuides");
		this.igProvider.loadAll(false);
	}
}

package ca.uhn.fhir.jpa.starter.tus;

import me.desair.tus.server.exception.TusException;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface FileStrategy {
	void transferToFinalStorage(String uploadUrl) throws TusException, IOException, UnsupportedAudioFileException;
}

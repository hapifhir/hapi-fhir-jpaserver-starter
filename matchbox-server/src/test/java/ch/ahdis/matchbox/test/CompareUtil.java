package ch.ahdis.matchbox.test;

/*
 * #%L
 * Matchbox Engine
 * %%
 * Copyright (C) 2022 ahdis
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


public class CompareUtil {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompareUtil.class);

	static public void logMemory() {
		Runtime runtime = Runtime.getRuntime();
		long beforeGcTotalMemory = runtime.totalMemory() / 1024 / 1024;
		long beforeGcFreeMemory = runtime.freeMemory() / 1024 / 1024;
		long beforeGcUsedMemory = beforeGcTotalMemory - beforeGcFreeMemory;
		// Run the garbage collector
		runtime.gc();
		// Calculate the used memory
		long totalMemory = runtime.totalMemory() / 1024 / 1024;
		long freeMemory = runtime.freeMemory() / 1024 / 1024;
		long usedMemory = totalMemory - freeMemory;
		// Print the memory usage
		log.info("Total Memory: " + totalMemory + " MB" + " beforeGc: " + beforeGcTotalMemory + " MB");
		log.info("Free Memory: " + freeMemory + " MB" + " beforeGc: " + beforeGcFreeMemory + " MB");
		log.info("Used Memory: " + usedMemory + " MB" + " beforeGc: " + beforeGcUsedMemory + " MB");
	}

}

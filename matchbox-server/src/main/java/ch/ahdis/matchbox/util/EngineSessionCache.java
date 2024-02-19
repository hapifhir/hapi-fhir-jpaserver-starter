/*
* #%L
* Matchbox
* %%
* Copyright (c) 2023- by ahdis ag. All rights reserved.
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

package ch.ahdis.matchbox.util;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.hl7.fhir.validation.ValidationEngine;
import org.hl7.fhir.validation.cli.services.SessionCache;

/**
 * @author Oliver Egger
 * 
 *         We want to have a validation engines also that are not timed out as
 *         in the parent class.
 */
public class EngineSessionCache extends SessionCache {

    private final Map<String, ValidationEngine> cachedSessionsNoTimeout = new java.util.HashMap<String, ValidationEngine>();
    private final Map<ValidationEngine, String> cachedSessionIdsNoTimeout = new java.util.HashMap<ValidationEngine, String>();
    private final PassiveExpiringMap<ValidationEngine, String> cachedSessionIds;

    public EngineSessionCache() {
        cachedSessionIds = new PassiveExpiringMap<>(TIME_TO_LIVE, TIME_UNIT);
    }

    /**
     * Returns the stored {@link ValidationEngine} associated with the passed in
     * session id, if one such instance exists.
     * 
     * @param sessionId The {@link String} session id.
     * @return The {@link ValidationEngine} associated with the passed in id, or
     *         null if none exists.
     */
    @Override
    public ValidationEngine fetchSessionValidatorEngine(String sessionId) {
        if (cachedSessionsNoTimeout.containsKey(sessionId)) {
            return cachedSessionsNoTimeout.get(sessionId);
        }
        return super.fetchSessionValidatorEngine(sessionId);
    }

    /**
     * Returns the set of stored session ids.
     * 
     * @return {@link Set} of session ids.
     */
    @Override
    public Set<String> getSessionIds() {
        Set<String> mergedSet = new HashSet<String>();
        mergedSet.addAll(super.getSessionIds());
        mergedSet.addAll(cachedSessionsNoTimeout.keySet());
        return mergedSet;
    }

    /**
     * Stores the initialized {@link ValidationEngine} in the cache forever. If a
     * null key is
     * passed in, a new key is generated and returned.
     * 
     * @param sessionId        The {@link String} key to associate with this stored
     *                         {@link ValidationEngine}
     * @param validationEngine The {@link ValidationEngine} instance to cache.
     * @return The {@link String} id that will be associated with the stored
     *         {@link ValidationEngine}
     */
    public String cacheSessionForEver(String sessionId, ValidationEngine validationEngine) {
        cachedSessionsNoTimeout.put(sessionId, validationEngine);
        cachedSessionIdsNoTimeout.put(validationEngine, sessionId);
        return sessionId;
    }

    public String cacheSession(String sessionId, ValidationEngine validationEngine) {
        String id = super.cacheSession(sessionId, validationEngine);
        cachedSessionIds.put(validationEngine, id);
        return sessionId;
    }

    @Override
    public boolean sessionExists(String sessionId) {
        return super.sessionExists(sessionId) || cachedSessionsNoTimeout.containsKey(sessionId);
    }

    public String getSessionId(ValidationEngine validationEngine) {
        if (cachedSessionIdsNoTimeout.containsKey(validationEngine)) {
            return cachedSessionIdsNoTimeout.get(validationEngine);
        }
        if (cachedSessionIds.containsKey(validationEngine)) {
            return cachedSessionIds.get(validationEngine);
        }
        return null;
   }

}

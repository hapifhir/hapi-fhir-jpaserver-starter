package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.model.CacheEntity;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.iprd.fhir.utils.DateUtilityHelper;
import com.iprd.fhir.utils.Utils;
import com.iprd.report.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@Import(AppProperties.class)
@Service
public class CachingService {
	@Autowired
	AppProperties appProperties;

	NotificationDataSource notificationDataSource;

	private static final long DELAY = 3600000;

	public void cacheData(String orgId, Date date, List<IndicatorItem> indicators) {
		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (IndicatorItem item : indicators) {
			mapOfIdToMd5.put(item.getId(), Utils.md5Bytes(item.getFhirPath().getBytes(StandardCharsets.UTF_8)));
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<ScoreCardItem> data = ReportGeneratorFactory.INSTANCE.reportGenerator().getFacilityData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), indicators, Collections.emptyList());

		for (ScoreCardItem item : data) {
			List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getIndicatorId()), item.getOrgId());
			if (cacheEntities.isEmpty()) {
				CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(item.getIndicatorId()), date, Integer.valueOf(item.getValue()));
				notificationDataSource.insert(cacheEntity);
			} else {
				CacheEntity cacheEntity = cacheEntities.get(0);
				cacheEntity.setValue(Integer.valueOf(item.getValue()));
				notificationDataSource.update(cacheEntity);
			}
		}
	}


	@Scheduled(fixedDelay = 24 * DELAY, initialDelay = DELAY)
	private void cacheDailyData() {
		try {
			JsonReader reader = new JsonReader(new FileReader(appProperties.getAnc_config_file()));
			List<IndicatorItem> indicators = new Gson().fromJson(reader, new TypeToken<List<IndicatorItem>>() {
			}.getType());
			cacheData(appProperties.getCountry_org_id(), DateUtilityHelper.getCurrentSqlDate(), indicators);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}

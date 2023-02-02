package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.DashboardEnvironmentConfig;
import ca.uhn.fhir.jpa.starter.model.CacheEntity;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import com.iprd.fhir.utils.DateUtilityHelper;
import com.iprd.fhir.utils.Utils;
import com.iprd.report.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Import(AppProperties.class)
@Service
public class CachingService {
	@Autowired
	AppProperties appProperties;

	NotificationDataSource notificationDataSource;

	@Autowired
	DashboardEnvironmentConfig dashboardEnvironmentConfig;

	private static final long DELAY = 3600000;

	public void cacheData(String orgId, Date date, List<IndicatorItem> indicators) {
		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (IndicatorItem item : indicators) {
			mapOfIdToMd5.put(item.getId(), Utils.getMd5StringFromFhirPath(item.getFhirPath()));
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<ScoreCardItem> data = ReportGeneratorFactory.INSTANCE.reportGenerator().getFacilityData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), indicators, Collections.emptyList());

		for (ScoreCardItem item : data) {
			List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getIndicatorId()), item.getOrgId());
			if (cacheEntities.isEmpty()) {
				CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(item.getIndicatorId()), date, Double.valueOf(item.getValue()));
				notificationDataSource.insert(cacheEntity);
			} else {
				CacheEntity cacheEntity = cacheEntities.get(0);
				cacheEntity.setValue(Double.valueOf(item.getValue()));
				notificationDataSource.update(cacheEntity);
			}
		}
	}
	
	public void cacheDataForBarChart(String orgId, Date date, List<BarChartDefinition> barCharts) {
		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (BarChartDefinition barChart : barCharts) {
			for(BarChartItemDefinition barChartItem: barChart.getBarChartItemDefinitions()) {
				for(BarComponent barComponent:barChartItem.getBarComponentList()) {
					mapOfIdToMd5.put(String.valueOf(barChart.getId())+" "+String.valueOf(barChartItem.getId())+" "+String.valueOf(barComponent.getId()),Utils.getMd5KeyForLineCacheMd5(barComponent.getFhirPath(), barComponent.getBarChartItemId(), barChart.getId()));
				}
			}
			
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<BarChartItemDataCollection> barChartItemCollections = ReportGeneratorFactory.INSTANCE.reportGenerator().getBarChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), barCharts, Collections.emptyList());

		for (BarChartItemDataCollection barChartItemCollection : barChartItemCollections) {
			for(BarComponentData barComponent: barChartItemCollection.getBarComponentData()) {
			List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(barChartItemCollection.getChartId())+" "+String.valueOf(barChartItemCollection.getId())+" "+String.valueOf(barComponent.getId())), orgId);
			if (cacheEntities.isEmpty()) {
				CacheEntity cacheEntityForPatient = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(barChartItemCollection.getChartId())+" "+String.valueOf(barChartItemCollection.getId())+" "+String.valueOf(barComponent.getId())), date, Double.valueOf(barComponent.getValue()));
				notificationDataSource.insert(cacheEntityForPatient);
		
			} else {
				CacheEntity cacheEntityForPatient = cacheEntities.get(0);
				cacheEntityForPatient.setValue(Double.valueOf(barComponent.getValue()));
				notificationDataSource.update(cacheEntityForPatient);
				}
			}
		}
	}
	
	public void cacheTabularData(String orgId, Date date, List<TabularItem> indicators) {
		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (TabularItem item : indicators) {
			mapOfIdToMd5.put(item.getId(), Utils.getMd5StringFromFhirPath(item.getFhirPath()));
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<ScoreCardItem> data = ReportGeneratorFactory.INSTANCE.reportGenerator().getTabularData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), indicators, Collections.emptyList());

		for (ScoreCardItem item : data) {
			List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getIndicatorId()), item.getOrgId());
			if (cacheEntities.isEmpty()) {
				CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(item.getIndicatorId()), date, Double.valueOf(item.getValue()));
				notificationDataSource.insert(cacheEntity);
			} else {
				CacheEntity cacheEntity = cacheEntities.get(0);
				cacheEntity.setValue(Double.valueOf(item.getValue()));
				notificationDataSource.update(cacheEntity);
			}
		}
	}

	public void cachePieChartData(String orgId, Date date, List<PieChartDefinition> pieChartDefinitions){
		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for(PieChartDefinition pieChartDefinition : pieChartDefinitions){
			mapOfIdToMd5.put(String.valueOf(pieChartDefinition.getId()), Utils.getMd5StringFromFhirPath(pieChartDefinition.getFhirPath()));
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<PieChartItem> data = ReportGeneratorFactory.INSTANCE.reportGenerator().getPieChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), pieChartDefinitions, Collections.emptyList());

		for(PieChartItem item: data){
			List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(item.getId())), item.getOrgId());
			if(cacheEntities.isEmpty()){
				CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(String.valueOf(item.getId())), date, Double.valueOf(item.getValue()));
				notificationDataSource.insert(cacheEntity);
			} else {
				CacheEntity cacheEntity = cacheEntities.get(0);
				cacheEntity.setValue(Double.valueOf(item.getValue()));
				notificationDataSource.update(cacheEntity);
			}
		}
	}

	public void cacheDataLineChart(String orgId, Date date, List<LineChart> lineCharts) {
		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (LineChart lineChart : lineCharts) {
			for(LineChartItemDefinition lineDefinition: lineChart.getLineChartItemDefinitions()) {
				mapOfIdToMd5.put(String.valueOf(lineChart.getId())+" "+String.valueOf(lineDefinition.getId()), Utils.getMd5KeyForLineCacheMd5(lineDefinition.getFhirPath(), lineDefinition.getId(), lineChart.getId()));	
			}
		}
		
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<LineChartItemCollection> lineChartItemCollections = ReportGeneratorFactory.INSTANCE.reportGenerator().getLineChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), lineCharts, Collections.emptyList());

		for (LineChartItemCollection lineChartItemCollection : lineChartItemCollections) {
			for(LineChartItem lineChartItem: lineChartItemCollection.getValue()) {
				List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(lineChartItemCollection.getChartId())+" "+String.valueOf(lineChartItem.getLineId())), orgId);
				if (cacheEntities.isEmpty()) {
					CacheEntity cacheEntity = new CacheEntity(orgId,mapOfIdToMd5.get(String.valueOf(lineChartItemCollection.getChartId())+" "+String.valueOf(lineChartItem.getLineId())), date, Double.valueOf(lineChartItem.getValue()));
					notificationDataSource.insert(cacheEntity);
				} else {
					CacheEntity cacheEntity = cacheEntities.get(0);
					cacheEntity.setValue(Double.valueOf(lineChartItem.getValue()));
					notificationDataSource.update(cacheEntity);
				}	
			}
		}
	}
	
	@Scheduled(fixedDelay = 24 * DELAY, initialDelay = DELAY)
	private void cacheDailyData() {
		Map<String, DashboardConfigContainer> dashboardEnvToConfigMap = dashboardEnvironmentConfig.getDashboardEnvToConfigMap();
		dashboardEnvironmentConfig.getEnvToFilePathMapping().forEach((env, definitionTypeToFilePathMap) -> {
			cacheData(appProperties.getCountry_org_id(), DateUtilityHelper.getCurrentSqlDate(), dashboardEnvToConfigMap.get(env).getAnalyticsIndicatorItems());
			cacheDataForBarChart(appProperties.getCountry_org_id(), DateUtilityHelper.getCurrentSqlDate(), dashboardEnvToConfigMap.get(env).getBarChartDefinitions());
			cacheTabularData(appProperties.getCountry_org_id(), DateUtilityHelper.getCurrentSqlDate(), dashboardEnvToConfigMap.get(env).getTabularItems());
			cachePieChartData(appProperties.getCountry_org_id(), DateUtilityHelper.getCurrentSqlDate(), dashboardEnvToConfigMap.get(env).getPieChartDefinitions());
			cacheDataLineChart(appProperties.getCountry_org_id(), DateUtilityHelper.getCurrentSqlDate(), dashboardEnvToConfigMap.get(env).getLineCharts());
		});
	}
}

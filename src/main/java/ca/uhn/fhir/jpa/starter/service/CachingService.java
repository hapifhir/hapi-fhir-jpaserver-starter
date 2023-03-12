package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.DashboardEnvironmentConfig;
import ca.uhn.fhir.jpa.starter.model.CacheEntity;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import com.iprd.fhir.utils.DateUtilityHelper;
import com.iprd.fhir.utils.Operation;
import com.iprd.fhir.utils.OperationHelper;
import com.iprd.fhir.utils.Utils;
import com.iprd.report.*;
import com.iprd.report.model.data.BarChartItemDataCollection;
import com.iprd.report.model.data.BarComponentData;
import com.iprd.report.model.data.LineChartItem;
import com.iprd.report.model.data.LineChartItemCollection;
import com.iprd.report.model.data.PieChartItem;
import com.iprd.report.model.data.ScoreCardItem;
import com.iprd.report.model.definition.BarChartDefinition;
import com.iprd.report.model.definition.BarChartItemDefinition;
import com.iprd.report.model.definition.BarComponent;
import com.iprd.report.model.definition.IndicatorItem;
import com.iprd.report.model.definition.LineChart;
import com.iprd.report.model.definition.LineChartItemDefinition;
import com.iprd.report.model.definition.PieChartDefinition;
import com.iprd.report.model.definition.TabularItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Import(AppProperties.class)
@Service
public class CachingService {
	@Autowired
	AppProperties appProperties;

	NotificationDataSource notificationDataSource;

	@Autowired
	DashboardEnvironmentConfig dashboardEnvironmentConfig;
	
	private static final Logger logger = LoggerFactory.getLogger(CachingService.class);
	private static final int MAX_RETRY = 6;

	private static final long DELAY = 3600000;
	
//	@Async("asyncTaskExecutor")
	public void cacheData(String orgId, Date startDate,Date endDate, List<IndicatorItem>indicators, int count, Map<String, List<ScoreCardItem>> map,String filterString) {
//		logger.warn("-- Caching started for Score card chart data batch number "+String.valueOf(count));
		long startTime = System.nanoTime();
		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (IndicatorItem item : indicators) {
			mapOfIdToMd5.put(item.getId(),Utils.md5Bytes((item.getFhirPath()+filterString).getBytes(StandardCharsets.UTF_8)));
		}
		while(!startDate.equals(endDate)) {
			List<ScoreCardItem> data = map.get(startDate.toLocalDate().toString());
			final Date date = startDate;
			if(data!=null) {
				for (ScoreCardItem item : data) {
			    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getIndicatorId()), item.getOrgId());
			    	if (cacheEntities.isEmpty()) {
						CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(item.getIndicatorId()), date, Double.valueOf(item.getValue()),Date.valueOf(LocalDate.now()));
						notificationDataSource.insert(cacheEntity);
					} else {
						CacheEntity cacheEntity = cacheEntities.get(0);
						cacheEntity.setValue(Double.valueOf(item.getValue()));
						notificationDataSource.update(cacheEntity);
					}    
				}		
			}
			else {
				for (IndicatorItem item : indicators) {
		    		CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(item.getId()), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
					notificationDataSource.insert(cacheEntity);		    
				}
			}
			startDate = Date.valueOf(startDate.toLocalDate().plusDays(1));
		}
		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
	}
	
//	@Async("asyncTaskExecutor")
	public void cacheDataForBarChart(String orgId, Date startDate,Date endDate, List<BarChartDefinition> barCharts,int count,Map<String, List<BarChartItemDataCollection>> map,String filterString) {
//		logger.warn("-- Caching started for Bar chart data batch number "+String.valueOf(count));
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (BarChartDefinition barChart : barCharts) {
			for(BarChartItemDefinition barChartItem: barChart.getBarChartItemDefinitions()) {
				for(BarComponent barComponent:barChartItem.getBarComponentList()) {
					mapOfIdToMd5.put(String.valueOf(barChart.getId())+" "+String.valueOf(barChartItem.getId())+" "+String.valueOf(barComponent.getId()),Utils.getMd5KeyForLineCacheMd5((barComponent.getFhirPath()+filterString), barComponent.getBarChartItemId(), barChart.getId()));
				}
			}
			
		}
		while(!startDate.equals(endDate)) {
			List<BarChartItemDataCollection> data = map.get(startDate.toLocalDate().toString());
			final Date date = startDate;
			if(data!=null) {
				for (BarChartItemDataCollection barChartItemCollection : data) {
					for(BarComponentData barComponent: barChartItemCollection.getBarComponentData()) {
				    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(barChartItemCollection.getChartId())+" "+String.valueOf(barChartItemCollection.getId())+" "+String.valueOf(barComponent.getId())), orgId);
						if (cacheEntities.isEmpty()) {
							CacheEntity cacheEntityForPatient = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(barChartItemCollection.getChartId())+" "+String.valueOf(barChartItemCollection.getId())+" "+String.valueOf(barComponent.getId())), date, Double.valueOf(barComponent.getValue()),Date.valueOf(LocalDate.now()));
							notificationDataSource.insert(cacheEntityForPatient);
					
						} 
						else {
							CacheEntity cacheEntityForPatient = cacheEntities.get(0);
							cacheEntityForPatient.setValue(Double.valueOf(barComponent.getValue()));
							notificationDataSource.update(cacheEntityForPatient);
						}
					}
				}
			}
			else {
				for (BarChartDefinition item : barCharts) {
					for(BarChartItemDefinition itemDef: item.getBarChartItemDefinitions()) {
						for(BarComponent barComponent:itemDef.getBarComponentList()) {
							CacheEntity cacheEntityForPatient = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(item.getId())+" "+String.valueOf(itemDef.getId())+" "+String.valueOf(barComponent.getId())), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
							notificationDataSource.insert(cacheEntityForPatient);		    		
						}
					}
				}
			}
			startDate = Date.valueOf(startDate.toLocalDate().plusDays(1));
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
	}
	
//	@Async("asyncTaskExecutor")
	public void cacheTabularData(String orgId, Date startDate,Date endDate, List<TabularItem> indicators,int count,Map<String, List<ScoreCardItem>> map,String filterString) {
//		logger.warn("-- Caching started for Tabular data batch number "+String.valueOf(count));
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (TabularItem item : indicators) {
			mapOfIdToMd5.put(item.getId(),Utils.md5Bytes((item.getFhirPath()+filterString).getBytes(StandardCharsets.UTF_8)));
		}
		while(!startDate.equals(endDate)) {
			List<ScoreCardItem> data = map.get(startDate.toLocalDate().toString());
			final Date date = startDate;
			if(data!=null) {
				for (ScoreCardItem item : data) {
			    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getIndicatorId()), item.getOrgId());
					if (cacheEntities.isEmpty()) {
						CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(item.getIndicatorId()), date, Double.valueOf(item.getValue()),Date.valueOf(LocalDate.now()));
						notificationDataSource.insert(cacheEntity);
					} else {
						CacheEntity cacheEntity = cacheEntities.get(0);
						cacheEntity.setValue(Double.valueOf(item.getValue()));
						notificationDataSource.update(cacheEntity);
					}					
				}	
			}
			else {
				for (TabularItem item : indicators) {
					CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(item.getId()), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
					notificationDataSource.insert(cacheEntity);		    
				}
			}
			startDate = Date.valueOf(startDate.toLocalDate().plusDays(1));
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
	}

//	@Async("asyncTaskExecutor")
	public void cachePieChartData(String orgId, Date startDate,Date endDate, List<PieChartDefinition> pieChartDefinitions,int count,Map<String, List<PieChartItem>> map,String filterString){
//		logger.warn("-- Caching started for PieChart data batch number "+String.valueOf(count));
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for(PieChartDefinition pieChartDefinition : pieChartDefinitions){
			mapOfIdToMd5.put(String.valueOf(pieChartDefinition.getId()),Utils.md5Bytes((pieChartDefinition.getFhirPath()+filterString).getBytes(StandardCharsets.UTF_8)));
		}
		while(!startDate.equals(endDate)) {
			List<PieChartItem> data = map.get(startDate.toLocalDate().toString());
			final Date date = startDate;
			if(data!=null) {
				for(PieChartItem item: data){
			    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(item.getId())), item.getOrgId());
					if(cacheEntities.isEmpty()){
						CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(String.valueOf(item.getId())), date, Double.valueOf(item.getValue()),Date.valueOf(LocalDate.now()));
						notificationDataSource.insert(cacheEntity);
					} else {
						CacheEntity cacheEntity = cacheEntities.get(0);
						cacheEntity.setValue(Double.valueOf(item.getValue()));
						notificationDataSource.update(cacheEntity);
					}					
				}	
			}
			else {
				for (PieChartDefinition item : pieChartDefinitions) {
					CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(item.getId())), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
					notificationDataSource.insert(cacheEntity);		    
				}
			}
			startDate = Date.valueOf(startDate.toLocalDate().plusDays(1));
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
	}

//	@Async("asyncTaskExecutor")
	public void cacheDataLineChart(String orgId, Date startDate,Date endDate, List<LineChart> lineCharts,int count,Map<String, List<LineChartItemCollection>> map,String filterString) {
//		logger.warn("-- Caching started for Line data batch number "+String.valueOf(count));
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (LineChart lineChart : lineCharts) {
			for(LineChartItemDefinition lineDefinition: lineChart.getLineChartItemDefinitions()) {
				mapOfIdToMd5.put(String.valueOf(lineChart.getId())+" "+String.valueOf(lineDefinition.getId()), Utils.getMd5KeyForLineCacheMd5(lineDefinition.getFhirPath()+filterString, lineDefinition.getId(), lineChart.getId()));	
			}
		}
		while(!startDate.equals(endDate)) {
			List<LineChartItemCollection> data = map.get(startDate.toLocalDate().toString());
			final Date date = startDate;
			if(data!=null) {
				for (LineChartItemCollection lineChartItemCollection : data) {
					for(LineChartItem lineChartItem: lineChartItemCollection.getValue()) {
				    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(lineChartItemCollection.getChartId())+" "+String.valueOf(lineChartItem.getLineId())), orgId);
						if (cacheEntities.isEmpty()) {
							CacheEntity cacheEntity = new CacheEntity(orgId,mapOfIdToMd5.get(String.valueOf(lineChartItemCollection.getChartId())+" "+String.valueOf(lineChartItem.getLineId())), date, Double.valueOf(lineChartItem.getValue()),Date.valueOf(LocalDate.now()));
							notificationDataSource.insert(cacheEntity);
						} else {
							CacheEntity cacheEntity = cacheEntities.get(0);
							cacheEntity.setValue(Double.valueOf(lineChartItem.getValue()));
							notificationDataSource.update(cacheEntity);
						}
					}
				}	
			}
			else {
				for (LineChart item : lineCharts) {
					for(LineChartItemDefinition lineChartItemDefinition:item.getLineChartItemDefinitions()) {
						CacheEntity cacheEntity = new CacheEntity(orgId,mapOfIdToMd5.get(String.valueOf(item.getId())+" "+String.valueOf(lineChartItemDefinition.getId())), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
						notificationDataSource.insert(cacheEntity);		    	
					}
				}
			}
			startDate = Date.valueOf(startDate.toLocalDate().plusDays(1));
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
	}
	
	@Async("asyncTaskExecutor")
	public void cacheData(String orgId, Date date, List<IndicatorItem> indicators,int count,String filterString) {
//		logger.warn("-- Caching started for Score card chart data batch number "+String.valueOf(count));
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (IndicatorItem item : indicators) {
			mapOfIdToMd5.put(item.getId(),Utils.md5Bytes((item.getFhirPath()+filterString).getBytes(StandardCharsets.UTF_8)));
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<ScoreCardItem> data = ReportGeneratorFactory.INSTANCE.reportGenerator().getFacilityData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), indicators, Collections.emptyList()).get(date.toLocalDate().toString());
		if(data!=null) {
			for (ScoreCardItem item : data) {
				OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
				    @Override public void doIt() {
				    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getIndicatorId()), item.getOrgId());
						if (cacheEntities.isEmpty()) {
							CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(item.getIndicatorId()), date, Double.valueOf(item.getValue()),Date.valueOf(LocalDate.now()));
							notificationDataSource.insert(cacheEntity);
						} else {
							CacheEntity cacheEntity = cacheEntities.get(0);
							cacheEntity.setValue(Double.valueOf(item.getValue()));
							notificationDataSource.update(cacheEntity);
						}
				    }
				});	
			}	
		}else {
			for (IndicatorItem item : indicators) {
	    		CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(item.getId()), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
				notificationDataSource.insert(cacheEntity);		    
			}
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
	}
	
	@Async("asyncTaskExecutor")
	public void cacheDataForBarChart(String orgId, Date date, List<BarChartDefinition> barCharts,int count,String filterString) {
//		logger.warn("-- Caching started for Bar chart data batch number "+String.valueOf(count));
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (BarChartDefinition barChart : barCharts) {
			for(BarChartItemDefinition barChartItem: barChart.getBarChartItemDefinitions()) {
				for(BarComponent barComponent:barChartItem.getBarComponentList()) {
					mapOfIdToMd5.put(String.valueOf(barChart.getId())+" "+String.valueOf(barChartItem.getId())+" "+String.valueOf(barComponent.getId()),Utils.getMd5KeyForLineCacheMd5(barComponent.getFhirPath()+filterString, barComponent.getBarChartItemId(), barChart.getId()));
				}
			}
			
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<BarChartItemDataCollection> barChartItemCollections = ReportGeneratorFactory.INSTANCE.reportGenerator().getBarChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), barCharts, Collections.emptyList()).get(date.toLocalDate().toString());
		if(barChartItemCollections!=null) {
			for (BarChartItemDataCollection barChartItemCollection : barChartItemCollections) {
				for(BarComponentData barComponent: barChartItemCollection.getBarComponentData()) {
					OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
					    @Override public void doIt() {
					    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(barChartItemCollection.getChartId())+" "+String.valueOf(barChartItemCollection.getId())+" "+String.valueOf(barComponent.getId())), orgId);
							if (cacheEntities.isEmpty()) {
								CacheEntity cacheEntityForPatient = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(barChartItemCollection.getChartId())+" "+String.valueOf(barChartItemCollection.getId())+" "+String.valueOf(barComponent.getId())), date, Double.valueOf(barComponent.getValue()),Date.valueOf(LocalDate.now()));
								notificationDataSource.insert(cacheEntityForPatient);
						
							} else {
								CacheEntity cacheEntityForPatient = cacheEntities.get(0);
								cacheEntityForPatient.setValue(Double.valueOf(barComponent.getValue()));
								notificationDataSource.update(cacheEntityForPatient);
							}	
					    }
					});	
				}
			}	
		}else {
			for (BarChartDefinition item : barCharts) {
				for(BarChartItemDefinition itemDef: item.getBarChartItemDefinitions()) {
					for(BarComponent barComponent:itemDef.getBarComponentList()) {
						CacheEntity cacheEntityForPatient = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(item.getId())+" "+String.valueOf(itemDef.getId())+" "+String.valueOf(barComponent.getId())), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
						notificationDataSource.insert(cacheEntityForPatient);		    		
					}
				}
			}
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
	}
	
	@Async("asyncTaskExecutor")
	public void cacheTabularData(String orgId, Date date, List<TabularItem> indicators,int count,String filterString) {
//		logger.warn("-- Caching started for Tabular data batch number "+String.valueOf(count));
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (TabularItem item : indicators) {
			mapOfIdToMd5.put(item.getId(),Utils.md5Bytes((item.getFhirPath()+filterString).getBytes(StandardCharsets.UTF_8)));
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<ScoreCardItem> scoreCardItems = ReportGeneratorFactory.INSTANCE.reportGenerator().getTabularData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), indicators, Collections.emptyList()).get(date.toLocalDate().toString());
		if(scoreCardItems!=null) {
			for (ScoreCardItem item : scoreCardItems) {
				OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
				    @Override public void doIt() {
				    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getIndicatorId()), item.getOrgId());
						if (cacheEntities.isEmpty()) {
							CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(item.getIndicatorId()), date, Double.valueOf(item.getValue()),Date.valueOf(LocalDate.now()));
							notificationDataSource.insert(cacheEntity);
						} else {
							CacheEntity cacheEntity = cacheEntities.get(0);
							cacheEntity.setValue(Double.valueOf(item.getValue()));
							notificationDataSource.update(cacheEntity);
						}	
				    }
				});	
			}	
		}else {
			for (TabularItem item : indicators) {
				CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(item.getId()), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
				notificationDataSource.insert(cacheEntity);		    
			}
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
	}

	@Async("asyncTaskExecutor")
	public void cachePieChartData(String orgId, Date date, List<PieChartDefinition> pieChartDefinitions,int count,String filterString){
//		logger.warn("-- Caching started for PieChart data batch number "+String.valueOf(count));
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for(PieChartDefinition pieChartDefinition : pieChartDefinitions){
			String key = pieChartDefinition.getFhirPath() + filterString;
			mapOfIdToMd5.put(String.valueOf(pieChartDefinition.getId()),Utils.md5Bytes(key.getBytes(StandardCharsets.UTF_8)));
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<PieChartItem> pieChartItems = ReportGeneratorFactory.INSTANCE.reportGenerator().getPieChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), pieChartDefinitions, Collections.emptyList()).get(date.toLocalDate().toString());
		if(pieChartItems!=null) {
			for(PieChartItem item: pieChartItems){
				OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
				    @Override public void doIt() {
				    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(item.getId())), item.getOrgId());
						if(cacheEntities.isEmpty()){
							CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(String.valueOf(item.getId())), date, Double.valueOf(item.getValue()),Date.valueOf(LocalDate.now()));
							notificationDataSource.insert(cacheEntity);
						} else {
							CacheEntity cacheEntity = cacheEntities.get(0);
							cacheEntity.setValue(Double.valueOf(item.getValue()));
							notificationDataSource.update(cacheEntity);
						}	
				    }
				});	
			}	
		}else {
			for (PieChartDefinition item : pieChartDefinitions) {
				CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(item.getId())), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
				notificationDataSource.insert(cacheEntity);		    
			}
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
	}
	
	@Async("asyncTaskExecutor")
	public void cacheDataLineChart(String orgId, Date date, List<LineChart> lineCharts,int count,String filterString) {
//		logger.warn("-- Caching started for Line data batch number "+String.valueOf(count));
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (LineChart lineChart : lineCharts) {
			for(LineChartItemDefinition lineDefinition: lineChart.getLineChartItemDefinitions()) {
				mapOfIdToMd5.put(String.valueOf(lineChart.getId())+" "+String.valueOf(lineDefinition.getId()), Utils.getMd5KeyForLineCacheMd5(lineDefinition.getFhirPath()+filterString, lineDefinition.getId(), lineChart.getId()));	
			}
		}
		
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<LineChartItemCollection> lineChartItemCollections = ReportGeneratorFactory.INSTANCE.reportGenerator().getLineChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), lineCharts, Collections.emptyList()).get(date.toLocalDate().toString());
		if(lineChartItemCollections!=null) {
			for (LineChartItemCollection lineChartItemCollection : lineChartItemCollections) {
				for(LineChartItem lineChartItem: lineChartItemCollection.getValue()) {
					OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
					    @Override public void doIt() {
					    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(lineChartItemCollection.getChartId())+" "+String.valueOf(lineChartItem.getLineId())), orgId);
							if (cacheEntities.isEmpty()) {
								CacheEntity cacheEntity = new CacheEntity(orgId,mapOfIdToMd5.get(String.valueOf(lineChartItemCollection.getChartId())+" "+String.valueOf(lineChartItem.getLineId())), date, Double.valueOf(lineChartItem.getValue()),Date.valueOf(LocalDate.now()));
								notificationDataSource.insert(cacheEntity);
							} else {
								CacheEntity cacheEntity = cacheEntities.get(0);
								cacheEntity.setValue(Double.valueOf(lineChartItem.getValue()));
								notificationDataSource.update(cacheEntity);
							}	
					    }
					});		
				}
			}	
		}
		else {
			for (LineChart item : lineCharts) {
				for(LineChartItemDefinition lineChartItemDefinition:item.getLineChartItemDefinitions()) {
					CacheEntity cacheEntity = new CacheEntity(orgId,mapOfIdToMd5.get(String.valueOf(item.getId())+" "+String.valueOf(lineChartItemDefinition.getId())), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
					notificationDataSource.insert(cacheEntity);		    	
				}
			}
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
	}
}

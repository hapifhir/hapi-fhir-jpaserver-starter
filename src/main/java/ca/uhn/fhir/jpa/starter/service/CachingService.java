package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.DashboardEnvironmentConfig;
import ca.uhn.fhir.jpa.starter.model.CacheEntity;
import ca.uhn.fhir.jpa.starter.model.MapCacheEntity;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import com.google.common.collect.Lists;
import com.google.openlocationcode.OpenLocationCode;
import com.iprd.fhir.utils.Operation;
import com.iprd.fhir.utils.OperationHelper;
import com.iprd.fhir.utils.Utils;
import com.iprd.report.*;
import com.iprd.report.model.data.*;
import com.iprd.report.model.definition.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Import(AppProperties.class)
@Service
public class CachingService {
	@Autowired
	AppProperties appProperties;

	NotificationDataSource notificationDataSource;

	@Autowired
	DashboardEnvironmentConfig dashboardEnvironmentConfig;
	@Autowired
	FhirClientAuthenticatorService fhirClientAuthenticatorService;
	
	private static final Logger logger = LoggerFactory.getLogger(CachingService.class);
	private static final int MAX_RETRY = 6;

	private static final long DELAY = 3600000;
	
	public void cacheData(String orgId, Date startDate, List<IndicatorItem>indicators, int count, Map<String, List<ScoreCardItem>> map,String filterString) {
	   long start = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (IndicatorItem item : indicators) {
			mapOfIdToMd5.put(item.getId(),Utils.md5Bytes((item.getFhirPath().getExpression()+item.getId()+filterString).getBytes(StandardCharsets.UTF_8)));
		}
			List<ScoreCardItem> data = map.get(startDate.toLocalDate().toString());
			final Date date = (Date) startDate.clone();
			if(data!=null) {
				ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
				ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<CacheEntity>();
				 data.forEach(item -> {
					OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
					    @Override public void doIt() {
							List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getIndicatorId()), item.getOrgId());
					    	if (cacheEntities.isEmpty()) {
								CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(item.getIndicatorId()), date, Double.valueOf(item.getValue()),Date.valueOf(LocalDate.now()));
								cacheEntitiesForInsert.add(cacheEntity);
							} else {
								CacheEntity cacheEntity = cacheEntities.get(0);
								cacheEntity.setValue(Double.valueOf(item.getValue()));
								cacheEntitiesForUpdate.add(cacheEntity);
							}   
					    }
					});
				});		
				   notificationDataSource.insertObjects(cacheEntitiesForInsert);
				   notificationDataSource.updateObjects(cacheEntitiesForUpdate);
			}
			else {
				ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
				indicators.forEach(item -> {
					OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
					    @Override public void doIt() {
					    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getId()), orgId);
					    	if (cacheEntities.isEmpty()) {
								CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(item.getId()), date, Double.valueOf(-1),Date.valueOf(LocalDate.now()));
								cacheEntitiesForInsert.add(cacheEntity);
						}
					  }
					});
					
				});
				notificationDataSource.insertObjects(cacheEntitiesForInsert);
			}
		   long end = System.nanoTime();
		   double diff = (end-start);
//		   logger.warn("CACHE DATA "+String.valueOf(diff));
	}

	public void cacheDataForBarChart(String orgId, Date startDate, List<BarChartDefinition> barCharts,int count,Map<String, List<BarChartItemDataCollection>> map,String filterString) {
		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (BarChartDefinition barChart : barCharts) {
			for(BarChartItemDefinition barChartItem: barChart.getBarChartItemDefinitions()) {
				for(BarComponent barComponent:barChartItem.getBarComponentList()) {
					mapOfIdToMd5.put(String.valueOf(barChart.getId())+" "+String.valueOf(barChartItem.getId())+" "+String.valueOf(barComponent.getId()),Utils.getMd5KeyForLineCacheMd5WithCategory((barComponent.getFhirPath().getExpression()+filterString), barComponent.getBarChartItemId(), barChart.getId(),barChart.getCategoryId()));
				}
			}

		}

			List<BarChartItemDataCollection> data = map.get(startDate.toLocalDate().toString());
			final Date date = (Date) startDate.clone();
			if (data != null) {
				ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
				ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<CacheEntity>();
			    data.forEach(barChartItemCollection -> {
			        barChartItemCollection.getData().forEach(barComponentCategory -> {
						  barComponentCategory.getBarComponentData().stream().forEach(barComponent -> {
							  OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
								  @Override
								  public void doIt() {
									  List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(barChartItemCollection.getChartId()) + " " + String.valueOf(barComponentCategory.getId()) + " " + String.valueOf(barComponent.getId())), orgId);
									  if (cacheEntities.isEmpty()) {
										  CacheEntity cacheEntityForPatient = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(barChartItemCollection.getChartId()) + " " + String.valueOf(barComponentCategory.getId()) + " " + String.valueOf(barComponent.getId())), date, Double.valueOf(barComponent.getValue()), Date.valueOf(LocalDate.now()));
										  cacheEntitiesForInsert.add(cacheEntityForPatient);

									  } else {
										  CacheEntity cacheEntityForPatient = cacheEntities.get(0);
										  cacheEntityForPatient.setValue(Double.valueOf(barComponent.getValue()));
										  cacheEntitiesForUpdate.add(cacheEntityForPatient);
									  }
								  }
							  });
						  });
					  });
			    });
		    	notificationDataSource.insertObjects(cacheEntitiesForInsert);
		    	notificationDataSource.updateObjects(cacheEntitiesForUpdate);
			} else {
				ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			    barCharts.forEach(chart -> {
			        chart.getBarChartItemDefinitions().forEach(itemDef -> {
			            itemDef.getBarComponentList().forEach(barComponent -> {
			                OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
			                    @Override
			                    public void doIt() {
			                    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(chart.getId()) + " " + String.valueOf(itemDef.getId()) + " " + String.valueOf(barComponent.getId())), orgId);
				                    if (cacheEntities.isEmpty()) {
				                        CacheEntity cacheEntityForPatient = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(chart.getId()) + " " + String.valueOf(itemDef.getId()) + " " + String.valueOf(barComponent.getId())), date, Double.valueOf(0), Date.valueOf(LocalDate.now()));
				                        cacheEntitiesForInsert.add(cacheEntityForPatient);
				                    }
			                    }
			                });
			            });
			        });
			    });
                notificationDataSource.insertObjects(cacheEntitiesForInsert);
			}

	}

	public void cacheTabularData(String orgId, Date startDate, List<TabularItem> indicators,int count,Map<String, List<ScoreCardItem>> map,String filterString) {

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (TabularItem item : indicators) {
			mapOfIdToMd5.put(item.getId(),Utils.md5Bytes((item.getFhirPath().getExpression()+filterString).getBytes(StandardCharsets.UTF_8)));
		}
		
			List<ScoreCardItem> data = map.get(startDate.toLocalDate().toString());
			final Date date = (Date) startDate.clone();
			if(data!=null) {
				ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
				ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<CacheEntity>();
				 data.forEach(item -> {
					OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
					    @Override public void doIt() {
					List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getIndicatorId()), item.getOrgId());
					if (cacheEntities.isEmpty()) {
						CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(item.getIndicatorId()), date, Double.valueOf(item.getValue()),Date.valueOf(LocalDate.now()));
						cacheEntitiesForInsert.add(cacheEntity);
					} else {
						CacheEntity cacheEntity = cacheEntities.get(0);
						cacheEntity.setValue(Double.valueOf(item.getValue()));
						cacheEntitiesForUpdate.add(cacheEntity);
					}
					    }
					});
				});	
				 notificationDataSource.insertObjects(cacheEntitiesForInsert);
			    	notificationDataSource.updateObjects(cacheEntitiesForUpdate);
			}
			else {
				ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
				indicators.forEach(item -> {
					OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
					    @Override public void doIt() {
					    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getId()),orgId);
							if (cacheEntities.isEmpty()) {
								CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(item.getId()), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
								cacheEntitiesForInsert.add(cacheEntity);
							}
					    }
					});
				});
                notificationDataSource.insertObjects(cacheEntitiesForInsert);
			}

	}

	public void cachePieChartData(String orgId, Date startDate, List<PieChartDefinition> pieChartDefinitions,int count,Map<String, List<PieChartItemDataCollection>> map,String filterString){

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();

		for(PieChartDefinition pieChartDefinition : pieChartDefinitions) {
			for (PieChartCategoryDefinition pieChartItem : pieChartDefinition.getItem()) {
				mapOfIdToMd5.put(String.valueOf(pieChartItem.getId()) + pieChartDefinition.getCategoryId(), Utils.md5Bytes((pieChartItem.getFhirPath().getExpression() + filterString + pieChartDefinition.getCategoryId()).getBytes(StandardCharsets.UTF_8)));
			}
		}

		List<PieChartItemDataCollection> data = map.get(startDate.toLocalDate().toString());
		final Date date = (Date) startDate.clone();
		if(data!=null) {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<CacheEntity>();
			 data.forEach(item -> {
					 item.getData().forEach(pieChartItem -> {
						 OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
							 @Override
							 public void doIt() {
								 List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(pieChartItem.getId()) + item.getCategoryId()), pieChartItem.getOrgId());
								 if (cacheEntities.isEmpty()) {
									 CacheEntity cacheEntity = new CacheEntity(pieChartItem.getOrgId(), mapOfIdToMd5.get(String.valueOf(pieChartItem.getId()) + item.getCategoryId()), date, Double.valueOf(pieChartItem.getValue()), Date.valueOf(LocalDate.now()));
									 cacheEntitiesForInsert.add(cacheEntity);

								 } else {
									 CacheEntity cacheEntity = cacheEntities.get(0);
									 cacheEntity.setValue(Double.valueOf(pieChartItem.getValue()));
									 cacheEntitiesForUpdate.add(cacheEntity);
								 }
							 }
						 });
					 });
				 });
			 notificationDataSource.insertObjects(cacheEntitiesForInsert);
			 notificationDataSource.updateObjects(cacheEntitiesForUpdate);
		}
		else {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			pieChartDefinitions.forEach(pieChartDefinition -> {
				pieChartDefinition.getItem().forEach( item -> {
				OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
				    @Override public void doIt() {
				    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(item.getId()) + pieChartDefinition.getCategoryId()), orgId);
						if(cacheEntities.isEmpty()){
							CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(item.getId()) + pieChartDefinition.getCategoryId()), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
							cacheEntitiesForInsert.add(cacheEntity);
						}
				    }
				});
			});
				});
            notificationDataSource.insertObjects(cacheEntitiesForInsert);
		}
	}

	public void cacheDataLineChart(String orgId, Date startDate, List<LineChart> lineCharts,int count,Map<String, List<LineChartItemCollection>> map,String filterString) {

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (LineChart lineChart : lineCharts) {
			for(LineChartItemDefinition lineDefinition: lineChart.getLineChartItemDefinitions()) {
				mapOfIdToMd5.put(String.valueOf(lineChart.getId())+" "+String.valueOf(lineDefinition.getId()), Utils.getMd5KeyForLineCacheMd5WithCategory(lineDefinition.getFhirPath().getExpression()+filterString, lineDefinition.getId(), lineChart.getId(),lineChart.getCategoryId()));
			}
		}

		List<LineChartItemCollection> data = map.get(startDate.toLocalDate().toString());
		final Date date = (Date) startDate.clone();
		if (data != null) {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<CacheEntity>();
		    data.forEach(lineChartItemCollection -> {
		        lineChartItemCollection.getValue().forEach(lineChartItem -> {
		            OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
		                @Override
		                public void doIt() {
		                    List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(lineChartItemCollection.getChartId()) + " " + String.valueOf(lineChartItem.getLineId())), orgId);
		                    if (cacheEntities.isEmpty()) {
		                        CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(lineChartItemCollection.getChartId()) + " " + String.valueOf(lineChartItem.getLineId())), date, Double.valueOf(lineChartItem.getValue()), Date.valueOf(LocalDate.now()));
		    					cacheEntitiesForInsert.add(cacheEntity);

		                    } else {
		                        CacheEntity cacheEntity = cacheEntities.get(0);
		                        cacheEntity.setValue(Double.valueOf(lineChartItem.getValue()));
		                        cacheEntitiesForUpdate.add(cacheEntity);
		                    }
		                }
		            });
		        });
		    });
		    notificationDataSource.insertObjects(cacheEntitiesForInsert);
			notificationDataSource.updateObjects(cacheEntitiesForUpdate);
		} else {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
		    lineCharts.forEach(chart -> {
		        chart.getLineChartItemDefinitions().forEach(lineChartItemDefinition -> {
		            OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
		                @Override
		                public void doIt() {
		                	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(chart.getId()) + " " + String.valueOf(lineChartItemDefinition.getId())), orgId);
		                    if (cacheEntities.isEmpty()) {
			                    CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(chart.getId()) + " " + String.valueOf(lineChartItemDefinition.getId())), date, Double.valueOf(0), Date.valueOf(LocalDate.now()));
			                    cacheEntitiesForInsert.add(cacheEntity);
		                    }
		                }
		            });
		        });
		    });
            notificationDataSource.insertObjects(cacheEntitiesForInsert);
		}
	}
	
	@Async("asyncTaskExecutor")
	public void cacheData(String orgId, Date date, List<IndicatorItem> indicators,String filterString) {
//		logger.warn("-- Caching started for Score card chart data batch number "+String.valueOf(count));
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (IndicatorItem item : indicators) {
			mapOfIdToMd5.put(item.getId(),Utils.md5Bytes((item.getFhirPath().getExpression()+item.getId()+filterString).getBytes(StandardCharsets.UTF_8)));
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) fhirClientAuthenticatorService.getFhirClient());
		List<ScoreCardItem> data = ReportGeneratorFactory.INSTANCE.reportGenerator().getFacilityData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), indicators, Collections.emptyList()).get(date.toLocalDate().toString());
		if(data!=null) {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<CacheEntity>();
			for (ScoreCardItem item : data) {
				OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
				    @Override public void doIt() {
				    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getIndicatorId()), item.getOrgId());
						if (cacheEntities.isEmpty()) {
							CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(item.getIndicatorId()), date, Double.valueOf(item.getValue()),Date.valueOf(LocalDate.now()));
							cacheEntitiesForInsert.add(cacheEntity);
						} else {
							CacheEntity cacheEntity = cacheEntities.get(0);
							cacheEntity.setValue(Double.valueOf(item.getValue()));
							cacheEntitiesForUpdate.add(cacheEntity);
						}
				    }
				});	
			}	
			 	notificationDataSource.insertObjects(cacheEntitiesForInsert);
				notificationDataSource.updateObjects(cacheEntitiesForUpdate);
		}else {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			for (IndicatorItem item : indicators) {
				OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
				    @Override public void doIt() {
				    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getId()), orgId);
				    	if (cacheEntities.isEmpty()) {
							CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(item.getId()), date, Double.valueOf(-1),Date.valueOf(LocalDate.now()));
							cacheEntitiesForInsert.add(cacheEntity);
						}    
				    }
				});	
			}
            notificationDataSource.insertObjects(cacheEntitiesForInsert);
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
					mapOfIdToMd5.put(String.valueOf(barChart.getId())+" "+String.valueOf(barChartItem.getId())+" "+String.valueOf(barComponent.getId()),Utils.getMd5KeyForLineCacheMd5WithCategory(barComponent.getFhirPath().getExpression()+filterString, barComponent.getBarChartItemId(), barChart.getId(),barChart.getCategoryId()));
				}
			}

		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) fhirClientAuthenticatorService.getFhirClient());
		List<BarChartItemDataCollection> barChartItemCollections = ReportGeneratorFactory.INSTANCE.reportGenerator().getBarChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), barCharts, Collections.emptyList()).get(date.toLocalDate().toString());
		if(barChartItemCollections!=null) {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<CacheEntity>();
			for (BarChartItemDataCollection barChartItemCollection : barChartItemCollections) {
				for(BarComponentCategory barComponentCategory: barChartItemCollection.getData()) {
					for(BarComponentData barComponent: barComponentCategory.getBarComponentData())
					OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
						@Override public void doIt() {
							List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(barChartItemCollection.getChartId())+" "+String.valueOf(barComponentCategory.getId())+" "+String.valueOf(barComponent.getId())), orgId);
							if (cacheEntities.isEmpty()) {
								CacheEntity cacheEntityForPatient = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(barChartItemCollection.getChartId())+" "+String.valueOf(barComponentCategory.getId())+" "+String.valueOf(barComponent.getId())), date, Double.valueOf(barComponent.getValue()),Date.valueOf(LocalDate.now()));
								cacheEntitiesForInsert.add(cacheEntityForPatient);

							} else {
								CacheEntity cacheEntityForPatient = cacheEntities.get(0);
								cacheEntityForPatient.setValue(Double.valueOf(barComponent.getValue()));
								cacheEntitiesForUpdate.add(cacheEntityForPatient);
							}
						}
					});
				}
			}
			notificationDataSource.insertObjects(cacheEntitiesForInsert);
			notificationDataSource.updateObjects(cacheEntitiesForUpdate);
		}else {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			for (BarChartDefinition item : barCharts) {
				for(BarChartItemDefinition itemDef: item.getBarChartItemDefinitions()) {
					for(BarComponent barComponent:itemDef.getBarComponentList()) {
						List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(item.getId()) + " " + String.valueOf(itemDef.getId()) + " " + String.valueOf(barComponent.getId())), orgId);
						if (cacheEntities.isEmpty()) {
							CacheEntity cacheEntityForPatient = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(item.getId()) + " " + String.valueOf(itemDef.getId()) + " " + String.valueOf(barComponent.getId())), date, Double.valueOf(0), Date.valueOf(LocalDate.now()));
							cacheEntitiesForInsert.add(cacheEntityForPatient);
						}
					}
				}
			}
			notificationDataSource.insertObjects(cacheEntitiesForInsert);
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
			mapOfIdToMd5.put(item.getId(),Utils.md5Bytes((item.getFhirPath().getExpression()+filterString).getBytes(StandardCharsets.UTF_8)));
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) fhirClientAuthenticatorService.getFhirClient());
		List<ScoreCardItem> scoreCardItems = ReportGeneratorFactory.INSTANCE.reportGenerator().getTabularData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), indicators, Collections.emptyList()).get(date.toLocalDate().toString());
		if(scoreCardItems!=null) {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<CacheEntity>();
			for (ScoreCardItem item : scoreCardItems) {
				OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
				    @Override public void doIt() {
				    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getIndicatorId()), item.getOrgId());
						if (cacheEntities.isEmpty()) {
							CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(item.getIndicatorId()), date, Double.valueOf(item.getValue()),Date.valueOf(LocalDate.now()));
							cacheEntitiesForInsert.add(cacheEntity);
						} else {
							CacheEntity cacheEntity = cacheEntities.get(0);
							cacheEntity.setValue(Double.valueOf(item.getValue()));
							cacheEntitiesForUpdate.add(cacheEntity);
						}	
				    }
				});	
			}
			notificationDataSource.insertObjects(cacheEntitiesForInsert);
			notificationDataSource.updateObjects(cacheEntitiesForUpdate);
		}else {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			for (TabularItem item : indicators) {
				List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(item.getId()),orgId);
				if (cacheEntities.isEmpty()) {
					CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(item.getId()), date, Double.valueOf(0),Date.valueOf(LocalDate.now()));
					cacheEntitiesForInsert.add(cacheEntity);
				}    
			}
			notificationDataSource.insertObjects(cacheEntitiesForInsert);
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

		for (PieChartDefinition pieChartDefinition : pieChartDefinitions) {
			if (pieChartDefinition != null) {
				for (PieChartCategoryDefinition pieChartItem : pieChartDefinition.getItem()) {
					String key = pieChartItem.getFhirPath().getExpression() + filterString + pieChartDefinition.getCategoryId();
					mapOfIdToMd5.put(String.valueOf(pieChartItem.getId()) + pieChartDefinition.getCategoryId(), Utils.md5Bytes(key.getBytes(StandardCharsets.UTF_8)));
				}
			}
		}

		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) fhirClientAuthenticatorService.getFhirClient());
		List<PieChartItemDataCollection> pieChartItems = ReportGeneratorFactory.INSTANCE.reportGenerator().getPieChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), pieChartDefinitions, Collections.emptyList()).get(date.toLocalDate().toString());
		if(pieChartItems!=null) {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<CacheEntity>();
			for (PieChartItemDataCollection pieChartItem: pieChartItems) {
				for (PieChartItem item : pieChartItem.getData()) {
					OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
						@Override
						public void doIt() {
							List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(item.getId()) + pieChartItem.getCategoryId()), item.getOrgId());
							if (cacheEntities.isEmpty()) {
								CacheEntity cacheEntity = new CacheEntity(item.getOrgId(), mapOfIdToMd5.get(String.valueOf(item.getId()) + pieChartItem.getCategoryId()), date, Double.valueOf(item.getValue()), Date.valueOf(LocalDate.now()));
								cacheEntitiesForInsert.add(cacheEntity);
							} else {
								CacheEntity cacheEntity = cacheEntities.get(0);
								cacheEntity.setValue(Double.valueOf(item.getValue()));
								cacheEntitiesForUpdate.add(cacheEntity);
							}
						}
					});
				}
			}
			notificationDataSource.insertObjects(cacheEntitiesForInsert);
			notificationDataSource.updateObjects(cacheEntitiesForUpdate);
		}else {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			for (PieChartDefinition pieChartDefinition : pieChartDefinitions) {
				for (PieChartCategoryDefinition item : pieChartDefinition.getItem()) {
					List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(item.getId()) + pieChartDefinition.getCategoryId()), orgId);
					if (cacheEntities.isEmpty()) {
						CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(item.getId()) + pieChartDefinition.getCategoryId()), date, Double.valueOf(0), Date.valueOf(LocalDate.now()));
						cacheEntitiesForInsert.add(cacheEntity);
					}
				}
			}
			notificationDataSource.insertObjects(cacheEntitiesForInsert);
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
				mapOfIdToMd5.put(String.valueOf(lineChart.getId())+" "+String.valueOf(lineDefinition.getId()), Utils.getMd5KeyForLineCacheMd5WithCategory(lineDefinition.getFhirPath().getExpression()+filterString, lineDefinition.getId(), lineChart.getId(),lineChart.getCategoryId()));
			}
		}
		
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) fhirClientAuthenticatorService.getFhirClient());
		List<LineChartItemCollection> lineChartItemCollections = ReportGeneratorFactory.INSTANCE.reportGenerator().getLineChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), lineCharts, Collections.emptyList()).get(date.toLocalDate().toString());
		if(lineChartItemCollections!=null) {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<CacheEntity>();
			for (LineChartItemCollection lineChartItemCollection : lineChartItemCollections) {
				for(LineChartItem lineChartItem: lineChartItemCollection.getValue()) {
					OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
					    @Override public void doIt() {
					    	List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(lineChartItemCollection.getChartId())+" "+String.valueOf(lineChartItem.getLineId())), orgId);
							if (cacheEntities.isEmpty()) {
								CacheEntity cacheEntity = new CacheEntity(orgId,mapOfIdToMd5.get(String.valueOf(lineChartItemCollection.getChartId())+" "+String.valueOf(lineChartItem.getLineId())), date, Double.valueOf(lineChartItem.getValue()),Date.valueOf(LocalDate.now()));
								cacheEntitiesForInsert.add(cacheEntity);
							} else {
								CacheEntity cacheEntity = cacheEntities.get(0);
								cacheEntity.setValue(Double.valueOf(lineChartItem.getValue()));
								cacheEntitiesForUpdate.add(cacheEntity);
							}	
					    }
					});		
				}
			}
			notificationDataSource.insertObjects(cacheEntitiesForInsert);
			notificationDataSource.updateObjects(cacheEntitiesForUpdate);
		}
		else {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<CacheEntity>();
			for (LineChart item : lineCharts) {
				for(LineChartItemDefinition lineChartItemDefinition:item.getLineChartItemDefinitions()) {
		            List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(String.valueOf(item.getId()) + " " + String.valueOf(lineChartItemDefinition.getId())), orgId);
                    if (cacheEntities.isEmpty()) {
	                    CacheEntity cacheEntity = new CacheEntity(orgId, mapOfIdToMd5.get(String.valueOf(item.getId()) + " " + String.valueOf(lineChartItemDefinition.getId())), date, Double.valueOf(0), Date.valueOf(LocalDate.now()));
	                    cacheEntitiesForInsert.add(cacheEntity);
                    }  	
				}
			}
			notificationDataSource.insertObjects(cacheEntitiesForInsert);
		}
	}

	public void cacheMapData (Date date, Set<Map.Entry<String, Map<String, PositionData>>> dateResponseMap, ArrayList<MapCacheEntity> resultToCache, ArrayList<MapCacheEntity> resultToUpdateCache) {
		logger.warn("-- Adding items to insert or update map cache for "+date);
		for (Map.Entry<String, Map<String, PositionData>> entry: dateResponseMap){
			String orgId = entry.getKey().substring(13);
			for (Map.Entry<String, PositionData> positionDataEntry : entry.getValue().entrySet()) {
				Double lat = positionDataEntry.getValue().getLat();
				Double lng = positionDataEntry.getValue().getLng();
				String plusCode = OpenLocationCode.encode(lat, lng);
				String locId = positionDataEntry.getKey();
				Map<String, Integer> categoryResponseMap = positionDataEntry.getValue().getCategoryResponse();
				for (Map.Entry<String, Integer> categoryEntry: categoryResponseMap.entrySet()){
					String categoryId = categoryEntry.getKey();
					Integer weight = categoryEntry.getValue();
					String idForMapCache = date.toString() + orgId + locId + categoryId;
					List<MapCacheEntity> existingMapCache = notificationDataSource.getMapCacheByKeyId(idForMapCache);
					if(existingMapCache.isEmpty()){
						MapCacheEntity mapCache = new MapCacheEntity(idForMapCache, orgId, date, categoryId, lat, lng, plusCode, weight, Date.valueOf(LocalDate.now()));
						if(!resultToCache.contains(mapCache)){
							resultToCache.add(mapCache);
						}
					}else{
						MapCacheEntity existingMapCacheEntity =  existingMapCache.get(0);
						existingMapCacheEntity.setWeight(weight);
						if(!resultToUpdateCache.contains(existingMapCacheEntity)){
							resultToUpdateCache.add(existingMapCacheEntity);
						}
					}
				}
			}
		}
		logger.warn("-- Caching started for Map Data");
		notificationDataSource.insertObjects(resultToCache);
		notificationDataSource.updateObjects(resultToUpdateCache);
	}

	public void processMapDataForCache(List<String> orgIdList, String startDate, String endDate, ArrayList<MapCacheEntity> resultToCache, ArrayList<MapCacheEntity> resultToUpdateCache, List<MapCodes> mapCodes){
		notificationDataSource = NotificationDataSource.getInstance();
		List<List<String>> orgIDListBatch = Lists.partition(orgIdList, 50);
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) FhirClientAuthenticatorService.getFhirClient());
		List<DateWiseMapData> dateWiseMapDataList = new ArrayList<DateWiseMapData>();
		for (List<String> orgIds: orgIDListBatch){
			dateWiseMapDataList.addAll(ReportGeneratorFactory.INSTANCE.reportGenerator().getMapData(fhirClientProvider, String.join(",", orgIds), new DateRange(startDate, endDate), mapCodes));
		}
		for(DateWiseMapData item: dateWiseMapDataList){
			resultToCache.clear();
			resultToUpdateCache.clear();
			Date date = Date.valueOf(LocalDate.parse(item.getDateKey(), DateTimeFormatter.ISO_DATE));
			if (item.getDateResponse().size() != 0){
				Set<Map.Entry<String, Map<String, PositionData>>> dateResponseMap = item.getDateResponse().entrySet();
				cacheMapData(date, dateResponseMap, resultToCache, resultToUpdateCache);
			}
		}
	}

	//Don't remove this function. This may be utilised later
	/*@Async("asyncTaskExecutor")
	public void performCachingForMapDataIfRequired(List<String> allClinics, String from, String to) {
		List<String> clinicsToCache = new ArrayList<>();
		Date startDateForCaching = Date.valueOf(LocalDate.parse(to, DateTimeFormatter.ISO_DATE));
		Date endDateForCaching = Date.valueOf(LocalDate.parse(from, DateTimeFormatter.ISO_DATE));

		List<String> mapDataCacheKey = Utils.getKeysForMapCache(allClinics, from, to);
		notificationDataSource = NotificationDataSource.getInstance();
		List<MapCacheEntity> result = notificationDataSource.getMapCacheByKeyIdList(mapDataCacheKey);
		if(result.size() != mapDataCacheKey.size()){
			for(String key: mapDataCacheKey){
				List<MapCacheEntity> existingMapCache = notificationDataSource.getMapCacheByKeyId(key);
				if(existingMapCache.isEmpty()){
					String[] parametersOfCache = key.split("_");
					Date dateToCache = Date.valueOf(LocalDate.parse(parametersOfCache[0], DateTimeFormatter.ISO_DATE));
					clinicsToCache.add(parametersOfCache[1]);
					if(dateToCache.before(startDateForCaching)){
						startDateForCaching = dateToCache;
					}
					if (dateToCache.after(endDateForCaching)){
						endDateForCaching = dateToCache;
					}
				}
			}
			if (!clinicsToCache.isEmpty()){
				cacheMapData(clinicsToCache, startDateForCaching.toString(), endDateForCaching.toString());
			}
		}
	}*/
}

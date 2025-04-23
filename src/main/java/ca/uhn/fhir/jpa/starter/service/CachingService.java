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
import java.util.function.Function;

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



	private <T, U, K> void cacheDataGeneric(
		NotificationDataSource notificationDataSource,
		List<T> items,
		String orgId,
		Date date,
		LinkedHashMap<K, String> mapOfIdToMd5,
		Function<T, K> idExtractor,
		Function<T, String> orgIdExtractor,
		Function<T, String> valueExtractor,
		List<U> fallbackItems,
		Function<U, K> fallbackIdExtractor,
		double fallbackValue
	) {
		if (items != null) {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<>();
			ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<>();

			items.forEach(item -> {
				OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
					@Override
					public void doIt() {
						List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(idExtractor.apply(item)), orgIdExtractor.apply(item));
						if (cacheEntities.isEmpty()) {
							CacheEntity cacheEntity = new CacheEntity(
								orgIdExtractor.apply(item),
								mapOfIdToMd5.get(idExtractor.apply(item)),
								date,
								Double.valueOf(valueExtractor.apply(item)),
								Date.valueOf(LocalDate.now())
							);
							cacheEntitiesForInsert.add(cacheEntity);
						} else {
							CacheEntity cacheEntity = cacheEntities.get(0);
							cacheEntity.setValue(Double.valueOf(valueExtractor.apply(item)));
							cacheEntitiesForUpdate.add(cacheEntity);
						}
					}
				});
			});

			notificationDataSource.insertObjects(cacheEntitiesForInsert);
			notificationDataSource.updateObjects(cacheEntitiesForUpdate);
		} else {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<>();

			fallbackItems.forEach(item -> {
				List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(fallbackIdExtractor.apply(item)), orgId);
				if (cacheEntities.isEmpty()) {
					CacheEntity cacheEntity = new CacheEntity(
						orgId,
						mapOfIdToMd5.get(fallbackIdExtractor.apply(item)),
						date,
						fallbackValue,
						Date.valueOf(LocalDate.now())
					);
					cacheEntitiesForInsert.add(cacheEntity);
				}
			});

			notificationDataSource.insertObjects(cacheEntitiesForInsert);
		}
	}

	@Async("asyncTaskExecutor")
	private <T, U, K> void cacheDataAsyncGeneric(
		NotificationDataSource notificationDataSource,
		List<T> items,
		String orgId,
		Date date,
		LinkedHashMap<K, String> mapOfIdToMd5,
		Function<T, K> idExtractor,
		Function<T, String> orgIdExtractor,
		Function<T, String> valueExtractor,
		List<U> fallbackItems,
		Function<U, K> fallbackIdExtractor,
		double fallbackValue
	){
		if(items !=null) {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<>();
			ArrayList<CacheEntity> cacheEntitiesForUpdate = new ArrayList<>();

			for (T item : items) {
				OperationHelper.doWithRetry(MAX_RETRY, new Operation() {
					@Override
					public void doIt() {
						List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date, mapOfIdToMd5.get(idExtractor.apply(item)), orgIdExtractor.apply(item));
						if (cacheEntities.isEmpty()) {
							CacheEntity cacheEntity = new CacheEntity(
								orgIdExtractor.apply(item),
								mapOfIdToMd5.get(idExtractor.apply(item)),
								date,
								Double.valueOf(valueExtractor.apply(item)),
								Date.valueOf(LocalDate.now())
							);
							cacheEntitiesForInsert.add(cacheEntity);
						} else {
							CacheEntity cacheEntity = cacheEntities.get(0);
							cacheEntity.setValue(Double.valueOf(valueExtractor.apply(item)));
							cacheEntitiesForUpdate.add(cacheEntity);
						}
					}
				});
			}
			notificationDataSource.insertObjects(cacheEntitiesForInsert);
			notificationDataSource.updateObjects(cacheEntitiesForUpdate);
		} else {
			ArrayList<CacheEntity> cacheEntitiesForInsert = new ArrayList<>();

			for(U item : fallbackItems) {
				List<CacheEntity> cacheEntities = notificationDataSource.getCacheByDateIndicatorAndOrgId(date,mapOfIdToMd5.get(fallbackIdExtractor.apply(item)),orgId);
				if(cacheEntities.isEmpty()){
					CacheEntity cacheEntity = new CacheEntity(
						orgId,
						mapOfIdToMd5.get(fallbackIdExtractor.apply(item)),
						date,
						fallbackValue,
						Date.valueOf(LocalDate.now())
					);
					cacheEntitiesForInsert.add(cacheEntity);
				}
			}
			notificationDataSource.insertObjects(cacheEntitiesForInsert);
		}
	}


	public void cacheData(String orgId, Date startDate, List<IndicatorItem>indicators, int count, Map<String, List<ScoreCardItem>> map,String filterString) {
	   long start = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (IndicatorItem item : indicators) {
			mapOfIdToMd5.put(item.getId(), Utils.md5Bytes((item.getFhirPath().getExpression() + item.getId() + filterString).getBytes(StandardCharsets.UTF_8)));
		}

		List<ScoreCardItem> data = map.get(startDate.toLocalDate().toString());
		final Date date = (Date) startDate.clone();

		cacheDataGeneric(notificationDataSource, data, orgId, date, mapOfIdToMd5, ScoreCardItem::getIndicatorId, ScoreCardItem::getOrgId, ScoreCardItem::getValue, indicators, IndicatorItem::getId, -1.0);

		long end = System.nanoTime();
		double diff = (end - start) / 1_000_000.0;;
		logger.warn("-- Time for Caching Score card  "+String.valueOf(diff));
	}

	public void cacheDataForBarChart(String orgId, Date startDate, List<BarChartDefinition> barCharts,int count,Map<String, List<BarChartItemDataCollection>> map,String filterString) {
		long start = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		List<BarComponentWrapper> fallbackItems = new ArrayList<>();

		for (BarChartDefinition barChart : barCharts) {
			for (BarChartItemDefinition barChartItem : barChart.getBarChartItemDefinitions()) {
				for (BarComponent barComponent : barChartItem.getBarComponentList()) {
					String key = String.valueOf(barChart.getId()) + " " + String.valueOf(barChartItem.getId()) + " " + String.valueOf(barComponent.getId());
					mapOfIdToMd5.put(key, Utils.getMd5KeyForLineCacheMd5WithCategory(
						barComponent.getFhirPath().getExpression() + filterString,
						barComponent.getBarChartItemId(),
						barChart.getId(),
						barChart.getCategoryId()
					));
					fallbackItems.add(new BarComponentWrapper(barComponent, barChart.getId(), barChartItem.getId()));
				}
			}
		}

		List<BarChartItemDataCollection> data = map.get(startDate.toLocalDate().toString());
		final Date date = (Date) startDate.clone();

		List<BarComponentDataWrapper> flatBarComponentData = new ArrayList<>();
		if (data != null) {
			data.forEach(collection -> {
				collection.getData().forEach(category -> {
					category.getBarComponentData().forEach(barComponentData -> {
						flatBarComponentData.add(new BarComponentDataWrapper(
							barComponentData,
							collection.getChartId(),
							category.getId()
						));
					});
				});
			});
		}

		cacheDataGeneric(notificationDataSource, flatBarComponentData, orgId, date, mapOfIdToMd5,
			wrapper -> String.valueOf(wrapper.chartId) + " " + String.valueOf(wrapper.categoryId) + " " + String.valueOf(wrapper.data.getId()),
			wrapper -> orgId, // BarComponentData may not have getOrgId, using orgId
			wrapper -> wrapper.data.getValue(),
			fallbackItems,
			wrapper -> String.valueOf(wrapper.chartId) + " " + String.valueOf(wrapper.barChartItemId) + " " + String.valueOf(wrapper.component.getId()),
			0.0
		);

		long end = System.nanoTime();
		double diff = (end - start) / 1_000_000.0;;
		logger.warn("-- Time for Caching BarChart  "+String.valueOf(diff));
	}

	public void cacheTabularData(String orgId, Date startDate, List<TabularItem> indicators,int count,Map<String, List<ScoreCardItem>> map,String filterString) {
		long start = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (TabularItem item : indicators) {
			mapOfIdToMd5.put(item.getId(), Utils.md5Bytes((item.getFhirPath().getExpression() + filterString).getBytes(StandardCharsets.UTF_8)));
		}

		List<ScoreCardItem> data = map.get(startDate.toLocalDate().toString());
		final Date date = (Date) startDate.clone();

		cacheDataGeneric(notificationDataSource, data, orgId, date, mapOfIdToMd5, ScoreCardItem::getIndicatorId, ScoreCardItem::getOrgId, ScoreCardItem::getValue, indicators, TabularItem::getId, 0.0);

		long end = System.nanoTime();
		double diff = (end - start) / 1_000_000.0;;
		logger.warn("-- Time for Caching TabularData  "+String.valueOf(diff));
	}

	public void cachePieChartData(String orgId, Date startDate, List<PieChartDefinition> pieChartDefinitions,int count,Map<String, List<PieChartItemDataCollection>> map,String filterString){
		long start = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		List<PieChartCategoryWrapper> fallbackItems = new ArrayList<>();

		for (PieChartDefinition pieChartDefinition : pieChartDefinitions) {
			String categoryId = pieChartDefinition.getCategoryId();
			for (PieChartCategoryDefinition pieChartItem : pieChartDefinition.getItem()) {
				String key = String.valueOf(pieChartItem.getId()) + categoryId;
				mapOfIdToMd5.put(key, Utils.md5Bytes((pieChartItem.getFhirPath().getExpression() + filterString + categoryId).getBytes(StandardCharsets.UTF_8)));
				fallbackItems.add(new PieChartCategoryWrapper(pieChartItem, categoryId));
			}
		}

		List<PieChartItemDataCollection> data = map.get(startDate.toLocalDate().toString());
		final Date date = (Date) startDate.clone();

		List<PieChartItemWrapper> flatPieChartItems = new ArrayList<>();
		if (data != null) {
			data.forEach(collection -> {
				String categoryId = collection.getCategoryId();
				collection.getData().forEach(pieChartItem -> {
					flatPieChartItems.add(new PieChartItemWrapper(pieChartItem, categoryId));
				});
			});
		}

		cacheDataGeneric(notificationDataSource, flatPieChartItems, orgId, date, mapOfIdToMd5,
			wrapper -> String.valueOf(wrapper.item.getId()) + wrapper.categoryId,
			wrapper -> wrapper.item.getOrgId(),
			wrapper -> wrapper.item.getValue(),
			fallbackItems,
			wrapper -> String.valueOf(wrapper.categoryDefinition.getId()) + wrapper.categoryId,
			0.0
		);

		long end = System.nanoTime();
		double diff = (end - start) / 1_000_000.0;;
		logger.warn("-- Time for Caching PieChart  "+String.valueOf(diff));
	}

	public void cacheDataLineChart(String orgId, Date startDate, List<LineChart> lineCharts,int count,Map<String, List<LineChartItemCollection>> map,String filterString) {
		long start = System.nanoTime();
		
		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		List<LineChartItemDefinitionWrapper> fallbackItems = new ArrayList<>();

		for (LineChart lineChart : lineCharts) {
			int chartId = lineChart.getId();
			for (LineChartItemDefinition lineDefinition : lineChart.getLineChartItemDefinitions()) {
				String key = String.valueOf(chartId) + " " + String.valueOf(lineDefinition.getId());
				mapOfIdToMd5.put(key, Utils.getMd5KeyForLineCacheMd5WithCategory(
					lineDefinition.getFhirPath().getExpression() + filterString,
					lineDefinition.getId(),
					chartId,
					lineChart.getCategoryId()
				));
				fallbackItems.add(new LineChartItemDefinitionWrapper(lineDefinition, chartId));
			}
		}

		List<LineChartItemCollection> data = map.get(startDate.toLocalDate().toString());
		final Date date = (Date) startDate.clone();

		List<LineChartItemWrapper> flatLineChartItems = new ArrayList<>();
		if (data != null) {
			data.forEach(collection -> {
				int chartId = collection.getChartId();
				collection.getValue().forEach(lineChartItem -> {
					flatLineChartItems.add(new LineChartItemWrapper(lineChartItem, chartId));
				});
			});
		}

		cacheDataGeneric(notificationDataSource, flatLineChartItems, orgId, date, mapOfIdToMd5,
			wrapper -> String.valueOf(wrapper.chartId) + " " + String.valueOf(wrapper.item.getLineId()),
			wrapper -> orgId, // LineChartItem may not have getOrgId, using orgId
			wrapper -> wrapper.item.getValue(),
			fallbackItems,
			wrapper -> String.valueOf(wrapper.chartId) + " " + String.valueOf(wrapper.definition.getId()),
			0.0
		);

		long end = System.nanoTime();
		double diff = (end - start) / 1_000_000.0;;
		logger.warn("-- Time for Caching LineChart  "+String.valueOf(diff));
	}
	
	@Async("asyncTaskExecutor")
	public void cacheData(String orgId, Date date, List<IndicatorItem> indicators,String filterString) {
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (IndicatorItem item : indicators) {
			mapOfIdToMd5.put(item.getId(),Utils.md5Bytes((item.getFhirPath().getExpression() + filterString).getBytes(StandardCharsets.UTF_8)));
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) fhirClientAuthenticatorService.getFhirClient());
		List<ScoreCardItem> data = ReportGeneratorFactory.INSTANCE.reportGenerator()
			.getFacilityData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), indicators, Collections.emptyList())
			.get(date.toLocalDate().toString());

		cacheDataAsyncGeneric(notificationDataSource, data, orgId, date, mapOfIdToMd5, ScoreCardItem::getIndicatorId, ScoreCardItem::getOrgId, ScoreCardItem::getValue, indicators, IndicatorItem::getId, -1.0);

		long endTime = System.nanoTime();
		long diff = (endTime - startTime);
		logger.warn("-- Time for Caching Async cacheData  "+String.valueOf(diff));
	}
	
	@Async("asyncTaskExecutor")
	public void cacheDataForBarChart(String orgId, Date date, List<BarChartDefinition> barCharts,int count,String filterString) {
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		List<BarComponentWrapper> fallbackItems = new ArrayList<>();
		for (BarChartDefinition barChart : barCharts) {
			int chartId = barChart.getId();
			for (BarChartItemDefinition barChartItem : barChart.getBarChartItemDefinitions()) {
				int barChartItemId = barChartItem.getId();
				for (BarComponent barComponent : barChartItem.getBarComponentList()) {
					String key = String.valueOf(chartId) + " " + String.valueOf(barChartItemId) + " " + String.valueOf(barComponent.getId());
					mapOfIdToMd5.put(key, Utils.getMd5KeyForLineCacheMd5WithCategory(
						barComponent.getFhirPath().getExpression() + filterString,
						barComponent.getBarChartItemId(),
						chartId,
						barChart.getCategoryId()
					));
					fallbackItems.add(new BarComponentWrapper(barComponent, chartId, barChartItemId));
				}
			}
		}

		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) fhirClientAuthenticatorService.getFhirClient());
		List<BarChartItemDataCollection> barChartItemCollections = ReportGeneratorFactory.INSTANCE.reportGenerator()
			.getBarChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), barCharts, Collections.emptyList())
			.get(date.toLocalDate().toString());

		List<BarComponentDataWrapper> flatBarComponentData = new ArrayList<>();
		if (barChartItemCollections != null) {
			for (BarChartItemDataCollection collection : barChartItemCollections) {
				int chartId = collection.getChartId();
				for (BarComponentCategory category : collection.getData()) {
					int categoryId = category.getId();
					for (BarComponentData data : category.getBarComponentData()) {
						flatBarComponentData.add(new BarComponentDataWrapper(data, chartId, categoryId));
					}
				}
			}
		}

		cacheDataAsyncGeneric(notificationDataSource, flatBarComponentData, orgId, date, mapOfIdToMd5,
			item -> String.valueOf(item.chartId) + " " + String.valueOf(item.data.getBarChartItemId()) + " " + String.valueOf(item.data.getId()),
			item -> orgId, // BarComponentData does not have getOrgId, using orgId
			item -> item.data.getValue(),
			fallbackItems,
			item -> String.valueOf(item.chartId) + " " + String.valueOf(item.barChartItemId) + " " + String.valueOf(item.component.getId()),
			0.0
		);

		long endTime = System.nanoTime();
		long diff = (endTime - startTime);
		logger.warn("-- Time for Caching Async Barchar  "+String.valueOf(diff));
	}

	// Wrapper class for BarComponentData
	private static class BarComponentDataWrapper {
		final BarComponentData data;
		final int chartId;
		final int categoryId;

		BarComponentDataWrapper(BarComponentData data, int chartId, int categoryId) {
			this.data = data;
			this.chartId = chartId;
			this.categoryId = categoryId;
		}
	}

	// Wrapper class for BarComponent
	private static class BarComponentWrapper {
		final BarComponent component;
		final int chartId;
		final int barChartItemId;

		BarComponentWrapper(BarComponent component, int chartId, int barChartItemId) {
			this.component = component;
			this.chartId = chartId;
			this.barChartItemId = barChartItemId;
		}
	}

	@Async("asyncTaskExecutor")
	public void cacheTabularData(String orgId, Date date, List<TabularItem> indicators,int count,String filterString) {
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<Integer, String> mapOfIdToMd5 = new LinkedHashMap<>();
		for (TabularItem item : indicators) {
			mapOfIdToMd5.put(item.getId(),Utils.md5Bytes((item.getFhirPath().getExpression() + filterString).getBytes(StandardCharsets.UTF_8)));
		}
		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) fhirClientAuthenticatorService.getFhirClient());
		List<ScoreCardItem> scoreCardItems = ReportGeneratorFactory.INSTANCE.reportGenerator()
			.getTabularData(fhirClientProvider,orgId,new DateRange(date.toString(),date.toString()),indicators,Collections.emptyList())
			.get(date.toLocalDate().toString());

		cacheDataAsyncGeneric(notificationDataSource, scoreCardItems, orgId, date, mapOfIdToMd5, ScoreCardItem::getIndicatorId, ScoreCardItem::getOrgId, ScoreCardItem::getValue, indicators, TabularItem::getId, 0.0);

		long endTime = System.nanoTime();
		long diff = (endTime - startTime);
		logger.warn("-- Time for Caching Async TabularData  "+String.valueOf(diff));
	}

	@Async("asyncTaskExecutor")
	public void cachePieChartData(String orgId, Date date, List<PieChartDefinition> pieChartDefinitions,int count,String filterString){
		
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		List<PieChartCategoryWrapper> fallbackItems = new ArrayList<>();
		for (PieChartDefinition pieChartDefinition : pieChartDefinitions) {
			if (pieChartDefinition != null) {
				String categoryId = pieChartDefinition.getCategoryId();
				for (PieChartCategoryDefinition pieChartItem : pieChartDefinition.getItem()) {
					String key = String.valueOf(pieChartItem.getId()) + categoryId;
					mapOfIdToMd5.put(key, Utils.md5Bytes((pieChartItem.getFhirPath().getExpression() + filterString + categoryId).getBytes(StandardCharsets.UTF_8)));
					fallbackItems.add(new PieChartCategoryWrapper(pieChartItem, categoryId));
				}
			}
		}

		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) fhirClientAuthenticatorService.getFhirClient());
		List<PieChartItemDataCollection> pieChartItems = ReportGeneratorFactory.INSTANCE.reportGenerator()
			.getPieChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), pieChartDefinitions, Collections.emptyList())
			.get(date.toLocalDate().toString());

		List<PieChartItemWrapper> flatPieChartItems = new ArrayList<>();
		if (pieChartItems != null) {
			for (PieChartItemDataCollection collection : pieChartItems) {
				String categoryId = collection.getCategoryId();
				for (PieChartItem item : collection.getData()) {
					flatPieChartItems.add(new PieChartItemWrapper(item, categoryId));
				}
			}
		}

		cacheDataAsyncGeneric(notificationDataSource, flatPieChartItems, orgId, date, mapOfIdToMd5,
			item -> String.valueOf(item.item.getId()) + item.categoryId,
			item -> item.item.getOrgId(),
			item -> item.item.getValue(),
			fallbackItems,
			item -> String.valueOf(item.categoryDefinition.getId()) + item.categoryId,
			0.0
		);

		long endTime = System.nanoTime();
		long diff = (endTime - startTime);
		logger.warn("-- Time for Caching Async PieChart  "+String.valueOf(diff));
	}

	// Wrapper class for PieChartItem
	private static class PieChartItemWrapper {
		final PieChartItem item;
		final String categoryId;

		PieChartItemWrapper(PieChartItem item, String categoryId) {
			this.item = item;
			this.categoryId = categoryId;
		}
	}

	// Wrapper class for PieChartCategoryDefinition
	private static class PieChartCategoryWrapper {
		final PieChartCategoryDefinition categoryDefinition;
		final String categoryId;

		PieChartCategoryWrapper(PieChartCategoryDefinition categoryDefinition, String categoryId) {
			this.categoryDefinition = categoryDefinition;
			this.categoryId = categoryId;
		}
	}
	
	@Async("asyncTaskExecutor")
	public void cacheDataLineChart(String orgId, Date date, List<LineChart> lineCharts,int count,String filterString) {
		long startTime = System.nanoTime();

		notificationDataSource = NotificationDataSource.getInstance();
		LinkedHashMap<String, String> mapOfIdToMd5 = new LinkedHashMap<>();
		List<LineChartItemDefinitionWrapper> fallbackItems = new ArrayList<>();
		for (LineChart lineChart : lineCharts) {
			int chartId = lineChart.getId();
			for (LineChartItemDefinition lineDefinition : lineChart.getLineChartItemDefinitions()) {
				String key = String.valueOf(chartId) + " " + String.valueOf(lineDefinition.getId());
				mapOfIdToMd5.put(key, Utils.getMd5KeyForLineCacheMd5WithCategory(
					lineDefinition.getFhirPath().getExpression() + filterString,
					lineDefinition.getId(),
					chartId,
					lineChart.getCategoryId()
				));
				fallbackItems.add(new LineChartItemDefinitionWrapper(lineDefinition, chartId));
			}
		}

		FhirClientProvider fhirClientProvider = new FhirClientProviderImpl((GenericClient) fhirClientAuthenticatorService.getFhirClient());
		List<LineChartItemCollection> lineChartItemCollections = ReportGeneratorFactory.INSTANCE.reportGenerator()
			.getLineChartData(fhirClientProvider, orgId, new DateRange(date.toString(), date.toString()), lineCharts, Collections.emptyList())
			.get(date.toLocalDate().toString());

		List<LineChartItemWrapper> flatLineChartItems = new ArrayList<>();
		if (lineChartItemCollections != null) {
			for (LineChartItemCollection collection : lineChartItemCollections) {
				int chartId = collection.getChartId();
				for (LineChartItem item : collection.getValue()) {
					flatLineChartItems.add(new LineChartItemWrapper(item, chartId));
				}
			}
		}

		cacheDataAsyncGeneric(notificationDataSource, flatLineChartItems, orgId, date, mapOfIdToMd5,
			item -> String.valueOf(item.chartId) + " " + String.valueOf(item.item.getLineId()),
			item -> orgId, // LineChartItem does not have getOrgId, using orgId
			item -> item.item.getValue(),
			fallbackItems,
			item -> String.valueOf(item.chartId) + " " + String.valueOf(item.definition.getId()),
			0.0
		);

		long endTime = System.nanoTime();
		long diff = (endTime - startTime);
		logger.warn("-- Time for Caching Async LineData  "+String.valueOf(diff));
	}

	// Wrapper class for LineChartItem
	private static class LineChartItemWrapper {
		final LineChartItem item;
		final int chartId;

		LineChartItemWrapper(LineChartItem item, int chartId) {
			this.item = item;
			this.chartId = chartId;
		}
	}

	// Wrapper class for LineChartItemDefinition
	private static class LineChartItemDefinitionWrapper {
		final LineChartItemDefinition definition;
		final int chartId;

		LineChartItemDefinitionWrapper(LineChartItemDefinition definition, int chartId) {
			this.definition = definition;
			this.chartId = chartId;
		}
	}

	public void cacheMapData (Date date, Set<Map.Entry<String, Map<String, PositionData>>> dateResponseMap, ArrayList<MapCacheEntity> resultToCache, ArrayList<MapCacheEntity> resultToUpdateCache) {

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

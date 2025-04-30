package ca.uhn.fhir.jpa.starter.service;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.model.IndicatorColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CSVConverter {
	private static final Logger logger = LoggerFactory.getLogger(CSVConverter.class);

	@Autowired
	private Map<String, DashboardConfigContainer> dashboardEnvToConfigMap;

	@Value("${report.env}")
	private String reportEnv;

	public byte[] convertReportToCSV(List<ReportEntry> reportEntries) {
		try {

			DashboardConfigContainer configContainer = dashboardEnvToConfigMap.getOrDefault(reportEnv, new DashboardConfigContainer());
			List<IndicatorColumn> indicatorColumns = configContainer.getIndicatorColumns();
			if (indicatorColumns == null || indicatorColumns.isEmpty()) {
				logger.error("No indicator columns found for environment: {}", reportEnv);
				return new byte[0];
			}

			List<String[]> csvRows = new ArrayList<>();

			List<String> header = new ArrayList<>();
			header.add("date");
			header.add("state");
			header.add("lga");
			header.add("ward");
			header.add("facility");

			for (IndicatorColumn indicator : indicatorColumns) {
				header.add(indicator.getName().replace(" ", "_"));
			}
			csvRows.add(header.toArray(new String[0]));

			for (ReportEntry entry : reportEntries) {
				List<String> row = new ArrayList<>();
				row.add(entry.getDate());
				row.add(entry.getState());
				row.add(entry.getLga());
				row.add(entry.getWard());
				row.add(entry.getFacility());

				Map<String, String> indicatorValues = entry.getIndicatorValues();
				for (IndicatorColumn indicator : indicatorColumns) {
					String value = indicatorValues.getOrDefault(indicator.getName(), "0");
					if (value.equals("No data found") || value.startsWith("Error: ")) {
						value = "0";
					}
					row.add(value);
				}
				csvRows.add(row.toArray(new String[0]));
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (CSVWriter csvWriter = new CSVWriter(
				new OutputStreamWriter(baos, StandardCharsets.UTF_8),
				CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.DEFAULT_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER,
				CSVWriter.DEFAULT_LINE_END)) {
				csvWriter.writeAll(csvRows);
			}
			logger.info("Generated CSV with {} rows", csvRows.size());
			return baos.toByteArray();
		} catch (Exception e) {
			logger.error("Failed to convert report to CSV: {}", e.getMessage(), e);
			return new byte[0];
		}
	}
}
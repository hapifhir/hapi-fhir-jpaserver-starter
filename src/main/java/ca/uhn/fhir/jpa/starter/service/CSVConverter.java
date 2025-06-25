package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.ReportProperties;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ca.uhn.fhir.jpa.starter.DashboardConfigContainer;
import ca.uhn.fhir.jpa.starter.model.IndicatorColumn;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private ReportProperties reportProperties;

	public byte[] convertReportToCSV(List<ReportEntry> reportEntries) {
		try {
			DashboardConfigContainer configContainer = dashboardEnvToConfigMap.getOrDefault(reportProperties.getEnv(), new DashboardConfigContainer());
			List<IndicatorColumn> indicatorColumns = configContainer.getIndicatorColumns();
			if (indicatorColumns == null || indicatorColumns.isEmpty()) {
				logger.error("No indicator columns found for environment: {}", reportProperties.getEnv());
				return new byte[0];
			}

			// Determine which location fields (state, lga, ward) have valid values
			boolean hasValidState = false;
			boolean hasValidLga = false;
			boolean hasValidWard = false;
			for (ReportEntry entry : reportEntries) {
				if (isValidValue(entry.getState())) {
					hasValidState = true;
				}
				if (isValidValue(entry.getLga())) {
					hasValidLga = true;
				}
				if (isValidValue(entry.getWard())) {
					hasValidWard = true;
				}
			}

			// Build dynamic header
			List<String> header = new ArrayList<>();
			header.add("date");
			if (hasValidState) {
				header.add("state");
			}
			if (hasValidLga) {
				header.add("lga");
			}
			if (hasValidWard) {
				header.add("ward");
			}
			header.add("facility");
			for (IndicatorColumn indicator : indicatorColumns) {
				header.add(indicator.getName().replace(" ", "_"));
			}

			// Build CSV rows
			List<String[]> csvRows = new ArrayList<>();
			csvRows.add(header.toArray(new String[0]));

			for (ReportEntry entry : reportEntries) {
				List<String> row = new ArrayList<>();
				row.add(entry.getDate());
				if (hasValidState) {
					row.add(entry.getState() != null ? entry.getState() : "");
				}
				if (hasValidLga) {
					row.add(entry.getLga() != null ? entry.getLga() : "");
				}
				if (hasValidWard) {
					row.add(entry.getWard() != null ? entry.getWard() : "");
				}
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
			logger.info("Generated CSV with {} rows and header: {}", csvRows.size(), String.join(",", header));
			return baos.toByteArray();
		} catch (Exception e) {
			logger.error("Failed to convert report to CSV: {}", e.getMessage(), e);
			return new byte[0];
		}
	}

	private boolean isValidValue(String value) {
		return value != null && !value.trim().isEmpty() && !value.equalsIgnoreCase("N/A");
	}
}
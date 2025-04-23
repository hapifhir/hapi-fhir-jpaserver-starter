package ca.uhn.fhir.jpa.starter.service;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVConverter {
	private static final Logger logger = LoggerFactory.getLogger(CSVConverter.class);

	private static final List<String> INDICATOR_COLUMNS = Arrays.asList(
		"Patient Registration",
		"ANC Registration",
		"Schedule Appointment",
		"Postnatal",
		"Antenatal",
		"Labour and Delivery",
		"Birth Register",
		"Child Immunization",
		"Family Planning",
		"Tetanus Diphtheria",
		"Child Growth Monitoring",
		"In-Patients",
		"Community Immunization",
		"Out-Patients"
	);

	public static byte[] convertReportToCSV(List<ReportEntry> reportEntries) {
		try {
			List<String[]> csvRows = new ArrayList<>();

			List<String> header = new ArrayList<>();
			header.add("date");
			header.add("state");
			header.add("lga");
			header.add("ward");
			header.add("facility");

			for (String indicator : INDICATOR_COLUMNS) {
				header.add(indicator.replace(" ", "_"));
			}
			csvRows.add(header.toArray(new String[0]));


			for (ReportEntry entry : reportEntries) {
				List<String> row = new ArrayList<>();
				row.add(entry.getDate());
				row.add(entry.getState());
				row.add(entry.getLga());
				row.add(entry.getWard());
				row.add(entry.getFacility());


				for (String indicator : INDICATOR_COLUMNS) {
					String value = entry.getIndicatorValues().getOrDefault(indicator, "0");
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
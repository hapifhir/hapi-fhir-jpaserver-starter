package com.iprd.fhir.utils;

import android.util.Pair;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DateUtilityHelper {

	static long millisecondsInADay = 24 * 60 * 60 * 1000; // Number of milliseconds in a day

	public static long differenceInSeconds(String dateString1,String dateString2){
		  // Create DateTimeFormatter for parsing the date-time strings
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        // Parse the date-time strings into LocalDateTime objects
        LocalDateTime dateTime1 = LocalDateTime.parse(dateString1, formatter);
        LocalDateTime dateTime2 = LocalDateTime.parse(dateString2, formatter);

        // Convert LocalDateTime objects to Instant objects
        Instant instant1 = dateTime1.atZone(ZoneId.of("UTC")).toInstant();
        Instant instant2 = dateTime2.atZone(ZoneId.of("UTC")).toInstant();

        // Calculate the difference in seconds
        Duration duration = Duration.between(instant2, instant1);
        long secondsDifference = duration.getSeconds();
        return secondsDifference;
	}
	
	public static String addOneMinute(String inputDateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        OffsetDateTime originalDateTime = OffsetDateTime.parse(inputDateTime, formatter);

        // Add one minute to the OffsetDateTime
        OffsetDateTime updatedDateTime = originalDateTime.plusMinutes(1);

        // Format the updated OffsetDateTime back into a string
        String updatedDateTimeString = updatedDateTime.format(formatter);

        return updatedDateTimeString;
	}
	
	public static Date getCurrentSqlDate() {
		return new Date(System.currentTimeMillis());
	}

	public static java.sql.Date utilDateToSqlDate(java.util.Date date) {
		return new java.sql.Date(date.getTime());
	}

	public static Timestamp utilDateToTimestamp(java.util.Date date) {
		return new Timestamp(date.getTime());
	}

	public static Date timeStampToDate(Timestamp timestamp) {
		return Date.valueOf(timestamp.toLocalDateTime().toLocalDate());
	}

	public static Date epochToSqlDate(Long epoch) {
		return new Date(epoch);
	}

	public static Date getPreviousDay(Timestamp timestamp) {
		LocalDate previousDay = timestamp.toLocalDateTime().minusDays(1).toLocalDate();
		return Date.valueOf(previousDay);
	}

	public static Date getPreviousDateByDays(Date date, long days) {
		LocalDate previousDate = date.toLocalDate().minusDays(days);
		return Date.valueOf(previousDate);
	}

	public static String sqlTimestampToFormattedDateString(Timestamp timestamp) {
		return DateTimeFormatter.ISO_INSTANT.format(timestamp.toInstant()).split("T")[0];
	}

	public static String toDateString(java.util.Date date, String format) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		return simpleDateFormat.format(date);
	}

	public static List<Pair<Date, Date>> getQuarterlyDates(Date start, Date end) {
		LocalDate startLocalDate = start.toLocalDate();
		LocalDate endLocalDate = end.toLocalDate();

		List<Pair<Date, Date>> quarterDatePairs = new ArrayList<>();

		int startYear = startLocalDate.getYear();
		int endYear = endLocalDate.getYear();

		for (int year = startYear; year <= endYear; year++) {
			Date[] quarterStarts = {
				Date.valueOf(year + "-01-01"),
				Date.valueOf(year + "-04-01"),
				Date.valueOf(year + "-07-01"),
				Date.valueOf(year + "-10-01")
			};
			Date[] quarterEnds = {
				Date.valueOf(year + "-03-31"),
				Date.valueOf(year + "-06-30"),
				Date.valueOf(year + "-09-30"),
				Date.valueOf(year + "-12-31")
			};

			int startQuarter = (year == startYear) ? (startLocalDate.getMonthValue() - 1) / 3 : 0;
			int endQuarter = (year == endYear) ? (endLocalDate.getMonthValue() - 1) / 3 : 3;

			for (int quarter = startQuarter; quarter <= endQuarter; quarter++) {
				quarterDatePairs.add(new Pair<>(quarterStarts[quarter], quarterEnds[quarter]));
			}
		}

		return quarterDatePairs;
	}

	public static List<Pair<Date, Date>> getMonthlyDates(Date start, Date end) {
		List<Pair<Date, Date>> monthDatePairs = new ArrayList<>();

		LocalDate startDate = start.toLocalDate();
		LocalDate endDate = end.toLocalDate();

		while (!startDate.isAfter(endDate)) {
			LocalDate startMonthDate = startDate.withDayOfMonth(1);
			LocalDate endDMonthDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
			monthDatePairs.add(new Pair<Date, Date>(Date.valueOf(startMonthDate), Date.valueOf(endDMonthDate)));
			startDate = endDMonthDate.plusDays(1);
		}
		return monthDatePairs;
	}

	public static List<Pair<Date, Date>> getWeeklyDates(Date start, Date end) {
		LocalDate startDate = start.toLocalDate();
		LocalDate endDate = end.toLocalDate();

		Long weekNumber = ChronoUnit.WEEKS.between(startDate, endDate);

		List<Pair<Date, Date>> weekDatePairs = new ArrayList<>();

		for (int i = 0; i <= weekNumber; i++) {
			LocalDate endOfWeek = startDate.plusDays(6);
			weekDatePairs.add(new Pair<Date,Date>(Date.valueOf(startDate),Date.valueOf(endOfWeek)));
			startDate = endOfWeek.plusDays(1);
		}
		return weekDatePairs;
	}

	public static List<Pair<Date, Date>> getDailyDates(Date start, Date end) {
		LocalDate startDate = start.toLocalDate();
		LocalDate endDate = end.toLocalDate();

		Long dayNumber = ChronoUnit.DAYS.between(startDate, endDate);

		List<Pair<Date, Date>> dailyDatePairs = new ArrayList<>();

		for (int i = 0; i <= dayNumber; i++) {
			LocalDate endOfDay = startDate;
			dailyDatePairs.add(new Pair<Date,Date>(Date.valueOf(startDate),Date.valueOf(endOfDay)));
			startDate = startDate.plusDays(1);
		}
		return dailyDatePairs;
	}

	public static Pair<Date, Date> getCurrentWeekDates() {
		LocalDate current = getCurrentSqlDate().toLocalDate();
		LocalDate prev = current.minusDays(6);
		return new Pair<>(Date.valueOf(prev),Date.valueOf(current));
	}

	public static Pair<Date, Date> getPrevWeekDates() {
		LocalDate current = getCurrentSqlDate().toLocalDate().minusDays(7);
		LocalDate prev = current.minusDays(6);
		return new Pair<>(Date.valueOf(prev),Date.valueOf(current));
	}

	public static long calculateMillisecondsRelativeToCurrentTime(long days) {
		long currentMillis = System.currentTimeMillis(); // Current timestamp in milliseconds
		long targetMillis = currentMillis - (days * millisecondsInADay);
		return targetMillis;
	}
}
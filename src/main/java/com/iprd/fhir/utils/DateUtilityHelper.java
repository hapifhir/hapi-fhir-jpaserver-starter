package com.iprd.fhir.utils;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import android.util.Pair;

public class DateUtilityHelper {
	
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

	public static List<Pair<Date, Date>> getQuarterlyDates(Date start,Date end) {
		LocalDate localDate = start.toLocalDate();
		Date firstQuarterStart = Date.valueOf(localDate.getYear()+"-01-01");
		Date firstQuarterEnd = Date.valueOf(localDate.getYear()+"-03-31");
		Date secondQuarterStart = Date.valueOf(localDate.getYear()+"-04-01");
		Date secondQuarterEnd = Date.valueOf(localDate.getYear()+"-06-30");
		Date thirdQuarterStart = Date.valueOf(localDate.getYear()+"-04-01");
		Date thirdQuarterEnd = Date.valueOf(localDate.getYear()+"-09-30");
		Date fourthQuarterStart = Date.valueOf(localDate.getYear()+"-10-01");
		Date fourthQuarterEnd = Date.valueOf(localDate.getYear()+"-12-31");
		List<Pair<Date, Date>> quarterDatePairs = new ArrayList<>();
		quarterDatePairs.add( new Pair<>(firstQuarterStart, firstQuarterEnd));
		quarterDatePairs.add( new Pair<>(secondQuarterStart, secondQuarterEnd));
		quarterDatePairs.add( new Pair<>(thirdQuarterStart, thirdQuarterEnd));
		quarterDatePairs.add( new Pair<>(fourthQuarterStart, fourthQuarterEnd));
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

		  for (int i = 0; i < weekNumber; i++) {
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

		  for (int i = 0; i < dayNumber; i++) {
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
}

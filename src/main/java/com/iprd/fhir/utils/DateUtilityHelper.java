package com.iprd.fhir.utils;

import kotlin.Pair;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

	public static List<Pair<Date, Date>> getQuarterDates() {
		LocalDate localDate = LocalDate.now();
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
}

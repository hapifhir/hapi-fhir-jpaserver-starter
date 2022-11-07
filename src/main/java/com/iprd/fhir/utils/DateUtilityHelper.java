package com.iprd.fhir.utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
}

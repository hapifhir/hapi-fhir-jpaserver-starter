package com.iprd.fhir.utils;

import android.util.Pair;
import org.junit.Test;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static com.iprd.fhir.utils.DateUtilityHelper.getCurrentSqlDate;
import static com.iprd.fhir.utils.DateUtilityHelper.getCurrentWeekDates;
import static com.iprd.fhir.utils.DateUtilityHelper.getPrevWeekDates;
import static org.junit.Assert.assertEquals;

public class DateUtilityHelperTest {

	@Test
	public void testGetCurrentWeekDates() {
		LocalDate current = getCurrentSqlDate().toLocalDate();
		LocalDate expectedStartOfWeek = current.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
		LocalDate expectedEndOfWeek = expectedStartOfWeek.plusDays(6);
		Pair<Date, Date> weekDates = getCurrentWeekDates();
		assertEquals("Start of the week should be Sunday", Date.valueOf(expectedStartOfWeek), weekDates.first);
		assertEquals("End of the week should be Saturday",Date.valueOf(expectedEndOfWeek), weekDates.second);
	}

	@Test
	public void testGetPrevWeekDates() {
		LocalDate current = getCurrentSqlDate().toLocalDate();
		LocalDate startOfPrevWeek = current.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
		LocalDate endOfPrevWeek = startOfPrevWeek.plusDays(6);
		Pair<Date, Date> weekDates = getPrevWeekDates();
		assertEquals("Start of the previous week should be Sunday", Date.valueOf(startOfPrevWeek), weekDates.first);
		assertEquals("End of the previous week should be Saturday",Date.valueOf(endOfPrevWeek), weekDates.second);
	}
}
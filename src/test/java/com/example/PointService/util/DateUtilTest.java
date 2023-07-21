package com.example.PointService.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateUtilTest {

    @Test
    @DisplayName("1년 1일 후 날짜 받아오기")
    public void oneYearOneDayLaterTest() {
        var today = LocalDate.of(2023, 1, 1);
        var oneYearOneDayLater = today.plusYears(1).plusDays(1);
        var expireDay = LocalDate.of(2024, 1, 2);

        assertEquals(oneYearOneDayLater, expireDay);
    }
}
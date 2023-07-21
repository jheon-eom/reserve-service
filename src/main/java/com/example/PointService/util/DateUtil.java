package com.example.PointService.util;

import java.time.LocalDate;

public class DateUtil {

    public static LocalDate createExpireDate() {
        LocalDate today = LocalDate.now();

        return today.plusYears(1).plusDays(1);
    }
}

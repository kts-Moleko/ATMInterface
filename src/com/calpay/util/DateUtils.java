package com.calpay.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Date and time utility methods.
 * 
 * <p><b>South African Context:</b>
 * <ul>
 *   <li>Time zone: SAST (UTC+2, no DST)</li>
 *   <li>Date format: dd/MM/yyyy (South African standard)</li>
 *   <li>Academic year: January to December</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public final class DateUtils {
    
    // South African date format
    public static final DateTimeFormatter SA_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public static final DateTimeFormatter SA_DATETIME_FORMAT = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public static final DateTimeFormatter SA_DATETIME_FULL_FORMAT = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private DateUtils() {
        // Utility class
    }
    
    /**
     * Gets current date.
     * 
     * @return current date
     */
    public static LocalDate now() {
        return LocalDate.now();
    }
    
    /**
     * Gets current datetime.
     * 
     * @return current datetime
     */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }
    
    /**
     * Formats a date in South African format (dd/MM/yyyy).
     * 
     * @param date date to format
     * @return formatted date string
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(SA_DATE_FORMAT) : "";
    }
    
    /**
     * Formats a datetime in South African format (dd/MM/yyyy HH:mm).
     * 
     * @param dateTime datetime to format
     * @return formatted datetime string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(SA_DATETIME_FORMAT) : "";
    }
    
    /**
     * Parses a date string in South African format.
     * 
     * @param dateStr date string (dd/MM/yyyy)
     * @return parsed date
     */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, SA_DATE_FORMAT);
    }
    
    /**
     * Parses a datetime string in South African format.
     * 
     * @param dateTimeStr datetime string (dd/MM/yyyy HH:mm)
     * @return parsed datetime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, SA_DATETIME_FORMAT);
    }
    
    /**
     * Gets the first day of the current month.
     * 
     * @return first day of month
     */
    public static LocalDate getFirstDayOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }
    
    /**
     * Gets the last day of the current month.
     * 
     * @return last day of month
     */
    public static LocalDate getLastDayOfMonth() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(now.lengthOfMonth());
    }
    
    /**
     * Gets the start of day datetime.
     * 
     * @param date date
     * @return start of day
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }
    
    /**
     * Gets the end of day datetime.
     * 
     * @param date date
     * @return end of day
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(23, 59, 59, 999999999);
    }
    
    /**
     * Calculates days between two dates.
     * 
     * @param start start date
     * @param end end date
     * @return number of days
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }
    
    /**
     * Calculates months between two dates.
     * 
     * @param start start date
     * @param end end date
     * @return number of months
     */
    public static long monthsBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.MONTHS.between(start, end);
    }
    
    /**
     * Checks if date is in the past.
     * 
     * @param date date to check
     * @return true if in the past
     */
    public static boolean isPast(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }
    
    /**
     * Checks if date is in the future.
     * 
     * @param date date to check
     * @return true if in the future
     */
    public static boolean isFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }
    
    /**
     * Checks if date is today.
     * 
     * @param date date to check
     * @return true if today
     */
    public static boolean isToday(LocalDate date) {
        return date.isEqual(LocalDate.now());
    }
    
    /**
     * Gets the current academic year.
     * 
     * <p><b>South African academic year:</b> January to December.
     * 
     * @return current academic year
     */
    public static int getCurrentAcademicYear() {
        return LocalDate.now().getYear();
    }
    
    /**
     * Gets month name.
     * 
     * @param month month (1-12)
     * @return month name
     */
    public static String getMonthName(int month) {
        String[] months = {
            "", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        return month >= 1 && month <= 12 ? months[month] : "";
    }
    
    /**
     * Gets short month name.
     * 
     * @param month month (1-12)
     * @return short month name
     */
    public static String getShortMonthName(int month) {
        String[] months = {
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };
        return month >= 1 && month <= 12 ? months[month] : "";
    }
}
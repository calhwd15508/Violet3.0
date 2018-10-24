package com.example.violet30;

import java.util.Calendar;

/**
 * Created by howardzhang on 5/16/18.
 */

/*
Time class that organizes all time information used throughout app
can output time as:
    description string
    milliseconds
 */

/*
TIME FORMATTED LIKE THIS
Keywords: FROM NOW, NEXT, TOMORROW, TODAY
Examples:
    2 hours from now
    next tuesday
    tomorrow
    January 2
    today
Then add specific times, keyword AT:
    at 2:00
    at 4:30 pm
 */
public class Time {

    //special key words for time speech parsing
    private final String[] specialKeys = {"from now", "next", "tomorrow", "today"};
    //calendar object to store time
    private Calendar cal;
    //calendar object to store the current time
    private Calendar currentCal;
    //time in milliseconds
    private long ms;
    //time description by string
    private String timeString = "";

    //constructor
    public Time (String speech){

        //creates calendar instances
        currentCal = Calendar.getInstance();
        cal = Calendar.getInstance();

        //parses string to create time object
        speech = speech.trim().toLowerCase();
        String[] splitSpeech = speech.split(" ");
        int form = 4;
        for(int i = 0; i < specialKeys.length; i++){
            if(speech.contains(specialKeys[i])){
                form = i;
            }
        }
        switch(form){
            case 0:
                int number = -1;
                for(String word : splitSpeech){
                    if(word.equals("from")){
                        break;
                    } else if(word.contains("day")){
                        if (number == -1){
                            throw new IllegalArgumentException();
                        }
                        cal.add(Calendar.DAY_OF_YEAR, number);
                        number = -1;
                    }else if(word.contains("hour")){
                        if(number == -1){
                            throw new IllegalArgumentException();
                        }
                        cal.add(Calendar.HOUR, number);
                        number = -1;
                    } else if(word.contains("minute")){
                        if(number == -1){
                            throw new IllegalArgumentException();
                        }
                        cal.add(Calendar.MINUTE, number);
                        number = -1;
                    } else if(word.contains("second")){
                        if(number == -1){
                            throw new IllegalArgumentException();
                        }
                        cal.add(Calendar.SECOND, number);
                        number = -1;
                    } else if(!word.equals("and")){
                        try{
                            number = Integer.parseInt(word);
                        }catch(NumberFormatException e){
                            throw new IllegalArgumentException();
                        }
                    }
                }
                if(cal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR) &&
                        cal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR)){
                    timeString = formatTimeString();
                }else if(cal.get(Calendar.YEAR) != currentCal.get(Calendar.YEAR)){
                    timeString = formatDateString() + " " + cal.get(Calendar.YEAR);
                }else{
                    timeString = formatDateString() + " at " + formatTimeString();
                }
                break;
            case 1:
                switch(splitSpeech[1]){
                    case "monday":
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        break;
                    case "tuesday":
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                        break;
                    case "wednesday":
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                        break;
                    case "thursday":
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                        break;
                    case "friday":
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                        break;
                    case "saturday":
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                        break;
                    case "sunday":
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        break;
                }

                if(speech.contains("at")){
                    formatTime(speech.split("at")[1].trim());
                    timeString = cal.get(Calendar.DAY_OF_WEEK) + " at " + formatTimeString();
                }else{
                    cal.set(Calendar.HOUR, 8);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.set(Calendar.AM_PM, Calendar.AM);
                    timeString = cal.get(Calendar.DAY_OF_WEEK) + "";
                }

                if(cal.compareTo(currentCal) < 0){
                    cal.add(Calendar.DAY_OF_YEAR, 7);
                }
                break;
            case 2:
                cal.add(Calendar.DAY_OF_YEAR, 1);
                if(speech.contains("at")){
                    formatTime(speech.split("at")[1].trim());
                    timeString = formatDateString() + " at " + formatTimeString();
                }else{
                    cal.set(Calendar.HOUR, 8);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.set(Calendar.AM_PM, Calendar.AM);
                    timeString = formatDateString();
                }
                break;
            case 3:
                if(speech.contains("at")){
                    formatTime(speech.split("at")[1].trim());
                    timeString = formatDateString() + " at " + formatTimeString();
                }else{
                    cal.set(Calendar.HOUR, 8);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.set(Calendar.AM_PM, Calendar.AM);
                    timeString = formatDateString();
                }
                break;
            case 4:
                switch(splitSpeech[0]){
                    case "january":
                        cal.set(Calendar.MONTH, Calendar.JANUARY);
                        break;
                    case "february":
                        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
                        break;
                    case "march":
                        cal.set(Calendar.MONTH, Calendar.MARCH);
                        break;
                    case "april":
                        cal.set(Calendar.MONTH, Calendar.APRIL);
                        break;
                    case "may":
                        cal.set(Calendar.MONTH, Calendar.MAY);
                        break;
                    case "june":
                        cal.set(Calendar.MONTH, Calendar.JUNE);
                        break;
                    case "july":
                        cal.set(Calendar.MONTH, Calendar.JULY);
                        break;
                    case "august":
                        cal.set(Calendar.MONTH, Calendar.AUGUST);
                        break;
                    case "september":
                        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
                        break;
                    case "october":
                        cal.set(Calendar.MONTH, Calendar.OCTOBER);
                        break;
                    case "november":
                        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
                        break;
                    case "december":
                        cal.set(Calendar.MONTH, Calendar.DECEMBER);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }

                try{
                    int day = Integer.parseInt(String.valueOf(splitSpeech[1].charAt(0)));
                    cal.set(Calendar.DAY_OF_MONTH, day);
                }catch(NumberFormatException e){
                    throw new IllegalArgumentException();
                }

                if(speech.contains("at")){
                    formatTime(speech.split("at")[1].trim());
                    timeString = formatDateString() + formatTimeString();
                }else{
                    cal.set(Calendar.HOUR, 8);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.set(Calendar.AM_PM, Calendar.AM);
                    timeString = formatDateString();
                }

                try{
                    int year = Integer.parseInt(splitSpeech[2]);
                    cal.set(Calendar.YEAR, year);
                    timeString = timeString + " " + cal.get(Calendar.YEAR);
                }catch(NumberFormatException e){
                    findClosestYear();
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
        ms = cal.getTimeInMillis();
    }

    //helper classes to help parse speech
    private void formatTime(String speech){
        String[] splitSpeech = speech.split(" ");
        if(speech.contains(":")){
            String[] splitTime = splitSpeech[0].split(":");
            try{
                int hour = Integer.parseInt(splitTime[0]);
                if(hour == 12){
                    hour = 0;
                }
                int minute = Integer.parseInt(splitTime[1]);
                cal.set(Calendar.HOUR, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }catch(NumberFormatException e){
                throw new IllegalArgumentException();
            }
        }else{
            try{
                int hour = Integer.parseInt(splitSpeech[0]);
                if(hour == 12){
                    hour = 0;
                }
                cal.set(Calendar.HOUR, hour);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }catch(NumberFormatException e){
                throw new IllegalArgumentException();
            }
        }
        if(speech.contains("a.m.")){
            cal.set(Calendar.AM_PM, Calendar.AM);
        }else{
            cal.set(Calendar.AM_PM, Calendar.PM);
        }
    }
    private String formatDateString(){
        return getMonth(cal.get(Calendar.MONTH)) + " " + cal.get(Calendar.DAY_OF_MONTH);
    }
    private String getMonth(int month){
        switch(month){
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";
            default:
                return "Month error";
        }
    }
    private void findClosestYear(){
        if(cal.compareTo(currentCal) < 0){
            cal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR) + 1);
        }else{
            cal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
        }
    }

    //helper functions to create timeString
    private String formatTimeString(){
        int ampm = cal.get(Calendar.AM_PM);
        int min = cal.get(Calendar.MINUTE);
        String minString = "";
        if(min < 10){
            minString = "0" + min;
        }else{
            minString = "" + min;
        }
        if(ampm == 0){
            return cal.get(Calendar.HOUR) + ":" + minString
                    + " am";
        }
        return cal.get(Calendar.HOUR) + ":" + minString
                + " pm";
    }

    //access methods for timeString and ms
    public String toString(){
        return timeString;
    }
    public long getMs(){
        return ms;
    }

}

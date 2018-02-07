package com.matson.tos.vo;

import java.util.Date;

public class CalendarVO {

	private String nameOfHoliday;
	private Date hlidayDate;
	private String repeatInterval;
	public String getNameOfHoliday() {
		return nameOfHoliday;
	}
	public void setNameOfHoliday(String nameOfHoliday) {
		this.nameOfHoliday = nameOfHoliday;
	}
	public Date getHlidayDate() {
		return hlidayDate;
	}
	public void setHlidayDate(Date hlidayDate) {
		this.hlidayDate = hlidayDate;
	}
	public String getRepeatInterval() {
		return repeatInterval;
	}
	public void setRepeatInterval(String repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

}

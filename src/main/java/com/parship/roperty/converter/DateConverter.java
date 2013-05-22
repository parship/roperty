package com.parship.roperty.converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:58
 */
public class DateConverter extends AbstractPropertyConverter<Date> {

	protected static final String FORMAT_STRING = "yyyy-MM-dd";
	ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(FORMAT_STRING);
		}
	};

	@Override
	public Date toObject(final String value) {
		try {
			return df.get().parse(value);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Could not parse string to date: " + value + " expected format: " + FORMAT_STRING, e);
		}
	}

	@Override
	public String toString(final Date value) {
		return df.get().format(value);
	}
}

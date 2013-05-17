package com.freiheit.superoperty.converter;

import com.parship.roperty.persistence.PropertyConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;


/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:58
 */
public class DateConverter extends AbstractPropertyConverter {

	protected static final String FORMAT_STRING = "yyyy-MM-dd";
	ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(FORMAT_STRING);
		}
	};

	@Override
	public Object toObject(final String value) {
		try {
			return df.get().parse(value);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Could not parse string to date: " + value + " expected format: " + FORMAT_STRING, e);
		}
	}

	@Override
	public String toString(final Object value) {
		return df.get().format(value);
	}
}

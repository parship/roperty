package com.parship.roperty.converter;

import org.junit.Test;

import com.parship.roperty.converter.DateConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author mfinsterwalder
 * @since 2013-05-16 17:32
 */
public class DateConverterTest {

	private DateConverter converter = new DateConverter();
	private SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	@Test
	public void toObject() {
		Date date = (Date)converter.toObject("2012-10-25");
		assertThat(df.format(date), is("25.10.2012 00:00:00"));
	}

	@Test
	public void toStringTest() throws ParseException {
		assertThat(converter.toString(df.parse("10.04.2013 16:34:26")), is("2013-04-10"));
	}
}

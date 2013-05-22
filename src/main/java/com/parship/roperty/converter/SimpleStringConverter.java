package com.parship.roperty.converter;

public class SimpleStringConverter extends AbstractPropertyConverter<String> {
	
	@Override
	public String toObject(String value) {
		return value;
	}

}

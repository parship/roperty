package com.parship.commons.util;

import java.util.ArrayList;
import java.util.List;


/**
 * @author mfinsterwalder
 * @since 2013-04-02 22:44
 */
public class CollectionUtil {
	public static <T> List<T> arrayList(final T... values) {
		ArrayList<T> result = new ArrayList<>();
		for (T t : values) {
			result.add(t);
		}
		return result;
	}
}

/*
 * Roperty - An advanced property management and retrival system
 * Copyright (C) 2013 PARSHIP GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.parship.commons.util;

import java.util.Collection;


/**
 * A simple Design-by-contract helper class to make parameter checks explicit.
 *
 * @author mfinsterwalder
 * @since 2010-10-13
 */
public class Ensure {

	/**
	 * Check that the provided object is not null.
	 *
	 * @param obj Object to check
	 * @param parameterName name of the parameter to display in the error message
	 */
	public static void notNull(final Object obj, final String parameterName) {
		if (obj == null) {
			throw new IllegalArgumentException("\"" + parameterName + "\" must not be null");
		}
	}

	/**
	 * Check that the provided object is null.
	 *
	 * @param obj Object to check
	 * @param parameterName name of the parameter to display in the error message
	 */
	public static void isNull(final Object obj, final String parameterName) {
		if (obj != null) {
			throw new IllegalArgumentException("\"" + parameterName + "\" must be null");
		}
	}

	/**
	 * Check that the provided string is not empty (not null, not "").
	 *
	 * @param str string to check
	 * @param parameterName name of the parameter to display in the error message
	 */
	public static void notEmpty(final String str, final String parameterName) {
		if (str == null || str.length() == 0) {
			throw new IllegalArgumentException("\"" + parameterName + "\" must not be null or empty, but was: " + str);
		}
	}

	/**
	 * Check that the provided Collection is not empty).
	 *
	 * @param collection collection to check
	 * @param parameterName name of the parameter to display in the error message
	 */
	public static void notEmpty(final Collection<?> collection, final String parameterName) {
		if (collection.isEmpty()) {
			throw new IllegalArgumentException("\"" + parameterName + "\" must not be null or empty, but was: " + collection);
		}
	}

	/**
	 * Check, that the given condition is true
	 *
	 * @param cond condition to evaluate
	 * @param conditionName name to display in the error message
	 */
	public static void that(final boolean cond, final String conditionName) {
		if (!cond) {
			throw new IllegalArgumentException("\"" + conditionName + "\" must be true");
		}
	}
}

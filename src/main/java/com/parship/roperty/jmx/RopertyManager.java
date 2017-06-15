/*
 * Roperty - An advanced property management and retrival system
 * Copyright (C) 2013 PARSHIP GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parship.roperty.jmx;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.parship.roperty.Roperty;
import com.parship.roperty.keyvalues.KeyValues;


/**
 * @author mfinsterwalder
 * @since 2013-05-28 12:08
 */
public class RopertyManager implements RopertyManagerMBean {

	private final Set<Roperty<?, ?>> roperties = new HashSet<>();

	public void add(Roperty<?,?> roperty) {
        Objects.requireNonNull(roperty, "\"roperty\" must not be null");
        roperties.add(roperty);
	}

	@Override
	public String dump(String key) {
		StringBuilder builder = new StringBuilder(roperties.size() * 8);
		for (Roperty<?,?> roperty : roperties) {
			KeyValues<?> keyValues = roperty.getKeyValues(key);
			if (keyValues != null) {
				builder.append(keyValues);
				builder.append("\n\n");
			}
		}
		return builder.toString();
	}

	@Override
	public String dump() {
		StringBuilder builder = new StringBuilder(roperties.size() * 8);
		for (Roperty<?,?> roperty : roperties) {
			builder.append(roperty.dump());
			builder.append("\n\n");
		}
		return builder.toString();
	}

	@Override
	public void dumpToSystemOut() {
		for (Roperty<?,?> roperty : roperties) {
			roperty.dump(System.out);
			System.out.println();
		}
	}

	@Override
	public void reload() {
		for (Roperty<?,?> roperty : roperties) {
			roperty.reload();
		}
	}

	@Override
	public String listRoperties() {
		return roperties.toString();
	}

	public void reset() {
		roperties.clear();
	}

	public void remove(final Roperty<?,?> roperty) {
		roperties.remove(roperty);
	}
}

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

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.parship.roperty.KeyValues;
import com.parship.roperty.Roperty;

/**
 * @author mfinsterwalder
 * @since 2013-05-28 12:08
 */
public class RopertyManager implements RopertyManagerMBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(RopertyManager.class);
	private static final RopertyManager instance = new RopertyManager();

	private final Map<Roperty, Roperty> roperties = new WeakHashMap<>();

	public static RopertyManager getInstance() {
		return instance;
	}

	public RopertyManager() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.registerMBean(this, new ObjectName("com.parship.roperty", "type", RopertyManagerMBean.class.getSimpleName()));
		} catch (InstanceAlreadyExistsException e) {
			// nothing to do
        } catch (Exception e) {
			LOGGER.warn("Could not register MBean for Roperty", e);
		}
	}

	public void add(Roperty roperty) {
        Objects.requireNonNull(roperty, "\"roperty\" must not be null");
        roperties.put(roperty, null);
	}

	@Override
	public String dump(String key) {
		StringBuilder builder = new StringBuilder(roperties.keySet().size() * 8);
		for (Roperty roperty : roperties.keySet()) {
			KeyValues keyValues = roperty.getKeyValues(key);
			if (keyValues != null) {
				builder.append(keyValues);
				builder.append("\n\n");
			}
		}
		return builder.toString();
	}

	@Override
	public String dump() {
		StringBuilder builder = new StringBuilder(roperties.keySet().size() * 8);
		for (Roperty roperty : roperties.keySet()) {
			builder.append(roperty.dump());
			builder.append("\n\n");
		}
		return builder.toString();
	}

	@Override
	public void dumpToSystemOut() {
		for (Roperty roperty : roperties.keySet()) {
			roperty.dump(System.out);
			System.out.println();
		}
	}

	@Override
	public void reload() {
		for (Roperty roperty : roperties.keySet()) {
			roperty.reload();
		}
	}

	@Override
	public String listRoperties() {
		return roperties.keySet().toString();
	}

	public void reset() {
		roperties.clear();
	}

	public void remove(final Roperty roperty) {
		roperties.remove(roperty);
	}
}

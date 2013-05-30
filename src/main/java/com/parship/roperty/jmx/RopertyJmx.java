package com.parship.roperty.jmx;

import com.parship.commons.util.Ensure;
import com.parship.roperty.KeyValues;
import com.parship.roperty.Persistence;
import com.parship.roperty.Roperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * @author mfinsterwalder
 * @since 2013-05-28 12:08
 */
public class RopertyJmx implements RopertyJmxMBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(RopertyJmx.class);

	private static Map<Roperty, Roperty> roperties = new WeakHashMap<>();
	private static volatile boolean registered = false;

	public static synchronized void register() {
		if (!registered) {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			try {
				mbs.registerMBean(new RopertyJmx(), new ObjectName("com.parship.roperty", "type", RopertyJmxMBean.class.getSimpleName()));
				registered = true;
			} catch (InstanceAlreadyExistsException e) {
				// nothing to do
			} catch (Exception e) {
				LOGGER.warn("Could not register MBean for Roperty", e);
			}
		}
	}

	public static void addRoperty(Roperty roperty, Persistence persistence) {
		Ensure.notNull(roperty, "roperty");
		register();
		roperties.put(roperty, roperty);
	}

	@Override
	public void dumpToSystemOut() {
		System.out.println(dump());
	}

	@Override
	public String dump(String key) {
		StringBuilder builder = new StringBuilder();
		for (Roperty roperty : roperties.keySet()) {
			KeyValues keyValues = roperty.KeyValues(key);
			if (keyValues != null) {
				builder.append(keyValues.toString());
				builder.append("\n\n");
			}
		}
		return builder.toString();
	}

	@Override
	public String dump() {
		StringBuilder builder = new StringBuilder();
		for (Roperty roperty : roperties.keySet()) {
			builder.append(roperty.toString());
			builder.append("\n\n");
		}
		return builder.toString();
	}

	@Override
	public void reload() {
		for (Roperty roperty : roperties.keySet()) {
			roperty.reload();
		}
	}
}

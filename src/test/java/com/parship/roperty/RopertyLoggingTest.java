package com.parship.roperty;

import ch.qos.logback.classic.Level;
import com.parship.commons.util.LoggingTestRule;
import org.junit.Rule;
import org.junit.Test;


/**
 * @author mfinsterwalder
 * @since 2013-05-28 11:45
 */
public class RopertyLoggingTest {
	@Rule
	public LoggingTestRule rule = new LoggingTestRule(Level.DEBUG);

	private Roperty r = new Roperty();

	@Test
	public void everyGetIsLoggedOnDebugLevelDefaultValue() {
		r.get("key", "default", null);
		rule.verifyLogDebug("Getting value for key: 'key' with given default: 'default'. Returning value: 'default'");
	}

	@Test
	public void everyGetIsLoggedOnDebugLevelSetValue() {
		r.set("key", "otherValue");
		r.get("key", "default", null);
		rule.verifyLogDebug("Getting value for key: 'key' with given default: 'default'. Returning value: 'otherValue'");
	}
}

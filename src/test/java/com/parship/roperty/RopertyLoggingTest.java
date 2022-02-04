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

package com.parship.roperty;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * @author mfinsterwalder
 * @since 2013-05-28 11:45
 */
public class RopertyLoggingTest {

    @RegisterExtension
	private final TestLoggingExtension logExtension = new TestLoggingExtension(Level.DEBUG);

	private final Roperty r = new RopertyImpl();

	@Test
	public void everyGetIsLoggedOnDebugLevelDefaultValue() {
		r.get("key", "default", null);
		logExtension.verifyLogDebug("Getting value for key: 'key' with given default: 'default'. Returning value: 'default'");
	}

	@Test
	public void everyGetIsLoggedOnDebugLevelSetValue() {
		r.set("key", "otherValue", null);
		r.get("key", "default", null);
		logExtension.verifyLogDebug("Getting value for key: 'key' with given default: 'default'. Returning value: 'otherValue'");
	}
}

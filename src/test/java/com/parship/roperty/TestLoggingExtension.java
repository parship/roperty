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

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentMatchers;
import org.slf4j.LoggerFactory;

/**
 * @author mfinsterwalder
 * @since 2013-05-24 13:25
 */
public class TestLoggingExtension implements BeforeEachCallback, AfterEachCallback {

    private final Level levelToSet;
    private Level levelToRestore;
    private final Appender<ILoggingEvent> appenderMock = mock(Appender.class);

    public TestLoggingExtension() {
        this.levelToSet = Level.DEBUG;
    }

    public TestLoggingExtension(final Level logLevelToSet) {
        this.levelToSet = logLevelToSet;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        when(appenderMock.getName()).thenReturn("MOCK");
        getRootLogger().addAppender(appenderMock);
        if (levelToSet != null) {
            levelToRestore = getRootLogger().getLevel();
            getRootLogger().setLevel(levelToSet);
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        getRootLogger().detachAppender(appenderMock);
        if (levelToRestore != null) {
            getRootLogger().setLevel(levelToRestore);
        }
    }

    private Logger getRootLogger() {
        return (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    }

    public void verifyLogDebug(final String expectedLog) {
        verifyLog(Level.DEBUG, expectedLog);
    }

    public void verifyLogInfo(final String expectedLog) {
        verifyLog(Level.INFO, expectedLog);
    }

    public void verifyLogWarn(final String expectedLog) {
        verifyLog(Level.WARN, expectedLog);
    }

    public void verifyLogError(final String expectedLog) {
        verifyLog(Level.ERROR, expectedLog);
    }

    public void verifyLog(final Level level, final CharSequence expectedLog) {
        verify(appenderMock, atLeastOnce())
            .doAppend(ArgumentMatchers.argThat(new LogArgumentMatcher(level, expectedLog)));
    }
}

package com.parship.commons.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;


/**
 * @author mfinsterwalder
 * @since 2013-05-24 13:25
 */
public class LoggingTestRule extends TestWatcher {

	private Level levelToSet;
	private Level levelToRestore;
	private Appender<ILoggingEvent> appenderMock = mock(Appender.class);

	public LoggingTestRule() {
	}

	public LoggingTestRule(final Level logLevelToSet) {
		this.levelToSet = logLevelToSet;
	}

	@Override
	protected void starting(final Description description) {
		when(appenderMock.getName()).thenReturn("MOCK");
		getRootLogger().addAppender(appenderMock);
		if (levelToSet != null) {
			levelToRestore = getRootLogger().getLevel();
			getRootLogger().setLevel(levelToSet);
		}
	}

	@Override
	protected void finished(final Description description) {
		getRootLogger().detachAppender(appenderMock);
		if (levelToRestore != null) {
			getRootLogger().setLevel(levelToRestore);
		}
	}

	private Logger getRootLogger() {
		return (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
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

	public void verifyLog(final Level level, final String expectedLog) {
		verify(appenderMock, atLeastOnce())
			.doAppend(Matchers.<ILoggingEvent>argThat(new ArgumentMatcher() {

				@Override
				public boolean matches(final Object argument) {
					return ((ILoggingEvent)argument).getLevel().equals(level)
						&& ((ILoggingEvent)argument).getFormattedMessage().contains(expectedLog);
				}

				@Override
				public void describeTo(final org.hamcrest.Description description) {
					description.appendText("[" + level + "] ..." + expectedLog + "...");
				}
			}));
	}
}

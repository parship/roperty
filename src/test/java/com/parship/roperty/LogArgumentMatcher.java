package com.parship.roperty;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.mockito.ArgumentMatcher;

/**
 * Created by dheid on 22.03.17.
 */
class LogArgumentMatcher implements ArgumentMatcher<ILoggingEvent> {

    private final Level level;
    private final CharSequence expectedLog;

    LogArgumentMatcher(Level level, CharSequence expectedLog) {
        this.level = level;
        this.expectedLog = expectedLog;
    }

    @Override
    public boolean matches(final ILoggingEvent argument) {
        return argument.getLevel().equals(level)
            && argument.getFormattedMessage().contains(expectedLog);
    }

    @Override
    public String toString() {
        return "[" + level + "] ..." + expectedLog + "...";
    }
}

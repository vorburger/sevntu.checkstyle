package com.github.sevntu.checkstyle.checks.logging;

import static com.github.sevntu.checkstyle.checks.logging.LoggerDeclarationsCountCheck.MSG_KEY;

import org.junit.Test;

import com.github.sevntu.checkstyle.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * LoggerDeclarationsCountCheck test.
 * 
 * @author Michael Vorburger <mike@vorburger.ch>
 */
public class LoggerDeclarationsCountCheckTest extends BaseCheckTestSupport {

    private final DefaultConfiguration config = createCheckConfig(LoggerDeclarationsCountCheck.class);

    @Test
    public void testLoggerDeclarationsCount() throws Exception {
        final String expected[] = {
                "17: " + getCheckMessage(MSG_KEY),
        };
        verify(config, getPath("CheckLoggingTestClass.java"), expected);
    }
}

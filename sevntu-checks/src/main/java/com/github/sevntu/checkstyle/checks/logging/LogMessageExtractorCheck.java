/*
 * Copyright (c) 2016 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.github.sevntu.checkstyle.checks.logging;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

/**
 * Check which extracts the content of Logger messages somewhere (e.g. a file).
 *
 * <p>This can be used to create a comprehensive list of all log messages.
 *
 * <p>It is a first step towards more formal tracking of all messages
 * from a system with a unique ID, using e.g. a framework such
 * as jboss-logging.
 *
 * <p>Does not actually Check anything, i.e. never emits any Checkstyle warnings.
 */
public class LogMessageExtractorCheck extends AbstractLogMessageCheck {

    static final File DEFAULT_REPORT_FILE = new File("target/logger-messages.txt");

    private File logMessagesReportFile = DEFAULT_REPORT_FILE;

    public void setLogMessagesReportFileName(String fileName) {
        logMessagesReportFile = new File(fileName);
        logMessagesReportFile.getParentFile().mkdirs();
    }

    public File getLogMessagesReportFile() {
        return logMessagesReportFile;
    }

    @Override
    protected void visitLogMessage(DetailAST ast, String logMessage) {
        final File file = new File(getFileContents().getFileName());
        final String fileName = FileNameUtil.getPathRelativeToMavenProjectRootIfPossible(file).getPath();
        final int lineNumber = ast.getLineNo();
        final LogMessageOccurence log = new LogMessageOccurence(fileName, lineNumber, logMessage);
        updateMessagesReportFile(log);
    }

    protected void updateMessagesReportFile(LogMessageOccurence log) {
        try {
            Files.append(log.toString() + "\n", getLogMessagesReportFile(), Charsets.UTF_8);
        }
        catch (IOException exception) {
            log(0, "Failed to append to file: " + logMessagesReportFile.getPath() + ", due to: "
                    + exception.getMessage());
            // LOG.error("Failed to append to file: {}", logMessagesReportFile.getPath(), e);
            // TODO How to correctly do logging within a *Check?  In the mean time, let's just:
            // throw new RuntimeException("Failed to append to file: " + logMessagesReportFile.getPath(), e);
        }
    }

    public static class LogMessageOccurence {

        // relative to current project root
        public final String javaSourceFilePath;
        public final int lineNumber;
        public final String message;

        public LogMessageOccurence(String javaSourceFilePath, int lineNumber, String message) {
            this.javaSourceFilePath = Preconditions.checkNotNull(javaSourceFilePath, "javaSourceFilePath");
            this.lineNumber = lineNumber;
            this.message = Preconditions.checkNotNull(message, "message");
        }

        @Override
        public String toString() {
            return javaSourceFilePath + ":" + lineNumber + ":" + message;
        }
    }
}
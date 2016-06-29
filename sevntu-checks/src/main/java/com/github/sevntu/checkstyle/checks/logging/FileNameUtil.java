/*
 * Copyright (c) 2016 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.github.sevntu.checkstyle.checks.logging;

import java.io.File;
import java.nio.file.Path;

import com.google.common.base.Optional;

/**
 * Utility to convert absolute file name to path relative to project.
 *
 * <p>Current implementation use a sad heuristic based on detecting a pom.xml.
 * This is of course sub-optimal to say the very least.  Improvements welcome.
 *
 * @author Michael Vorburger
 */
public final class FileNameUtil {

    // TODO https://groups.google.com/forum/#!topic/checkstyle-devel/Rfwx81YhVQk

    private FileNameUtil() {
    }

    static File getPathRelativeToMavenProjectRootIfPossible(File absoluteFile) {
        return getOptionalPathRelativeToMavenProjectRoot(absoluteFile).or(absoluteFile);
    }

    static Optional<File> getOptionalPathRelativeToMavenProjectRoot(File absoluteFile) {
        if (!absoluteFile.isAbsolute()) {
            return Optional.of(absoluteFile);
        }
        File projectRoot = absoluteFile;
        while (!isProjectRootDir(projectRoot) && projectRoot.getParentFile() != null) {
            projectRoot = projectRoot.getParentFile();
        }
        if (isProjectRootDir(projectRoot)) {
            final Path absolutePath = absoluteFile.toPath();
            final Path basePath = projectRoot.toPath();
            final Path relativePath = basePath.relativize(absolutePath);
            return Optional.of(relativePath.toFile());
        }
        return Optional.absent();
    }

    private static boolean isProjectRootDir(File file) {
        return new File(file, "pom.xml").exists();
    }

}

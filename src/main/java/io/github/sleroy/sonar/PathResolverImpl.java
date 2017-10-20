/*
 * Copyright (C) 2017 Sylvain Leroy - BYOS Company All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the MIT license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the MIT license with
 * this file. If not, please write to: contact@sylvainleroy.com, or visit : https://sylvainleroy.com
 */
package io.github.sleroy.sonar;

import java.io.File;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;

import io.github.sleroy.sonar.api.PathResolver;

public class PathResolverImpl implements PathResolver {
    private static final Logger LOG = LoggerFactory.getLogger(PathResolverImpl.class);

    /**
     * Tests if a path is existing.
     *
     * @param f
     *            the file
     * @return true if the file does exists
     */
    static boolean doesFileExist(File f) {
	return f.exists();
    }

    /**
     * Computes the absolute path of a resource from a string obtained from
     * Sonar Properties
     *
     * @param context
     *            the sensor context
     * @param path
     *            the path to check
     * @return the absolute resource path or null if the resource does not
     *         exist.
     */
    @Override
    public Optional<String> getAbsolutePath(SensorContext context, String path) {
	if (path != null) {
	    File candidateFile = new File(path);
	    LOG.debug("#1 Trying to resolve path in {}", candidateFile);
	    if (!candidateFile.isAbsolute()) {
		candidateFile = new File(context.fileSystem().baseDir().getAbsolutePath(), path);
		LOG.debug("#2 Trying to resolve path in {}", candidateFile);
	    }

	    if (!PathResolverImpl.doesFileExist(candidateFile)) {
		return Optional.empty();
	    }

	    return Optional.of(candidateFile.getAbsolutePath());
	}

	return Optional.empty();
    }

    /**
     * Returns the path provided by the setting if valid.
     *
     * @param context
     *            the sensor context
     * @param settingKey
     *            the setting key
     * @return the absolute path or nothing.
     */
    @Override
    public Optional<String> getPathFromSetting(SensorContext context, String settingKey) {
	// Prefer the specified path
	final String propertyValue = context.config().get(settingKey).orElse("");

	// Fall back to a file system search if null or doesn't exist
	if (propertyValue == null || propertyValue.isEmpty()) {
	    LOG.debug("Path {} not specified", settingKey);
	    return Optional.empty();
	} else {
	    LOG.debug("Found {} Lint path to be '{}'", settingKey, propertyValue);
	}

	final Optional<String> absolutePath = getAbsolutePath(context, propertyValue);
	LOG.debug("EsLint Absolute path is {}", absolutePath);
	return absolutePath;
    }

    @Override
    public Optional<String> getPathFromSetting(SensorContext context, String settingKey, String defaultValue) {
	final Optional<String> path = this.getPathFromSetting(context, settingKey);

	// Fall back to a file system search if null or doesn't exist
	if (path.isPresent()) {
	    return path;
	} else {
	    LOG.debug("Path {} not specified, falling back to {}", settingKey, defaultValue);
	    return getAbsolutePath(context, defaultValue);
	}
    }
}

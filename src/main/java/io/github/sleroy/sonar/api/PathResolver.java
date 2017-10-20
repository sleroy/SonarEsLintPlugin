/*
 * Copyright (C) 2017 Sylvain Leroy - BYOS Company All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the MIT license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the MIT license with
 * this file. If not, please write to: contact@sylvainleroy.com, or visit : https://sylvainleroy.com
 */
package io.github.sleroy.sonar.api;

import java.util.Optional;

import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.sensor.SensorContext;

@ScannerSide
public interface PathResolver {
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
    Optional<String> getAbsolutePath(SensorContext context, String path);

    /**
     * Matches if the property returns a valid path. If the path is invalid, it
     * does not provide a default value rather an Optional answer.
     *
     * @param context
     *            the sensor context
     * @param settingKey
     *            the setting key
     * @return the absolute path provided by the setting or a missing value.
     */
    Optional<String> getPathFromSetting(SensorContext context, String settingKey);

    Optional<String> getPathFromSetting(SensorContext context, String settingKey, String defaultValue);
}

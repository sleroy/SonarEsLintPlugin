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

import java.util.Optional;

import org.sonar.api.config.Configuration;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;

public class GenericConfiguration implements Configuration {

    private final Settings	      settings		  = new MapSettings();
    private final ConfigurationBridge configurationBridge = new ConfigurationBridge(settings);

    @Override
    public Optional<String> get(String key) { // TODO Auto-generated method stub
	return configurationBridge.get(key);
    }

    @Override
    public String[] getStringArray(String key) {

	return configurationBridge.getStringArray(key);
    }

    @Override
    public boolean hasKey(String key) {

	return configurationBridge.hasKey(key);
    }

    /**
     * Sets the property.
     *
     * @param key
     *            the key
     * @param _value
     *            the value
     */
    public void setProperty(String key, int _value) {
	settings.setProperty(key, _value);

    }

    /**
     * Sets the property.
     *
     * @param key
     *            the key
     * @param _value
     *            the value
     */
    public void setProperty(String key, String _value) {
	settings.setProperty(key, _value);

    }

}

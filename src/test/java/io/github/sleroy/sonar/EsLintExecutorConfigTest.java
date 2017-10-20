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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Optional;

import org.assertj.core.util.Files;
import org.junit.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import io.github.sleroy.sonar.api.PathResolver;

public class EsLintExecutorConfigTest {

    @Test
    public void canGetSetPathToTsLint() {
	final EsLintExecutorConfig config = getNewConfig();
	config.setPathToEsLint("My path");

	assertEquals("My path", config.getPathToEsLint());
    }

    @Test
    public void canGetSetPathToTsLintConfig() {
	final EsLintExecutorConfig config = getNewConfig();
	config.setConfigFile("My path");

	assertEquals("My path", config.getConfigFile());
    }

    @Test
    public void canGetSetRulesDir() {
	final EsLintExecutorConfig config = getNewConfig();
	config.setRulesDir("My path");

	assertEquals("My path", config.getRulesDir());
    }

    @Test
    public void canGetSetTimeout() {
	final EsLintExecutorConfig config = getNewConfig();
	config.setTimeoutMs(12);

	assertEquals((Integer) 12, config.getTimeoutMs());
    }

    @Test
    public <T> void fromSettings_checkConfigurationOrder() {
	final GenericConfiguration settings = new GenericConfiguration();

	final FileSystem fileSystemMock = mock(FileSystem.class);
	final SensorContext sensorContextMock = mock(SensorContext.class);
	when(sensorContextMock.fileSystem()).thenReturn(fileSystemMock);
	when(fileSystemMock.baseDir()).thenReturn(Files.currentFolder());
	when(sensorContextMock.config()).thenReturn(settings);
	final PathResolver pathResolver = spy(new PathResolverImpl());

	final EsLintExecutorConfig config = EsLintExecutorConfig.fromSettings(sensorContextMock, pathResolver);
	assertNotEquals("No local Eslint file", EsLintExecutorConfig.CONFIG_JS_FILENAME, config.getConfigFile());

	verify(pathResolver, atLeastOnce()).getPathFromSetting(sensorContextMock, EsLintPlugin.SETTING_ES_LINT_CONFIG_PATH);
	verify(pathResolver, atLeastOnce()).getAbsolutePath(sensorContextMock, EsLintExecutorConfig.CONFIG_FILENAME);
	verify(pathResolver, atLeastOnce()).getAbsolutePath(sensorContextMock, EsLintExecutorConfig.CONFIG_JS_FILENAME);
	verify(pathResolver, atLeastOnce()).getAbsolutePath(sensorContextMock, EsLintExecutorConfig.CONFIG_JSON_FILENAME);
	verify(pathResolver, atLeastOnce()).getAbsolutePath(sensorContextMock, EsLintExecutorConfig.CONFIG_YAML2_FILENAME);
	verify(pathResolver, atLeastOnce()).getAbsolutePath(sensorContextMock, EsLintExecutorConfig.CONFIG_YAML_FILENAME);
    }

    @Test
    public void fromSettings_initialisesFromSettingsAndResolver() {

	final PathResolver resolver = mock(PathResolver.class);

	when(
		resolver.getPathFromSetting(
			any(SensorContext.class), eq(EsLintPlugin.SETTING_ES_LINT_PATH), eq(EsLintExecutorConfig.ESLINT_FALLBACK_PATH)))
				.thenReturn(Optional.of("eslint"));

	when(
		resolver.getPathFromSetting(
			any(SensorContext.class), eq(EsLintPlugin.SETTING_ES_LINT_CONFIG_PATH),
			eq(EsLintExecutorConfig.CONFIG_JS_FILENAME))).thenReturn(Optional.of(".eslintrc.json"));

	when(resolver.getPathFromSetting(any(SensorContext.class), eq(EsLintPlugin.SETTING_ES_LINT_RULES_DIR), eq(null)))
		.thenReturn(Optional.of("rulesdir"));

	final SensorContextTester create = SensorContextTester.create(new File(""));
	create.settings().setProperty(EsLintPlugin.SETTING_ES_LINT_TIMEOUT, 12000);
	final EsLintExecutorConfig config = EsLintExecutorConfig.fromSettings(create, resolver);

	assertEquals("eslint", config.getPathToEsLint());
	assertNull(config.getConfigFile());
	assertEquals("rulesdir", config.getRulesDir());

	assertEquals((Integer) 12000, config.getTimeoutMs());
    }

    @Test
    public void fromSettings_setsTimeoutTo5000msMinimum_ifSetToLess() {

	final PathResolver resolver = mock(PathResolver.class);

	final SensorContextTester create = SensorContextTester.create(Files.currentFolder());
	create.settings().setProperty(EsLintPlugin.SETTING_ES_LINT_TIMEOUT, 1000);
	final EsLintExecutorConfig config = EsLintExecutorConfig.fromSettings(create, resolver);

	assertEquals((Integer) EsLintExecutorConfig.MAX_TIMEOUT, config.getTimeoutMs());
    }

    @Test
    public <T> void fromSettings_testDefaultValues() {

	mock(FileSystem.class);
	final SensorContext sensorContextMock = SensorContextTester.create(Files.currentFolder());
	sensorContextMock.settings().setProperty(EsLintPlugin.SETTING_ES_LINT_TIMEOUT, 12000);

	final PathResolver pathResolver = new PathResolverImpl();

	final EsLintExecutorConfig config = EsLintExecutorConfig.fromSettings(sensorContextMock, pathResolver);
	assertNotEquals("Eslint is not installed locally", EsLintExecutorConfig.ESLINT_FALLBACK_PATH, config.getPathToEsLint());
	assertNotEquals("No local Eslint file", EsLintExecutorConfig.CONFIG_JS_FILENAME, config.getConfigFile());
	assertNull(config.getRulesDir());
	assertEquals((Integer) 12000, config.getTimeoutMs());
    }

    private EsLintExecutorConfig getNewConfig() {
	return new EsLintExecutorConfig();
    }
}

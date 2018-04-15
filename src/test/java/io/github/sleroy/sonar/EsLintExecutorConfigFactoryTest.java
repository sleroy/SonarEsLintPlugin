/*
 * Copyright (C) 2017 Sylvain Leroy - BYOSkill Company All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the MIT license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the MIT license with
 * this file. If not, please write to: sleroy at byoskill.com, or visit : www.byoskill.com
 *
 */
package io.github.sleroy.sonar;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.util.Files;
import org.junit.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;

import io.github.sleroy.sonar.api.PathResolver;

public class EsLintExecutorConfigFactoryTest {
    @Test
    public <T> void fromSettings_checkConfigurationOrder() {
	final GenericConfiguration settings = new GenericConfiguration();

	final FileSystem fileSystemMock = mock(FileSystem.class);
	final SensorContext sensorContextMock = mock(SensorContext.class);
	when(sensorContextMock.fileSystem()).thenReturn(fileSystemMock);
	when(fileSystemMock.baseDir()).thenReturn(Files.currentFolder());
	when(sensorContextMock.config()).thenReturn(settings);
	final PathResolver pathResolver = spy(new PathResolverImpl());

	final EsLintExecutorConfig config = EsLintExecutorConfigFactory.fromSettings(sensorContextMock, pathResolver);
	assertNotEquals("No local Eslint file", EsLintExecutorConfig.CONFIG_JS_FILENAME, config.getConfigFile());

	verify(pathResolver, atLeastOnce()).getPathFromSetting(sensorContextMock,
		EsLintPlugin.SETTING_ES_LINT_CONFIG_PATH);
	verify(pathResolver, atLeastOnce()).getAbsolutePath(sensorContextMock, EsLintExecutorConfig.CONFIG_FILENAME);
	verify(pathResolver, atLeastOnce()).getAbsolutePath(sensorContextMock, EsLintExecutorConfig.CONFIG_JS_FILENAME);
	verify(pathResolver, atLeastOnce()).getAbsolutePath(sensorContextMock,
		EsLintExecutorConfig.CONFIG_JSON_FILENAME);
	verify(pathResolver, atLeastOnce()).getAbsolutePath(sensorContextMock,
		EsLintExecutorConfig.CONFIG_YAML2_FILENAME);
	verify(pathResolver, atLeastOnce()).getAbsolutePath(sensorContextMock,
		EsLintExecutorConfig.CONFIG_YAML_FILENAME);
    }

}

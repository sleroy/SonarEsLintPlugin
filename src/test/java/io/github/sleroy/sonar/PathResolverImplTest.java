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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URL;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

public class PathResolverImplTest {
    private PathResolverImpl	resolver;
    private SensorContextTester	sensorContext;

    private File existingFile;

    @Test
    public void returnsAbsolutePathToFallbackFile_ifPrimaryNotConfiguredAndFallbackExists() {
	assertSamePath(existingFile, resolver.getPathFromSetting(sensorContext, "new path key", "existing.ts"));
    }

    @Test
    public void returnsAbsolutePathToFallbackFile_ifPrimaryNotConfiguredButEmptyAndFallbackExists() {
	sensorContext.settings().setProperty("new path key", "");
	assertSamePath(existingFile, resolver.getPathFromSetting(sensorContext, "new path key", "existing.ts"));
    }

    @Test
    public void returnsAbsolutePathToFile_ifAlreadyAbsoluteAndExists() {
	sensorContext.settings().setProperty("new path key", existingFile.getAbsolutePath());
	assertSamePath(existingFile, resolver.getPathFromSetting(sensorContext, "new path key", "not me"));
    }

    @Test
    public void returnsAbsolutePathToFile_ifSpecifiedAndExists() {

	assertSamePath(existingFile, resolver.getPathFromSetting(sensorContext, "path key", "not me"));
    }

    @Test
    public void returnsNull_ifPrimaryNotConfiguredAndFallbackNull() {
	final Optional<String> pathFromSetting = resolver.getPathFromSetting(sensorContext, "new path key", null);
	assertFalse(pathFromSetting.isPresent());
    }

    @Test
    public void returnsNull_ifRequestedPathDoesNotExist() {
	sensorContext.settings().setProperty("new path key", "missing.ts");
	final Optional<String> pathFromSetting = resolver.getPathFromSetting(sensorContext, "new path key", "notexisting.ts");
	assertFalse(pathFromSetting.isPresent());
    }

    @Before
    public void setUp() throws Exception {
	final URL filePath = PathResolverImplTest.class.getClassLoader().getResource("./existing.ts");
	existingFile = new File(filePath.toURI());
	final String parentPath = existingFile.getParent();

	sensorContext = SensorContextTester.create(new File(parentPath));
	sensorContext.settings().setProperty("path key", "existing.ts");

	final DefaultInputFile file = TestInputFileBuilder.create("bla", "existing.ts").setLanguage(EsLintLanguage.LANGUAGE_KEY).build();

	sensorContext.fileSystem().add(file);

	resolver = new PathResolverImpl();
    }

    /**
     * Asserts that the provided path is the same as the argument. Paths are
     * converted in File objects to avoid case-sensitive issues on some
     * filesystems.
     *
     * @param existingFile
     *            the path to test against
     * @param argument
     *            the argument
     */
    private void assertSamePath(File existingFile, Optional<String> argument) {
	if (!argument.isPresent()) {
	    assertNull("File should not exists", existingFile);
	} else {
	    assertEquals(existingFile, argument.map(f -> new File(f)).get());
	}
    }
}

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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import io.github.sleroy.sonar.model.EsLintIssue;

public class EsLintParserTest {
    /**
     * Tests the output of EsLint when the analysis has failed on a parsing
     * error
     */
    @Test
    public void eslint_parsingFailure() throws IOException {
	final String parseRow1 = FileUtils
		.readFileToString(new File("src/test/resources/results/parsingFailure.json"), Charset.defaultCharset());
	final List<String> toParse = new ArrayList<>();
	toParse.add(parseRow1);

	final Map<String, List<EsLintIssue>> issues = new EsLintParserImpl().parse(toParse);

	assertEquals("Expected one file", 1, issues.size());

	assertEquals(
		"Expected one error, parsing error", 1, issues.get("c:/workspace/SonarTsPlugin/src/test/resources/angular.html").size());
    }

    /**
     * Tests the output of EsLint when no results are returned and the analysis
     * is a success.
     */
    @Test
    public void eslint_successnoresults() {
	final String parseRow1 = "[]";
	final List<String> toParse = new ArrayList<>();
	toParse.add(parseRow1);

	final Map<String, List<EsLintIssue>> issues = new EsLintParserImpl().parse(toParse);

	assertEquals(0, issues.size());
    }

    /**
     * Tests the output of EsLint when the analysis is a success with results
     */
    @Test
    public void eslint_successWithResults() throws IOException {
	final String parseRow1 = FileUtils.readFileToString(new File("src/test/resources/results/ok.json"), Charset.defaultCharset());
	final List<String> toParse = new ArrayList<>();
	toParse.add(parseRow1);

	final Map<String, List<EsLintIssue>> issues = new EsLintParserImpl().parse(toParse);

	assertEquals("Expected one file", 1, issues.size());

	assertEquals(
		"Expected fifty-eight violations", 58, issues.get("c:/workspace/SonarTsPlugin/src/test/resources/dashboard.js").size());
    }

    @Test
    public void parseAGoodProjectWithNoIssues() {
	final List<String> toParse = new ArrayList<>();
	toParse.add("");

	final Map<String, List<EsLintIssue>> issues = new EsLintParserImpl().parse(toParse);

	assertEquals(0, issues.size());
    }
}

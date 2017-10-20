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
import java.util.stream.Stream;

import org.sonar.api.batch.sensor.SensorContext;

import io.github.sleroy.sonar.api.PathResolver;

public class EsLintExecutorConfig {
    public static final String ESLINT_FALLBACK_PATH  = "node_modules" + File.separatorChar + "eslint" + File.separatorChar + "bin"
	    + File.separatorChar + "eslint.js";
    public static final String CONFIG_JS_FILENAME    = ".eslintrc.js";
    public static final String CONFIG_JSON_FILENAME  = ".eslintrc.json";
    public static final String CONFIG_YAML_FILENAME  = ".eslintrc.yml";
    public static final String CONFIG_YAML2_FILENAME = ".eslintrc.yaml";
    public static final String CONFIG_FILENAME	     = ".eslintrc";
    public static final int    MAX_TIMEOUT	     = 10000;

    public static EsLintExecutorConfig fromSettings(SensorContext ctx, PathResolver resolver) {
	final EsLintExecutorConfig toReturn = new EsLintExecutorConfig();

	resolver.getPathFromSetting(ctx, EsLintPlugin.SETTING_ES_LINT_PATH, EsLintExecutorConfig.ESLINT_FALLBACK_PATH).ifPresent(
		f -> toReturn.pathToEsLint = f);

	try (Stream< Optional<String>>stream = Stream.of(
		resolver.getPathFromSetting(ctx, EsLintPlugin.SETTING_ES_LINT_CONFIG_PATH),
		resolver.getAbsolutePath(ctx, EsLintExecutorConfig.CONFIG_FILENAME),
		resolver.getAbsolutePath(ctx, EsLintExecutorConfig.CONFIG_JS_FILENAME),
		resolver.getAbsolutePath(ctx, EsLintExecutorConfig.CONFIG_JSON_FILENAME),
		resolver.getAbsolutePath(ctx, EsLintExecutorConfig.CONFIG_YAML2_FILENAME),
		resolver.getAbsolutePath(ctx, EsLintExecutorConfig.CONFIG_YAML_FILENAME))) {
	    stream.filter(Optional::isPresent).map(Optional::get).findFirst().ifPresent(f -> toReturn.configFile = f);
	}

	resolver.getPathFromSetting(ctx, EsLintPlugin.SETTING_ES_LINT_RULES_DIR, null).ifPresent(f -> toReturn.rulesDir = f);

	toReturn.timeoutMs = Math.max(
		EsLintExecutorConfig.MAX_TIMEOUT,
		ctx.config().getInt(EsLintPlugin.SETTING_ES_LINT_TIMEOUT).orElse(EsLintExecutorConfig.MAX_TIMEOUT));

	return toReturn;
    }

    private String pathToEsLint;
    private String configFile;

    private String rulesDir;

    private Integer timeoutMs;

    public String getConfigFile() {
	return configFile;
    }

    public String getPathToEsLint() {
	return pathToEsLint;
    }

    public String getRulesDir() {
	return rulesDir;
    }

    public Integer getTimeoutMs() {
	return timeoutMs;
    }

    public void setConfigFile(String configFile) {
	this.configFile = configFile;
    }

    public void setPathToEsLint(String pathToEsLint) {
	this.pathToEsLint = pathToEsLint;
    }

    public void setRulesDir(String rulesDir) {
	this.rulesDir = rulesDir;
    }

    public void setTimeoutMs(Integer timeoutMs) {
	this.timeoutMs = timeoutMs;
    }

}

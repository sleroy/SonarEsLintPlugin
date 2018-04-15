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

import java.io.File;
import java.util.Objects;

public class EsLintExecutorConfig {
    public static final String ESLINT_FALLBACK_PATH  = "node_modules" + File.separatorChar + "eslint"
	    + File.separatorChar + "bin"
	    + File.separatorChar + "eslint.js";
    public static final String CONFIG_JS_FILENAME    = ".eslintrc.js";
    public static final String CONFIG_JSON_FILENAME  = ".eslintrc.json";
    public static final String CONFIG_YAML_FILENAME  = ".eslintrc.yml";
    public static final String CONFIG_YAML2_FILENAME = ".eslintrc.yaml";
    public static final String CONFIG_FILENAME	     = ".eslintrc";
    public static final int    MAX_TIMEOUT	     = 10000;

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

    /**
     * Checks if is path to eslint is the same at the argument
     *
     * @param eslintPath
     *            the eslintPath
     * @return true, if both path are similar
     */
    public boolean isPathToEsLint(final String eslintPath) {
	return Objects.equals(pathToEsLint, eslintPath);
    }

    public void setConfigFile(final String configFile) {
	this.configFile = configFile;
    }

    public void setPathToEsLint(final String pathToEsLint) {
	this.pathToEsLint = pathToEsLint;
    }

    public void setRulesDir(final String rulesDir) {
	this.rulesDir = rulesDir;
    }

    public void setTimeoutMs(final Integer timeoutMs) {
	this.timeoutMs = timeoutMs;
    }

    @Override
    public String toString() {
	return "EsLintExecutorConfig [pathToEsLint=" + pathToEsLint + ", configFile=" + configFile + ", rulesDir="
		+ rulesDir + ", timeoutMs=" + timeoutMs + "]";
    }

}

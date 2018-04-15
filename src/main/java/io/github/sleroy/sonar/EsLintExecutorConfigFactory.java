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

import java.util.Optional;
import java.util.stream.Stream;

import org.sonar.api.batch.sensor.SensorContext;

import io.github.sleroy.sonar.api.PathResolver;

/**
 * A factory for creating EsLintExecutorConfig objects.
 */
public class EsLintExecutorConfigFactory {

    private static int evaluateTimeoutSetting(final SensorContext ctx) {
	return Math.max(
		EsLintExecutorConfig.MAX_TIMEOUT,
		ctx.config().getInt(EsLintPlugin.SETTING_ES_LINT_TIMEOUT).orElse(EsLintExecutorConfig.MAX_TIMEOUT));
    }

    /**
     * Buildsw the configuration from the settings.
     *
     * @param ctx
     *            the ctx
     * @param resolver
     *            the resolver
     * @return the es lint executor config
     */
    public static EsLintExecutorConfig fromSettings(final SensorContext ctx, final PathResolver resolver) {
	final EsLintExecutorConfig toReturn = new EsLintExecutorConfig();

	resolver.getPathFromSetting(ctx, EsLintPlugin.SETTING_ES_LINT_PATH, EsLintExecutorConfig.ESLINT_FALLBACK_PATH)
		.ifPresent(f -> toReturn.setPathToEsLint(f));

	try (Stream<Optional<String>> stream = Stream.of(
		resolver.getPathFromSetting(ctx, EsLintPlugin.SETTING_ES_LINT_CONFIG_PATH),
		resolver.getAbsolutePath(ctx, EsLintExecutorConfig.CONFIG_FILENAME),
		resolver.getAbsolutePath(ctx, EsLintExecutorConfig.CONFIG_JS_FILENAME),
		resolver.getAbsolutePath(ctx, EsLintExecutorConfig.CONFIG_JSON_FILENAME),
		resolver.getAbsolutePath(ctx, EsLintExecutorConfig.CONFIG_YAML2_FILENAME),
		resolver.getAbsolutePath(ctx, EsLintExecutorConfig.CONFIG_YAML_FILENAME))) {
	    stream.filter(Optional::isPresent).map(Optional::get).findFirst()
		    .ifPresent(path -> toReturn.setConfigFile(path));
	}

	resolver.getPathFromSetting(ctx, EsLintPlugin.SETTING_ES_LINT_RULES_DIR, null)
		.ifPresent(path -> toReturn.setRulesDir(path));

	toReturn.setTimeoutMs(evaluateTimeoutSetting(ctx));

	return toReturn;
    }

}

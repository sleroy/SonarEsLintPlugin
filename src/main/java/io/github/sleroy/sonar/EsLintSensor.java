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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;

import io.github.sleroy.sonar.api.EsLintExecutor;
import io.github.sleroy.sonar.api.EsLintParser;
import io.github.sleroy.sonar.api.PathResolver;
import io.github.sleroy.sonar.model.EsLintIssue;

public class EsLintSensor implements Sensor {
    private static final Logger LOG = LoggerFactory.getLogger(EsLintSensor.class);

    private final Configuration settings;
    private final PathResolver resolver;
    private final EsLintExecutor executor;
    private final EsLintParser parser;

    public EsLintSensor(final Configuration settings, final PathResolver resolver, final EsLintExecutor executor,
                        final EsLintParser parser) {
        this.settings = settings;
        this.resolver = resolver;
        this.executor = executor;
        this.parser = parser;
    }

    /**
     * Builds the file map with JS files.
     *
     * @param ctx   the ctx
     * @param paths the paths
     * @return the map
     */
    private Map<String, InputFile> buildFileMapWithJSFiles(final SensorContext ctx, final List<String> paths) {
        final Map<String, InputFile> fileMap = new HashMap<>(100);
        for (final InputFile file : ctx.fileSystem()
            .inputFiles(ctx.fileSystem().predicates().hasLanguage(EsLintLanguage.LANGUAGE_KEY))) {

            final String pathAdjusted = file.absolutePath();
            paths.add(pathAdjusted);
            fileMap.put(pathAdjusted, file);
        }
        LOG.info("Build filemap with {} JS files", fileMap.size());
        return fileMap;
    }

    private Set<String> buildRuleNameSet(final Collection<ActiveRule> allRules) {
        final Set<String> ruleNames = new HashSet<>(allRules.size());
        ruleNames.addAll(allRules.stream().map(rule -> rule.ruleKey().rule()).collect(Collectors.toList()));
        return ruleNames;
    }

    @Override
    public void describe(final SensorDescriptor desc) {
        desc.name("Linting sensor for Javascript files").onlyOnLanguage(EsLintLanguage.LANGUAGE_KEY);
    }

    @Override
    public void execute(final SensorContext ctx) {
        if (!settings.getBoolean(EsLintPlugin.SETTING_ES_LINT_ENABLED).orElse(Boolean.FALSE)) {
            LOG.debug("Skipping eslint execution - {} set to false", EsLintPlugin.SETTING_ES_LINT_ENABLED);
            return;
        }

        final EsLintExecutorConfig config = EsLintExecutorConfigFactory.fromSettings(ctx, resolver);

        if (config.getPathToEsLint() == null) {
            LOG.warn("Path to eslint not defined or not found. Skipping eslint analysis.");
            return;
        }
        if (config.getConfigFile() == null) {
            LOG.warn(
                "Path to .eslintrc.* configuration file either not defined or not found - Skipping eslint analysis.");
            return;
        }

        final Collection<ActiveRule> allRules = ctx.activeRules().findByRepository(EsRulesDefinition.REPOSITORY_NAME);
        LOG.info("ESLint plugin is embedded with a profile containing {} rules", allRules.size());

        final Set<String> ruleNames = buildRuleNameSet(allRules);

        final List<String> paths = new ArrayList<>(100);
        final Map<String, InputFile> fileMap = buildFileMapWithJSFiles(ctx, paths);

        // Execute the ESLint plugin and obtain JSON Results
        final List<String> jsonResults = executor.execute(config, paths);
        LOG.debug("Obtained {} JSON Results", jsonResults.size());
        // Parse the ESLint issues
        final Map<String, List<EsLintIssue>> issues = parser.parse(jsonResults);

        if (issues == null) {
            LOG.warn("Eslint returned no result at all");
            return;
        }
        LOG.info("{} Files have been analyzed", issues.size());

        // Each issue bucket will contain info about a single file
        for (final Entry<String, List<EsLintIssue>> filePathEntry : issues.entrySet()) {
            final List<EsLintIssue> batchIssues = filePathEntry.getValue();

            if (batchIssues == null || batchIssues.isEmpty()) {
                LOG.debug("The file {} has no issue", filePathEntry.getKey());
                continue;
            }

            final String filePath = filePathEntry.getKey();
            if (!fileMap.containsKey(filePath)) {
                LOG.warn("EsLint reported issues against a file that wasn't sent to it - will be ignored: {}",
                    filePath);
                continue;
            }

            final InputFile file = fileMap.get(filePath);

            for (final EsLintIssue issue : batchIssues) {

                final String ruleName = obtainRuleNameToAssociateThisIssue(ruleNames, issue);

                final NewIssue newIssue = ctx.newIssue()
                    .forRule(RuleKey.of(EsRulesDefinition.REPOSITORY_NAME, ruleName));

                final NewIssueLocation newIssueLocation = newIssue
                    .newLocation().on(file).message(issue.getMessage()).at(file.selectLine(issue.getLine()));

                newIssue.at(newIssueLocation);
                newIssue.save();
            }
        }
    }

    /**
     * Obtain rule name to associate this issue.
     *
     * @param ruleNames the rule names
     * @param issue     the issue
     * @return the string
     */
    private String obtainRuleNameToAssociateThisIssue(final Set<String> ruleNames, final EsLintIssue issue) {
        // Make sure the rule we're violating is one we recognise - if
        // not, we'll
        // fall back to the generic 'eslint-issue' rule
        String ruleName = "";
        if (issue.getRuleId() != null) {
            ruleName = issue.getRuleId().replace('/', '-');
        } else {
            LOG.warn("An issue has returned no Rule ID : {}", issue);
        }
        final boolean isNotRecognizedRule = issue.getRuleId() == null || !ruleNames.contains(ruleName);
        if (isNotRecognizedRule) {
            LOG.info("Rule {} has not yet being defined into the EsLint plugin", ruleName);
            ruleName = EsRulesDefinition.ESLINT_UNKNOWN_RULE.getKey();
        }
        return ruleName;
    }
}

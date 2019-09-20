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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.debt.DebtRemediationFunction.Type;
import org.sonar.api.server.rule.RulesDefinition;

import io.github.sleroy.sonar.model.EsLintRule;

public class EsRulesDefinitionTest {

    Configuration settings;
    EsRulesDefinition definition;
    RulesDefinition.Context context;

    @Test
    public void CheckAdditionalRulesConfigProvided() {
        final EsRulesDefinition rulesDef = new EsRulesDefinition(settings);
        final List<EsLintRule> rules = rulesDef.getRules();
        assertNotNull(rules);
        assertEquals(1, rules.size()); // 4 enabled rules, 1 disabled rule
    }

    @Test
    public void CheckCustomRulesConfigNotProvided() {

        final Configuration settings = mock(Configuration.class);
        when(settings.get(EsLintPlugin.SETTING_ES_RULE_CONFIGS)).thenReturn(Optional.of("src/test/resources/eslint-rules-test.properties"));

        final EsRulesDefinition rulesDef = new EsRulesDefinition(settings);
        final List<EsLintRule> rules = rulesDef.getRules();
        assertNotNull(rules);
        assertEquals(1, rules.size());
    }

    @Test
    public void ConfiguresAdditionalRules() {


        // cfg2
        final RulesDefinition.Rule rule2 = getRule("react-rule1");
        assertNotNull(rule2);
        assertEquals("disallow to reference modules with variables and require to use the getter syntax instead angular.module('myModule') (y022)", rule2.name());


    }

    @Test
    public void CreatesRepository() {
        final RulesDefinition.Context context = mock(RulesDefinition.Context.class, RETURNS_DEEP_STUBS);
        definition.define(context);

        verify(context).createRepository(eq(EsRulesDefinition.REPOSITORY_NAME), eq(EsLintLanguage.LANGUAGE_KEY));
    }

    @Test
    public void LoadRulesFromInvalidStream() throws IOException {
        final List<EsLintRule> rules = new ArrayList<>();
        final InputStream testStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Test exception");
            }
        };
        EsRulesDefinition.loadRules(testStream, rules);
    }

    @Before
    public void setUp() throws Exception {

        settings = mock(Configuration.class);

        when(settings.get(EsLintPlugin.SETTING_ES_RULE_CONFIGS)).thenReturn(Optional.of("src/test/resources/eslint-rules-test.properties"));


        definition = new EsRulesDefinition(settings);
        context = new RulesDefinition.Context();
        definition.define(context);
    }

    private RulesDefinition.Rule getRule(String name) {
        RulesDefinition.Repository repository = context.repository(EsRulesDefinition.REPOSITORY_NAME);
        return repository.rule(name);
    }
}

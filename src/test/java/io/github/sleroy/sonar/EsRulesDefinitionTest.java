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

    Configuration	    settings;
    EsRulesDefinition	    definition;
    RulesDefinition.Context context;

    @Test
    public void CheckAdditionalRulesConfigProvided() {
	final EsRulesDefinition rulesDef = new EsRulesDefinition(settings);
	final List<EsLintRule> rules = rulesDef.getRules();
	assertNotNull(rules);
	assertEquals(4, rules.size()); // 4 enabled rules, 1 disabled rule
    }

    @Test
    public void CheckCustomRulesConfigNotProvided() {

	final Configuration settings = mock(Configuration.class);
	when(settings.getStringArray(EsLintPlugin.SETTING_ES_RULE_CONFIGS)).thenReturn(new String[0]);

	final EsRulesDefinition rulesDef = new EsRulesDefinition(settings);
	final List<EsLintRule> rules = rulesDef.getRules();
	assertNotNull(rules);
	assertEquals(0, rules.size());
    }

    @Test
    public void ConfiguresAdditionalRules() {
	// cfg1
	final RulesDefinition.Rule rule1 = getRule("custom-rule-1");
	assertNull(rule1);

	// cfg2
	final RulesDefinition.Rule rule2 = getRule("custom-rule-2");
	assertNotNull(rule2);
	assertEquals("test rule #2", rule2.name());
	assertEquals(Severity.MINOR, rule2.severity());
	assertEquals("#2 description", rule2.htmlDescription());
	assertEquals(null, rule2.debtRemediationFunction());
	assertEquals(RuleType.CODE_SMELL, rule2.type());

	// cfg3
	final RulesDefinition.Rule rule3 = getRule("custom-rule-3");
	assertNotNull(rule3);
	assertEquals("test rule #3", rule3.name());
	assertEquals(Severity.INFO, rule3.severity());
	assertEquals("#3 description", rule3.htmlDescription());
	assertEquals(Type.CONSTANT_ISSUE, rule3.debtRemediationFunction().type());
	assertEquals(null, rule3.debtRemediationFunction().gapMultiplier());
	assertEquals("15min", rule3.debtRemediationFunction().baseEffort());
	assertEquals(RuleType.CODE_SMELL, rule3.type());

	// cfg4
	final RulesDefinition.Rule rule4 = getRule("custom-rule-4");
	assertNotNull(rule4);
	assertEquals("test rule #4", rule4.name());
	assertEquals(Severity.MINOR, rule4.severity());
	assertEquals("#4 description", rule4.htmlDescription());
	assertEquals(Type.LINEAR, rule4.debtRemediationFunction().type());
	assertEquals("5min", rule4.debtRemediationFunction().gapMultiplier());
	assertEquals(null, rule4.debtRemediationFunction().baseEffort());
	assertEquals(RuleType.BUG, rule4.type());

	// cfg5
	final RulesDefinition.Rule rule5 = getRule("custom-rule-5");
	assertNotNull(rule5);
	assertEquals("test rule #5", rule5.name());
	assertEquals(Severity.MAJOR, rule5.severity());
	assertEquals("#5 description", rule5.htmlDescription());
	assertEquals(RuleType.VULNERABILITY, rule5.type());

	assertEquals("30min", rule5.debtRemediationFunction().gapMultiplier());
	assertEquals("15min", rule5.debtRemediationFunction().baseEffort());
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

	when(settings.getStringArray(EsLintPlugin.SETTING_ES_RULE_CONFIGS)).thenReturn(
		new String[] { EsLintPlugin.SETTING_ES_RULE_CONFIGS + ".cfg1.config", EsLintPlugin.SETTING_ES_RULE_CONFIGS + ".cfg2.config",
			EsLintPlugin.SETTING_ES_RULE_CONFIGS + ".cfg3.config" });

	// config with one disabled rule
	when(settings.get(EsLintPlugin.SETTING_ES_RULE_CONFIGS + ".cfg1.config")).thenReturn(
		Optional.of(
			"custom-rule-1=false\n" + "custom-rule-1.name=test rule #1\n" + "custom-rule-1.severity=MAJOR\n"
				+ "custom-rule-1.description=#1 description\n" + "custom-rule-1.tags=angular,eslint, typscript\n" + "\n"));

	// config with a basic rule (no debt settings)
	when(settings.get(EsLintPlugin.SETTING_ES_RULE_CONFIGS + ".cfg2.config")).thenReturn(
		Optional.of(
			"custom-rule-2=true\n" + "custom-rule-2.name=test rule #2\n" + "custom-rule-2.severity=MINOR\n"
				+ "custom-rule-2.description=#2 description\n" + "\n"));

	// config with a advanced rules (including debt settings)
	when(settings.get(EsLintPlugin.SETTING_ES_RULE_CONFIGS + ".cfg3.config")).thenReturn(
		Optional.of(
			"custom-rule-3=true\n" + "custom-rule-3.name=test rule #3\n" + "custom-rule-3.severity=INFO\n"
				+ "custom-rule-3.description=#3 description\n" + "custom-rule-3.debtFunc=" + Type.CONSTANT_ISSUE + "\n"
				+ "custom-rule-3.debtScalar=15min\n" + "custom-rule-3.debtOffset=1min\n"
				+ "custom-rule-3.debtType=INVALID_TYPE_GOES_HERE\n" + "\n" + "custom-rule-4=true\n"
				+ "custom-rule-4.name=test rule #4\n" + "custom-rule-4.severity=MINOR\n"
				+ "custom-rule-4.description=#4 description\n" + "custom-rule-4.debtFunc=" + Type.LINEAR + "\n"
				+ "custom-rule-4.debtScalar=5min\n" + "custom-rule-4.debtOffset=2h\n" + "custom-rule-4.debtType="
				+ RuleType.BUG.name() + "\n" + "\n" + "custom-rule-5=true\n" + "custom-rule-5.name=test rule #5\n"
				+ "custom-rule-5.severity=MAJOR\n" + "custom-rule-5.description=#5 description\n"
				+ "custom-rule-5.debtFunc=" + Type.LINEAR_OFFSET + "\n" + "custom-rule-5.debtScalar=30min\n"
				+ "custom-rule-5.debtOffset=15min\n" + "custom-rule-5.debtType=" + RuleType.VULNERABILITY.name() + "\n"
				+ "\n"));

	definition = new EsRulesDefinition(settings);
	context = new RulesDefinition.Context();
	definition.define(context);
    }

    private RulesDefinition.Rule getRule(String name) {
	return context.repository(EsRulesDefinition.REPOSITORY_NAME).rule(name);
    }
}

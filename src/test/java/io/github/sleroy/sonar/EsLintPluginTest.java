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
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.Plugin.Context;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

public class EsLintPluginTest {
    public static final int EXPECTED_PROPERTIES = 6;

    private static Optional<Property> findPropertyByName(Property[] properties, String name) {

	return Stream.of(properties).filter(p -> p.key().equals(name)).findFirst();
    }

    EsLintPlugin plugin;

    @Test
    public void advertisesAppropriateExtensions() {
	final Context context = new Context(SonarRuntimeImpl.forSonarLint(Version.create(6, 2)));

	plugin.define(context);

	final List extensions = context.getExtensions();

	assertTrue(extensions.contains(EsLintRuleProfile.class));
	assertTrue(extensions.contains(EsLintLanguage.class));
	assertTrue(extensions.contains(EsLintSensor.class));
	assertTrue(extensions.contains(EsRulesDefinition.class));
    }

    @Test
    public void decoratedWithPropertiesAnnotation() {
	final Annotation[] annotations = plugin.getClass().getAnnotations();

	assertEquals(1, annotations.length);
	assertEquals(Properties.class, annotations[0].annotationType());
    }

    @Test
    public void definesExpectedProperties() {
	final Annotation annotation = plugin.getClass().getAnnotations()[0];
	final Properties propertiesAnnotation = (Properties) annotation;

	assertEquals(EsLintPluginTest.EXPECTED_PROPERTIES, propertiesAnnotation.value().length);

	final Property[] properties = propertiesAnnotation.value();

	assertNotNull(EsLintPluginTest.findPropertyByName(properties, EsLintPlugin.SETTING_ES_LINT_ENABLED));
	assertNotNull(EsLintPluginTest.findPropertyByName(properties, EsLintPlugin.SETTING_ES_LINT_PATH));
	assertNotNull(EsLintPluginTest.findPropertyByName(properties, EsLintPlugin.SETTING_ES_LINT_CONFIG_PATH));
	assertNotNull(EsLintPluginTest.findPropertyByName(properties, EsLintPlugin.SETTING_ES_LINT_TIMEOUT));
	assertNotNull(EsLintPluginTest.findPropertyByName(properties, EsLintPlugin.SETTING_ES_LINT_RULES_DIR));
	assertNotNull(EsLintPluginTest.findPropertyByName(properties, EsLintPlugin.SETTING_ES_RULE_CONFIGS));
    }

    @Test
    public void ruleConfigsSetting_definedAppropriately() {
	final Property property = this.findPropertyByName(EsLintPlugin.SETTING_ES_RULE_CONFIGS).get();

	assertEquals(PropertyType.STRING, property.type());
	assertEquals("", property.defaultValue());
	assertEquals(false, property.project());
	assertEquals(true, property.global());
	assertEquals(2, property.fields().length);

	// name
	assertEquals("name", property.fields()[0].key());
	assertEquals(PropertyType.STRING, property.fields()[0].type());

	// config
	assertEquals("config", property.fields()[1].key());
	assertEquals(PropertyType.TEXT, property.fields()[1].type());
	assertEquals(120, property.fields()[1].indicativeSize());
    }

    @Test
    public void rulesDirSetting_definedAppropriately() {
	final Property property = this.findPropertyByName(EsLintPlugin.SETTING_ES_LINT_RULES_DIR).get();

	assertEquals(PropertyType.STRING, property.type());
	assertEquals("", property.defaultValue());
	assertEquals(true, property.project());
	assertEquals(false, property.global());
    }

    @Before
    public void setUp() throws Exception {
	plugin = new EsLintPlugin();
    }

    @After
    public void tearDown() throws Exception {
	//
    }

    @Test
    public void tsLintPathSetting_definedAppropriately() {
	final Property property = this.findPropertyByName(EsLintPlugin.SETTING_ES_LINT_PATH).get();

	assertEquals(PropertyType.STRING, property.type());
	assertEquals("", property.defaultValue());
	assertEquals(true, property.project());
	assertEquals(true, property.global());
    }

    @Test
    public void tsLintTimeoutSettings_definedAppropriately() {
	final Property property = this.findPropertyByName(EsLintPlugin.SETTING_ES_LINT_TIMEOUT).get();

	assertEquals(PropertyType.INTEGER, property.type());
	assertEquals("60000", property.defaultValue());
	assertEquals(true, property.project());
	assertEquals(false, property.global());
    }

    private Optional<Property> findPropertyByName(String property) {
	return EsLintPluginTest.findPropertyByName(((Properties) plugin.getClass().getAnnotations()[0]).value(), property);
    }
}

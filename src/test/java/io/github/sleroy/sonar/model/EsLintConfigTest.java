/*
 * Copyright (C) 2017 Sylvain Leroy - BYOS Company All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the MIT license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the MIT license with
 * this file. If not, please write to: contact@sylvainleroy.com, or visit : https://sylvainleroy.com
 */
package io.github.sleroy.sonar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EsLintConfigTest {
    EsLintConfig model;

    @Test
    public void addRule_WithBoolean_CreatesRule() {
	model.addEnabledRule("the rule");

	assertTrue(model.getRules().containsKey("the rule"));
	assertEquals(true, model.getRules().get("the rule"));
    }

    @Test
    public void addRule_WithObjects_CreatesRule() {
	model.addRuleWithArgs("the rule", 1, "string");

	assertTrue(model.getRules().containsKey("the rule"));

	final Object[] ruleParams = (Object[]) model.getRules().get("the rule");

	assertNotNull(ruleParams);
	assertTrue(ruleParams[0].equals(1));
	assertTrue(ruleParams[1].equals("string"));
    }

    @Test
    public void getRules_DoesNotReturnNull() {
	assertNotNull(model.getRules());
    }

    @Before
    public void setUp() throws Exception {
	model = new EsLintConfig();
    }

    @After
    public void tearDown() throws Exception {
	//
    }
}

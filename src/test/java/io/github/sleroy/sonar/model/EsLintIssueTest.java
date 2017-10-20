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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EsLintIssueTest {
    EsLintIssue model;

    @Test
    public void getSetFailure() {
	model.setMessage("the failure");
	assertEquals("the failure", model.getMessage());
    }

    @Test
    public void getSetName() {
	model.setName("the file");
	assertEquals("the file", model.getName());
    }

    @Test
    public void getSetRuleName() {
	model.setRuleId("the rule");
	assertEquals("the rule", model.getRuleId());
    }

    @Before
    public void setUp() throws Exception {
	model = new EsLintIssue();
    }

    @After
    public void tearDown() throws Exception {
	//
    }
}

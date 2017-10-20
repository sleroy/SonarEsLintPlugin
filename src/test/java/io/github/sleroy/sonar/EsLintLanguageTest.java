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

import org.junit.After;
import org.junit.Before;

public class EsLintLanguageTest {
    EsLintLanguage language;

    @Before
    public void setUp() throws Exception {
	language = new EsLintLanguage();
    }

    @After
    public void tearDown() throws Exception {
	//
    }

}

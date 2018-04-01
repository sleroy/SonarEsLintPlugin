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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.ScannerSide;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.sleroy.sonar.api.EsLintParser;
import io.github.sleroy.sonar.model.EsLintFile;
import io.github.sleroy.sonar.model.EsLintIssue;

@ScannerSide
public class EsLintParserImpl implements EsLintParser {
    private static final Logger	 LOG		 = LoggerFactory.getLogger(EsLintParserImpl.class);
    private static final Pattern REPLACE_PATTERN = Pattern.compile("\\]\\[");

    private static String getFixedUpOutput(final String toParse) {
	if (toParse.contains("][")) {
	    // Pre 4.0.0-versions of TsLint return nonsense for its JSON output
	    // when faced with multiple files so we need to fix it up before we
	    // do anything else
	    return EsLintParserImpl.REPLACE_PATTERN.matcher(toParse).replaceAll(",");
	}

	return toParse;
    }

    @Override
    public @Nonnull Map<String, List<EsLintIssue>> parse(final List<String> toParse) {
	final GsonBuilder builder = new GsonBuilder();
	final Gson gson = builder.create();

	final List<EsLintFile> allScannedFiles = new ArrayList<>(100);

	for (final String batch : toParse) {
	    final EsLintFile[] batchIssues = gson.fromJson(EsLintParserImpl.getFixedUpOutput(batch),
		    EsLintFile[].class);

	    if (batchIssues == null) {
		continue;
	    }
	    allScannedFiles.addAll(Arrays.asList(batchIssues));
	}

	// Remap by filename
	final Map<String, List<EsLintFile>> fileBag = allScannedFiles
		.stream().collect(Collectors.groupingBy(f -> f.getFilePath().replace('\\', '/')));

	// Reduce all issues
	final Map<String, List<EsLintIssue>> toIssues = fileBag.entrySet().stream()
		.collect(Collectors.toMap(
			Entry::getKey, v -> v.getValue().stream().map(EsLintFile::getMessages)
				.flatMap(List::stream)
				.collect(Collectors.toList())));

	return toIssues;
    }
}
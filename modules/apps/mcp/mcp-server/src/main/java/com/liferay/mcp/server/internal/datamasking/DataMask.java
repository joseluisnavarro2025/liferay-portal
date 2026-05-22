/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.internal.datamasking;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jose Luis Navarro
 */
public class DataMask {

	public DataMask(
		String externalReferenceCode, String name, Pattern detectionPattern,
		Pattern replacementPattern, String replacementValue) {

		_externalReferenceCode = externalReferenceCode;
		_name = name;
		_detectionPattern = detectionPattern;
		_replacementPattern = replacementPattern;
		_replacementValue = replacementValue;
	}

	public ApplyResult apply(String text) {
		Matcher matcher = _detectionPattern.matcher(text);

		StringBuilder sb = new StringBuilder();

		int matchCount = 0;

		while (matcher.find()) {
			matchCount++;

			if (_replacementPattern == null) {
				matcher.appendReplacement(sb, _replacementValue);
			}
			else {
				String matchedText = matcher.group();

				String replaced = _replacementPattern.matcher(
					matchedText
				).replaceAll(
					_replacementValue
				);

				matcher.appendReplacement(
					sb, Matcher.quoteReplacement(replaced));
			}
		}

		if (matchCount == 0) {
			return new ApplyResult(text, 0);
		}

		matcher.appendTail(sb);

		return new ApplyResult(sb.toString(), matchCount);
	}

	public String getExternalReferenceCode() {
		return _externalReferenceCode;
	}

	public String getName() {
		return _name;
	}

	public static final class ApplyResult {

		public ApplyResult(String text, int matchCount) {
			_text = text;
			_matchCount = matchCount;
		}

		public int getMatchCount() {
			return _matchCount;
		}

		public String getText() {
			return _text;
		}

		private final int _matchCount;
		private final String _text;

	}

	private final Pattern _detectionPattern;
	private final String _externalReferenceCode;
	private final String _name;
	private final Pattern _replacementPattern;
	private final String _replacementValue;

}
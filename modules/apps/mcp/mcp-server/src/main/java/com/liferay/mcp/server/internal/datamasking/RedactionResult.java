/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.internal.datamasking;

import java.util.Map;

/**
 * @author Jose Luis Navarro
 */
public class RedactionResult {

	public RedactionResult(
		String text, Map<String, Integer> matchCountsByExternalReferenceCode) {

		_text = text;
		_matchCountsByExternalReferenceCode =
			matchCountsByExternalReferenceCode;
	}

	public Map<String, Integer> getMatchCountsByExternalReferenceCode() {
		return _matchCountsByExternalReferenceCode;
	}

	public String getText() {
		return _text;
	}

	public int getTotalMatchCount() {
		int total = 0;

		for (int matchCount : _matchCountsByExternalReferenceCode.values()) {
			total += matchCount;
		}

		return total;
	}

	private final Map<String, Integer> _matchCountsByExternalReferenceCode;
	private final String _text;

}
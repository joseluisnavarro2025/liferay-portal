/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.petra.lang.CentralizedThreadLocal;

/**
 * @author Jose Luis Navarro
 */
public class MCPServerProfileDataMaskThreadLocal {

	public static boolean isSkipDeleteReasonValidation() {
		return _skipDeleteReasonValidation.get();
	}

	public static void setSkipDeleteReasonValidation(
		boolean skipDeleteReasonValidation) {

		_skipDeleteReasonValidation.set(skipDeleteReasonValidation);
	}

	private static final ThreadLocal<Boolean> _skipDeleteReasonValidation =
		new CentralizedThreadLocal<>(
			MCPServerProfileDataMaskThreadLocal.class +
				"._skipDeleteReasonValidation",
			() -> Boolean.FALSE);

}
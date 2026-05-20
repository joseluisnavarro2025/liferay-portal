/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.internal.datamasking;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Jose Luis Navarro
 */
public class DataMaskTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testApplyAppliesReplacementRegexCaptureGroups() {
		DataMask dataMask = new DataMask(
			"IPv4 → /24", _ipv4DetectionPattern, _ipv4ReplacementPattern,
			"$1.0/24");

		Assert.assertEquals(
			"Connected from 192.168.1.0/24",
			dataMask.apply("Connected from 192.168.1.42"));
	}

	@Test
	public void testApplyHandlesEmptyText() {
		Assert.assertEquals("", _emailMask().apply(""));
	}

	@Test
	public void testApplyReplacesEveryMatchWithLiteralValue() {
		Assert.assertEquals(
			"From [EMAIL_ADDRESS] to [EMAIL_ADDRESS]",
			_emailMask().apply("From a@b.com to c@d.org"));
	}

	@Test
	public void testApplyReplacesSingleMatchWithLiteralValue() {
		Assert.assertEquals(
			"Contact: [EMAIL_ADDRESS]",
			_emailMask().apply("Contact: alice@example.com"));
	}

	@Test
	public void testApplyReturnsSameStringWhenNoMatch() {
		String input = "No email here at all.";

		Assert.assertSame(input, _emailMask().apply(input));
	}

	private DataMask _emailMask() {
		return new DataMask(
			"Email addresses", _emailDetectionPattern, null, "[EMAIL_ADDRESS]");
	}

	private static final Pattern _emailDetectionPattern = Pattern.compile(
		"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
	private static final Pattern _ipv4DetectionPattern = Pattern.compile(
		"\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");
	private static final Pattern _ipv4ReplacementPattern = Pattern.compile(
		"(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\.\\d{1,3}");

}
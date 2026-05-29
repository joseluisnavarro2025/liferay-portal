/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.data.masking.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.data.masking.client.dto.v1_0.DataMaskValidationRequest;
import com.liferay.headless.data.masking.client.dto.v1_0.DataMaskValidationResult;
import com.liferay.headless.data.masking.client.problem.Problem;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jose Luis Navarro
 */
@RunWith(Arquillian.class)
public class DataMaskResourceTest extends BaseDataMaskResourceTestCase {

	@Override
	@Test
	public void testValidateDataMask() throws Exception {
		DataMaskValidationRequest dataMaskTestRequest =
			new DataMaskValidationRequest();

		dataMaskTestRequest.setDetectionRegex(_EMAIL_DETECTION_REGEX);
		dataMaskTestRequest.setReplacementValue("[EMAIL]");
		dataMaskTestRequest.setSampleText(
			"From alice@example.com to bob@example.org");

		DataMaskValidationResult dataMaskTestResult =
			dataMaskResource.validateDataMask(dataMaskTestRequest);

		Assert.assertNull(dataMaskTestResult.getError());
		Assert.assertEquals(
			Integer.valueOf(2), dataMaskTestResult.getMatchCount());
		Assert.assertEquals(
			"From [EMAIL] to [EMAIL]", dataMaskTestResult.getOutput());
	}

	@Test
	public void testValidateDataMaskWhenDetectionRegexIsInvalid()
		throws Exception {

		DataMaskValidationRequest dataMaskTestRequest =
			new DataMaskValidationRequest();

		dataMaskTestRequest.setDetectionRegex("[unclosed");
		dataMaskTestRequest.setReplacementValue("[REDACTED]");
		dataMaskTestRequest.setSampleText("anything");

		DataMaskValidationResult dataMaskTestResult =
			dataMaskResource.validateDataMask(dataMaskTestRequest);

		Assert.assertNotNull(dataMaskTestResult.getError());
		Assert.assertEquals(
			Integer.valueOf(0), dataMaskTestResult.getMatchCount());
		Assert.assertEquals("anything", dataMaskTestResult.getOutput());
	}

	@Test(expected = Problem.ProblemException.class)
	public void testValidateDataMaskWhenDetectionRegexIsMissing()
		throws Exception {

		DataMaskValidationRequest dataMaskTestRequest =
			new DataMaskValidationRequest();

		dataMaskTestRequest.setReplacementValue("[REDACTED]");
		dataMaskTestRequest.setSampleText("anything");

		dataMaskResource.validateDataMask(dataMaskTestRequest);
	}

	@Test
	public void testValidateDataMaskWhenReplacementRegexHasCaptureGroups()
		throws Exception {

		DataMaskValidationRequest dataMaskTestRequest =
			new DataMaskValidationRequest();

		dataMaskTestRequest.setDetectionRegex(
			"\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");
		dataMaskTestRequest.setReplacementRegex(
			"(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\.\\d{1,3}");
		dataMaskTestRequest.setReplacementValue("$1.0/24");
		dataMaskTestRequest.setSampleText("Connected from 192.168.1.42");

		DataMaskValidationResult dataMaskTestResult =
			dataMaskResource.validateDataMask(dataMaskTestRequest);

		Assert.assertNull(dataMaskTestResult.getError());
		Assert.assertEquals(
			Integer.valueOf(1), dataMaskTestResult.getMatchCount());
		Assert.assertEquals(
			"Connected from 192.168.1.0/24", dataMaskTestResult.getOutput());
	}

	@Test
	public void testValidateDataMaskWhenReplacementRegexIsInvalid()
		throws Exception {

		DataMaskValidationRequest dataMaskTestRequest =
			new DataMaskValidationRequest();

		dataMaskTestRequest.setDetectionRegex(_EMAIL_DETECTION_REGEX);
		dataMaskTestRequest.setReplacementRegex("[unclosed");
		dataMaskTestRequest.setReplacementValue("[REDACTED]");
		dataMaskTestRequest.setSampleText("alice@example.com");

		DataMaskValidationResult dataMaskTestResult =
			dataMaskResource.validateDataMask(dataMaskTestRequest);

		Assert.assertNotNull(dataMaskTestResult.getError());
		Assert.assertEquals(
			Integer.valueOf(0), dataMaskTestResult.getMatchCount());
		Assert.assertEquals(
			"alice@example.com", dataMaskTestResult.getOutput());
	}

	@Test(expected = Problem.ProblemException.class)
	public void testValidateDataMaskWhenReplacementValueIsMissing()
		throws Exception {

		DataMaskValidationRequest dataMaskTestRequest =
			new DataMaskValidationRequest();

		dataMaskTestRequest.setDetectionRegex(_EMAIL_DETECTION_REGEX);
		dataMaskTestRequest.setSampleText("anything");

		dataMaskResource.validateDataMask(dataMaskTestRequest);
	}

	@Test
	public void testValidateDataMaskWhenSampleHasNoMatches() throws Exception {
		DataMaskValidationRequest dataMaskTestRequest =
			new DataMaskValidationRequest();

		dataMaskTestRequest.setDetectionRegex(_EMAIL_DETECTION_REGEX);
		dataMaskTestRequest.setReplacementValue("[EMAIL]");
		dataMaskTestRequest.setSampleText("No email here at all.");

		DataMaskValidationResult dataMaskTestResult =
			dataMaskResource.validateDataMask(dataMaskTestRequest);

		Assert.assertNull(dataMaskTestResult.getError());
		Assert.assertEquals(
			Integer.valueOf(0), dataMaskTestResult.getMatchCount());
		Assert.assertEquals(
			"No email here at all.", dataMaskTestResult.getOutput());
	}

	@Test(expected = Problem.ProblemException.class)
	public void testValidateDataMaskWhenSampleTextIsMissing() throws Exception {
		DataMaskValidationRequest dataMaskTestRequest =
			new DataMaskValidationRequest();

		dataMaskTestRequest.setDetectionRegex(_EMAIL_DETECTION_REGEX);
		dataMaskTestRequest.setReplacementValue("[EMAIL]");

		dataMaskResource.validateDataMask(dataMaskTestRequest);
	}

	private static final String _EMAIL_DETECTION_REGEX =
		"\\b[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}\\b";

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.data.masking.internal.resource.v1_0;

import com.liferay.headless.data.masking.dto.v1_0.DataMaskValidationRequest;
import com.liferay.headless.data.masking.dto.v1_0.DataMaskValidationResult;
import com.liferay.headless.data.masking.internal.masking.DataMask;
import com.liferay.headless.data.masking.resource.v1_0.DataMaskResource;
import com.liferay.portal.kernel.util.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Jose Luis Navarro
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/data-mask.properties",
	scope = ServiceScope.PROTOTYPE, service = DataMaskResource.class
)
public class DataMaskResourceImpl extends BaseDataMaskResourceImpl {

	// Would it make sense to do this as an object action instead of a full rest API as in https://github.com/liferay/liferay-portal/blob/18c7ae06848e70085cf222cb212d143e863ce9f9/modules/apps/commerce/commerce-service/src/main/java/com/liferay/commerce/internal/object/action/executor/SplitCommerceOrderByCatalogObjectActionExecutorImpl.java#L50?
	@Override
	public DataMaskValidationResult validateDataMask(
			DataMaskValidationRequest dataMaskTestRequest)
		throws Exception {

		String detectionRegex = dataMaskTestRequest.getDetectionRegex();
		String replacementValue = dataMaskTestRequest.getReplacementValue();
		String sampleText = dataMaskTestRequest.getSampleText();

		DataMaskValidationResult dataMaskTestResult =
			new DataMaskValidationResult();

		dataMaskTestResult.setMatchCount(() -> 0);
		dataMaskTestResult.setOutput(() -> sampleText);

		if (Validator.isNull(detectionRegex) ||
			Validator.isNull(replacementValue) ||
			Validator.isNull(sampleText)) {

			dataMaskTestResult.setError(
				() ->
					"detectionRegex, replacementValue, and sampleText are " +
						"required");

			return dataMaskTestResult;
		}

		try {
			Pattern detectionPattern = Pattern.compile(detectionRegex);

			String replacementRegex = dataMaskTestRequest.getReplacementRegex();

			Pattern replacementPattern = null;

			if (Validator.isNotNull(replacementRegex)) {
				replacementPattern = Pattern.compile(replacementRegex);
			}

			DataMask dataMask = new DataMask(
				"test", detectionPattern, replacementPattern, replacementValue);

			String finalOutput = dataMask.apply(sampleText);

			int finalMatchCount = _countMatches(detectionPattern, sampleText);

			dataMaskTestResult.setMatchCount(() -> finalMatchCount);

			dataMaskTestResult.setOutput(() -> finalOutput);
		}
		catch (PatternSyntaxException patternSyntaxException) {
			dataMaskTestResult.setError(patternSyntaxException::getMessage);
		}
		catch (RuntimeException runtimeException) {
			dataMaskTestResult.setError(runtimeException::getMessage);
		}

		return dataMaskTestResult;
	}

	private int _countMatches(Pattern detectionPattern, String text) {
		int count = 0;

		Matcher matcher = detectionPattern.matcher(text);

		while (matcher.find()) {
			count++;
		}

		return count;
	}

}
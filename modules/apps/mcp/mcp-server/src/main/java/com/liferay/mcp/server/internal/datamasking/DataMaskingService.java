/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.internal.datamasking;

import com.liferay.mcp.server.internal.constants.MCPServerConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Jose Luis Navarro
 */
public class DataMaskingService {

	public DataMaskingService(
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryLocalService objectEntryLocalService) {

		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryLocalService = objectEntryLocalService;
	}

	public String redact(
		long companyId, long profileObjectEntryId, String text) {

		if (Validator.isNull(text) || (profileObjectEntryId == 0) ||
			!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-90204")) {

			return text;
		}

		List<DataMask> dataMasks = _loadDataMasks(
			companyId, profileObjectEntryId);

		if (dataMasks.isEmpty()) {
			return text;
		}

		String current = text;

		for (DataMask dataMask : dataMasks) {
			try {
				current = dataMask.apply(current);
			}
			catch (RuntimeException runtimeException) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Unable to apply data mask \"", dataMask.getName(),
							"\" due to runtime error: ",
							runtimeException.getMessage()));
				}
			}
		}

		return current;
	}

	private ObjectDefinition _fetchObjectDefinition(
		long companyId, String externalReferenceCode) {

		return _objectDefinitionLocalService.
			fetchObjectDefinitionByExternalReferenceCode(
				externalReferenceCode, companyId);
	}

	private List<ObjectEntry> _getActiveProfileDataMaskObjectEntries(
		ObjectDefinition profileDataMaskObjectDefinition,
		long profileObjectEntryId) {

		List<ObjectEntry> activeProfileDataMaskObjectEntries =
			new ArrayList<>();

		for (ObjectEntry profileDataMaskObjectEntry :
				_objectEntryLocalService.getObjectEntries(
					0, profileDataMaskObjectDefinition.getObjectDefinitionId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			Map<String, Serializable> values =
				profileDataMaskObjectEntry.getValues();

			long mcpServerProfileId = GetterUtil.getLong(
				values.get("mcpServerProfileId"));

			if ((mcpServerProfileId != profileObjectEntryId) ||
				!GetterUtil.getBoolean(values.get("active"), true)) {

				continue;
			}

			activeProfileDataMaskObjectEntries.add(profileDataMaskObjectEntry);
		}

		activeProfileDataMaskObjectEntries.sort(
			Comparator.comparing(
				objectEntry -> GetterUtil.getInteger(
					objectEntry.getValues(
					).get(
						"executionOrder"
					),
					Integer.MAX_VALUE)));

		return activeProfileDataMaskObjectEntries;
	}

	private Map<Long, ObjectEntry> _getMaskEntriesById(
		ObjectDefinition maskObjectDefinition) {

		Map<Long, ObjectEntry> masksByObjectEntryId = new HashMap<>();

		for (ObjectEntry maskObjectEntry :
				_objectEntryLocalService.getObjectEntries(
					0, maskObjectDefinition.getObjectDefinitionId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			masksByObjectEntryId.put(
				maskObjectEntry.getObjectEntryId(), maskObjectEntry);
		}

		return masksByObjectEntryId;
	}

	private List<DataMask> _loadDataMasks(
		long companyId, long profileObjectEntryId) {

		ObjectDefinition maskObjectDefinition = _fetchObjectDefinition(
			companyId,
			MCPServerConstants.EXTERNAL_REFERENCE_CODE_MCP_SERVER_DATA_MASK);

		ObjectDefinition profileDataMaskObjectDefinition =
			_fetchObjectDefinition(
				companyId,
				MCPServerConstants.
					EXTERNAL_REFERENCE_CODE_MCP_SERVER_PROFILE_DATA_MASK);

		if ((maskObjectDefinition == null) ||
			(profileDataMaskObjectDefinition == null)) {

			return Collections.emptyList();
		}

		Map<Long, ObjectEntry> masksByObjectEntryId = _getMaskEntriesById(
			maskObjectDefinition);

		if (masksByObjectEntryId.isEmpty()) {
			return Collections.emptyList();
		}

		List<ObjectEntry> profileDataMaskObjectEntries =
			_getActiveProfileDataMaskObjectEntries(
				profileDataMaskObjectDefinition, profileObjectEntryId);

		if (profileDataMaskObjectEntries.isEmpty()) {
			return Collections.emptyList();
		}

		return _toDataMasks(profileDataMaskObjectEntries, masksByObjectEntryId);
	}

	private List<DataMask> _toDataMasks(
		List<ObjectEntry> profileDataMaskObjectEntries,
		Map<Long, ObjectEntry> masksByObjectEntryId) {

		List<DataMask> dataMasks = new ArrayList<>();

		for (ObjectEntry profileDataMaskObjectEntry :
				profileDataMaskObjectEntries) {

			Map<String, Serializable> profileDataMaskValues =
				profileDataMaskObjectEntry.getValues();

			long maskObjectEntryId = GetterUtil.getLong(
				profileDataMaskValues.get(
					"r_dataMaskToProfileDataMasks_mcpServerDataMaskId"));

			ObjectEntry maskObjectEntry = masksByObjectEntryId.get(
				maskObjectEntryId);

			if (maskObjectEntry == null) {
				continue;
			}

			Map<String, Serializable> maskValues = maskObjectEntry.getValues();

			if (!GetterUtil.getBoolean(maskValues.get("active"), true)) {
				continue;
			}

			String name = (String)maskValues.get("name");
			String detectionRegex = (String)maskValues.get("detectionRegex");
			String replacementRegex = (String)maskValues.get(
				"replacementRegex");
			String replacementValue = (String)maskValues.get(
				"replacementValue");

			if (Validator.isNull(detectionRegex) ||
				Validator.isNull(replacementValue)) {

				continue;
			}

			try {
				Pattern detectionPattern = Pattern.compile(detectionRegex);

				Pattern replacementPattern = null;

				if (Validator.isNotNull(replacementRegex)) {
					replacementPattern = Pattern.compile(replacementRegex);
				}

				dataMasks.add(
					new DataMask(
						name, detectionPattern, replacementPattern,
						replacementValue));
			}
			catch (PatternSyntaxException patternSyntaxException) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Unable to compile data mask \"", name,
							"\" due to invalid regex pattern: ",
							patternSyntaxException.getMessage()));
				}
			}
		}

		return dataMasks;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DataMaskingService.class);

	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryLocalService _objectEntryLocalService;

}
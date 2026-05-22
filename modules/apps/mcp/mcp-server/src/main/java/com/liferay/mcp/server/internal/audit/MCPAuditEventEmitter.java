/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.internal.audit;

import com.liferay.mcp.server.internal.constants.MCPServerConstants;
import com.liferay.mcp.server.internal.datamasking.RedactionResult;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.audit.AuditException;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;

import java.util.Map;

/**
 * @author Jose Luis Navarro
 */
public class MCPAuditEventEmitter {

	public MCPAuditEventEmitter(
		AuditRouter auditRouter,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		UserLocalService userLocalService) {

		_auditRouter = auditRouter;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_userLocalService = userLocalService;
	}

	public void emitRedactionEvent(
		long companyId, long userId, long profileObjectEntryId,
		RedactionResult redactionResult, String httpMethod,
		Object liferayAIHubCellOnBehalfOf, Object userAgent) {

		if ((redactionResult.getTotalMatchCount() == 0) ||
			!_auditRouter.isDeployed()) {

			return;
		}

		try {
			JSONObject matchCountsJSONObject =
				JSONFactoryUtil.createJSONObject();

			for (Map.Entry<String, Integer> entry :
					redactionResult.getMatchCountsByExternalReferenceCode(
					).entrySet()) {

				matchCountsJSONObject.put(entry.getKey(), entry.getValue());
			}

			JSONObject additionalInfoJSONObject = JSONUtil.put(
				"featureContext",
				MCPServerConstants.AUDIT_FEATURE_CONTEXT_MCP_SERVER
			).put(
				"httpMethod", httpMethod
			).put(
				"matchCountsByExternalReferenceCode", matchCountsJSONObject
			).put(
				"resourceType", "mcp-server-profile"
			).put(
				"totalMatchCount", redactionResult.getTotalMatchCount()
			);

			if (liferayAIHubCellOnBehalfOf != null) {
				additionalInfoJSONObject.put(
					"onBehalfOf", String.valueOf(liferayAIHubCellOnBehalfOf));
			}

			if (userAgent != null) {
				additionalInfoJSONObject.put(
					"userAgent", String.valueOf(userAgent));
			}

			AuditMessage auditMessage = new AuditMessage(
				MCPServerConstants.EVENT_TYPE_MCP_REDACTION, companyId, 0,
				userId, _getUserName(userId), _getProfileClassName(companyId),
				String.valueOf(profileObjectEntryId), null, null,
				additionalInfoJSONObject);

			_auditRouter.route(auditMessage);
		}
		catch (AuditException auditException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to route MCP redaction audit message",
					auditException);
			}
		}
	}

	private String _getProfileClassName(long companyId) {
		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					MCPServerConstants.
						EXTERNAL_REFERENCE_CODE_MCP_SERVER_PROFILE,
					companyId);

		if (objectDefinition == null) {
			return MCPServerConstants.
				EXTERNAL_REFERENCE_CODE_MCP_SERVER_PROFILE;
		}

		return objectDefinition.getClassName();
	}

	private String _getUserName(long userId) {
		if (userId <= 0) {
			return StringPool.BLANK;
		}

		User user = _userLocalService.fetchUser(userId);

		if (user == null) {
			return StringPool.BLANK;
		}

		return user.getFullName();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MCPAuditEventEmitter.class);

	private final AuditRouter _auditRouter;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final UserLocalService _userLocalService;

}
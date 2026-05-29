/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.constants.MCPServerConstants;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.listener.RelevantObjectEntryModelListener;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.util.GetterUtil;

import java.io.Serializable;

import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;

/**
 * @author Jose Luis Navarro
 */
@Component(service = RelevantObjectEntryModelListener.class)
public class MCPServerDataMaskObjectEntryModelListener
	extends BaseModelListener<ObjectEntry>
	implements RelevantObjectEntryModelListener {

	@Override
	public String getObjectDefinitionExternalReferenceCode() {
		return MCPServerConstants.EXTERNAL_REFERENCE_CODE_MCP_SERVER_DATA_MASK;
	}

	@Override
	public void onBeforeUpdate(
			ObjectEntry originalObjectEntry, ObjectEntry objectEntry)
		throws ModelListenerException {

		Map<String, Serializable> originalValues =
			originalObjectEntry.getValues();

		if (!Objects.equals(originalValues.get("maskType"), "system")) {
			return;
		}

		Map<String, Serializable> values = objectEntry.getValues();

		if (!GetterUtil.getBoolean(values.get("active"), true)) {
			throw new ModelListenerException(
				"System data masks cannot be deactivated");
		}
	}

}
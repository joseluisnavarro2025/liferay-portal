/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.test.util.BatchEngineTestUtil;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jose Luis Navarro
 */
@FeatureFlag("LPD-63311")
@RunWith(Arquillian.class)
public class MCPServerDataMaskingTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_updateMCPServerConfiguration(true);

		String prefix = ".com.liferay.mcp.server.internal.batch.";

		BatchEngineTestUtil.processBatchEngineUnits(
			"com.liferay.mcp.server", MCPServerDataMaskingTest.class,
			new String[] {
				prefix + "01.object.definition",
				prefix + "02.object.definition",
				prefix + "03.object.definition",
				prefix + "04.list.type.definition",
				prefix + "05.object.definition", prefix + "06.object.entry"
			});
	}

	@After
	public void tearDown() throws Exception {
		_updateMCPServerConfiguration(false);
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-86164"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testEmailIsNotRedactedWhenAssociationIsInactive()
		throws Exception {

		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"GET /mcp/server-profiles");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email addresses");

		_setProfileDataMaskInactive(
			profileObjectEntry.getObjectEntryId(),
			emailMaskObjectEntry.getObjectEntryId());

		String responseText = _callListProfilesTool(profileName);

		Assert.assertThat(
			responseText, CoreMatchers.containsString(_SAMPLE_EMAIL));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString("[EMAIL_ADDRESS]")));
	}

	@FeatureFlags(
		featureFlags = {
			@FeatureFlag("LPD-86164"),
			@FeatureFlag(enable = false, value = "LPD-90204")
		}
	)
	@Test
	public void testEmailIsNotRedactedWhenFeatureFlagIsOff() throws Exception {
		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"GET /mcp/server-profiles");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email addresses");

		_addProfileDataMask(
			profileObjectEntry.getObjectEntryId(),
			emailMaskObjectEntry.getObjectEntryId(), true, 1);

		String responseText = _callListProfilesTool(profileName);

		Assert.assertThat(
			responseText, CoreMatchers.containsString(_SAMPLE_EMAIL));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString("[EMAIL_ADDRESS]")));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-86164"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testEmailIsRedactedInProfileResponse() throws Exception {
		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"GET /mcp/server-profiles");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email addresses");

		_addProfileDataMask(
			profileObjectEntry.getObjectEntryId(),
			emailMaskObjectEntry.getObjectEntryId(), true, 1);

		String responseText = _callListProfilesTool(profileName);

		Assert.assertThat(
			responseText, CoreMatchers.containsString("[EMAIL_ADDRESS]"));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_EMAIL)));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-86164"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testMasksAreAppliedInExecutionOrder() throws Exception {
		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"GET /mcp/server-profiles");

		ObjectEntry domainMaskObjectEntry = _addCustomMask(
			RandomTestUtil.randomString(), "example\\.com", "[DOMAIN]");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email addresses");

		_addProfileDataMask(
			profileObjectEntry.getObjectEntryId(),
			domainMaskObjectEntry.getObjectEntryId(), true, 1);
		_addProfileDataMask(
			profileObjectEntry.getObjectEntryId(),
			emailMaskObjectEntry.getObjectEntryId(), true, 2);

		String responseText = _callListProfilesTool(profileName);

		Assert.assertThat(
			responseText, CoreMatchers.containsString("contact@[DOMAIN]"));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString("[EMAIL_ADDRESS]")));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-86164"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testProfileDataMasksAreDeletedOnProfileRemove()
		throws Exception {

		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"GET /mcp/server-profiles");

		long profileObjectEntryId = profileObjectEntry.getObjectEntryId();

		Assert.assertEquals(
			_SYSTEM_MASK_COUNT, _countProfileDataMasks(profileObjectEntryId));

		_objectEntryLocalService.deleteObjectEntry(profileObjectEntry);

		Assert.assertEquals(0, _countProfileDataMasks(profileObjectEntryId));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-86164"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testRedactionContinuesWhenOneMaskThrows() throws Exception {
		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"GET /mcp/server-profiles");

		ObjectEntry badMaskObjectEntry = _addCustomMask(
			RandomTestUtil.randomString(), "Contact", "$5-no-such-group");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email addresses");

		_addProfileDataMask(
			profileObjectEntry.getObjectEntryId(),
			badMaskObjectEntry.getObjectEntryId(), true, 1);
		_addProfileDataMask(
			profileObjectEntry.getObjectEntryId(),
			emailMaskObjectEntry.getObjectEntryId(), true, 2);

		String responseText = _callListProfilesTool(profileName);

		Assert.assertThat(
			responseText, CoreMatchers.containsString("[EMAIL_ADDRESS]"));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_EMAIL)));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-86164"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testSystemMasksAreAutoAttachedOnProfileCreate()
		throws Exception {

		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"GET /mcp/server-profiles");

		Assert.assertEquals(
			_SYSTEM_MASK_COUNT,
			_countProfileDataMasks(profileObjectEntry.getObjectEntryId()));
	}

	private ObjectEntry _addCustomMask(
			String name, String detectionRegex, String replacementValue)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_MCP_SERVER_DATA_MASK", TestPropsValues.getCompanyId());

		return _objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			null,
			HashMapBuilder.<String, Serializable>put(
				"active", true
			).put(
				"detectionRegex", detectionRegex
			).put(
				"maskType", "custom"
			).put(
				"name", name
			).put(
				"replacementValue", replacementValue
			).build(),
			ServiceContextTestUtil.getServiceContext());
	}

	private ObjectEntry _addProfile(
			String name, String description, String... endpoints)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_MCP_SERVER_PROFILE", TestPropsValues.getCompanyId());

		return _objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			null,
			HashMapBuilder.<String, Serializable>put(
				"description", description
			).put(
				"endpoints", String.join("\n", endpoints)
			).put(
				"name", name
			).build(),
			ServiceContextTestUtil.getServiceContext());
	}

	private ObjectEntry _addProfileDataMask(
			long profileObjectEntryId, long maskObjectEntryId, boolean active,
			int executionOrder)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_MCP_SERVER_PROFILE_DATA_MASK",
					TestPropsValues.getCompanyId());

		return _objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			null,
			HashMapBuilder.<String, Serializable>put(
				"active", active
			).put(
				"executionOrder", executionOrder
			).put(
				"mcpServerProfileId", profileObjectEntryId
			).put(
				"r_dataMaskToProfileDataMasks_mcpServerDataMaskId",
				maskObjectEntryId
			).build(),
			ServiceContextTestUtil.getServiceContext());
	}

	private String _callListProfilesTool(String profileName) throws Exception {
		McpSyncClient mcpSyncClient = _getMcpSyncClient(profileName);

		try {
			mcpSyncClient.initialize();

			McpSchema.CallToolResult callToolResult = mcpSyncClient.callTool(
				new McpSchema.CallToolRequest(
					"getMCPServerProfilesPage", Collections.emptyMap()));

			List<McpSchema.Content> contents = callToolResult.content();

			McpSchema.TextContent content = (McpSchema.TextContent)contents.get(
				0);

			return content.text();
		}
		finally {
			mcpSyncClient.closeGracefully();
		}
	}

	private int _countProfileDataMasks(long profileObjectEntryId)
		throws Exception {

		ObjectDefinition profileDataMaskObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_MCP_SERVER_PROFILE_DATA_MASK",
					TestPropsValues.getCompanyId());

		int count = 0;

		for (ObjectEntry profileDataMaskObjectEntry :
				_objectEntryLocalService.getObjectEntries(
					0, profileDataMaskObjectDefinition.getObjectDefinitionId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			Map<String, Serializable> values =
				profileDataMaskObjectEntry.getValues();

			if (GetterUtil.getLong(values.get("mcpServerProfileId")) ==
					profileObjectEntryId) {

				count++;
			}
		}

		return count;
	}

	private ObjectEntry _findProfileDataMask(
			long profileObjectEntryId, long maskObjectEntryId)
		throws Exception {

		ObjectDefinition profileDataMaskObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_MCP_SERVER_PROFILE_DATA_MASK",
					TestPropsValues.getCompanyId());

		for (ObjectEntry profileDataMaskObjectEntry :
				_objectEntryLocalService.getObjectEntries(
					0, profileDataMaskObjectDefinition.getObjectDefinitionId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			Map<String, Serializable> values =
				profileDataMaskObjectEntry.getValues();

			long mcpServerProfileId = GetterUtil.getLong(
				values.get("mcpServerProfileId"));
			long mcpServerDataMaskId = GetterUtil.getLong(
				values.get("r_dataMaskToProfileDataMasks_mcpServerDataMaskId"));

			if ((mcpServerProfileId == profileObjectEntryId) &&
				(mcpServerDataMaskId == maskObjectEntryId)) {

				return profileDataMaskObjectEntry;
			}
		}

		return null;
	}

	private ObjectEntry _findSystemMask(String name) throws Exception {
		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_MCP_SERVER_DATA_MASK", TestPropsValues.getCompanyId());

		if (objectDefinition == null) {
			return null;
		}

		for (ObjectEntry objectEntry :
				_objectEntryLocalService.getObjectEntries(
					0, objectDefinition.getObjectDefinitionId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			Map<String, Serializable> values = objectEntry.getValues();

			if (name.equals(values.get("name"))) {
				return objectEntry;
			}
		}

		return null;
	}

	private String _getAuthorization() {
		try {
			Base64.Encoder encoder = Base64.getEncoder();

			String userNameAndPassword =
				"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD;

			return "Basic " +
				new String(
					encoder.encode(userNameAndPassword.getBytes("UTF-8")),
					"UTF-8");
		}
		catch (UnsupportedEncodingException unsupportedEncodingException) {
			throw new RuntimeException(unsupportedEncodingException);
		}
	}

	private McpSyncClient _getMcpSyncClient(String profileName) {
		return McpClient.sync(
			HttpClientStreamableHttpTransport.builder(
				"http://localhost:" + PortalUtil.getPortalServerPort(false) +
					"/o/"
			).customizeRequest(
				builder -> builder.header("Authorization", _getAuthorization())
			).endpoint(
				(profileName != null) ? "mcp/" + profileName : "mcp"
			).build()
		).capabilities(
			McpSchema.ClientCapabilities.builder(
			).elicitation(
				true, true
			).build()
		).build();
	}

	private void _setProfileDataMaskInactive(
			long profileObjectEntryId, long maskObjectEntryId)
		throws Exception {

		ObjectEntry profileDataMaskObjectEntry = _findProfileDataMask(
			profileObjectEntryId, maskObjectEntryId);

		Assert.assertNotNull(profileDataMaskObjectEntry);

		_objectEntryLocalService.deleteObjectEntry(profileDataMaskObjectEntry);

		_addProfileDataMask(profileObjectEntryId, maskObjectEntryId, false, 1);
	}

	private void _updateMCPServerConfiguration(boolean enabled)
		throws Exception {

		ConfigurationTestUtil.createFactoryConfiguration(
			"com.liferay.mcp.server.internal.configuration." +
				"MCPServerConfiguration.scoped",
			HashMapDictionaryBuilder.<String, Object>put(
				"companyId", TestPropsValues.getCompanyId()
			).put(
				"enabled", enabled
			).build());
	}

	private static final String _SAMPLE_EMAIL = "contact@example.com";

	private static final int _SYSTEM_MASK_COUNT = 9;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}
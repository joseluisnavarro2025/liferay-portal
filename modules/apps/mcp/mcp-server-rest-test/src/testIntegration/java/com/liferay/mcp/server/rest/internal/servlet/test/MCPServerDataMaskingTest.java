/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.servlet.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.test.util.BatchEngineTestUtil;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Http;
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

		String prefix = ".com.liferay.mcp.server.rest.internal.batch.";

		BatchEngineTestUtil.processBatchEngineUnits(
			"com.liferay.mcp.server.rest.impl", MCPServerDataMaskingTest.class,
			new String[] {
				prefix + "01.object.definition",
				prefix + "02.object.definition",
				prefix + "03.object.definition",
				prefix + "04.list.type.definition",
				prefix + "05.object.definition", prefix + "06.object.entry",
				prefix + "07.object.entry"
			});
	}

	@After
	public void tearDown() throws Exception {
		_updateMCPServerConfiguration(false);
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testAllSystemMasksAreAppliedInProfileResponse()
		throws Exception {

		String profileName = RandomTestUtil.randomString();

		_addProfile(
			profileName,
			StringBundler.concat(
				"Credit card: ", _SAMPLE_CREDIT_CARD, ". Email: ",
				_SAMPLE_EMAIL_ALT, ". IBAN: ", _SAMPLE_IBAN, ". IPv4: ",
				_SAMPLE_IPV4, ". IPv6: ", _SAMPLE_IPV6, ". BSN: ", _SAMPLE_BSN,
				". DNI: ", _SAMPLE_DNI, ". SSN: ", _SAMPLE_SSN, ". Phone: ",
				_SAMPLE_PHONE_INTL, "."),
			"mcp-server-profiles getMCPServerProfilesPage");

		String responseText = _callListProfilesTool(profileName);

		Assert.assertThat(
			responseText, CoreMatchers.containsString("[BANK_ACCOUNT_NUMBER]"));
		Assert.assertThat(
			responseText, CoreMatchers.containsString("[CREDIT_CARD_NUMBER]"));
		Assert.assertThat(
			responseText, CoreMatchers.containsString("[EMAIL_ADDRESS]"));
		Assert.assertThat(
			responseText, CoreMatchers.containsString("[NATIONAL_ID]"));
		Assert.assertThat(
			responseText, CoreMatchers.containsString("[PHONE_NUMBER]"));
		Assert.assertThat(
			responseText, CoreMatchers.containsString("192.168.1.0/24"));
		Assert.assertThat(
			responseText, CoreMatchers.containsString("2001:0db8:85a3::/48"));

		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_BSN)));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_CREDIT_CARD)));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_DNI)));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_EMAIL_ALT)));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_IBAN)));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_IPV4)));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_IPV6)));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_PHONE_INTL)));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_SSN)));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testEmailIsNotRedactedWhenAssociationIsInactive()
		throws Exception {

		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"mcp-server-profiles getMCPServerProfilesPage");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email Address");

		ObjectEntry emailProfileDataMaskObjectEntry = _findProfileDataMask(
			profileObjectEntry.getObjectEntryId(),
			emailMaskObjectEntry.getObjectEntryId());

		_setActive(emailProfileDataMaskObjectEntry, false, "Disabled by test.");

		String responseText = _callListProfilesTool(profileName);

		Assert.assertThat(
			responseText, CoreMatchers.containsString(_SAMPLE_EMAIL));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString("[EMAIL_ADDRESS]")));
	}

	@FeatureFlags(
		featureFlags = {
			@FeatureFlag("LPD-63311"),
			@FeatureFlag(enable = false, value = "LPD-90204")
		}
	)
	@Test
	public void testEmailIsNotRedactedWhenFeatureFlagIsOff() throws Exception {
		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"mcp-server-profiles getMCPServerProfilesPage");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email Address");

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
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testEmailIsRedactedInProfileResponse() throws Exception {
		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"mcp-server-profiles getMCPServerProfilesPage");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email Address");

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
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testErrorResponseIsRedacted() throws Exception {
		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, RandomTestUtil.randomString(),
			"mcp-server-profiles getMCPServerProfilesPage");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email Address");

		_addProfileDataMask(
			profileObjectEntry.getObjectEntryId(),
			emailMaskObjectEntry.getObjectEntryId(), true, 1);

		McpSchema.CallToolResult callToolResult = _callTool(
			profileName, "getMCPServerProfilesPage",
			HashMapBuilder.<String, Object>put(
				"filter", _SAMPLE_EMAIL
			).build());

		McpSchema.TextContent textContent =
			(McpSchema.TextContent)callToolResult.content(
			).get(
				0
			);

		String responseText = textContent.text();

		Assert.assertTrue(responseText, callToolResult.isError());
		Assert.assertThat(
			responseText, CoreMatchers.containsString("[EMAIL_ADDRESS]"));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_EMAIL)));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testMasksAreAppliedInExecutionOrder() throws Exception {
		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"mcp-server-profiles getMCPServerProfilesPage");

		ObjectEntry domainMaskObjectEntry = _addCustomMask(
			RandomTestUtil.randomString(), "example\\.com", "[DOMAIN]");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email Address");

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
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testProfileDataMaskCannotBeDeactivatedWithoutReason()
		throws Exception {

		ObjectEntry profileObjectEntry = _addProfile(
			RandomTestUtil.randomString(), "no PII here",
			"mcp-server-profiles getMCPServerProfilesPage");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email Address");

		ObjectEntry profileDataMaskObjectEntry = _addProfileDataMask(
			profileObjectEntry.getObjectEntryId(),
			emailMaskObjectEntry.getObjectEntryId(), true, 1);

		try {
			_setActive(profileDataMaskObjectEntry, false, null);

			Assert.fail(
				"Deactivating a profile data mask without a disable reason " +
					"should have thrown");
		}
		catch (Exception exception) {
			Assert.assertThat(
				exception.getMessage(),
				CoreMatchers.containsString("disable reason"));
		}

		ObjectEntry refreshedObjectEntry =
			_objectEntryLocalService.getObjectEntry(
				profileDataMaskObjectEntry.getObjectEntryId());

		Assert.assertTrue(
			GetterUtil.getBoolean(
				refreshedObjectEntry.getValues(
				).get(
					"active"
				)));

		ObjectEntry updatedObjectEntry = _setActive(
			profileDataMaskObjectEntry, false,
			"Not required for this profile.");

		Assert.assertFalse(
			GetterUtil.getBoolean(
				updatedObjectEntry.getValues(
				).get(
					"active"
				)));
		Assert.assertEquals(
			"Not required for this profile.",
			updatedObjectEntry.getValues(
			).get(
				"disableReason"
			));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testProfileDataMasksAreDeletedOnProfileRemove()
		throws Exception {

		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"mcp-server-profiles getMCPServerProfilesPage");

		long profileObjectEntryId = profileObjectEntry.getObjectEntryId();

		Assert.assertEquals(
			_SYSTEM_MASK_COUNT, _countProfileDataMasks(profileObjectEntryId));

		PermissionChecker originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		try {
			PermissionThreadLocal.setPermissionChecker(
				PermissionCheckerFactoryUtil.create(TestPropsValues.getUser()));

			_objectEntryLocalService.deleteObjectEntry(profileObjectEntry);
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(
				originalPermissionChecker);
		}

		Assert.assertEquals(0, _countProfileDataMasks(profileObjectEntryId));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testRedactionContinuesWhenOneMaskThrows() throws Exception {
		String profileName = RandomTestUtil.randomString();

		ObjectEntry profileObjectEntry = _addProfile(
			profileName, "Contact: " + _SAMPLE_EMAIL,
			"mcp-server-profiles getMCPServerProfilesPage");

		ObjectEntry badMaskObjectEntry = _addCustomMask(
			RandomTestUtil.randomString(), "Contact", "$5-no-such-group");

		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email Address");

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
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testRestApiInvokeAppliesMasksWhenDataMasksHeaderSet()
		throws Exception {

		_addProfile(
			RandomTestUtil.randomString(), "Contact: " + _SAMPLE_EMAIL,
			"mcp-server-profiles getMCPServerProfilesPage");

		String responseText = _invokeListProfilesViaRest(
			HashMapBuilder.put(
				"X-Liferay-Data-Masks", "L_MCP_SERVER_DATA_MASK_EMAIL_ADDRESS"
			).build());

		Assert.assertThat(
			responseText, CoreMatchers.containsString("[EMAIL_ADDRESS]"));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_EMAIL)));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testRestApiInvokeRespectsSelectedMasksOnly() throws Exception {
		_addProfile(
			RandomTestUtil.randomString(),
			StringBundler.concat(
				"Contact: ", _SAMPLE_EMAIL, " ", _SAMPLE_PHONE),
			"mcp-server-profiles getMCPServerProfilesPage");

		String responseText = _invokeListProfilesViaRest(
			HashMapBuilder.put(
				"X-Liferay-Data-Masks", "L_MCP_SERVER_DATA_MASK_EMAIL_ADDRESS"
			).build());

		Assert.assertThat(
			responseText, CoreMatchers.containsString("[EMAIL_ADDRESS]"));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString(_SAMPLE_EMAIL)));
		Assert.assertThat(
			responseText, CoreMatchers.containsString(_SAMPLE_PHONE));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testRestApiInvokeSkipsRedactionWhenDataMasksHeaderIsUnknown()
		throws Exception {

		_addProfile(
			RandomTestUtil.randomString(), "Contact: " + _SAMPLE_EMAIL,
			"mcp-server-profiles getMCPServerProfilesPage");

		String responseText = _invokeListProfilesViaRest(
			HashMapBuilder.put(
				"X-Liferay-Data-Masks", "L_UNKNOWN_DATA_MASK_ERC"
			).build());

		Assert.assertThat(
			responseText, CoreMatchers.containsString(_SAMPLE_EMAIL));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString("[EMAIL_ADDRESS]")));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testRestApiInvokeSkipsRedactionWhenNoDataMasksHeader()
		throws Exception {

		_addProfile(
			RandomTestUtil.randomString(), "Contact: " + _SAMPLE_EMAIL,
			"mcp-server-profiles getMCPServerProfilesPage");

		String responseText = _invokeListProfilesViaRest(
			Collections.emptyMap());

		Assert.assertThat(
			responseText, CoreMatchers.containsString(_SAMPLE_EMAIL));
		Assert.assertThat(
			responseText,
			CoreMatchers.not(CoreMatchers.containsString("[EMAIL_ADDRESS]")));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testSystemMaskCannotBeDeactivated() throws Exception {
		ObjectEntry emailMaskObjectEntry = _findSystemMask("Email Address");

		try {
			_setActive(emailMaskObjectEntry, false, null);

			Assert.fail("Deactivating a system data mask should have thrown");
		}
		catch (Exception exception) {
			Assert.assertThat(
				exception.getMessage(),
				CoreMatchers.containsString("System data masks"));
		}

		ObjectEntry refreshedObjectEntry =
			_objectEntryLocalService.getObjectEntry(
				emailMaskObjectEntry.getObjectEntryId());

		Assert.assertTrue(
			GetterUtil.getBoolean(
				refreshedObjectEntry.getValues(
				).get(
					"active"
				)));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testSystemMasksAreAutoAttachedOnProfileCreate()
		throws Exception {

		ObjectEntry profileObjectEntry = _addProfile(
			RandomTestUtil.randomString(), "no PII here",
			"mcp-server-profiles getMCPServerProfilesPage");

		Assert.assertEquals(
			_SYSTEM_MASK_COUNT,
			_countProfileDataMasks(profileObjectEntry.getObjectEntryId()));
	}

	@FeatureFlags(
		featureFlags = {@FeatureFlag("LPD-63311"), @FeatureFlag("LPD-90204")}
	)
	@Test
	public void testSystemMasksAreSeededOnDefaultProfile() throws Exception {
		ObjectEntry defaultProfileObjectEntry = _findProfile("default");

		Assert.assertNotNull(defaultProfileObjectEntry);
		Assert.assertEquals(
			_SYSTEM_MASK_COUNT,
			_countProfileDataMasks(
				defaultProfileObjectEntry.getObjectEntryId()));
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
			String name, String description, String... tools)
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
				"name", name
			).put(
				"tools", String.join("\n", tools)
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
				"disableReason", active ? null : "Disabled by test."
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

	private McpSchema.CallToolResult _callTool(
			String profileName, String toolName, Map<String, Object> arguments)
		throws Exception {

		McpSyncClient mcpSyncClient = _getMcpSyncClient(profileName);

		try {
			mcpSyncClient.initialize();

			return mcpSyncClient.callTool(
				new McpSchema.CallToolRequest(toolName, arguments));
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

			long relationshipProfileId = GetterUtil.getLong(
				values.get("mcpServerProfileId"));

			if (relationshipProfileId == profileObjectEntryId) {
				count++;
			}
		}

		return count;
	}

	private ObjectEntry _findProfile(String name) throws Exception {
		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_MCP_SERVER_PROFILE", TestPropsValues.getCompanyId());

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

			long profileId = GetterUtil.getLong(
				values.get("mcpServerProfileId"));
			long maskId = GetterUtil.getLong(
				values.get("r_dataMaskToProfileDataMasks_mcpServerDataMaskId"));

			if ((profileId == profileObjectEntryId) &&
				(maskId == maskObjectEntryId)) {

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

	private String _invokeListProfilesViaRest(Map<String, String> headers)
		throws Exception {

		String url = StringBundler.concat(
			"http://localhost:", PortalUtil.getPortalServerPort(false),
			"/o/mcp-server/v1.0/tool-sets/mcp-server-profiles/tools",
			"/getMCPServerProfilesPage/invoke");

		Http.Options options = new Http.Options();

		options.addHeader("Authorization", _getAuthorization());
		options.addHeader("Content-Type", "application/json");

		for (Map.Entry<String, String> entry : headers.entrySet()) {
			options.addHeader(entry.getKey(), entry.getValue());
		}

		options.setBody("{}", "application/json", "UTF-8");
		options.setLocation(url);
		options.setMethod(Http.Method.POST);

		return _http.URLtoString(options);
	}

	private ObjectEntry _setActive(
			ObjectEntry objectEntry, boolean active, String disableReason)
		throws Exception {

		return _objectEntryLocalService.updateObjectEntry(
			TestPropsValues.getUserId(), objectEntry.getObjectEntryId(), 0,
			HashMapBuilder.<String, Serializable>putAll(
				objectEntry.getValues()
			).put(
				"active", active
			).put(
				"disableReason", disableReason
			).build(),
			ServiceContextTestUtil.getServiceContext());
	}

	private void _updateMCPServerConfiguration(boolean enabled)
		throws Exception {

		ConfigurationTestUtil.createFactoryConfiguration(
			"com.liferay.mcp.server.rest.internal.configuration." +
				"MCPServerConfiguration.scoped",
			HashMapDictionaryBuilder.<String, Object>put(
				"companyId", TestPropsValues.getCompanyId()
			).put(
				"enabled", enabled
			).build());
	}

	private static final String _SAMPLE_BSN = "123456789";

	private static final String _SAMPLE_CREDIT_CARD = "4111-1111-1111-1111";

	private static final String _SAMPLE_DNI = "12345678A";

	private static final String _SAMPLE_EMAIL = "contact@example.com";

	private static final String _SAMPLE_EMAIL_ALT = "alice@example.com";

	private static final String _SAMPLE_IBAN = "DE89370400440532013000";

	private static final String _SAMPLE_IPV4 = "192.168.1.42";

	private static final String _SAMPLE_IPV6 =
		"2001:0db8:85a3:0000:0000:8a2e:0370:7334";

	private static final String _SAMPLE_PHONE = "+1-202-555-0199";

	private static final String _SAMPLE_PHONE_INTL = "+34-600-123-456";

	private static final String _SAMPLE_SSN = "123-45-6789";

	private static final int _SYSTEM_MASK_COUNT = 9;

	@Inject
	private Http _http;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}
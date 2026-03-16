/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.BatchEngineExportTaskExecutor;
import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.configuration.BatchEngineTaskCompanyConfiguration;
import com.liferay.batch.engine.model.BatchEngineExportTask;
import com.liferay.batch.engine.service.BatchEngineExportTaskLocalService;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.field.util.ObjectFieldUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.test.rule.Inject;

import java.io.Serializable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jose Luis Navarro
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class BatchEngineExportTaskPerformanceTest
	extends BaseBatchEngineTaskExecutorTest {

	@After
	public void tearDown() throws Exception {
		if ((_metricsPath != null) && Files.exists(_metricsPath)) {

			// Commented out to avoid deleting the metrics file
			// Files.delete(_metricsPath);

		}

		if (_configurationRestored) {
			return;
		}

		ConfigurationProviderUtil.saveCompanyConfiguration(
			BatchEngineTaskCompanyConfiguration.class,
			TestPropsValues.getCompanyId(),
			HashMapDictionaryBuilder.<String, Object>put(
				"exportBatchSize", _defaultExportBatchSizeForRestore
			).build());

		_configurationRestored = true;
	}

	@Test
	public void testExportBatchTimingRatio() throws Throwable {
		_metricsPath = Path.of(_METRICS_LOG_PATH);

		if (_metricsPath.getParent() != null) {
			Files.createDirectories(_metricsPath.getParent());
		}

		_defaultExportBatchSizeForRestore = _EXPORT_BATCH_SIZE;

		ConfigurationProviderUtil.saveCompanyConfiguration(
			BatchEngineTaskCompanyConfiguration.class,
			TestPropsValues.getCompanyId(),
			HashMapDictionaryBuilder.<String, Object>put(
				"exportBatchSize", _EXPORT_BATCH_SIZE
			).build());

		_configurationRestored = false;

		ObjectDefinition objectDefinition = _createDogObjectDefinition();

		List<Long> entryIds = _addDogObjectEntries(
			objectDefinition, _DOG_ENTRY_COUNT);

		Role exportRole = _createExportRole();

		User exportUser = _createExportUser(exportRole);

		_grantIndividualViewPermissions(objectDefinition, entryIds, exportRole);

		BatchEngineExportTask batchEngineExportTask =
			_batchEngineExportTaskLocalService.createBatchEngineExportTask(
				RandomTestUtil.randomLong(), null, exportUser.getCompanyId(),
				exportUser.getUserId(), null,
				"com.liferay.object.rest.dto.v1_0.ObjectEntry", "JSON",
				BatchEngineTaskExecuteStatus.INITIAL.name(), null,
				new HashMap<>(), objectDefinition.getName());

		TransactionInvokerUtil.invoke(
			TransactionConfig.Factory.create(
				Propagation.REQUIRED, new Class<?>[] {Exception.class}),
			() -> {
				_batchEngineExportTaskExecutor.execute(
					batchEngineExportTask,
					new BatchEngineExportTaskExecutor.Settings() {

						@Override
						public boolean isCompressContent() {
							return false;
						}

						@Override
						public boolean isPersist() {
							return false;
						}

					});

				return null;
			});

		List<Long> batchTimesMsList = _parseBatchTimesFromMetricsLog(
			_metricsPath);

		double firstAvg = _average(batchTimesMsList.subList(0, _SAMPLE_SIZE));
		double lastAvg = _average(
			batchTimesMsList.subList(
				batchTimesMsList.size() - _SAMPLE_SIZE,
				batchTimesMsList.size()));

		double ratio = lastAvg / firstAvg;

		Assert.assertTrue(
			String.format(
				"Ratio of last %d batches avg (%.1f ms) to first %d batches " +
					"avg (%.1f ms) is %.2f; expected < 3.0",
				_SAMPLE_SIZE, lastAvg, _SAMPLE_SIZE, firstAvg, ratio),
			ratio < 3.0);
	}

	private List<Long> _addDogObjectEntries(
			ObjectDefinition objectDefinition, int count)
		throws Exception {

		List<Long> entryIds = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			ObjectEntry objectEntry =
				ObjectEntryLocalServiceUtil.addObjectEntry(
					0, user.getUserId(),
					objectDefinition.getObjectDefinitionId(), 0, null,
					HashMapBuilder.<String, Serializable>put(
						"name", "dog-" + i
					).build(),
					ServiceContextTestUtil.getServiceContext(
						TestPropsValues.getCompanyId(), 0, user.getUserId()));

			entryIds.add(objectEntry.getObjectEntryId());
		}

		return entryIds;
	}

	private double _average(List<Long> values) {
		long sum = 0;

		for (Long value : values) {
			sum += value;
		}

		return (double)sum / values.size();
	}

	private ObjectDefinition _createDogObjectDefinition() throws Exception {
		ObjectDefinition objectDefinition =
			ObjectDefinitionTestUtil.addCustomObjectDefinition(
				0, "Dog",
				Arrays.asList(
					ObjectFieldUtil.createObjectField(
						ObjectFieldConstants.BUSINESS_TYPE_TEXT,
						ObjectFieldConstants.DB_TYPE_STRING, "name", "name")));

		ObjectField nameField = ObjectFieldLocalServiceUtil.getObjectField(
			objectDefinition.getObjectDefinitionId(), "name");

		ObjectDefinitionLocalServiceUtil.updateTitleObjectFieldId(
			objectDefinition.getObjectDefinitionId(),
			nameField.getObjectFieldId());

		return ObjectDefinitionLocalServiceUtil.publishCustomObjectDefinition(
			user.getUserId(), objectDefinition.getObjectDefinitionId());
	}

	private Role _createExportRole() throws Exception {
		return RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);
	}

	private User _createExportUser(Role exportRole) throws Exception {
		User exportUser = UserTestUtil.addUser();

		UserLocalServiceUtil.addRoleUser(
			exportRole.getRoleId(), exportUser.getUserId());

		return exportUser;
	}

	private void _grantIndividualViewPermissions(
			ObjectDefinition objectDefinition, List<Long> entryIds, Role role)
		throws Exception {

		String className = objectDefinition.getClassName();

		for (Long entryId : entryIds) {
			ResourcePermissionLocalServiceUtil.setResourcePermissions(
				role.getCompanyId(), className,
				ResourceConstants.SCOPE_INDIVIDUAL, String.valueOf(entryId),
				role.getRoleId(), new String[] {ActionKeys.VIEW});
		}
	}

	private List<Long> _parseBatchTimesFromMetricsLog(Path path)
		throws Exception {

		Assert.assertTrue(
			"Metrics file was not created at " + path, Files.exists(path));

		List<Long> times = new ArrayList<>();
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

		for (String line : lines) {
			line = line.trim();

			if (line.isEmpty()) {
				continue;
			}

			int readMs = _parseIntFromJson(line, "readTimeMs");
			int writeMs = _parseIntFromJson(line, "writeTimeMs");

			times.add((long)(readMs + writeMs));
		}

		return times;
	}

	private int _parseIntFromJson(String line, String key) {
		String search = "\"" + key + "\":";

		int start = line.indexOf(search);

		if (start < 0) {
			return 0;
		}

		start += search.length();

		int end = start;

		while ((end < line.length()) && Character.isDigit(line.charAt(end))) {
			end++;
		}

		if (end == start) {
			return 0;
		}

		return GetterUtil.getInteger(line.substring(start, end));
	}

	private static final int _DOG_ENTRY_COUNT = 10000;

	private static final int _EXPORT_BATCH_SIZE = 100;

	private static final String _METRICS_LOG_PATH =
		"/home/me/dev/bundles/master/tomcat-10.1.52/temp" +
			"/batch_export_metrics.log";

	private static final int _SAMPLE_SIZE = 5;

	@Inject
	private BatchEngineExportTaskExecutor _batchEngineExportTaskExecutor;

	@Inject
	private BatchEngineExportTaskLocalService
		_batchEngineExportTaskLocalService;

	private boolean _configurationRestored = true;
	private int _defaultExportBatchSizeForRestore;
	private Path _metricsPath;

}
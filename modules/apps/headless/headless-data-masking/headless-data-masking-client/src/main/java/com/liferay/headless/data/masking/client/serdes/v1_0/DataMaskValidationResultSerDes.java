/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.data.masking.client.serdes.v1_0;

import com.liferay.headless.data.masking.client.dto.v1_0.DataMaskValidationResult;
import com.liferay.headless.data.masking.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Jose Luis Navarro
 * @generated
 */
@Generated("")
public class DataMaskValidationResultSerDes {

	public static DataMaskValidationResult toDTO(String json) {
		DataMaskValidationResultJSONParser dataMaskValidationResultJSONParser =
			new DataMaskValidationResultJSONParser();

		return dataMaskValidationResultJSONParser.parseToDTO(json);
	}

	public static DataMaskValidationResult[] toDTOs(String json) {
		DataMaskValidationResultJSONParser dataMaskValidationResultJSONParser =
			new DataMaskValidationResultJSONParser();

		return dataMaskValidationResultJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		DataMaskValidationResult dataMaskValidationResult) {

		if (dataMaskValidationResult == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (dataMaskValidationResult.getError() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"error\": ");

			sb.append("\"");

			sb.append(_escape(dataMaskValidationResult.getError()));

			sb.append("\"");
		}

		if (dataMaskValidationResult.getMatchCount() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"matchCount\": ");

			sb.append(dataMaskValidationResult.getMatchCount());
		}

		if (dataMaskValidationResult.getOutput() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"output\": ");

			sb.append("\"");

			sb.append(_escape(dataMaskValidationResult.getOutput()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		DataMaskValidationResultJSONParser dataMaskValidationResultJSONParser =
			new DataMaskValidationResultJSONParser();

		return dataMaskValidationResultJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		DataMaskValidationResult dataMaskValidationResult) {

		if (dataMaskValidationResult == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (dataMaskValidationResult.getError() == null) {
			map.put("error", null);
		}
		else {
			map.put(
				"error", String.valueOf(dataMaskValidationResult.getError()));
		}

		if (dataMaskValidationResult.getMatchCount() == null) {
			map.put("matchCount", null);
		}
		else {
			map.put(
				"matchCount",
				String.valueOf(dataMaskValidationResult.getMatchCount()));
		}

		if (dataMaskValidationResult.getOutput() == null) {
			map.put("output", null);
		}
		else {
			map.put(
				"output", String.valueOf(dataMaskValidationResult.getOutput()));
		}

		return map;
	}

	public static class DataMaskValidationResultJSONParser
		extends BaseJSONParser<DataMaskValidationResult> {

		@Override
		protected DataMaskValidationResult createDTO() {
			return new DataMaskValidationResult();
		}

		@Override
		protected DataMaskValidationResult[] createDTOArray(int size) {
			return new DataMaskValidationResult[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "error")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "matchCount")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "output")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			DataMaskValidationResult dataMaskValidationResult,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "error")) {
				if (jsonParserFieldValue != null) {
					dataMaskValidationResult.setError(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "matchCount")) {
				if (jsonParserFieldValue != null) {
					dataMaskValidationResult.setMatchCount(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "output")) {
				if (jsonParserFieldValue != null) {
					dataMaskValidationResult.setOutput(
						(String)jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}
// LIFERAY-REST-BUILDER-HASH:-575228448
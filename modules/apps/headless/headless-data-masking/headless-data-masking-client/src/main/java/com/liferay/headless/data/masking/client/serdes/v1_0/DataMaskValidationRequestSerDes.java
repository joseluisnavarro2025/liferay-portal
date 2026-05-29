/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.data.masking.client.serdes.v1_0;

import com.liferay.headless.data.masking.client.dto.v1_0.DataMaskValidationRequest;
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
public class DataMaskValidationRequestSerDes {

	public static DataMaskValidationRequest toDTO(String json) {
		DataMaskValidationRequestJSONParser
			dataMaskValidationRequestJSONParser =
				new DataMaskValidationRequestJSONParser();

		return dataMaskValidationRequestJSONParser.parseToDTO(json);
	}

	public static DataMaskValidationRequest[] toDTOs(String json) {
		DataMaskValidationRequestJSONParser
			dataMaskValidationRequestJSONParser =
				new DataMaskValidationRequestJSONParser();

		return dataMaskValidationRequestJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		DataMaskValidationRequest dataMaskValidationRequest) {

		if (dataMaskValidationRequest == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (dataMaskValidationRequest.getDetectionRegex() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"detectionRegex\": ");

			sb.append("\"");

			sb.append(_escape(dataMaskValidationRequest.getDetectionRegex()));

			sb.append("\"");
		}

		if (dataMaskValidationRequest.getReplacementRegex() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"replacementRegex\": ");

			sb.append("\"");

			sb.append(_escape(dataMaskValidationRequest.getReplacementRegex()));

			sb.append("\"");
		}

		if (dataMaskValidationRequest.getReplacementValue() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"replacementValue\": ");

			sb.append("\"");

			sb.append(_escape(dataMaskValidationRequest.getReplacementValue()));

			sb.append("\"");
		}

		if (dataMaskValidationRequest.getSampleText() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"sampleText\": ");

			sb.append("\"");

			sb.append(_escape(dataMaskValidationRequest.getSampleText()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		DataMaskValidationRequestJSONParser
			dataMaskValidationRequestJSONParser =
				new DataMaskValidationRequestJSONParser();

		return dataMaskValidationRequestJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		DataMaskValidationRequest dataMaskValidationRequest) {

		if (dataMaskValidationRequest == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (dataMaskValidationRequest.getDetectionRegex() == null) {
			map.put("detectionRegex", null);
		}
		else {
			map.put(
				"detectionRegex",
				String.valueOf(dataMaskValidationRequest.getDetectionRegex()));
		}

		if (dataMaskValidationRequest.getReplacementRegex() == null) {
			map.put("replacementRegex", null);
		}
		else {
			map.put(
				"replacementRegex",
				String.valueOf(
					dataMaskValidationRequest.getReplacementRegex()));
		}

		if (dataMaskValidationRequest.getReplacementValue() == null) {
			map.put("replacementValue", null);
		}
		else {
			map.put(
				"replacementValue",
				String.valueOf(
					dataMaskValidationRequest.getReplacementValue()));
		}

		if (dataMaskValidationRequest.getSampleText() == null) {
			map.put("sampleText", null);
		}
		else {
			map.put(
				"sampleText",
				String.valueOf(dataMaskValidationRequest.getSampleText()));
		}

		return map;
	}

	public static class DataMaskValidationRequestJSONParser
		extends BaseJSONParser<DataMaskValidationRequest> {

		@Override
		protected DataMaskValidationRequest createDTO() {
			return new DataMaskValidationRequest();
		}

		@Override
		protected DataMaskValidationRequest[] createDTOArray(int size) {
			return new DataMaskValidationRequest[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "detectionRegex")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "replacementRegex")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "replacementValue")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "sampleText")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			DataMaskValidationRequest dataMaskValidationRequest,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "detectionRegex")) {
				if (jsonParserFieldValue != null) {
					dataMaskValidationRequest.setDetectionRegex(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "replacementRegex")) {
				if (jsonParserFieldValue != null) {
					dataMaskValidationRequest.setReplacementRegex(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "replacementValue")) {
				if (jsonParserFieldValue != null) {
					dataMaskValidationRequest.setReplacementValue(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "sampleText")) {
				if (jsonParserFieldValue != null) {
					dataMaskValidationRequest.setSampleText(
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
// LIFERAY-REST-BUILDER-HASH:-237047863
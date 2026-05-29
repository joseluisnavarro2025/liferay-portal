/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.data.masking.client.dto.v1_0;

import com.liferay.headless.data.masking.client.function.UnsafeSupplier;
import com.liferay.headless.data.masking.client.serdes.v1_0.DataMaskValidationResultSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Jose Luis Navarro
 * @generated
 */
@Generated("")
public class DataMaskValidationResult implements Cloneable, Serializable {

	public static DataMaskValidationResult toDTO(String json) {
		return DataMaskValidationResultSerDes.toDTO(json);
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setError(
		UnsafeSupplier<String, Exception> errorUnsafeSupplier) {

		try {
			error = errorUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String error;

	public Integer getMatchCount() {
		return matchCount;
	}

	public void setMatchCount(Integer matchCount) {
		this.matchCount = matchCount;
	}

	public void setMatchCount(
		UnsafeSupplier<Integer, Exception> matchCountUnsafeSupplier) {

		try {
			matchCount = matchCountUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Integer matchCount;

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public void setOutput(
		UnsafeSupplier<String, Exception> outputUnsafeSupplier) {

		try {
			output = outputUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String output;

	@Override
	public DataMaskValidationResult clone() throws CloneNotSupportedException {
		return (DataMaskValidationResult)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof DataMaskValidationResult)) {
			return false;
		}

		DataMaskValidationResult dataMaskValidationResult =
			(DataMaskValidationResult)object;

		return Objects.equals(toString(), dataMaskValidationResult.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return DataMaskValidationResultSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:841367706
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.writer;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;

import java.io.IOException;
import java.io.OutputStream;

import java.util.Collection;
import java.util.List;

/**
 * @author Ivica Cardic
 */
public class JSONBatchEngineExportTaskItemWriterImpl
	implements BatchEngineExportTaskItemWriter {

	public JSONBatchEngineExportTaskItemWriterImpl(
			List<String> includeFieldNames, OutputStream outputStream)
		throws IOException {

		_outputStream = outputStream;

		ObjectWriter objectWriter = ObjectWriterFactory.getObjectWriter(
			includeFieldNames);

		_sequenceWriter = objectWriter.writeValuesAsArray(_outputStream);
	}

	@Override
	public void close() throws IOException {
		_sequenceWriter.close();

		_outputStream.close();
	}

	public long getLastJsonWriterTimeMs() {
		return _lastJsonWriterTimeMs;
	}

	@Override
	public void write(Collection<?> items) throws Exception {
		long writeStartMs = System.currentTimeMillis();
		_sequenceWriter.writeAll(items);
		_lastJsonWriterTimeMs = System.currentTimeMillis() - writeStartMs;
	}

	private long _lastJsonWriterTimeMs;
	private final OutputStream _outputStream;
	private final SequenceWriter _sequenceWriter;

}
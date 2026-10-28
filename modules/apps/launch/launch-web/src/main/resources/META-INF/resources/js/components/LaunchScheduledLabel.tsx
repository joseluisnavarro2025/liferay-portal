/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import React from 'react';

interface Props {
	scheduledDate: string | null;
}

export default function LaunchScheduledLabel({scheduledDate}: Props) {
	if (!scheduledDate) {
		return null;
	}

	return (
		<div className="align-items-center d-flex">
			<ClayLabel className="mb-0 mr-3" displayType="info">
				{Liferay.Language.get('scheduled')}
			</ClayLabel>

			<span className="align-items-center d-flex text-info">
				<ClayIcon
					className="inline-item inline-item-before"
					symbol="date-time"
				/>

				{new Date(scheduledDate).toLocaleString(
					Liferay.ThemeDisplay.getBCP47LanguageId(),
					{
						day: 'numeric',
						hour: '2-digit',
						minute: '2-digit',
						month: 'short',
						timeZoneName: 'short',
						year: 'numeric',
					}
				)}
			</span>
		</div>
	);
}

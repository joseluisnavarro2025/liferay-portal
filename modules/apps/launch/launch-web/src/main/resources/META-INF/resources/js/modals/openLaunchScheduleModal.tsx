/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render} from '@liferay/frontend-js-react-web';
import React from 'react';

import LaunchScheduleModal, {
	TimeZoneOption,
} from '../components/LaunchScheduleModal';

export default function openLaunchScheduleModal({
	launchSetName,
	portletNamespace,
	scheduleLaunchURL,
	timeZoneId,
	timeZones,
}: {
	launchSetName: string;
	portletNamespace: string;
	scheduleLaunchURL: string;
	timeZoneId: string;
	timeZones: TimeZoneOption[];
}) {
	const container = document.createElement('div');

	document.body.appendChild(container);

	const root = render(
		<LaunchScheduleModal
			launchSetName={launchSetName}
			onClose={() => {
				root?.unmount();

				container.remove();
			}}
			portletNamespace={portletNamespace}
			scheduleLaunchURL={scheduleLaunchURL}
			timeZoneId={timeZoneId}
			timeZones={timeZones}
		/>,
		{},
		container
	);
}

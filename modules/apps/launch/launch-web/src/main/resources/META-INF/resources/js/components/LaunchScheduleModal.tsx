/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayDatePicker from '@clayui/date-picker';
import ClayForm, {ClaySelectWithOption} from '@clayui/form';
import ClayModal, {useModal} from '@clayui/modal';
import {FieldBase} from 'frontend-js-components-web';
import {dateUtils, sub} from 'frontend-js-web';
import React, {useState} from 'react';

import {
	getLocaleDateFormat,
	is12HourLocale,
	isCompleteDateTime,
	toStorageDateTime,
	toZonedDate,
} from '../util/dateTime';
import submitLaunchAction from '../util/submitLaunchAction';

import type {FirstDayOfWeekLocale} from 'frontend-js-web';

export type TimeZoneOption = {
	label: string;
	value: string;
};

interface Props {
	launchSetName: string;
	onClose: () => void;
	portletNamespace: string;
	scheduleLaunchURL: string;
	timeZoneId: string;
	timeZones: TimeZoneOption[];
}

export default function LaunchScheduleModal({
	launchSetName,
	onClose: onCloseModal,
	portletNamespace,
	scheduleLaunchURL,
	timeZoneId: initialTimeZoneId,
	timeZones,
}: Props) {
	const locale = Liferay.ThemeDisplay.getBCP47LanguageId();

	const dateFormat = getLocaleDateFormat(locale);
	const use12Hours = is12HourLocale(locale);

	const [displayDateTime, setDisplayDateTime] = useState('');
	const [timeZoneId, setTimeZoneId] = useState(initialTimeZoneId);

	const {observer, onClose} = useModal({
		onClose: onCloseModal,
	});

	const storageDateTime = toStorageDateTime(
		displayDateTime,
		dateFormat,
		use12Hours
	);

	const currentYear = new Date().getFullYear();

	function handleSchedule() {
		const body = new FormData();

		body.append(
			`${portletNamespace}publishTime`,
			String(toZonedDate(storageDateTime, timeZoneId).getTime())
		);

		submitLaunchAction(scheduleLaunchURL, body);
	}

	return (
		<ClayModal observer={observer}>
			<ClayModal.Header>
				{sub(Liferay.Language.get('schedule-x'), launchSetName)}
			</ClayModal.Header>

			<ClayModal.Body>
				<p className="text-secondary">
					{Liferay.Language.get(
						'all-entries-must-be-ready-to-publish-by-the-scheduled-date-and-time.-otherwise,-the-launch-will-not-be-published'
					)}
				</p>

				<FieldBase
					id={`${portletNamespace}publishDateTime`}
					label={Liferay.Language.get('date-and-time')}
					required
				>
					<ClayDatePicker
						dateFormat={dateFormat}
						firstDayOfWeek={dateUtils.getFirstDayOfWeek(
							locale as FirstDayOfWeekLocale
						)}
						id={`${portletNamespace}publishDateTime`}
						months={dateUtils.getMonthsLong(locale)}
						onChange={setDisplayDateTime}
						placeholder={`${dateFormat} ${
							use12Hours ? 'HH:MM AM' : 'HH:MM'
						}`.toUpperCase()}
						time
						use12Hours={use12Hours}
						value={displayDateTime}
						weekdaysShort={dateUtils.getWeekdaysShort(locale)}
						years={{
							end: currentYear + 10,
							start: currentYear,
						}}
					/>
				</FieldBase>

				<ClayForm.Group className="mb-0">
					<label htmlFor={`${portletNamespace}timeZoneId`}>
						{Liferay.Language.get('time-zone')}
					</label>

					<ClaySelectWithOption
						id={`${portletNamespace}timeZoneId`}
						onChange={(event) => setTimeZoneId(event.target.value)}
						options={timeZones}
						value={timeZoneId}
					/>
				</ClayForm.Group>
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton displayType="secondary" onClick={onClose}>
							{Liferay.Language.get('cancel')}
						</ClayButton>

						<ClayButton
							disabled={!isCompleteDateTime(storageDateTime)}
							onClick={handleSchedule}
						>
							{Liferay.Language.get('schedule')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</ClayModal>
	);
}

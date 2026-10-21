/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import ClayModal, {useModal} from '@clayui/modal';
import {openModal, openToast} from 'frontend-js-components-web';
import {escapeHTML, fetch} from 'frontend-js-web';
import React, {useState} from 'react';

interface Props {
	portletNamespace: string;
	published: boolean;
	scheduleLaunchURL: string;
	scheduledDate: string | null;
	unscheduleLaunchURL: string;
}

export default function LaunchScheduleButton({
	portletNamespace,
	published,
	scheduleLaunchURL,
	scheduledDate,
	unscheduleLaunchURL,
}: Props) {
	const [publishDate, setPublishDate] = useState('');
	const [scheduleActive, setScheduleActive] = useState(false);

	const {observer, onClose} = useModal({
		onClose: () => setScheduleActive(false),
	});

	function handleSchedule() {
		const body = new FormData();

		body.append(
			`${portletNamespace}publishTime`,
			String(new Date(publishDate).getTime())
		);

		submit(scheduleLaunchURL, body);
	}

	function handleUnschedule() {
		openModal({
			bodyHTML: escapeHTML(
				Liferay.Language.get(
					'unschedule-the-launch-will-delete-the-scheduled-job-and-the-launch-will-be-moved-to-open.-do-you-want-to-proceed'
				)
			),
			buttons: [
				{
					displayType: 'secondary',
					label: Liferay.Language.get('cancel'),
					type: 'cancel',
				},
				{
					autoFocus: true,
					displayType: 'primary',
					label: Liferay.Language.get('confirm'),
					onClick: ({processClose}: {processClose: () => void}) => {
						processClose();

						submit(unscheduleLaunchURL);
					},
				},
			],
			role: 'alertdialog',
			title: Liferay.Language.get('unschedule'),
		});
	}

	async function submit(url: string, body: FormData = new FormData()) {
		try {
			const response = await fetch(url, {
				body,
				method: 'POST',
			});

			const {error, redirect, validationError} = await response.json();

			if (error || validationError) {
				openToast({
					message: escapeHTML(error || validationError),
					type: 'danger',
				});

				return;
			}

			window.location.href = redirect;
		}
		catch (exception) {
			openToast({
				message: escapeHTML((exception as Error).message),
				type: 'danger',
			});
		}
	}

	if (scheduledDate) {
		return (
			<div className="align-items-center c-gap-2 d-flex">
				<ClayLabel className="m-0" displayType="info">
					{`${Liferay.Language.get('scheduled')}: ${new Date(
						scheduledDate
					).toLocaleString()}`}
				</ClayLabel>

				<ClayButton
					className="text-nowrap"
					displayType="secondary"
					onClick={handleUnschedule}
					small
					type="button"
				>
					{Liferay.Language.get('unschedule')}
				</ClayButton>
			</div>
		);
	}

	if (published) {
		return null;
	}

	return (
		<>
			{scheduleActive && (
				<ClayModal observer={observer} size="sm">
					<ClayModal.Header>
						{Liferay.Language.get('schedule')}
					</ClayModal.Header>

					<ClayModal.Body>
						<ClayInput
							onChange={(event) =>
								setPublishDate(event.target.value)
							}
							type="datetime-local"
							value={publishDate}
						/>
					</ClayModal.Body>

					<ClayModal.Footer
						last={
							<ClayButton.Group spaced>
								<ClayButton
									displayType="secondary"
									onClick={onClose}
								>
									{Liferay.Language.get('cancel')}
								</ClayButton>

								<ClayButton
									disabled={!publishDate}
									onClick={handleSchedule}
								>
									{Liferay.Language.get('schedule')}
								</ClayButton>
							</ClayButton.Group>
						}
					/>
				</ClayModal>
			)}

			<ClayButton
				className="text-nowrap"
				displayType="secondary"
				onClick={() => setScheduleActive(true)}
				small
				type="button"
			>
				<ClayIcon
					className="inline-item inline-item-before"
					symbol="calendar"
				/>

				{Liferay.Language.get('schedule')}
			</ClayButton>
		</>
	);
}

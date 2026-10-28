/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayDropDown from '@clayui/drop-down';
import ClayIcon from '@clayui/icon';
import {openConfirmModal} from 'frontend-js-components-web';
import React, {useRef} from 'react';

import openLaunchScheduleModal from '../modals/openLaunchScheduleModal';
import openUnscheduleLaunchModal from '../modals/openUnscheduleLaunchModal';
import {TimeZoneOption} from './LaunchScheduleModal';

interface Props {
	launchSetName: string;
	portletNamespace: string;
	publishLaunchURL: string;
	published: boolean;
	scheduleLaunchURL: string;
	scheduled: boolean;
	timeZoneId: string;
	timeZones: TimeZoneOption[];
	unscheduleLaunchURL: string;
}

export default function LaunchPublishButton({
	launchSetName,
	portletNamespace,
	publishLaunchURL,
	published,
	scheduleLaunchURL,
	scheduled,
	timeZoneId,
	timeZones,
	unscheduleLaunchURL,
}: Props) {
	const formRef = useRef<HTMLFormElement>(null);

	function handlePublish() {
		openConfirmModal({
			message: Liferay.Language.get(
				'are-you-sure-you-want-to-publish-this-launch'
			),
			onConfirm: (confirmed: boolean) => {
				if (confirmed) {
					formRef.current?.submit();
				}
			},
		});
	}

	return (
		<form action={publishLaunchURL} method="post" ref={formRef}>
			<div className="d-flex">
				<ClayButton
					className={published ? undefined : 'rounded-0-right'}
					disabled={published || scheduled}
					displayType="primary"
					onClick={handlePublish}
					small
					type="button"
				>
					<ClayIcon
						className="inline-item inline-item-before"
						symbol="rocket"
					/>

					{published
						? Liferay.Language.get('published')
						: Liferay.Language.get('publish')}
				</ClayButton>

				{!published && (
					<ClayDropDown
						hasLeftSymbols
						trigger={
							<ClayButton
								aria-label={Liferay.Language.get(
									'publish-options'
								)}
								className="border-left px-2 rounded-0-left"
								small
								title={Liferay.Language.get('publish-options')}
							>
								<span className="inline-item">
									<ClayIcon symbol="caret-bottom" />
								</span>
							</ClayButton>
						}
					>
						<ClayDropDown.ItemList>
							{scheduled ? (
								<ClayDropDown.Item
									onClick={() =>
										openUnscheduleLaunchModal(
											unscheduleLaunchURL
										)
									}
									symbolLeft="date-time"
								>
									{Liferay.Language.get(
										'unschedule-publication'
									)}
								</ClayDropDown.Item>
							) : (
								<ClayDropDown.Item
									onClick={() =>
										openLaunchScheduleModal({
											launchSetName,
											portletNamespace,
											scheduleLaunchURL,
											timeZoneId,
											timeZones,
										})
									}
									symbolLeft="date-time"
								>
									{Liferay.Language.get(
										'schedule-publication'
									)}
								</ClayDropDown.Item>
							)}
						</ClayDropDown.ItemList>
					</ClayDropDown>
				)}
			</div>
		</form>
	);
}

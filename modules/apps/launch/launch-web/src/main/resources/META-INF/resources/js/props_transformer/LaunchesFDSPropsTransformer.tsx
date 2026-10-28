/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLabel from '@clayui/label';
import React from 'react';

import {Launch, isPublished} from '../api/launches';
import {TimeZoneOption} from '../components/LaunchScheduleModal';
import {
	WORKFLOW_STATUS_DRAFT,
	WORKFLOW_STATUS_SCHEDULED,
} from '../components/WorkflowStatusLabel';
import openLaunchScheduleModal from '../modals/openLaunchScheduleModal';
import openNewLaunchModal from '../modals/openNewLaunchModal';
import openUnscheduleLaunchModal from '../modals/openUnscheduleLaunchModal';

interface AdditionalProps {
	portletNamespace: string;
	scheduleLaunchURL: string;
	timeZoneId: string;
	timeZones: TimeZoneOption[];
	unscheduleLaunchURL: string;
	viewLaunchURL: string;
}

interface CreationMenu {
	primaryItems: Array<{[key: string]: unknown}>;
}

interface ItemAction {
	data?: {id?: string};
	[key: string]: unknown;
}

interface Props {
	additionalProps: AdditionalProps;
	creationMenu?: CreationMenu;
	itemsActions?: ItemAction[];
	[key: string]: unknown;
}

const ITEM_ACTION_VISIBILITIES: {
	[key: string]: ((launch: Launch) => boolean) | undefined;
} = {
	schedule: (launch) => launch.status?.code === WORKFLOW_STATUS_DRAFT,
	unschedule: (launch) => launch.status?.code === WORKFLOW_STATUS_SCHEDULED,
};

function getLaunchActionURL(url: string, launch: Launch) {
	return url.replace(/(?:%7B|\{)id(?:%7D|\})/, String(launch.id));
}

export default function LaunchesFDSPropsTransformer({
	additionalProps,
	creationMenu,
	itemsActions,
	...props
}: Props) {
	return {
		...props,
		additionalProps,
		creationMenu: creationMenu && {
			...creationMenu,
			primaryItems: creationMenu.primaryItems.map((primaryItem) => ({
				...primaryItem,
				onClick: () =>
					openNewLaunchModal({
						viewLaunchURL: additionalProps.viewLaunchURL,
					}),
			})),
		},
		customRenderers: {
			tableCell: [
				{
					component: ({itemData}: {itemData: Launch}) => {
						if (isPublished(itemData)) {
							return (
								<ClayLabel displayType="success">
									{Liferay.Language.get('published')}
								</ClayLabel>
							);
						}

						if (
							itemData.scheduledDate &&
							itemData.status?.code === WORKFLOW_STATUS_DRAFT
						) {
							return (
								<ClayLabel displayType="danger">
									{Liferay.Language.get('failed')}
								</ClayLabel>
							);
						}

						if (
							itemData.status?.code === WORKFLOW_STATUS_SCHEDULED
						) {
							return (
								<ClayLabel displayType="info">
									{Liferay.Language.get('scheduled')}
								</ClayLabel>
							);
						}

						return (
							<ClayLabel displayType="info">
								{Liferay.Language.get('in-progress')}
							</ClayLabel>
						);
					},
					name: 'launchStatus',
					type: 'internal',
				},
			],
		},
		itemsActions: itemsActions?.map((itemAction) => {
			const isVisible =
				ITEM_ACTION_VISIBILITIES[itemAction.data?.id ?? ''];

			return isVisible ? {...itemAction, isVisible} : itemAction;
		}),
		onActionDropdownItemClick({
			action,
			itemData,
		}: {
			action: ItemAction;
			itemData: Launch;
		}) {
			if (action.data?.id === 'schedule') {
				openLaunchScheduleModal({
					launchSetName: itemData.name,
					portletNamespace: additionalProps.portletNamespace,
					scheduleLaunchURL: getLaunchActionURL(
						additionalProps.scheduleLaunchURL,
						itemData
					),
					timeZoneId: additionalProps.timeZoneId,
					timeZones: additionalProps.timeZones,
				});
			}
			else if (action.data?.id === 'unschedule') {
				openUnscheduleLaunchModal(
					getLaunchActionURL(
						additionalProps.unscheduleLaunchURL,
						itemData
					)
				);
			}
		},
	};
}

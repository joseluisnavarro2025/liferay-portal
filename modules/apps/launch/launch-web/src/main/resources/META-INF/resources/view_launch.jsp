<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
ViewLaunchDisplayContext viewLaunchDisplayContext = (ViewLaunchDisplayContext)request.getAttribute(LaunchWebKeys.VIEW_LAUNCH_DISPLAY_CONTEXT);

portletDisplay.setShowBackIcon(true);
portletDisplay.setURLBack(viewLaunchDisplayContext.getBackURL());

String launchSetName = viewLaunchDisplayContext.getLaunchSetName();

if (Validator.isNotNull(launchSetName)) {
	renderResponse.setTitle(launchSetName);
}

String scheduledDate = viewLaunchDisplayContext.getScheduledDate();
%>

<liferay-ui:error exception="<%= PortalException.class %>" message="unable-to-publish-the-launch" />

<div class="launch-header">
	<div class="launch-header__title">
		<h2 class="mb-0 text-truncate"><%= HtmlUtil.escape(GetterUtil.getString(launchSetName)) %></h2>

		<%-- A React component renders into its placeholder's parent, so each one needs a parent of its own --%>

		<div>
			<react:component
				module="{LaunchScheduledLabel} from launch-web"
				props='<%=
					HashMapBuilder.<String, Object>put(
						"scheduledDate", scheduledDate
					).build()
				%>'
			/>
		</div>
	</div>

	<div class="launch-header__actions">
		<div>
			<react:component
				module="{LaunchPreviewButton} from launch-web"
				props='<%=
					HashMapBuilder.<String, Object>put(
						"itemSelectedEventName", viewLaunchDisplayContext.getPreviewPageItemSelectedEventName()
					).put(
						"itemSelectorURL", viewLaunchDisplayContext.getPreviewPageItemSelectorURL()
					).put(
						"launchSetId", viewLaunchDisplayContext.getLaunchSetId()
					).build()
				%>'
			/>
		</div>

		<div>
			<react:component
				module="{LaunchPublishButton} from launch-web"
				props='<%=
					HashMapBuilder.<String, Object>put(
						"launchSetName", GetterUtil.getString(launchSetName)
					).put(
						"published", viewLaunchDisplayContext.isPublished()
					).put(
						"publishLaunchURL", viewLaunchDisplayContext.getPublishLaunchURL()
					).put(
						"scheduled", scheduledDate != null
					).put(
						"scheduleLaunchURL", viewLaunchDisplayContext.getScheduleLaunchURL()
					).put(
						"timeZoneId", viewLaunchDisplayContext.getTimeZoneId()
					).put(
						"timeZones", viewLaunchDisplayContext.getTimeZonesJSONArray()
					).put(
						"unscheduleLaunchURL", viewLaunchDisplayContext.getUnscheduleLaunchURL()
					).build()
				%>'
			/>
		</div>
	</div>
</div>

<liferay-frontend:screen-navigation
	context="<%= new LaunchSet(viewLaunchDisplayContext.getLaunchSetId(), launchSetName) %>"
	key="<%= LaunchScreenNavigationConstants.SCREEN_NAVIGATION_KEY_LAUNCH %>"
	navCssClass="container-fluid-max-xxxl"
	portletURL='<%=
		PortletURLBuilder.createRenderURL(
			renderResponse
		).setMVCRenderCommandName(
			"/launch/view_launch"
		).setParameter(
			"launchSetId", viewLaunchDisplayContext.getLaunchSetId()
		).setParameter(
			"launchSetName", launchSetName
		).build()
	%>'
/>
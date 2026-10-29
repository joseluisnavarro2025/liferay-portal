/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.web.internal.portlet.action;

import com.liferay.launch.set.publisher.LaunchSetPublisher;
import com.liferay.launch.web.internal.constants.LaunchPortletKeys;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;
import jakarta.portlet.PortletException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 * @author Jose Luis Navarro
 */
@Component(
	property = {
		"jakarta.portlet.name=" + LaunchPortletKeys.LAUNCH,
		"mvc.command.name=/launch/publish_launch"
	},
	service = MVCActionCommand.class
)
public class PublishLaunchMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		long launchSetId = ParamUtil.getLong(actionRequest, "launchSetId");

		try {
			TransactionInvokerUtil.invoke(
				_transactionConfig,
				() -> {
					_launchSetPublisher.publish(launchSetId);

					return null;
				});
		}
		catch (Throwable throwable) {
			PortletException portletException = new PortletException(throwable);

			try {
				_launchSetPublisher.failPublish(
					launchSetId, PrincipalThreadLocal.getUserId(),
					GetterUtil.getString(
						throwable.getMessage(), String.valueOf(throwable)));
			}
			catch (Exception exception) {
				portletException.addSuppressed(exception);
			}

			throw portletException;
		}

		sendRedirect(actionRequest, actionResponse);
	}

	private static final TransactionConfig _transactionConfig =
		TransactionConfig.Factory.create(
			Propagation.REQUIRED, new Class<?>[] {Exception.class});

	@Reference
	private LaunchSetPublisher _launchSetPublisher;

}
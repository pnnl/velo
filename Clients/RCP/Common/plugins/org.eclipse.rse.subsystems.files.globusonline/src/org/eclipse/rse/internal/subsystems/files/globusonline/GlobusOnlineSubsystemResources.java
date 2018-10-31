/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 * David Dykstal (IBM) - added RESID_FTP_SETTINGS_LABEL
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.globusonline;

import org.eclipse.osgi.util.NLS;

public class GlobusOnlineSubsystemResources extends NLS {
	private static final String BUNDLE_NAME = GlobusOnlineSubsystemResources.class.getName(); //$NON-NLS-1$
	static {
		NLS.initializeMessages(BUNDLE_NAME, GlobusOnlineSubsystemResources.class);
	}
	private GlobusOnlineSubsystemResources() {
	}
	
	public static String	RESID_GLOBUS_ONLINE_CONNECTORSERVICE_NAME;
	public static String	RESID_GLOBUS_ONLINE_CONNECTORSERVICE_DESCRIPTION;
	public static String RESID_GLOBUS_ONLINE_SETTINGS_LABEL;
	
}

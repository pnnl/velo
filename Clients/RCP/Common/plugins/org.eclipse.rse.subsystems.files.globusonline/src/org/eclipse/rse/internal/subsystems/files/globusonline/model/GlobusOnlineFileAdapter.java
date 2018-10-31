/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * Martin Oberhuber (Wind River) - [235363][api][breaking] IHostFileToRemoteFileAdapter methods should return AbstractRemoteFile
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.globusonline.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.internal.services.files.globusonline.GlobusOnlineHostFile;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;


public class GlobusOnlineFileAdapter implements IHostFileToRemoteFileAdapter
{

	public AbstractRemoteFile[] convertToRemoteFiles(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile[] nodes)
	{
		List results = new ArrayList();
		for (int i = 0; i < nodes.length; i++)
		{
			GlobusOnlineHostFile node = (GlobusOnlineHostFile)nodes[i];
			GlobusOnlineRemoteFile globusOnlineFile = new GlobusOnlineRemoteFile(ss, context, parent, node);
			results.add(globusOnlineFile);
			ss.cacheRemoteFile(globusOnlineFile);
		}
		return (GlobusOnlineRemoteFile[]) results.toArray(new GlobusOnlineRemoteFile[results.size()]);
	}

	public AbstractRemoteFile convertToRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile node)
	{
		GlobusOnlineRemoteFile file = new GlobusOnlineRemoteFile(ss, context, parent, (GlobusOnlineHostFile) node);
		ss.cacheRemoteFile(file);
		return file;
	}

}

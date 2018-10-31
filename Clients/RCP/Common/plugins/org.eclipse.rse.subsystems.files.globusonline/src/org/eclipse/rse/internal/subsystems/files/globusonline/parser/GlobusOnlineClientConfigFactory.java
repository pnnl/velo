/********************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 *   Javier Montalvo Orus (Symbian) - improved autodetection of FTPListingParser
 *   Javier Montalvo Orus (Symbian) - [212382] additional "initCommands" slot for ftpListingParsers extension point
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.files.globusonline.parser;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.ParserInitializationException;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.internal.services.files.globusonline.parser.IGlobusOnlineClientConfigFactory;
import org.eclipse.rse.internal.services.files.globusonline.parser.IGlobusOnlineClientConfigProxy;
import org.osgi.framework.Bundle;

public class GlobusOnlineClientConfigFactory implements IGlobusOnlineClientConfigFactory {

	private static GlobusOnlineClientConfigFactory factory = null;

	private Hashtable globusOnlineConfigProxyById = new Hashtable();
	private Hashtable globusOnlineParsers = new Hashtable();
	private IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.rse.subsystems.files.globusonline","globusOnlineListingParsers"); //$NON-NLS-1$ //$NON-NLS-2$
	private UnixGlobusOnlineEntryParser defaultGlobusOnlineEntryParser = new UnixGlobusOnlineEntryParser();

	/**
	 * Constructor of the parser factory
	 * @return an instance of the factory
	 */
	public static GlobusOnlineClientConfigFactory getParserFactory()
	{
		if(factory==null)
		{
			factory = new GlobusOnlineClientConfigFactory();
		}

		return factory;
	}

	private GlobusOnlineClientConfigFactory() {

		IConfigurationElement[] ce = ep.getConfigurationElements();
		for (int i = 0; i < ce.length; i++) {

			String id = ce[i].getAttribute("id"); //$NON-NLS-1$
			String label = ce[i].getAttribute("label"); //$NON-NLS-1$
			String priority = ce[i].getAttribute("priority"); //$NON-NLS-1$
			String systemTypeRegex =  ce[i].getAttribute("systemTypeRegex"); //$NON-NLS-1$
			String className = ce[i].getAttribute("class"); //$NON-NLS-1$
			Bundle declaringBundle = Platform.getBundle(ce[i].getContributor().getName());

			String listCommandModifiers = ce[i].getAttribute("listCommandModifiers");  //$NON-NLS-1$

			String defaultDateFormatStr = ce[i].getAttribute("defaultDateFormatStr"); //$NON-NLS-1$
			String recentDateFormatStr = ce[i].getAttribute("recentDateFormatStr"); //$NON-NLS-1$
			String serverLanguageCode = ce[i].getAttribute("serverLanguageCode"); //$NON-NLS-1$
			String shortMonthNames = ce[i].getAttribute("shortMonthNames"); //$NON-NLS-1$
			String serverTimeZoneId = ce[i].getAttribute("serverTimeZoneId"); //$NON-NLS-1$

			IConfigurationElement[] initialCommandsSequence = ce[i].getChildren("initCommand"); //$NON-NLS-1$
			String[] initialCommands = new String[initialCommandsSequence.length];

			for (int j = 0; j < initialCommands.length; j++) {
				initialCommands[j] = initialCommandsSequence[j].getAttribute("cmd"); //$NON-NLS-1$
			}

			GlobusOnlineClientConfigProxy globusOnlineClientConfigProxy = new GlobusOnlineClientConfigProxy(id,label,priority,systemTypeRegex,className,declaringBundle,listCommandModifiers,
					defaultDateFormatStr,recentDateFormatStr,serverLanguageCode,shortMonthNames,serverTimeZoneId, initialCommands);

			globusOnlineConfigProxyById.put(id, globusOnlineClientConfigProxy);

		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.internal.services.files.ftp.parser.IFTPClientConfigFactory#getFTPClientConfig(java.lang.String)
	 */
	public IGlobusOnlineClientConfigProxy getGlobusOnlineClientConfig(String parser, String systemName)
	{

		if (systemName == null) {
			// avoid NPE if systemName could not be determined
			systemName = ""; //$NON-NLS-1$
		}

		System.out.println("GlobusOnlineClientConfigFactor.getGlobusOnlineClientConfig");
		GlobusOnlineClientConfigProxy foundGlobusOnlineClientConfigProxy = null;

		if(parser.equals("AUTO")) //$NON-NLS-1$
		{
			int previousPriority = Integer.MAX_VALUE;
			GlobusOnlineClientConfigProxy foundProxy = null;

			Enumeration globusOnlineConfigProxyEnum = globusOnlineConfigProxyById.elements();

			while(globusOnlineConfigProxyEnum.hasMoreElements())
			{
				GlobusOnlineClientConfigProxy proxy = (GlobusOnlineClientConfigProxy)globusOnlineConfigProxyEnum.nextElement();

				if(proxy.getSystemTypeRegex()!=null)
				{
					Pattern globusOnlineSystemTypesRegex = Pattern.compile(proxy.getSystemTypeRegex());
					if(globusOnlineSystemTypesRegex.matcher(systemName).matches())
					{
						int priority = proxy.getPriority();

						if(priority < previousPriority)
						{
							foundProxy = proxy;
							previousPriority = priority;
						}
					}
				}
			}

			//process the selected proxy
			if(foundProxy != null)
			{
//				GlobusOnlineClientConfig config = null;

				if(!globusOnlineParsers.containsKey(foundProxy.getClassName()))
				{
					GlobusOnlineFileEntryParser entryParser = null;
					try {
						entryParser = (GlobusOnlineFileEntryParser)foundProxy.getDeclaringBundle().loadClass(foundProxy.getClassName()).newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					globusOnlineParsers.put(foundProxy.getClassName(), entryParser);
				}
/*
				config = new GlobusOnlineClientConfig(foundProxy.getClassName());

				//not necessary checking for null, as null is valid input
				config.setDefaultDateFormatStr(foundProxy.getDefaultDateFormatStr());
				config.setRecentDateFormatStr(foundProxy.getRecentDateFormatStr());
				config.setServerLanguageCode(foundProxy.getServerLanguageCode());
				config.setShortMonthNames(foundProxy.getShortMonthNames());
				config.setServerTimeZoneId(foundProxy.getServerTimeZoneId());

				//not necessary storing in the hashtable, as discovered will not be reused
				foundProxy.setFTPClientConfig(config);
*/
				foundGlobusOnlineClientConfigProxy = foundProxy;

			}
		}


		if(foundGlobusOnlineClientConfigProxy==null)
		{
			if(globusOnlineConfigProxyById.containsKey(parser))
			{
				//restore parser from the proxy hashtable
				foundGlobusOnlineClientConfigProxy = (GlobusOnlineClientConfigProxy)globusOnlineConfigProxyById.get(parser);

				//activate if necessary
//				if(foundGlobusOnlineClientConfigProxy.getGlobusOnlineClientConfig()==null)
//				{
//					GlobusOnlineClientConfig config = null;

					if(!globusOnlineParsers.containsKey(foundGlobusOnlineClientConfigProxy.getClassName()))
					{
						FTPFileEntryParser entryParser = null;
						try {
							entryParser = (FTPFileEntryParser)foundGlobusOnlineClientConfigProxy.getDeclaringBundle().loadClass(foundGlobusOnlineClientConfigProxy.getClassName()).newInstance();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
						globusOnlineParsers.put(foundGlobusOnlineClientConfigProxy.getClassName(), entryParser);
					}
/*
					config = new GlobusOnlineClientConfig(foundGlobusOnlineClientConfigProxy.getClassName());

					//not necessary checking for null, as null is valid input
					config.setDefaultDateFormatStr(foundGlobusOnlineClientConfigProxy.getDefaultDateFormatStr());
					config.setRecentDateFormatStr(foundGlobusOnlineClientConfigProxy.getRecentDateFormatStr());
					config.setServerLanguageCode(foundGlobusOnlineClientConfigProxy.getServerLanguageCode());
					config.setShortMonthNames(foundGlobusOnlineClientConfigProxy.getShortMonthNames());
					config.setServerTimeZoneId(foundGlobusOnlineClientConfigProxy.getServerTimeZoneId());

					foundGlobusOnlineClientConfigProxy.setGlobusOnlineClientConfig(config);
*/
//				}
			}
		}

		return foundGlobusOnlineClientConfigProxy;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.internal.services.files.ftp.parser.IFTPClientConfigFactory#getKeySet()
	 */
	public String[] getKeySet()
	{
		return (String[])globusOnlineConfigProxyById.keySet().toArray(new String[globusOnlineConfigProxyById.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory#createFileEntryParser(java.lang.String)
	 */
	
	public GlobusOnlineFileEntryParser createFileEntryParser(String key)	throws ParserInitializationException {

		GlobusOnlineFileEntryParser entryParser = null;
		if (key == null) {
			// avoid NPE in containsKey when the SYST command returned null
			key = ""; //$NON-NLS-1$
		}
		if(!globusOnlineParsers.containsKey(key))
		{
			entryParser = defaultGlobusOnlineEntryParser;
		}
		else
		{
			entryParser = (GlobusOnlineFileEntryParser)globusOnlineParsers.get(key);
		}

		return entryParser;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory#createFileEntryParser(org.apache.commons.net.ftp.FTPClientConfig)
	 */
	/*
	public GlobusOnlineFileEntryParser createFileEntryParser(GlobusOnlineClientConfig config)	throws ParserInitializationException {

		String key = config.getServerSystemKey();
		return createFileEntryParser(key);

	}
	*/

}

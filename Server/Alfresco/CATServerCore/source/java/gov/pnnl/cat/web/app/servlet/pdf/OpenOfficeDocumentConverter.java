/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.cat.web.app.servlet.pdf;
import java.io.File;
import java.net.MalformedURLException;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * 
 * OpenOfficeDocumentConverter.java
 * Version: $Revision: $
 *
 * Pacific Northwest National Laboratory
 * Battelle Memorial Institute
 * Copyright (c) 2007
 * @version $Revision: 1.0 $
 */
public class OpenOfficeDocumentConverter implements OpenOfficeConstants {

	private XComponentContext mXRemoteContext;
	private Object mDesktop;
	private XBridge mBridge;
	
	/**
	 * Method connect.
	 * @param hostIP String
	 * @param port int
	 * @throws Exception
	 */
	public void connect(String hostIP, int port) throws Exception {
		mXRemoteContext = Bootstrap.createInitialComponentContext(null);

		Object connector = mXRemoteContext.getServiceManager()
				.createInstanceWithContext("com.sun.star.connection.Connector",
						mXRemoteContext);

		XConnector xConnector = (XConnector) UnoRuntime.queryInterface(
				XConnector.class, connector);
		XConnection connection = xConnector
				.connect("socket,host="+hostIP+",port=" + port);
		Object bridgeFactory = mXRemoteContext.getServiceManager()
				.createInstanceWithContext("com.sun.star.bridge.BridgeFactory",
						mXRemoteContext);
		XBridgeFactory xBridgeFactory = (XBridgeFactory) UnoRuntime
				.queryInterface(XBridgeFactory.class, bridgeFactory);
		mBridge = xBridgeFactory.createBridge("", "urp", connection, null);
		Object serviceManager = mBridge
				.getInstance("StarOffice.ServiceManager");
		XMultiComponentFactory xRemoteServiceManager = (XMultiComponentFactory) UnoRuntime
				.queryInterface(XMultiComponentFactory.class, serviceManager);
		XPropertySet xProperySet = (XPropertySet) UnoRuntime.queryInterface(
				XPropertySet.class, xRemoteServiceManager);
		Object defaultContext = xProperySet.getPropertyValue("DefaultContext");
		XComponentContext xOfficeComponentContext = (XComponentContext) UnoRuntime
				.queryInterface(XComponentContext.class, defaultContext);

		mDesktop = xRemoteServiceManager.createInstanceWithContext(
				"com.sun.star.frame.Desktop", xOfficeComponentContext);
	}

	/**
	 * Convert a document to a different format.
	 * 
	 * @param targetFile Name of the file, which is the target of the export.
	 * @param conversionFilter Contains the conversion filter (see link above).
	 * @param sourceFile String
	 * @throws Exception
	 */
	public void convert(String sourceFile, String targetFile,
			String conversionFilter) throws Exception {
		XComponent doc = null;
		try {
			doc = openDocument(sourceFile);

			XStorable xStorable = (XStorable) UnoRuntime.queryInterface(
					XStorable.class, doc);

			PropertyValue[] convertProps = new PropertyValue[2];

			// TRUE appears to be the default for this, set anyways
			convertProps[0] = new PropertyValue();
			convertProps[0].Name = OVERWRITE_PROPERTY;
			convertProps[0].Value = Boolean.TRUE;

			convertProps[1] = new PropertyValue();
			convertProps[1].Name = FILTERNAME_PROPERTY;
			convertProps[1].Value = conversionFilter;

			try {
				xStorable.storeToURL(createURL(targetFile),
						convertProps);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		finally {
			if(doc != null) {
				doc.dispose();
			}
		}
	}
	
	public void disconnect() {
		UnoRuntime.setCurrentContext(null);
		XComponent xcomponent = (XComponent) UnoRuntime.queryInterface(
				XComponent.class, mBridge);
		if(xcomponent != null) {
			xcomponent.dispose();
		}
	}
	
	/**
	 * Method createURL.
	 * @param file String
	 * @return String
	 * @throws MalformedURLException
	 */
	private String createURL(String file) throws MalformedURLException {
		return new File(file).toURL().toExternalForm().replace("file:/", "file:///");
	}

	/**
	 * Method openDocument.
	 * @param sourceFile String
	 * @return XComponent
	 * @throws Exception
	 */
	private XComponent openDocument(String sourceFile) throws Exception {
		XComponentLoader loader = (XComponentLoader) UnoRuntime
				.queryInterface(XComponentLoader.class, mDesktop);

		PropertyValue[] openProps = new PropertyValue[1];
		openProps[0] = new PropertyValue();
		// HIDDEN_PROPERTY set to TRUE to disable GUI
		openProps[0].Name = HIDDEN_PROPERTY;
		openProps[0].Value = Boolean.TRUE;

		// Load a given document
		XComponent doc = loader.loadComponentFromURL(createURL(sourceFile),
				"_blank",
				0,
				openProps);
		return doc;
	}

	/**
	 * Method main.
	 * @param args String[]
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		OpenOfficeDocumentConverter converter = new OpenOfficeDocumentConverter();
		converter.connect("localhost", 8100);
		String filter = OpenOfficeConstants.PDF_EXPORT_FILTER;
		String outputExtension = ".pdf";
		try {
		System.err.println("Converting Monkey HTML");
		converter.convert("examples/Monkey.htm", "exampleOutput/MonkeyHTML"+outputExtension, filter);
		System.err.println("Converting TIF");
		converter.convert("examples/1_p00.tif", "exampleOutput/1"+outputExtension, filter);
		System.err.println("Converting BMP");
		converter.convert("examples/redBlob.bmp", "exampleOutput/redBlobBMP"+outputExtension, filter);
		System.err.println("Converting JPG");
		converter.convert("examples/redBlob.jpg", "exampleOutput/redBlobJPG"+outputExtension, filter);
		System.err.println("Converting PNG");
		converter.convert("examples/redBlob.png", "exampleOutput/redBlobPNG"+outputExtension, filter);
		System.err.println("Converting DOC");
		converter.convert("examples/ExampleDoc.doc", "exampleOutput/ExampleDoc"+outputExtension, filter);
		System.err.println("Converting XHTML");
		converter.convert("examples/xmlversion.htm", "exampleOutput/ExampleXHTML"+outputExtension, filter);
		System.err.println("Converting Excel");
		converter.convert("examples/ExampleExcel.xls", "exampleOutput/ExampleExcel"+outputExtension, filter);
		System.err.println("Converting text");
		converter.convert("examples/ExampleTXT.txt", "exampleOutput/ExampleTXT"+outputExtension, filter);
		System.err.println("Converting Powerpoint");
		converter.convert("examples/ExamplePowerpoint.ppt", "exampleOutput/ExamplePowerpoint"+outputExtension, filter);
		System.err.println("Converting HTML");
		converter.convert("examples/ExampleHTML.htm", "exampleOutput/ExampleHTML"+outputExtension, filter);
		}
		finally {
			converter.disconnect();
			System.err.println("Finished");
		}

	}
}

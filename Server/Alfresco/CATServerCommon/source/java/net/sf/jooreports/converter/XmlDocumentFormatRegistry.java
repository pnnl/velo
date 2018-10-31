//
// JOOConverter - The Open Source Java/OpenOffice Document Converter
// Copyright (C) 2004-2006 - Mirko Nasato <mirko@artofsolving.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// http://www.gnu.org/copyleft/lesser.html
//
package net.sf.jooreports.converter;

import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

import org.apache.commons.io.IOUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XmlDocumentFormatRegistry extends BasicDocumentFormatRegistry implements DocumentFormatRegistry {

	private static final String DEFAULT_CONFIGURATION =
		"/"+ XmlDocumentFormatRegistry.class.getPackage().getName().replace('.', '/')
		+ "/document-formats.xml";

	public XmlDocumentFormatRegistry() {
		load(getClass().getResourceAsStream(DEFAULT_CONFIGURATION));
	}

	public XmlDocumentFormatRegistry(InputStream inputStream) {
		load(inputStream);
	}

	private void load(InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("inputStream is null");
		}
		XStream xstream = createXStream();
		try {
			ObjectInputStream in = xstream.createObjectInputStream(new InputStreamReader(inputStream));
			while (true) {
				try {
					addDocumentFormat((DocumentFormat) in.readObject());
				} catch (EOFException endOfFile) {
					break;
				}
			}
		} catch (Exception exception) {
			throw new RuntimeException("invalid registry configuration", exception);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	private XStream createXStream() {
		XStream xstream = new XStream(new DomDriver());
		xstream.setMode(XStream.NO_REFERENCES);
		xstream.alias("document-format", DocumentFormat.class);
		xstream.aliasField("mime-type", DocumentFormat.class, "mimeType");
        xstream.aliasField("file-extension", DocumentFormat.class, "fileExtension");
		xstream.aliasField("export-filters", DocumentFormat.class, "exportFilters");
		xstream.aliasField("export-options", DocumentFormat.class, "exportOptions");
		
		// Velo Change:  changed converter API to match latest version of xstream
		xstream.alias("family", DocumentFamily.class);
        xstream.registerConverter(new AbstractSingleValueConverter() {
          
          @Override
          public Object fromString(String name) {
            return DocumentFamily.getFamily(name);
          }
          
          @Override
          public boolean canConvert(Class type) {
            return type.equals(DocumentFamily.class);
          }
        });
		return xstream;
	}
}

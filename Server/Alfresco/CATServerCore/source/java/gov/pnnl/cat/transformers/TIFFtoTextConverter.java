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
package gov.pnnl.cat.transformers;

import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformer2;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

/**
 * Converts TIFF images to text using the Tesseract OCR software
 * TIFFtoTextConverter.java
 * Version: $Revision: $
 *
 * Pacific Northwest National Laboratory
 * Battelle Memorial Institute
 * Copyright (c) 2007
 * @version $Revision: 1.0 $
 */
public class TIFFtoTextConverter extends AbstractContentTransformer2 {

	private final static Log LOG = LogFactory.getLog(TIFFtoTextConverter.class);
	
	// This constant needs to be parameterized
	//private final static String TESSERACT_DIR = "C:\\OCR\\tesseract-1.03\\bin.dbg";
	private final static String TESSERACT_CONFIGURATION = "batch";
	//private final static String TESSERACT_BINARY_NAME = "tesseract.exe";

	private final static String TIFF_MIME_TYPE = "image/tiff";
	private final static String TIFF_ENCODING = "tiff";

	/**
	 * Method getReliability.
	 * @param sourceMimetype String
	 * @param targetMimetype String
	 * @return double
	 */
	public double getReliability(String sourceMimetype, String targetMimetype) {
		return TIFF_MIME_TYPE.equals(sourceMimetype) && targetMimetype.equals(MimetypeMap.MIMETYPE_TEXT_PLAIN) ? 1.0 : 0.0;
	}
	
	private String exePath;

	/**
	 * Method setExePath.
	 * @param exePath String
	 */
	public void setExePath(String exePath) {
		this.exePath = exePath;
	}
	
	/*public void  doitJAI() throws IOException {
        FileSeekableStream ss = new FileSeekableStream("d:/multi.tif");
        ImageDecoder dec = ImageCodec.createImageDecoder("tiff", ss, null);
        int count = dec.getNumPages();
        TIFFEncodeParam param = new TIFFEncodeParam();
        param.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);
        param.setLittleEndian(false); // Intel
        System.out.println("This TIF has " + count + " image(s)");
        for (int i = 0; i < count; i++) {
            RenderedImage page = dec.decodeAsRenderedImage(i);
            File f = new File("d:/single_" + i + ".tif");
            System.out.println("Saving " + f.getCanonicalPath());
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(page);
            pb.add(f.toString());
            pb.add("tiff");
            pb.add(param);
            RenderedOp r = JAI.create("filestore",pb);
            r.dispose();
        }
    }*/

	/**
	 * Method transformInternal.
	 * @param reader ContentReader
	 * @param writer ContentWriter
	 * @param options TransformationOptions
	 * @throws Exception
	 */
	@Override
	protected void transformInternal(ContentReader reader,
			ContentWriter writer, TransformationOptions options) throws Exception {
		// Create a temporary text output file that will be
		// as output location for the text
		File tempTxtFile = TempFileProvider.createTempFile("tmp", ".txt");
		// Create a temporary TIFF file that will be used
		// as input to Tesseract
		File tempTIFFFile = TempFileProvider.createTempFile("tmp", ".tif");
		InputStream inStream = reader.getContentInputStream();
		OutputStream outStream = writer.getContentOutputStream();
		File tesseractExe = new File(exePath);
		String tesseractParentDir = tesseractExe.getParentFile().getAbsolutePath();
		try {
			// Read the image
			RenderedImage[] pages = readImage(inStream, TIFF_ENCODING);
			StringBuilder fullOCRText = new StringBuilder();
			for(int i = 0; i < pages.length; i++) {		
				// Save an uncompressed copy of the image to disk
				// Tesseract only likes uncompressed TIFFs
				saveUncompressedTIFFImageToDisk(pages[i], tempTIFFFile);
				// Run tesseract on the uncompressed version
				// and read the OCR text
				String ocrPageText = runTesseract(tempTIFFFile, tempTxtFile,
					tesseractParentDir, tesseractExe.getName());
				fullOCRText.append(ocrPageText).append('\n');
			}
			// Write out the resulting text to the ContentWriter
			writeResults(fullOCRText.toString(), writer.getEncoding(), outStream);
		}
		finally {
			try {
				if(inStream != null) inStream.close();
			}
			catch(IOException ioe) {
				LOG.warn(ioe);
			}
			try {
				if(outStream != null) outStream.close();
			}
			catch(IOException ioe) {
				LOG.warn(ioe);
			}
			try {
				File tempDocFile = new File(tempTIFFFile.getAbsolutePath()+".doc");
				tempDocFile.delete();
				tempTIFFFile.delete();
				tempTxtFile.delete();
			}
			catch(Exception e) {
				// Ignore
			}
		}
	}

	/**
	 * Writes a String of text to an OutputStream using the proper encoding
	 * @param text
	 * @param encoding
	 * @param os
	
	 * @throws IOException */
	private void writeResults(String text, String encoding, OutputStream os)
			throws IOException {
		byte[] textBytes;
		if(encoding != null) {
			textBytes = text.getBytes(encoding);
		}
		else {
			textBytes = text.getBytes();
		}
		os.write(textBytes);
	}
	
	/**
	 * Reads a TIFF image from an InputStream
	 * @param inStream
	
	
	 * @param encoding String
	 * @return RenderedImage[]
	 * @throws IOException */
	private RenderedImage[] readImage(InputStream inStream, String encoding) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(inStream);
		ImageDecoder dec = ImageCodec.createImageDecoder(encoding, bis,
				null);
		int numPages = dec.getNumPages();
		RenderedImage[] pages = new RenderedImage[numPages];
		for(int i = 0; i < numPages; i++) {
			pages[i] = dec.decodeAsRenderedImage(i);
		}
		return pages;
	}

	/**
	 * Save an uncompressed TIFF image to a File
	 * @param image
	 * @param outFile
	
	 * @throws IOException */
	private void saveUncompressedTIFFImageToDisk(RenderedImage image, File outFile)
			throws IOException {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(	new FileOutputStream(outFile));
			TIFFEncodeParam tiffparam = new TIFFEncodeParam();
			ImageEncoder enc = ImageCodec.createImageEncoder(TIFF_ENCODING, bos,
				tiffparam);
			ParameterBlock params = new ParameterBlock();
			params.addSource(image);

			RenderedOp image2 = JAI.create("scale", params);
			enc.encode(image2);
		}
		finally {
			if(bos != null) {
				try {
					bos.close();
				}
				catch(IOException ioe) {
					// Do nothing
				}
			}
		}
	}

	/**
	 * Run the Tesseract OCR executable
	 * @param tiffFile
	 * @param textOutputFile
	 * @param tesseractDirectory
	
	
	
	 * @param tesseractBinaryName String
	 * @return String
	 * @throws IOException * @throws InterruptedException */
	private String runTesseract(File tiffFile, File textOutputFile,
			String tesseractDirectory, String tesseractBinaryName) throws IOException, InterruptedException {
		String tesseractExe = new File(tesseractDirectory, tesseractBinaryName)
				.getAbsolutePath();
		String outputFilename = getMSDOSCompatibleName(textOutputFile);
		String tiffFileMSDOSName = getMSDOSCompatibleName(tiffFile);
		File workingDir = new File(tesseractDirectory);		
		Runtime runtime = Runtime.getRuntime();
		LOG.debug("Executing the command:" + tesseractExe + " " + tiffFileMSDOSName + " " + outputFilename + " " + TESSERACT_CONFIGURATION + " wd: " + workingDir.getAbsolutePath());
		Process p = runtime.exec(new String[] { tesseractExe, tiffFileMSDOSName,
				outputFilename, TESSERACT_CONFIGURATION }, null, workingDir);
		int result = p.waitFor();
		LOG.debug("Tesseract result:" + result);
		BufferedReader reader = new BufferedReader(new FileReader(
				textOutputFile));
		String line = null;
		StringBuilder buf = new StringBuilder();
		while((line = reader.readLine()) != null) {
			buf.append(line).append('\n');
		}
		reader.close();
		return buf.toString();
	}

	/**
	 * Gets the MS-DOS 8.3-style filename for the given file.
	 * @param inputFile
	
	 * @return String
	 */
	private static String getMSDOSCompatibleName(File inputFile) {
		inputFile = inputFile.getAbsoluteFile();
		Comparator<File> alphabeticalSorter = new Comparator<File>() {
			public int compare(File file1, File file2) {
				if(file1.isDirectory() && file2.isFile()) {
					return 1;
				}
				else if(file2.isDirectory() && file1.isFile()) {
					return -1;
				}
				return file1.getName().compareTo(file2.getName());
			}
		};
		List<String> filenameStack = new ArrayList<String>();
		String inputFilePath = inputFile.getAbsolutePath();
		int firstPathSeparator = inputFilePath.indexOf(File.separator);
		String driveLetter = firstPathSeparator > 0 ? inputFilePath.substring(
				0, firstPathSeparator).toUpperCase() : "";
		File parentFile = null;
		File file = inputFile;
		String filename;
		while((parentFile = file.getParentFile()) != null) {
			filename = file.getName().toUpperCase();
			String extension = "";
			int periodIndex = filename.lastIndexOf('.');
			if(periodIndex > 0) {
				extension = filename.substring(periodIndex).toUpperCase();
				extension = extension.replaceAll(" ", "");
				extension = extension.substring(0, Math.min(4, extension
						.length()));
			}
			boolean tooLong = (periodIndex > -1 && filename.substring(0,
					periodIndex).length() > 8);
			boolean hasSpaces = filename.indexOf(' ') != -1;
			boolean longExtension = (filename.lastIndexOf('.') < filename
					.length() - 4 && filename.lastIndexOf('.') != -1);
			boolean extraPeriods = filename.lastIndexOf('.') != filename
					.indexOf('.');
			if(tooLong || hasSpaces || longExtension || extraPeriods) {
				filename = filename.replaceAll(" ", "");
				File[] files = parentFile.listFiles();
				if(files != null) {
					Arrays.sort(files);
					int index = Arrays.binarySearch(files, file,
							alphabeticalSorter);
					if(index < 0) {
						index = -(index + 1);
					}
					index--;
					String first6Letters = filename.substring(0, Math.min(6,
							filename.length()));
					String lowercaseFirst6Letters = first6Letters.toLowerCase();
					int tildeNumber = 1;
					for(int i = index; i >= 0; i--) {
						String fileInDir = files[i].getName().replaceAll(" ",
								"");
						int lastPeriod = fileInDir.lastIndexOf('.');
						if(lastPeriod > 7
								&& fileInDir.toLowerCase().startsWith(
										lowercaseFirst6Letters)) {
							tildeNumber++;
						}
						else {
							break;
						}
					}
					if(tildeNumber < 10) {
						filename = first6Letters + '~' + tildeNumber;
					}
					else {
						int tildeSectionLength = Integer.toString(tildeNumber)
								.length();
						filename = first6Letters.substring(0, Math.max(
								first6Letters.length(), Math.max(1,
										6 - tildeSectionLength)))
								+ '~' + tildeNumber;
					}
				}
				else {
					// throw exception
				}
			}
			if(file != inputFile) {
				filename = filename + extension;
			}
			filenameStack.add(filename);
			file = parentFile;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(driveLetter);
		builder.append(File.separator);
		int stackSize = filenameStack.size();
		for(int i = stackSize - 1; i >= 0; i--) {
			builder.append(filenameStack.get(i));
			if(i != 0) {
				builder.append(File.separator);
			}
		}
		return builder.toString();
	}

  /**
   * Method isTransformable.
   * @param sourceMimetype String
   * @param targetMimetype String
   * @param options TransformationOptions
   * @return boolean
   * @see org.alfresco.repo.content.transform.ContentTransformer#isTransformable(String, String, TransformationOptions)
   */
  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    return TIFF_MIME_TYPE.equals(sourceMimetype) && targetMimetype.equals(MimetypeMap.MIMETYPE_TEXT_PLAIN) ? true : false;
  }
 
}

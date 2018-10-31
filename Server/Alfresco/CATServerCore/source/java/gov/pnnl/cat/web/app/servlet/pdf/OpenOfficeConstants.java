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
/**
 * 
 * OpenOfficeConstants.java
 * Version: $Revision: $
 *
 * Pacific Northwest National Laboratory
 * Battelle Memorial Institute
 * Copyright (c) 2007
 * @version $Revision: 1.0 $
 */
public interface OpenOfficeConstants {
	
	public final static String HIDDEN_PROPERTY = "Hidden";
	public final static String OVERWRITE_PROPERTY = "Overwrite";
	public final static String FILTERNAME_PROPERTY = "FilterName";
	
	// SUCCESSFULLY TESTED ON .doc, .ppt, .xls, .htm, .txt, .bmp, .png, .tif
	public final static String PDF_EXPORT_FILTER = "writer_pdf_Export";

	/* SUCCESSFULLY TESTED ON .ppt, .bmp, .png, .tif
	   FAILED FOR .doc, .xls, .htm, .txt */
	public final static String DRAW_GIF_FILTER = "draw_gif_Export";
	public final static String DRAW_JPG_FILTER = "draw_jpg_Export";
	
	
	// FAILED TO CONVERT .doc, .ppt, .xls, .bmp, .png, .tif, .htm, .txt
	public final static String GIF_EXPORT_FILTER = "gif_Export";
	public final static String BMP = "BMP - MS Windows";
	
	/*
	 Successfully tested on .doc, .htm, .txt
	 Failed on .ppt, .xls, .bmp, .png, .tif
	 */
	public final static String RTF_FILTER = "Rich Text Format";
	
	/*
	  Successfully tested on .doc, .htm, .txt
	  Failed on .ppt, .xls, .bmp, .png, .tif
	 */
	public final static String HTML_StarWriter_FILTER = "HTML (StarWriter)";
	
	// UNTESTED, EXPECTED TO WORK
	public final static String Word_97_FILTER = "MS Word 97";
	
	public final static String DRAW_PCD_PHOTO_CD_BASE_FILTER = "draw_PCD_Photo_CD_Base";
	public final static String DRAW_PCD_PHOTO_CD_BASE16_FILTER = "draw_PCD_Photo_CD_Base16";
	public final static String DRAW_PCD_PHOTO_CD_BASE4_FILTER = "draw_PCD_Photo_CD_Base4";
	public final static String DRAW_STAROFFICE_XML_DRAW_TEMPLATE_FILTER = "draw_StarOffice_XML_Draw_Template";
	public final static String DRAW_BMP_EXPORT_FILTER = "draw_bmp_Export";
	public final static String DRAW_EMF_FILTER = "draw_emf_Export";
	public final static String DRAW_EPS_FILTER = "draw_eps_Export";
	public final static String DRAW_FLASH_FILTER = "draw_flash_Export";
	public final static String DRAW_HTML_FILTER = "draw_html_Export";
	public final static String DRAW_MET_FILTER = "draw_met_Export";
	public final static String DRAW_PBM_FILTER = "draw_pbm_Export";
	public final static String DRAW_PCT_FILTER = "draw_pct_Export";
	public final static String DRAW_PDF_FILTER = "draw_pdf_Export";
	public final static String DRAW_PGM_FILTER = "draw_pgm_Export";
	public final static String DRAW_PNG_FILTER = "draw_png_Export";
	public final static String DRAW_PPM_FILTER = "draw_ppm_Export";
	public final static String DRAW_RAS_FILTER = "draw_ras_Export";
	public final static String DRAW_SVG_FILTER = "draw_svg_Export";
	public final static String DRAW_SVM_FILTER = "draw_svm_Export";
	public final static String DRAW_TIF_FILTER = "draw_tif_Export";
	public final static String DRAW_WMF_FILTER = "draw_wmf_Export";
	public final static String DRAW_XPM_FILTER = "draw_xpm_Export";
	
	public final static String WRITER_STAROFFICE_XML_TEMPLATE_FILTER = "writer_StarOffice_XML_Writer_Template";
	public final static String WRITER_GLOBALDOCUMENT_STAROFFICE_FILTER = "writer_globaldocument_StarOffice_XML_Writer";
	public final static String WRITER_GLOBALDOCUMENT_STAROFFICE_XML_WRITER_GLOBALDOCUMENT_FILTER = "writer_globaldocument_StarOffice_XML_Writer_GlobalDocument";
	public final static String WRITER_GLOBALDOCUMENT_PDF_EXPORT_FILTER = "writer_globaldocument_pdf_Export";
	public final static String WRITER_WEB_HTML_HELP_FILTER = "writer_web_HTML_help";
	public final static String WRITER_WEB_STAROFFICE_XML_WRITER_FILTER = "writer_web_StarOffice_XML_Writer";
	public final static String WRITER_WEB_STAROFFICE_XML_WRITER_WEB_TEMPLATE_FILTER = "writer_web_StarOffice_XML_Writer_Web_Template";
	public final static String WRITER_WEB_PDF_FILTER = "writer_web_pdf_Export";

	// UNTESTED
	public final static String HTML_FILTER = "HTML";
	public final static String GIF_IMPORT_FILTER = "gif_Import";
	public final static String APORTISDOC_PALM_DB = "AportisDoc Palm DB";
	public final static String CGM = "CGM - Computer Graphics Metafile";
	public final static String DIF = "DIF";
	public final static String DXF = "DXF - AutoCAD Interchange";
	public final static String DocBookFile = "DocBook File";
	public final static String EMF_FILTER = "EMF - MS Windows Metafile";
	public final static String EPS_FILTER = "EPS - Encapsulated PostScript";
	public final static String Flat_XML_File_FILTER = "Flat XML File";
	public final static String GIF_FILTER = "GIF - Graphics Interchange";
	public final static String HTML_StarCalc_FILTER = "HTML (StarCalc)";	
	public final static String JPEG_FILTER = "JPG - JPEG";
	public final static String Lotus_FILTER = "Lotus";
	public final static String MET_FILTER = "MET - OS/2 Metafile";
	public final static String Excel_4_FILTER = "MS Excel 4.0";
	public final static String Excel_4_Vorlage_FILTER = "MS Excel 4.0 Vorlage/Template";
	public final static String Excel_5_FILTER = "MS Excel 5.0/95";
	public final static String Excel_5_Vorlage_FILTER = "MS Excel 5.0/95 Vorlage/Template";
	public final static String Excel_95_FILTER = "MS Excel 95";
	public final static String Excel_95_Vorlage_FILTER = "MS Excel 95 Vorlage/Template";
	public final static String Excel_97_FILTER = "MS Excel 97";
	public final static String Excel_97_Vorlage_FILTER = "MS Excel 97 Vorlage/Template";
	public final static String Powerpoint_97_FILTER = "MS PowerPoint 97";
	public final static String Powerpoint_97_Vorlage_FILTER = "MS PowerPoint 97 Vorlage";
	public final static String WinWord_6_FILTER = "MS WinWord 6.0";
	public final static String Word_95_FILTER = "MS Word 95";
	public final static String Word_95_Vorlage_FILTER = "MS Word 95 Vorlage";
	public final static String Word_97_Vorlage_FILTER = "MS Word 97 Vorlage";
	public final static String MathML_FILTER = "MathML XML (Math)";
	public final static String MathType_FILTER = "MathType 3.x";
	public final static String Word_2003_XML_FILTER = "Microsoft Word 2003 XML";
	public final static String PBM_FILTER = "PBM - Portable Bitmap";
	public final static String PCT_FILTER = "PCT - Mac Pict";
	public final static String PCX_FILTER = "PCX - Zsoft Paintbrush";
	public final static String PGM_FILTER = "PGM - Portable Graymap";
	public final static String PNG_FILTER = "PNG - Portable Network Graphic";
	public final static String PPM_FILTER = "PPM - Portable Pixelmap";
	public final static String PSD_FILTER = "PSD - Adobe Photoshop";
	public final static String RAS_FILTER = "RAS - Sun Rasterfile";
	public final static String RTF_StarCalc_FILTER = "Rich Text Format (StarCalc)";
	public final static String SGF_FILTER = "SGF - StarOffice Writer SGF";
	public final static String SGV_FILTER = "SGV - StarDraw 2.0";
	public final static String SVM_FILTER = "SVM - StarView Metafile";
	public final static String SYLK_FILTER = "SYLK";
	public final static String StarCalc_1_FILTER = "StarCalc 1.0";
	public final static String StarCalc_1_Vorlage_FILTER = "StarCalc 3.0";
	public final static String STAR_CALC_3_FILTER = "StarCalc 3.0 Vorlage/Template";
	public final static String STAR_CALC_4_FILTER = "StarCalc 4.0";
	public final static String STAR_CALC_4_VORLAGE_FILTER = "StarCalc 4.0 Vorlage/Template";
	public final static String STAR_CALC_5_FILTER = "StarCalc 5.0";
	public final static String STAR_CALC_5_VORLAGE_FILTER = "StarCalc 5.0 Vorlage/Template";
	public final static String STARCHART_3_FILTER = "StarChart 3.0";
	public final static String STARCHART_4_FILTER = "StarChart 4.0";
	public final static String STARCHART_5_FILTER = "StarChart 5.0";
	public final static String STARDRAW_3_FILTER = "StarDraw 3.0";
	public final static String STARDRAW_3_STARIMPRESS_FILTER = "StarDraw 3.0 (StarImpress)";
	public final static String STARDRAW_3_VORLAGE_FILTER = "StarDraw 3.0 Vorlage";
	public final static String STARDRAW_3_STARIMPRESS_VORLAGE_FILTER = "StarDraw 3.0 Vorlage (StarImpress)";
	public final static String STARDRAW_5_FILTER = "StarDraw 5.0";
	public final static String STARDRAW_5_STARIMPRESS_FILTER = "StarDraw 5.0 (StarImpress)";
	public final static String STARDRAW_5_VORLAGE_FILTER = "StarDraw 5.0 Vorlage";
	public final static String STARDRAW_5_STARIMPRESS_VORLAGE_FILTER = "StarDraw 5.0 Vorlage (StarImpress)";
	public final static String STAR_IMPRESS_4_FILTER = "StarImpress 4.0";
	public final static String STAR_IMPRESS_4_VORLAGE_FILTER = "StarImpress 4.0 Vorlage";
	public final static String STAR_IMPRESS_5_FILTER = "StarImpress 5.0";
	public final static String STAR_IMPRESS_5_PACKED_FILTER = "StarImpress 5.0 (packed)";
	public final static String STAR_IMPRESS_5_VORLAGE_FILTER = "StarImpress 5.0 Vorlage";
	public final static String STARMATH_2_FILTER = "StarMath 2.0";
	public final static String STARMATH_3_FILTER = "StarMath 3.0";
	public final static String STARMATH_4_FILTER = "StarMath 4.0";
	public final static String STARMATH_5_FILTER = "StarMath 5.0";
	public final static String STAROFFICE_XML_CALC_FILTER = "StarOffice XML (Calc)";
	public final static String STAROFFICE_XML_CHART_FILTER = "StarOffice XML (Chart)";
	public final static String STAROFFICE_XML_DRAW_FILTER = "StarOffice XML (Draw)";
	public final static String STAROFFICE_XML_IMPRESS_FILTER = "StarOffice XML (Impress)";
	public final static String STAROFFICE_XML_MATH_FILTER = "StarOffice XML (Math)";
	public final static String STAROFFICE_XML_WRITER_FILTER = "StarOffice XML (Writer)";
	public final static String STARWRITER_1_FILTER = "StarWriter 1.0";
	public final static String STARWRITER_2_FILTER = "StarWriter 2.0";
	public final static String STARWRITER_3_FILTER = "StarWriter 3.0";
	public final static String STARWRITER_3_GLOBALDOCUMENT_FILTER = "StarWriter 3.0 (StarWriter/GlobalDocument)";
	public final static String STARWRITER_3_WEB_FILTER = "StarWriter 3.0 (StarWriter/Web)";
	public final static String STARWRITER_3_VORLAGE_FILTER = "StarWriter 3.0 Vorlage/Template";
	public final static String STARWRITER_4_FILTER = "StarWriter 4.0";
	public final static String STARWRITER_4_GLOBALDOCUMENT_FILTER = "StarWriter 4.0 (StarWriter/GlobalDocument)";
	public final static String STARWRITER_4_WEB_FILTER = "StarWriter 4.0 (StarWriter/Web)";
	public final static String STARWRITER_4_VORLAGE_FILTER = "StarWriter 4.0 Vorlage/Template";
	public final static String STARWRITER_4_GLOBALDOCUMENT_FILTER_2 = "StarWriter 4.0/GlobalDocument";
	public final static String STARWRITER_5_FILTER = "StarWriter 5.0";
	public final static String STARWRITER_5_GLOBALDOCUMENT_FILTER = "StarWriter 5.0 (StarWriter/GlobalDocument)";
	public final static String STARWRITER_5_WEB_FILTER = "StarWriter 5.0 (StarWriter/Web)";
	public final static String STARWRITER_5_VORLAGE_FILTER = "StarWriter 5.0 Vorlage/Template";
	public final static String STARWRITER_5_GLOBALDOCUMENT_FILTER_2 = "StarWriter 5.0/GlobalDocument";
	public final static String STARWRITER_DOS_FILTER = "StarWriter DOS";
	public final static String STARWRITER_WEB_4_VORLAGE_FILTER = "StarWriter/Web 4.0 Vorlage/Template";
	public final static String STARWRITER_WEB_5_VORLAGE_FILTER = "StarWriter/Web 5.0 Vorlage/Template";
	public final static String TGA_FILTER = "TGA - Truevision TARGA";
	public final static String TIF_FILTER = "TIF - Tag Image File";
	public final static String TEXT_FILTER = "Text";
	public final static String STARWRITER_WEB_FILTER = "Text (StarWriter/Web)";
	public final static String TEXT_ENCODED_FILTER = "Text (encoded)";
	public final static String TEXT_ENCODED_STARWRITER_GLOBALDOCUMENT_FILTER = "Text (encoded) (StarWriter/GlobalDocument)";
	public final static String TEXT_ENCODED_STARWRITER_WEB_FILTER = "Text (encoded) (StarWriter/Web)";
	public final static String TEXT_TXT_CSV_FILTER = "Text - txt - csv (StarCalc)";
	public final static String WMF_FILTER = "WMF - MS Windows Metafile";
	public final static String XBM_FILTER = "XBM - X-Consortium";
	public final static String XHTML_FILTER = "XHTML File";
	public final static String XPM_FILTER = "XPM";
	public final static String BMP_EXPORT_FILTER = "bmp_Export";
	public final static String BMP_IMPORT_FILTER = "bmp_Import";
	public final static String CALC_HTML_WEBQUERY_FILTER = "calc_HTML_WebQuery";
	public final static String CALC_STAROFFICE_XML_CALC_TEMPLATE_FILTER = "calc_StarOffice_XML_Calc_Template";
	public final static String CALC_PDF_EXPORT_FILTER = "calc_pdf_Export";
	public final static String DBASE_FILTER = "dBase";
	public final static String DXF_IMPORT_FILTER = "dxf_Import";
	public final static String EMF_EXPORT_FILTER = "emf_Export";
	public final static String EMF_IMPORT_FILTER = "emf_Import";
	public final static String EPS_EXPORT_FILTER = "eps_Export";
	public final static String EPS_IMPORT_FILTER = "eps_Import";
	public final static String IMPRESS_STAROFFICE_XML_DRAW_FILTER = "impress_StarOffice_XML_Draw";
	public final static String IMPRESS_STAROFFICE_XML_IMPRESS_TEMPLATE_FILTER = "impress_StarOffice_XML_Impress_Template";
	public final static String IMPRESS_BMP_EXPORT_FILTER = "impress_bmp_Export";
	public final static String IMPRESS_EMF_EXPORT_FILTER = "impress_emf_Export";
	public final static String IMPRESS_EPS_EXPORT_FILTER = "impress_eps_Export";
	public final static String IMPRESS_FLASH_EXPORT_FILTER = "impress_flash_Export";
	public final static String IMPRESS_GIF_EXPORT_FILTER = "impress_gif_Export";
	public final static String IMPRESS_HTML_EXPORT_FILTER = "impress_html_Export";
	public final static String IMPRESS_JPG_EXPORT_FILTER = "impress_jpg_Export";
	public final static String IMPRESS_MET_EXPORT_FILTER = "impress_met_Export";
	public final static String IMPRESS_PBM_EXPORT_FILTER = "impress_pbm_Export";
	public final static String IMPRESS_PCT_EXPORT_FILTER = "impress_pct_Export";
	public final static String IMPRESS_PDF_EXPORT_FILTER = "impress_pdf_Export";
	public final static String IMPRESS_PGM_EXPORT_FILTER = "impress_pgm_Export";
	public final static String IMPRESS_PNG_EXPORT_FILTER = "impress_png_Export";
	public final static String IMPRESS_PPM_EXPORT_FILTER = "impress_ppm_Export";
	public final static String IMPRESS_RAS_EXPORT_FILTER = "impress_ras_Export";
	public final static String IMPRESS_SVG_EXPORT_FILTER = "impress_svg_Export";
	public final static String IMPRESS_SVM_EXPORT_FILTER = "impress_svm_Export";
	public final static String IMPRESS_TIF_EXPORT_FILTER = "impress_tif_Export";
	public final static String IMPRESS_WMF_EXPORT_FILTER = "impress_wmf_Export";
	public final static String IMPRESS_XPM_EXPORT_FILTER = "impress_xpm_Export";
	public final static String JPG_EXPORT_FILTER = "jpg_Export";
	public final static String JPG_IMPORT_FILTER = "jpg_Import";
	public final static String MATH_PDF_EXPORT_FILTER = "math_pdf_Export";
	public final static String MET_EXPORT_FILTER = "met_Export";
	public final static String MET_IMPORT_FILTER = "met_Import";
	public final static String PBM_EXPORT_FILTER = "pbm_Export";
	public final static String PBM_IMPORT_FILTER = "pbm_Import";
	public final static String PCD_IMPORT_BASE_FILTER = "pcd_Import_Base";
	public final static String PCD_IMPORT_BASE16_FILTER = "pcd_Import_Base16";
	public final static String PCD_IMPORT_BASE4_FILTER = "pcd_Import_Base4";
	public final static String PCT_EXPORT_FILTER = "pct_Export";
	public final static String PCT_IMPORT_FILTER = "pct_Import";
	public final static String PCX_IMPORT_FILTER = "pcx_Import";
	public final static String PGM_EXPORT_FILTER = "pgm_Export";
	public final static String PGM_IMPORT_FILTER = "pgm_Import";
	public final static String PLACEWARE_EXPORT_FILTER = "placeware_Export";
	public final static String PNG_EXPORT_FILTER = "png_Export";
	public final static String PNG_IMPORT_FILTER = "png_Import";
	public final static String PPM_EXPORT_FILTER = "ppm_Export";
	public final static String PPM_IMPORT_FILTER = "ppm_Import";
	public final static String PSD_IMPORT_FILTER = "psd_Import";
	public final static String RAS_EXPORT_FILTER = "ras_Export";
	public final static String RAS_IMPORT_FILTER = "ras_Import";
	public final static String SGF_IMPORT_FILTER = "sgf_Import";
	public final static String SGV_IMPORT_FILTER = "sgv_Import";
	public final static String SVG_EXPORT_FILTER = "svg_Export";
	public final static String SVM_EXPORT_FILTER = "svm_Export";
	public final static String SVM_IMPORT_FILTER = "svm_Import";
	public final static String TGA_IMPORT_FILTER = "tga_Import";
	public final static String TIF_EXPORT_FILTER = "tif_Export";
	public final static String TIF_IMPORT_FILTER = "tif_Import";
	public final static String WMF_EXPORT_FILTER = "wmf_Export";
	public final static String WMF_IMPORT_FILTER = "wmf_Import";
	public final static String XBM_IMPORT_FILTER = "xbm_Import";
	public final static String XPM_EXPORT_FILTER = "xpm_Export";
	public final static String XPM_IMPORT_FILTER = "xpm_Import";
	
}

package edu.jhu.cvrg.waveform.main;
/*
Copyright 2013 Johns Hopkins University Institute for Computational Medicine

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
* @author Chris Jurado, Stephen Granite
* 
* 20130517 - SJG- Made a tool that is able to retrieve specific files from the server, 
* 			 handle and package them and clean the server once the download is dispatched
*/
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.axiom.om.OMElement;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformer;

import com.liferay.faces.portal.context.LiferayFacesContext;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;

import edu.jhu.cvrg.waveform.utility.AnalysisUtility;
import edu.jhu.cvrg.waveform.model.AnalysisResult;
import edu.jhu.cvrg.waveform.model.StudyEntry;
import edu.jhu.cvrg.waveform.utility.FTPUtility;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.utility.WebServiceUtility;

public class DownloadManager implements Serializable{

	private static final long serialVersionUID = 1L;
	private User userModel;
	private File userFolder;
	private String userFolderPath;
	
	/**
	 *   Used to create a default DownloadManager class with
	 *   a download folder specific to the user.  The purpose of these user folders
	 *   is to make sure that there are no collisions occur when multiple users 
	 *   are logged in.
	 */
	public DownloadManager(){
		
		String localDownloadFolder = ResourceUtility.getLocalDownloadFolder();
		userModel = ResourceUtility.getCurrentUser();;
		userFolder = new File(localDownloadFolder + userModel.getScreenName());
		userFolder.mkdir();
		if (localDownloadFolder.endsWith("/")) {
			userFolderPath = userFolder.getAbsolutePath() + "/";
		} else {
			userFolderPath = userFolder.getAbsolutePath() + "\\";			
		}

	}
	

	/**
	 * downloadRawFiles - a function to obtain ECG data files
	 * from the ECG storage server and return all the files selected to the user
	 * in a zip file.
	 * 
	 * @param selectedRawFiles
	 */
	public void downloadRawFiles(StudyEntry[] selectedRawFiles){
		String zipFileName = "";
		
		ArrayList<String> fileNames = new ArrayList<String>();
		
		for(StudyEntry studyEntry : selectedRawFiles){
			for(String fileName : studyEntry.getAllFilenames()){
				FTPUtility.downloadFromRemote(AnalysisUtility.extractPath(fileName), AnalysisUtility.extractName(fileName), userFolderPath);
				fileNames.add(AnalysisUtility.extractName(fileName));
			}

		}
		
		zipFileName = createZipFolder(fileNames, "raw");
		downloadSelectedFile(zipFileName, "zip");
		deleteFile(userFolderPath, AnalysisUtility.extractName(zipFileName));
	}
	
	
	/**
	 * downloadAnalysisResults - a function to obtain analysis results from 
	 * the ECG storage server and return the files in a way that is amenable to the 
	 * files selected
	 * 
	 * @param selectedResultFiles
	 */
	public void downloadAnalysisResults(AnalysisResult[] selectedResultFiles){
		
		
		String fileName = "", tempFileName = "", zipFileName = "";
		String consolidatedFileName = AnalysisUtility.extractName(tempFileName);		
		String chesFileNames ="", chesSubjectIds = "";
		StringBuffer nameBuffer = null, idBuffer = null;
		ArrayList<String> fileNames = new ArrayList<String>();				
		nameBuffer = new StringBuffer();
		idBuffer = new StringBuffer();
		
		for(AnalysisResult analysisResult : selectedResultFiles){

			/**
			 * With XML files, specifically those generated by the QT Screening/Chesnokov
			 * algorithm, there was a function in the past to generate an XLS file for the 
			 * data.  That function has been preserved as a web service, but the conversion
			 * of the files to CSV format was not made into a service.  Therefore this
			 * tool brings the XML files locally, converts them into CSV and then returns 
			 * the CSV files to the server.  Once the CSV files are on the server, the web
			 * service can convert them into an XLS file.
			 */
			if (analysisResult.getFileName().endsWith(".xml")) {

				fileName = analysisResult.getFileName();
				FTPUtility.downloadFromRemote(AnalysisUtility.extractPath(fileName), AnalysisUtility.extractName(fileName), userFolderPath);
				fileName = qtScreeningToCSV(userFolderPath, AnalysisUtility.extractName(fileName));
				FTPUtility.uploadToRemote(ResourceUtility.getFtpRoot() + AnalysisUtility.extractPath(analysisResult.getFileName()), new File(fileName));
				fileName = ResourceUtility.getFtpRoot() + AnalysisUtility.extractPath(analysisResult.getFileName()) + AnalysisUtility.extractName(analysisResult.getFileName());			
				fileName = fileName.replaceAll(".xml", ".csv");
				deleteFile(userFolderPath, AnalysisUtility.extractName(fileName));
				nameBuffer.append(fileName);
				nameBuffer.append("^");	
				idBuffer.append(analysisResult.getSubjectID());
				idBuffer.append("^");

			/**
			 * With non-XML files, we treat them as files that the user wants to download.
			 * We may change this functionality in the future.
			 */
			} else if (analysisResult.getFileName().endsWith("qrs")) {

				FTPUtility.downloadFromRemote(AnalysisUtility.extractPath(analysisResult.getFileName()), AnalysisUtility.extractName(analysisResult.getFileName()), userFolderPath);
				fileNames.add(AnalysisUtility.extractName(analysisResult.getFileName()));	

			}
			
		}
		
		if (idBuffer.length() > 0) {
			idBuffer.deleteCharAt(idBuffer.length() - 1);
			nameBuffer.deleteCharAt(nameBuffer.length() - 1);
			chesFileNames = nameBuffer.toString();
			chesSubjectIds = idBuffer.toString();
			
			consolidatedFileName = consolidateSelections("", chesSubjectIds, "", "", chesFileNames, "", userModel.getScreenName(), true);
			System.out.println("consolidated filename:" + consolidatedFileName);
			System.out.println("consolidated filepath:" + AnalysisUtility.extractPath(consolidatedFileName));
			System.out.println("consolidated name:" + AnalysisUtility.extractName(consolidatedFileName));
			FTPUtility.downloadFromRemote(AnalysisUtility.extractPath(consolidatedFileName), AnalysisUtility.extractName(consolidatedFileName), userFolderPath);
			
			if (fileNames.size() > 0) {
			
				fileNames.add(AnalysisUtility.extractName(consolidatedFileName));
				zipFileName = createZipFolder(fileNames, "results");
				downloadSelectedFile(zipFileName, "zip");
				deleteFile(userFolderPath, AnalysisUtility.extractName(zipFileName));
			
			} else {
			
				downloadSelectedFile(AnalysisUtility.extractName(consolidatedFileName), "xls");
				deleteFile(userFolderPath, AnalysisUtility.extractName(consolidatedFileName));
			
			}

		} else {
		
			if (fileNames.size() > 0) {
			
				zipFileName = createZipFolder(fileNames, "qrs");
				downloadSelectedFile(zipFileName, "zip");
				deleteFile(userFolderPath, AnalysisUtility.extractName(zipFileName));
			
			}
			
		}
		
		
	}
	
	/**
	 * qtScreeningToCSV - a function to convert QT Screening XML files to CSV files, so
	 * that they can be made into a XLS file, to make it easier for the end user to work
	 * with the data.
	 * 
	 * @param folderPath
	 * @param fileName
	 * @return csvOutputFilename
	 */
	private String qtScreeningToCSV(String folderPath, String fileName) {
		Document xmlDoc = null;
	    Document transformed = null;
	    InputStream xsltIS = null;
	    XSLTransformer xslTransformer = null;
		String row = null;
		String xhtml = null;
		BufferedReader in = null;
		StringBuffer sb = new StringBuffer();
	    String chesnokovFilename = folderPath + fileName;
	    String csvOutputFilename = "";
	    System.out.println(" ** converting " + chesnokovFilename);
		try {
	        in = new BufferedReader(new FileReader(chesnokovFilename));
	        while((row = in.readLine()) != null) {
	        	if(row.indexOf("<autoQRSResults") != -1) {
	            	sb.append("<autoQRSResults>");
	        	} else {
	            	sb.append(row);
	        	}
	        }
	        in.close();
	        xmlDoc = build(sb.toString());
	        xsltIS = this.getClass().getResourceAsStream("chesnokov_datatable.xsl");
			xslTransformer = new XSLTransformer(xsltIS);
			transformed = xslTransformer.transform(xmlDoc);
			xhtml = getString(transformed);
			System.out.println(" ** xslTransformation completed using: " + xsltIS.toString());
	
			int truncPosition = xhtml.indexOf("<html>");
			xhtml = xhtml.substring(truncPosition, xhtml.length());
			xhtml = xhtml.replaceAll("<html>", "");
			xhtml = xhtml.replaceAll("</html>", "");
			csvOutputFilename = chesnokovFilename.replaceAll("xml", "csv");
			
			System.out.println(" ** writing " + csvOutputFilename);
			BufferedWriter out = new BufferedWriter(new FileWriter(csvOutputFilename));
			out.write(xhtml);
			out.close();
		   
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    }
		deleteFile(userFolderPath, AnalysisUtility.extractName(fileName));
		return csvOutputFilename;
	
	}


	/**
	 * Helper method to build a <code>jdom.org.Document</code> from an 
	 * XML document represented as a String
	 * @param  xmlDocAsString  <code>String</code> representation of an XML
	 *         document with a document declaration.
	 *         e.g., <?xml version="1.0" encoding="UTF-8"?>
	 *                  <root><stuff>Some stuff</stuff></root>
	 * @return Document from an XML document represented as a String
	 */
	private static Document build(String xmlDocAsString) 
	        throws JDOMException {
		Document doc = null;
	    SAXBuilder builder = new SAXBuilder();
	    Reader stringreader = new StringReader(xmlDocAsString);
	    try {
	    	doc = builder.build(stringreader);
	    } catch(IOException ioex) {
	    	ioex.printStackTrace();
	    }
	    return doc;
	}


	/**
	 * Helper method to generate a String output of a
	 * <code>org.jdom.Document</code>
	 * @param  xmlDoc  Document XML document to be converted to String
	 * @return <code>String</code> representation of an XML
	 *         document with a document declaration.
	 *         e.g., <?xml version="1.0" encoding="UTF-8"?>
	 *                  <root><stuff>Some stuff</stuff></root>
	 */
	private static String getString(Document xmlDoc) 
	        throws JDOMException {
	    try {
	         XMLOutputter xmlOut = new XMLOutputter();
	         StringWriter stringwriter = new StringWriter();
	         xmlOut.output(xmlDoc, stringwriter);
	
	         return stringwriter.toString();
	    } catch (Exception ex) {
	        throw new JDOMException("Error converting Document to String"
	                + ex);
	    }
	}


	/**
	 * consolidateSelections - a function to aggregate all the results from a number of 
	 * analyses and place them all within one XLS file.
	 * 
	 * @param bergerSubjectIds
	 * @param chesnokovSubjectIds
	 * @param qrsScoreIds
	 * @param bergerFiles
	 * @param chesnokovFiles
	 * @param qrsScoreFiles
	 * @param uId
	 * @param isPublic
	 * @return fileNameForXLS
	 */
	private String consolidateSelections(String bergerSubjectIds, String chesnokovSubjectIds, String qrsScoreIds, 
			String bergerFiles, String chesnokovFiles, String qrsScoreFiles, 
			String uId, boolean isPublic) {
	
		OMElement omeResult;
	
		LinkedHashMap<String, String> parameterMap = new LinkedHashMap<String, String>();
	
		parameterMap.put("userid", uId);
		parameterMap.put("chesSubjectids", chesnokovSubjectIds);
		parameterMap.put("bergSubjectids", bergerSubjectIds);
		parameterMap.put("chesFiles", chesnokovFiles);
		parameterMap.put("bergFiles", bergerFiles);
		parameterMap.put("publicprivatefolder", String.valueOf(isPublic));
		parameterMap.put("ftphost", ResourceUtility.getFtpHost());
		parameterMap.put("ftpuser", ResourceUtility.getFtpUser());
		parameterMap.put("ftppassword", ResourceUtility.getFtpPassword());
		parameterMap.put("service", "DataStaging");
		parameterMap.put("logindatetime", new Long(System.currentTimeMillis()).toString());
		parameterMap.put("qrsscoreSubjectids", qrsScoreIds);
		parameterMap.put("qrsscoreFiles", qrsScoreFiles);
		parameterMap.put("verbose", "false");
	
		omeResult = WebServiceUtility.callWebService(parameterMap, isPublic, ResourceUtility.getConsolidateCsvMethod(), ResourceUtility.getNodeDataServiceName(), null);
		String fileNameForXLS = omeResult.getText(); 
		return fileNameForXLS;
	}

	/**
	 * createZipFolder - a function to generate a zip file for a list of files
	 * 
	 * @param fileNames
	 * @param fileType
	 * @return zipFileName
	 */
	private String createZipFolder(ArrayList<String> fileNames, String fileType){
		
		DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmssZ");
		Date date = new Date();
		String zipFileName = fileType + "-" + dateFormat.format(date) + ".zip";
		
		FileOutputStream outStream;
		ZipOutputStream zipOut;

		try {
			outStream = new FileOutputStream(userFolderPath + zipFileName);
			zipOut = new ZipOutputStream(outStream);
			
			for(String fileName : fileNames){
				addFileToZip(userFolderPath + fileName, zipOut);
				deleteFile(userFolderPath, fileName);
			}
			
			if(zipOut != null){
				zipOut.close();
			}

	      
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return zipFileName;
	}
	
	/**
	 * addFileToZip - a function to add a specified file to a zip archive
	 * 
	 * @param fileName
	 * @param zipOut
	 * @throws IOException
	 */
	private void addFileToZip(String fileName, ZipOutputStream zipOut) throws IOException{
		
		File file = new File(fileName);

		File zipDir = new File(userFolderPath);
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(file);
			String relativePath = zipDir.toURI().relativize(file.toURI()).getPath();
			ZipEntry zipEntry = new ZipEntry(relativePath);
			zipOut.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = inStream.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ZipException e){
			e.printStackTrace();
		}

		zipOut.closeEntry();
		inStream.close();
	}

	/**
	 * downloadSelectedFile - a function to return the specific file 
	 * of a specific type to the user's browser for download
	 * 
	 * @param filename
	 * @param filetype
	 */
	private void downloadSelectedFile(String filename, String filetype){
		
		String contentType = "application/zip";
	
		if(filetype.equals("csv")){
			contentType = "text/csv";
		}
		
		if(filetype.equals("xls")){
			contentType = "application/vnd.ms-excel";
		}
		
		FacesContext facesContext = (FacesContext) FacesContext.getCurrentInstance();
	    ExternalContext externalContext = facesContext.getExternalContext();
	    PortletResponse portletResponse = (PortletResponse) externalContext.getResponse();   
	    HttpServletResponse response = PortalUtil.getHttpServletResponse(portletResponse);

	    File file = new File(userFolder, filename);
	    BufferedInputStream input = null;
	    BufferedOutputStream output = null;
	
	    try {
	        input = new BufferedInputStream(new FileInputStream(file), 10240);
	
	        response.reset();
	        response.setHeader("Content-Type", contentType);
	        response.setHeader("Content-Length", String.valueOf(file.length()));
	        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
	        response.flushBuffer();
	        output = new BufferedOutputStream(response.getOutputStream(), 10240);
	
	        byte[] buffer = new byte[10240];
	        int length;
	        while ((length = input.read(buffer)) > 0) {
	            output.write(buffer, 0, length);
	        }
	
	        output.flush();
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	        try {
				output.close();
	            input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	
	    facesContext.responseComplete();
	}

	
	/**
	 * deleteFile - a function to perform clean up on the server, to reduce the amount
	 * of disk space wasted from downloads
	 * 
	 * @param localDownloadFolder
	 * @param fileName
	 */
	private void deleteFile(String localDownloadFolder, String fileName) {
		File file = new File(localDownloadFolder + fileName);
		if (file.delete())
			;//System.out.println("Deleting " + fileName);
	}

}

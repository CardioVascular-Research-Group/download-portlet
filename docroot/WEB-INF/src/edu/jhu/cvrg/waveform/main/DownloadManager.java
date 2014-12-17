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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;

import edu.jhu.cvrg.filestore.model.FSFile;
import edu.jhu.cvrg.waveform.model.AnalysisFileVO;
import edu.jhu.cvrg.waveform.model.DownloadFileVO;
import edu.jhu.cvrg.waveform.model.UploadFileVO;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;

public class DownloadManager implements Serializable{

	private static final long serialVersionUID = 1L;
	private User userModel;
	private File userFolder;
	private String userFolderPath;
	
	private String zipFileName;
	
	/**
	 *   Used to create a default DownloadManager class with
	 *   a download folder specific to the user.  The purpose of these user folders
	 *   is to make sure that there are no collisions occur when multiple users 
	 *   are logged in.
	 */
	public DownloadManager(){
		
		String localDownloadFolder = System.getProperty("java.io.tmpdir") + File.separator + "waveform/d" + File.separator;
		userModel = ResourceUtility.getCurrentUser();
		userFolder = new File(localDownloadFolder + userModel.getScreenName());
		userFolder.mkdirs();
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
	public void downloadRawFiles(UploadFileVO[] selectedRawFiles){
		downloadFiles(selectedRawFiles);
		
	}
	/**
	 * downloadAnalysisResults - a function to obtain analysis results from 
	 * the ECG storage server and return the files in a way that is amenable to the 
	 * files selected
	 * 
	 * @param selectedResultFiles
	 */
	public void downloadAnalysisResults(AnalysisFileVO[] selectedResultFiles){
		downloadFiles(selectedResultFiles);
	}


	private void downloadFiles(DownloadFileVO[] selectedRawFiles) {
		try {
			if(selectedRawFiles.length > 1){
			
				ZipOutputStream zipOut = createZipFolder("raw");
				for(DownloadFileVO file : selectedRawFiles){
					addFileToZip(file, zipOut);
				}
				if(zipOut != null){
					zipOut.close();
				}
			
				downloadSelectedFile(zipFileName, "zip");
				deleteFile(userFolderPath, this.extractName(zipFileName));
			}else{
				String fileName = selectedRawFiles[0].getFile().getName();
				if(selectedRawFiles[0] instanceof AnalysisFileVO){
					AnalysisFileVO aFile = (AnalysisFileVO)selectedRawFiles[0];
					fileName = aFile.getUserRecordName();
				}
				
				FSFile file = selectedRawFiles[0].getFile();
				
				downloadSelectedFile(file.getFileDataAsInputStream(), fileName, file.getFileSize(), file.getExtension());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	private ZipOutputStream createZipFolder(String fileType){
		DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmssZ");
		String zipFileName = fileType + "-" + dateFormat.format(new Date()) + ".zip";
		File zipFile = new File(userFolderPath+zipFileName);
		ZipOutputStream zipOut  = null;
		
		try {
			zipFile.createNewFile();
			FileOutputStream outStream = new FileOutputStream(zipFile);
			zipOut = new ZipOutputStream(outStream);
			this.zipFileName = zipFileName;
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return zipOut;
	}
	
	
	
	
	/**
	 * addFileToZip - a function to add a specified file to a zip archive
	 * 
	 * @param fileName
	 * @param zipOut
	 * @throws IOException
	 */
	private void addFileToZip(DownloadFileVO file, ZipOutputStream zipOut) throws IOException{
		
		InputStream inStream = null;
		try {
			inStream = file.getFile().getFileDataAsInputStream();
			String entryName = null;
			if(file instanceof AnalysisFileVO){
				AnalysisFileVO aFile = (AnalysisFileVO) file;
				entryName = aFile.getAnalysisJobId()+"/"+aFile.getUserRecordName();
			}else{
				entryName = file.getFile().getName();
			}
			
			ZipEntry zipEntry = new ZipEntry(entryName);
			
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
		}finally{
			zipOut.closeEntry();
			inStream.close();
		}
		
	}
	
	
	/**
	 * downloadSelectedFile - a function to return the specific file 
	 * of a specific type to the user's browser for download
	 * 
	 * @param filename
	 * @param filetype
	 */
	private void downloadSelectedFile(String filename, String filetype){
		
		File file = new File(userFolder, filename);
		try {
			downloadSelectedFile(new FileInputStream(file), filename, file.length(), filetype);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * downloadSelectedFile - a function to return the specific file 
	 * of a specific type to the user's browser for download
	 * 
	 * @param filename
	 * @param filetype
	 */
	private void downloadSelectedFile(InputStream inStream, String fileName, long fileLength, String filetype){
		
		String contentType = "application/zip";
	
		if(filetype.equals("csv")){
			contentType = "text/csv";
		}
		
		if(filetype.equals("xls")){
			contentType = "application/vnd.ms-excel";
		}
		
		if(filetype.equals("txt")){
			contentType = "text/plain";
		}
		
		//Indeterminated 9 bytes on the request
		fileLength+=9;
		
		FacesContext facesContext = (FacesContext) FacesContext.getCurrentInstance();
	    ExternalContext externalContext = facesContext.getExternalContext();
	    PortletResponse portletResponse = (PortletResponse) externalContext.getResponse();   
	    HttpServletResponse response = PortalUtil.getHttpServletResponse(portletResponse);

	    BufferedInputStream input = null;
	    BufferedOutputStream output = null;
	
	    try {
	        input = new BufferedInputStream(inStream, 10240);
	
	        response.reset();
	        response.setHeader("Content-Type", contentType);
	        response.setHeader("Content-Length", String.valueOf(fileLength));
	        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
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
	
	private String extractName(String sFilePathName){
		String sFileName="";
		int iIndexLastSlash = sFilePathName.lastIndexOf("/");
		sFileName = sFilePathName.substring(iIndexLastSlash+1);

		return sFileName;
	}

}

package edu.jhu.cvrg.waveform.backing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.model.User;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;

import edu.jhu.cvrg.data.dto.AnalysisJobDTO;
import edu.jhu.cvrg.data.dto.DocumentRecordDTO;
import edu.jhu.cvrg.data.dto.FileInfoDTO;
import edu.jhu.cvrg.data.factory.Connection;
import edu.jhu.cvrg.data.factory.ConnectionFactory;
import edu.jhu.cvrg.data.util.DataStorageException;
import edu.jhu.cvrg.waveform.main.DownloadManager;
import edu.jhu.cvrg.waveform.model.AnalysisFileVO;
import edu.jhu.cvrg.waveform.model.UploadFileVO;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;

@ManagedBean(name = "downloadBacking")
@ViewScoped
public class DownloadBacking extends BackingBean implements Serializable {

	private static final long serialVersionUID = 4778576272893200307L;
	

	private ArrayList<AnalysisFileVO> analysisResultList;
	private AnalysisFileVO[] selectedResultFiles;
	private ArrayList<UploadFileVO> rawFileList;
	private UploadFileVO[] selectedRawFiles;
	private DownloadManager downloadManager;
	private String fileLink;
	private User userModel;
	
	@PostConstruct
	public void init(){
		
		try{
			userModel = ResourceUtility.getCurrentUser();
		
			if(userModel != null){
				Connection theDB = ConnectionFactory.createConnection();
				
				List<FileInfoDTO> dbFileList = theDB.getFileListByUser(userModel.getUserId());
				
				DocumentRecordDTO documentRecord = null;
				analysisResultList = new ArrayList<AnalysisFileVO>();
				rawFileList = new ArrayList<UploadFileVO>();
				
				for (FileInfoDTO fileInfoDTO : dbFileList) {
					
					try {
						
						FileEntry liferayFile = DLAppLocalServiceUtil.getFileEntry(fileInfoDTO.getFileEntryId());
						
						if(documentRecord == null || !documentRecord.getDocumentRecordId().equals(fileInfoDTO.getDocumentRecordId())){
							documentRecord = theDB.getDocumentRecordById(fileInfoDTO.getDocumentRecordId());
						}
						
						if(fileInfoDTO.getAnalysisJobId() != null){
							AnalysisJobDTO analysisJob = theDB.getAnalysisJobById(fileInfoDTO.getAnalysisJobId());
							analysisResultList.add(new AnalysisFileVO(fileInfoDTO.getDocumentRecordId() + " - " +documentRecord.getSubjectId(), 
												   					  analysisJob.getDateOfAnalysis(), liferayFile.getTitle(), analysisJob.getServiceMethod(), 
												   					  liferayFile, analysisJob.getAnalysisJobId()));
						}else{
							rawFileList.add(new UploadFileVO(fileInfoDTO.getDocumentRecordId() + " - " +documentRecord.getSubjectId(), documentRecord.getOriginalFormat(), documentRecord.getDateOfRecording(), liferayFile.getTitle(), liferayFile));
						}
						
					} catch (PortalException e) {
						e.printStackTrace();
					} catch (SystemException e) {
						e.printStackTrace();
					} catch (DataStorageException e) {
						e.printStackTrace();
					}
					
					
				}
				
				downloadManager = new DownloadManager();
			}	
		} catch (NullPointerException e) {
			this.getLog().info("User not logged in.");
		} catch (DataStorageException e1) {
			this.getLog().info("Cannot connect to data the storage system.");
		}
	}
	
	public String downloadRawFiles(){

		if(selectedRawFiles.length != 0){
			downloadManager.downloadRawFiles(selectedRawFiles);
		}
		return "success";
	}
	
	public String downloadAnalysisResultFiles(){
		if(selectedResultFiles.length != 0){
			downloadManager.downloadAnalysisResults(selectedResultFiles);
		}
		return "success";
	}

	public AnalysisFileVO[] getSelectedResultFiles() {
		return selectedResultFiles;
	}

	public void setSelectedResultFiles(AnalysisFileVO[] selectedResultFiles) {
		this.selectedResultFiles = selectedResultFiles;
	}

	public UploadFileVO[] getSelectedRawFiles() {
		return selectedRawFiles;
	}

	public void setSelectedRawFiles(UploadFileVO[] selectedRawFiles) {
		this.selectedRawFiles = selectedRawFiles;
	}

	public String getFileLink() {
		return fileLink;
	}

	public void setFileLink(String fileLink) {
		this.fileLink = fileLink;
	}

	public ArrayList<AnalysisFileVO> getAnalysisResultList() {
		return analysisResultList;
	}

	public void setAnalysisResultList(ArrayList<AnalysisFileVO> analysisResultList) {
		this.analysisResultList = analysisResultList;
	}

	public ArrayList<UploadFileVO> getRawFileList() {
		return rawFileList;
	}

	public void setRawFileList(ArrayList<UploadFileVO> rawFileList) {
		this.rawFileList = rawFileList;
	}
	
	public User getUser(){
		return userModel;
	}

}
package edu.jhu.cvrg.waveform.backing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

import com.liferay.portal.model.User;

import edu.jhu.cvrg.data.dto.AnalysisJobDTO;
import edu.jhu.cvrg.data.dto.DocumentRecordDTO;
import edu.jhu.cvrg.data.dto.FileInfoDTO;
import edu.jhu.cvrg.data.factory.Connection;
import edu.jhu.cvrg.data.factory.ConnectionFactory;
import edu.jhu.cvrg.data.util.DataStorageException;
import edu.jhu.cvrg.filestore.exception.FSException;
import edu.jhu.cvrg.filestore.main.FileStoreFactory;
import edu.jhu.cvrg.filestore.main.FileStorer;
import edu.jhu.cvrg.filestore.model.FSFile;
import edu.jhu.cvrg.waveform.main.DownloadManager;
import edu.jhu.cvrg.waveform.model.AnalysisFileVO;
import edu.jhu.cvrg.waveform.model.FileTreeNode;
import edu.jhu.cvrg.waveform.model.LocalFileTree;
import edu.jhu.cvrg.waveform.model.UploadFileVO;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;

@ManagedBean(name = "downloadBacking")
@ViewScoped
public class DownloadBacking extends BackingBean implements Serializable {

	private static final long serialVersionUID = 4778576272893200307L;
	

	private LocalFileTree fileTree;
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
				if(fileTree == null && userModel != null){
					fileTree = new LocalFileTree(userModel.getUserId());
				}
				
				analysisResultList = new ArrayList<AnalysisFileVO>();
				rawFileList = new ArrayList<UploadFileVO>();
				
				loadAllNodes();
				
				downloadManager = new DownloadManager();
			}	
		} catch (NullPointerException e) {
			this.getLog().info("User not logged in.");
		} catch (DataStorageException e1) {
			this.getLog().info("Cannot connect to data the storage system.");
		}
	}

	private void loadAllNodes() throws DataStorageException {
		Connection theDB = ConnectionFactory.createConnection();
		List<FileInfoDTO> dbFileList = theDB.getAllFilesByUser(userModel.getUserId());
		DocumentRecordDTO documentRecord = null;
		
		for (FileInfoDTO fileInfoDTO : dbFileList) {
			documentRecord = loadFilesByRecord(theDB, documentRecord,fileInfoDTO);
		}
	}

	private DocumentRecordDTO loadFilesByRecord( Connection theDB, DocumentRecordDTO documentRecord, FileInfoDTO fileInfoDTO) {
		try {
			
			String[] args = {String.valueOf(ResourceUtility.getCurrentGroupId()), String.valueOf(ResourceUtility.getCurrentUserId()), String.valueOf(ResourceUtility.getCurrentCompanyId())};
			FileStorer fileStorer = FileStoreFactory.returnFileStore(ResourceUtility.getFileStorageType(), args);	
			
			FSFile file = fileStorer.getFile(fileInfoDTO.getFileEntryId(), false);
			
			if(documentRecord == null || !documentRecord.getDocumentRecordId().equals(fileInfoDTO.getDocumentRecordId())){
				documentRecord = theDB.getDocumentRecordById(fileInfoDTO.getDocumentRecordId());
			}
			
			if(file!=null){
				if(fileInfoDTO.getAnalysisJobId() != null){
					AnalysisJobDTO analysisJob = theDB.getAnalysisJobById(fileInfoDTO.getAnalysisJobId());
					analysisResultList.add(new AnalysisFileVO(fileInfoDTO.getDocumentRecordId() + " - " +documentRecord.getSubjectId(), 
										   					  analysisJob.getDateOfAnalysis(), file.getName(), analysisJob.getServiceMethod(), 
										   					file, analysisJob.getAnalysisJobId()));
				}else{
					rawFileList.add(new UploadFileVO(fileInfoDTO.getDocumentRecordId() + " - " +documentRecord.getSubjectId(), documentRecord.getOriginalFormat(), documentRecord.getDateOfRecording(), file.getName(), file));
				}
			}
			
		} catch (FSException e){
			e.printStackTrace();
		} catch (DataStorageException e) {
			e.printStackTrace();
		}
		return documentRecord;
	}
	
	public String downloadRawFiles(){

		if(selectedRawFiles.length != 0){
			downloadManager.downloadRawFiles(selectedRawFiles);
		}
		return "success";
	}
	
	public String downloadAllRawFiles(){

		if(rawFileList != null && !rawFileList.isEmpty()){
			downloadManager.downloadRawFiles(rawFileList.toArray(new UploadFileVO[rawFileList.size()]));
		}
		return "success";
	}
	
	public String downloadAnalysisResultFiles(){
		if(selectedResultFiles.length != 0){
			downloadManager.downloadAnalysisResults(selectedResultFiles);
		}
		return "success";
	}
	
	public String downloadAllAnalysisResultFiles(){
		if(analysisResultList != null && !analysisResultList.isEmpty()){
			downloadManager.downloadAnalysisResults(analysisResultList.toArray(new AnalysisFileVO[analysisResultList.size()]));
		}
		return "success";
	}
	
	public int getAnalysisFileListCount(){
		if(analysisResultList != null){
			return analysisResultList.size();
		}else{
			return 0;
		}
	}
	
	public int getRawFileListCount(){
		if(rawFileList != null){
			return rawFileList.size();
		}else{
			return 0;
		}
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

	public LocalFileTree getFileTree() {
		return fileTree;
	}

	public void setFileTree(LocalFileTree fileTree) {
		this.fileTree = fileTree;
	}
	
	public void onNodeSelect(NodeSelectEvent event) { 
    	getLog().info("Node selected... ID is " + fileTree.getSelectedNode().getUuid());
    	
    	analysisResultList = new ArrayList<AnalysisFileVO>();
		rawFileList = new ArrayList<UploadFileVO>();
		try {
			Connection theDB = ConnectionFactory.createConnection();
	    	if(fileTree.getSelectedNode() != null ){
    			loadSelection(theDB, (FileTreeNode) fileTree.getSelectedNode());
	    	}else{
		    	loadAllNodes();
		    }
    	} catch (DataStorageException e) {
			e.printStackTrace();
		}
    	
    }

	private void loadSelection(Connection theDB, FileTreeNode selectedNode) throws DataStorageException {
		if(selectedNode.isDocument() && selectedNode.isLeaf()){
			loadNode(theDB, selectedNode);
		}else{
			for (TreeNode node : selectedNode.getChildren()) {
				loadSelection(theDB, (FileTreeNode)node);
			}
		}
	}

	private void loadNode(Connection theDB, TreeNode node) throws DataStorageException {
		
		DocumentRecordDTO documentRecord = theDB.getDocumentRecordById(((FileTreeNode)node).getDocumentRecordId());
		List<FileInfoDTO> dbFileList = theDB.getAllFilesByDocumentRecordId(documentRecord.getDocumentRecordId());
		
		for (FileInfoDTO fileInfoDTO : dbFileList) {
			this.loadFilesByRecord(theDB, documentRecord, fileInfoDTO);
		}
	}
	
	public boolean getShowAnalysisResult(){
		return (this.getAnalysisResultList() != null && this.getAnalysisResultList().size() > 0);
	}
}
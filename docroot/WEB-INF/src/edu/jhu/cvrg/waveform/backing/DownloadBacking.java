package edu.jhu.cvrg.waveform.backing;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import edu.jhu.cvrg.waveform.main.DownloadManager;
import edu.jhu.cvrg.waveform.model.AnalysisResult;
import edu.jhu.cvrg.waveform.model.ResultsDetailList;
import edu.jhu.cvrg.waveform.model.StudyEntry;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.utility.ResultsStorageDBUtility;
import edu.jhu.cvrg.waveform.utility.StudyEntryUtility;

@ManagedBean(name = "downloadBacking")
@ViewScoped
public class DownloadBacking implements Serializable {

	private static final long serialVersionUID = 4778576272893200307L;
	static org.apache.log4j.Logger logger = Logger.getLogger(DownloadBacking.class);

	private ArrayList<AnalysisResult> analysisResultList;
	private AnalysisResult[] selectedResultFiles;
	private ResultsDetailList resultFilteringList;
	private ArrayList<StudyEntry> rawFileList;
	private StudyEntry[] selectedRawFiles;
	private DownloadManager downloadManager;
	private String fileLink;
	private String userID;
	private String MISSING_VALUE = "0";
	private StudyEntryUtility theDB;
	
	@PostConstruct
	public void init(){
		
		try{
			userID = ResourceUtility.getCurrentUser().getScreenName();
		} catch (NullPointerException e) {
			logger.error("User not logged in.");
			return;
		}
			
		String dbUser = ResourceUtility.getDbUser();
		String dbPassword = ResourceUtility.getDbPassword();
		String dbUri = ResourceUtility.getDbURI();
		String dbDriver = ResourceUtility.getDbDriver();
		String dbMainDatabase = ResourceUtility.getDbMainDatabase();
		
		if(dbUser.equals(MISSING_VALUE) || 
				dbPassword.equals(MISSING_VALUE) || 
				dbUri.equals(MISSING_VALUE) || 
				dbDriver.equals(MISSING_VALUE) ||
				dbMainDatabase.equals(MISSING_VALUE)){
			
			logger.error("Missing one or more configuration values for the database.");
			return;	
		}
		theDB = new StudyEntryUtility(dbUser, dbPassword, dbUri, dbDriver, dbMainDatabase);
		
		rawFileList = theDB.getEntries(userID);
		
		ResultsStorageDBUtility resultsDB = new ResultsStorageDBUtility(dbUser, dbPassword, dbUri,	dbDriver, dbMainDatabase);
		
		analysisResultList = resultsDB.getAnalysisResults(userID);
		resultFilteringList = new ResultsDetailList();
		resultFilteringList.setNewFileList(analysisResultList);
		
		downloadManager = new DownloadManager();
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

	public ArrayList<AnalysisResult> getAnalysisResultList() {
		return analysisResultList;
	}

	public void setAnalysisResultList(ArrayList<AnalysisResult> analysisResultList) {
		this.analysisResultList = analysisResultList;
	}

	public AnalysisResult[] getSelectedResultFiles() {
		return selectedResultFiles;
	}

	public void setSelectedResultFiles(AnalysisResult[] selectedResultFiles) {
		this.selectedResultFiles = selectedResultFiles;
	}

	public StudyEntry[] getSelectedRawFiles() {
		return selectedRawFiles;
	}

	public void setSelectedRawFiles(StudyEntry[] selectedRawFiles) {
		this.selectedRawFiles = selectedRawFiles;
	}

	public ArrayList<StudyEntry> getRawFileList() {
		return rawFileList;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getFileLink() {
		return fileLink;
	}

	public void setFileLink(String fileLink) {
		this.fileLink = fileLink;
	}
}
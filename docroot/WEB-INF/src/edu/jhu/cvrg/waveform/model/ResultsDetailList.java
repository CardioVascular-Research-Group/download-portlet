package edu.jhu.cvrg.waveform.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResultsDetailList {
	private List<AnalysisResult> resultsDetailList = new ArrayList<AnalysisResult>();
	
	public ResultsDetailList () {
		
	}
	
	public List<AnalysisResult> getAllFiles() {
		return resultsDetailList;
	}

	public void setNewFileList(ArrayList<AnalysisResult> newList) {
		resultsDetailList.clear();
		
		resultsDetailList = newList;
	}
	
	public AnalysisResult getFileByURI(String fileSearchURI) {
		AnalysisResult returnedFile = null;
		
		for (int i=0;i<resultsDetailList.size();i++) {
			if (fileSearchURI.equals(((AnalysisResult)resultsDetailList.get(i)).getFileName())) {
				returnedFile = (AnalysisResult)resultsDetailList.get(i);
				break;
			}
		}			
		
		return returnedFile;
	}
	
	public AnalysisResult getFileByExtension(String extension) {
		AnalysisResult returnedFile = null;
		
		for (int i=0;i<resultsDetailList.size();i++) {
			if (((AnalysisResult)resultsDetailList.get(i)).getFileName().endsWith(extension)) {
				returnedFile = (AnalysisResult)resultsDetailList.get(i);
				break;
			}
		}			
		
		return returnedFile;
	}
	
	public AnalysisResult getFileByRecordName(String theName) {
		AnalysisResult returnedFile = null;
		
		for (int i=0;i<resultsDetailList.size();i++) {
			if ((((AnalysisResult)resultsDetailList.get(i)).getFileName()).contains(theName)) {
				returnedFile = (AnalysisResult)resultsDetailList.get(i);
				break;
			}
		}			
		
		return returnedFile;
	}
	
	public void addFile(AnalysisResult newFile) {
		resultsDetailList.add(newFile);
	}
	
	public void addFile(String newFileURI) {
		AnalysisResult temp = new AnalysisResult(newFileURI);
		resultsDetailList.add(temp);
		
		AnalysisResult[] tempArray = (AnalysisResult[])resultsDetailList.toArray();
		
		Arrays.sort(tempArray);
		
		temp = null;
	}
}

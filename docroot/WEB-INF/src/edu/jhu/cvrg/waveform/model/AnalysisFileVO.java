package edu.jhu.cvrg.waveform.model;

import java.util.Date;

import com.liferay.portal.kernel.repository.model.FileEntry;

public class AnalysisFileVO extends DownloadFileVO{

	private static final long serialVersionUID = -4606752669553905546L;
	
	private Date dateOfAnalysis;
	private String algorithmUsed;
	
	public AnalysisFileVO(String subjectId, Date dateOfAnalysis,
			String recordName, String algorithmUsed, FileEntry liferayFile) {
		super(subjectId, recordName, liferayFile);
		this.dateOfAnalysis = dateOfAnalysis;
		this.algorithmUsed = algorithmUsed;
	}

	public Date getDateOfAnalysis() {
		return dateOfAnalysis;
	}

	public String getAlgorithmUsed() {
		return algorithmUsed;
	}

}

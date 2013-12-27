package edu.jhu.cvrg.waveform.model;

import java.util.Date;

import com.liferay.portal.kernel.repository.model.FileEntry;

public class AnalysisFileVO extends DownloadFileVO{

	private static final long serialVersionUID = -4606752669553905546L;
	
	private Date dateOfAnalysis;
	private String algorithmUsed;
	private Long analysisJobId;
	
	public AnalysisFileVO(String subjectId, Date dateOfAnalysis,
			String recordName, String algorithmUsed, FileEntry liferayFile, Long analysisJobId) {
		super(subjectId, recordName, liferayFile);
		this.dateOfAnalysis = dateOfAnalysis;
		this.algorithmUsed = algorithmUsed;
		this.analysisJobId = analysisJobId;
	}

	public Date getDateOfAnalysis() {
		return dateOfAnalysis;
	}

	public String getAlgorithmUsed() {
		return algorithmUsed;
	}
	
	public String getUserRecordName(){
		String replacement = '.'+this.getLiferayFile().getExtension();
		String target = ("_"+analysisJobId+replacement);
				
		return this.getRecordName().replace(target, replacement);
	}
	
	public Long getAnalysisJobId() {
		return analysisJobId;
	}
	
}

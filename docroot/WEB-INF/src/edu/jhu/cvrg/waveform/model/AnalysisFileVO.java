package edu.jhu.cvrg.waveform.model;

import java.util.Date;

import edu.jhu.cvrg.filestore.model.FSFile;

public class AnalysisFileVO extends DownloadFileVO{

	private static final long serialVersionUID = -4606752669553905546L;
	
	private Date dateOfAnalysis;
	private String algorithmUsed;
	private Long analysisJobId;
	
	public AnalysisFileVO(String subjectId, Date dateOfAnalysis,
			String recordName, String algorithmUsed, FSFile file, Long analysisJobId) {
		super(subjectId, recordName, file);
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
		String replacement = '.'+this.getFile().getExtension();
		String target = ("_"+analysisJobId+replacement);
				
		return this.getRecordName().replace(target, replacement);
	}
	
	public Long getAnalysisJobId() {
		return analysisJobId;
	}
	
}

package edu.jhu.cvrg.waveform.model;

import java.io.Serializable;

import edu.jhu.cvrg.filestore.model.FSFile;

public class DownloadFileVO implements Serializable{

	private static final long serialVersionUID = 6526332146461926850L;
	
	private String subjectId;
	private String recordName;
	private FSFile file;
	
	public DownloadFileVO(String subjectId, String recordName,
			FSFile file) {
		super();
		this.subjectId = subjectId;
		this.recordName = recordName;
		this.file = file;
	}
	
	public String getSubjectId() {
		return subjectId;
	}
	public String getRecordName() {
		return recordName;
	}
	public FSFile getFile() {
		return file;
	}
	
}

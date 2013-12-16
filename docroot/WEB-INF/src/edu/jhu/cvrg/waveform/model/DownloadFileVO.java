package edu.jhu.cvrg.waveform.model;

import java.io.Serializable;

import com.liferay.portal.kernel.repository.model.FileEntry;

public class DownloadFileVO implements Serializable{

	private static final long serialVersionUID = 6526332146461926850L;
	
	private String subjectId;
	private String recordName;
	private FileEntry liferayFile;
	
	public DownloadFileVO(String subjectId, String recordName,
			FileEntry liferayFile) {
		super();
		this.subjectId = subjectId;
		this.recordName = recordName;
		this.liferayFile = liferayFile;
	}
	
	public String getSubjectId() {
		return subjectId;
	}
	public String getRecordName() {
		return recordName;
	}
	public FileEntry getLiferayFile() {
		return liferayFile;
	}
	
	
}

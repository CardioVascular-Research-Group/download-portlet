package edu.jhu.cvrg.waveform.model;

import java.util.Date;

import com.liferay.portal.kernel.repository.model.FileEntry;

public class UploadFileVO extends DownloadFileVO{

	private static final long serialVersionUID = 1023910725115455855L;
	
	private String datatype;
	private Date dateOfRecording;
	
	public UploadFileVO(String subjectId, String datatype,
			Date dateOfRecording, String recordName, FileEntry liferayFile) {
		super(subjectId, recordName, liferayFile);
		this.datatype = datatype;
		this.dateOfRecording = dateOfRecording;
		
	}

	public String getDatatype() {
		return datatype;
	}

	public Date getDateOfRecording() {
		return dateOfRecording;
	}
	
}

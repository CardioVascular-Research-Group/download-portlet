package edu.jhu.cvrg.waveform.model;

import java.util.Date;

import edu.jhu.cvrg.data.enums.FileType;
import edu.jhu.cvrg.filestore.model.FSFile;

public class UploadFileVO extends DownloadFileVO{

	private static final long serialVersionUID = 1023910725115455855L;
	
	private FileType datatype;
	private Date dateOfRecording;
	
	public UploadFileVO(String subjectId, FileType datatype,
			Date dateOfRecording, String recordName, FSFile file) {
		super(subjectId, recordName, file);
		this.datatype = datatype;
		this.dateOfRecording = dateOfRecording;
		
	}

	public FileType getDatatype() {
		return datatype;
	}

	public Date getDateOfRecording() {
		return dateOfRecording;
	}
	
}

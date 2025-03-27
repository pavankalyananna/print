package com.pdf.printer.dto;


public class FileInfo {
    private String fileName;
    private String url;
    private int pageCount;

    // Constructor, Getters and Setters
    public FileInfo(String fileName, String url, int pageCount) {
        this.fileName = fileName;
        this.url = url;
        this.pageCount = pageCount;
    }

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	@Override
	public String toString() {
		return "FileInfo [fileName=" + fileName + ", url=" + url + ", pageCount=" + pageCount + "]";
	}

    // Add getters and setters
	
    
}
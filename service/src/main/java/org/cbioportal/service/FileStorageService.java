package org.cbioportal.service;

import java.io.FileNotFoundException;
import java.util.List;

import org.springframework.core.io.Resource;

public interface FileStorageService {
	Resource loadFileAsResource(String fileName) throws FileNotFoundException;
	List<String> getDownloadableStudies();

}

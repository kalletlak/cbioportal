package org.cbioportal.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cbioportal.service.FileStorageService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

@InternalApi
@RestController
@Validated
@Api(tags = "Download Study", description = " ")
public class DownloadStudyController {

	@Autowired
	private FileStorageService fileStorageService;

	@RequestMapping(value = "/download/studies_list", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getDownloadableStudies() throws IOException {
		return new ResponseEntity<>(fileStorageService.getDownloadableStudies(), HttpStatus.OK);
	}

	@RequestMapping(value = "/download/{fileName}", method = RequestMethod.GET)
	public void getFile(@PathVariable String fileName, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Resource resource;
		try {
			resource = fileStorageService.loadFileAsResource(fileName);

			InputStream inputStream = resource.getInputStream();

			// get output stream of the response
			OutputStream outStream = response.getOutputStream();

			byte[] buffer = new byte[4096];
			int bytesRead = -1;

			// write bytes read from the input stream into the output stream
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}

			inputStream.close();
			outStream.close();

		} catch (FileNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}

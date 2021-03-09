package tourGuide.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApiService {
	/**
	 * Logger log4j2
	 */
	private static final Logger logger = LogManager.getLogger("ApiService");

	/**
	 * HttpRequestService
	 */
	public HTTPRequestService httpRequestService;

	/**
	 * Configuration File
	 */
	private String configurationFilePath;

	/**
	 * Constructor
	 * 
	 * @param httpRequestService
	 * @param configurationFilePath
	 */
	public ApiService(HTTPRequestService httpRequestService, String configurationFilePath) {
		this.httpRequestService = httpRequestService;
		this.configurationFilePath = configurationFilePath;
	}

	/**
	 * Get host server URL from properties file
	 * 
	 * @param param
	 * @return
	 */
	public String getApiServerUrl(String param) {
		String serverUrl = "";
		try (InputStream inputStream = new FileInputStream(configurationFilePath)) {
			Properties properties = new Properties();
			properties.load(inputStream);
			serverUrl = properties.getProperty(param);
		} catch (FileNotFoundException e) {
			logger.error("File not found", e);
		} catch (IOException e) {
			logger.error("Error while read file", e);
		}
		return serverUrl;
	}

}

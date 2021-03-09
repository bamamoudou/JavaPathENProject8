package tourGuide.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class RewardCentralService extends ApiService {
	/**
	 * Constructor
	 * 
	 * @param httpRequestService
	 * @param configurationFilePath
	 */
	public RewardCentralService(HTTPRequestService httpRequestService, String configurationFilePath) {
		super(httpRequestService, configurationFilePath);
	}

	/**
	 * Get Attraction reward points from external reward central api
	 * 
	 * @param attractionId
	 * @param userId
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public int getAttractionRewardPoints(UUID attractionId, UUID userId) throws IOException, JSONException {
		int result = 0;

		Map<String, String> urlParams = new HashMap<>();
		urlParams.put("userId", userId.toString());
		urlParams.put("attractionId", attractionId.toString());
		JSONObject data = super.httpRequestService
				.getReq(super.getApiServerUrl("rewardcentral.host") + "/getAttractionRewardPoints", urlParams);
		if (data != null) {
			Integer status = data.getInt("status");
			if (status < 299) {
				result = data.getInt("content");
			}
		}
		return result;
	}
}

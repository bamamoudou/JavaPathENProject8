package tourGuide.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tourGuide.model.Provider;

public class TripPricerService extends ApiService {
	/**
	 * Constructor
	 * 
	 * @param httpRequestService
	 * @param configurationFilePath
	 */
	public TripPricerService(HTTPRequestService httpRequestService, String configurationFilePath) {
		super(httpRequestService, configurationFilePath);
	}

	/**
	 * Get user price plan from TripPricer API
	 * 
	 * @param apiKey
	 * @param attractionId
	 * @param adults
	 * @param children
	 * @param nightsStay
	 * @param rewardsPoints
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	public List<Provider> getPrice(String apiKey, UUID attractionId, int adults, int children, int nightsStay,
			int rewardsPoints) throws JSONException, IOException {
		List<Provider> result = null;

		Map<String, String> postParams = new HashMap<>();
		postParams.put("apiKey", apiKey);
		postParams.put("attractionId", attractionId.toString());
		postParams.put("adults", String.valueOf(adults));
		postParams.put("children", String.valueOf(children));
		postParams.put("nightsStay", String.valueOf(nightsStay));
		postParams.put("rewardsPoints", String.valueOf(rewardsPoints));

		JSONObject data = super.httpRequestService.postFormReq(super.getApiServerUrl("trippricer.host") + "/getPrice",
				postParams);
		if (data != null) {
			result = new ArrayList<>();
			Integer status = data.getInt("status");
			if (status < 299) {
				JSONArray content = (JSONArray) data.get("content");
				for (int i = 0; i < content.length(); i++) {
					JSONObject providerJson = content.getJSONObject(i);
					Provider provider = new Provider(UUID.fromString(providerJson.getString("tripId")),
							providerJson.getString("name"), providerJson.getDouble("price"));
					result.add(provider);
				}
			}
		}
		return result;
	}
}
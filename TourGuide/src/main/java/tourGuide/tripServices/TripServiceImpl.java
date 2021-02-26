package tourGuide.tripServices;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.model.AttractionNearby;
import tourGuide.model.ProviderData;
import tourGuide.model.User;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TripServiceImpl implements TripService {
	@Autowired
	private TripPricer tripPricer;

	private Logger logger = LoggerFactory.getLogger(TripServiceImpl.class);

	@Override
	public List<ProviderData> calculateProposals(User user, List<AttractionNearby> attractions,
			int cumulativeRewardPoints) {
		logger.debug("calculateProposals userName = " + user.getUserName() + " and attractionList of size "
				+ attractions.size() + " and rewardPoints = cumulativeRewardPoints");
		List<ProviderData> providers = new ArrayList<ProviderData>();
		for (AttractionNearby a : attractions) {
			List<Provider> proposals = tripPricer.getPrice(TRIP_PRICER_KEY, a.id,
					user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
					user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
			for (Provider provider : proposals) {
				providers.add(newProviderData(provider));
			}
		}
		return providers;
	}

	private ProviderData newProviderData(Provider provider) {
		return new ProviderData(provider.name, provider.price, provider.tripId);
	}
}
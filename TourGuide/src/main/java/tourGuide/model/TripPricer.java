package tourGuide.model;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import tourGuide.service.HTTPRequestService;
import tourGuide.service.TripPricerService;

public class TripPricer implements Callable<List<Provider>> {
	private final UUID attractionId;
	private final String apiKey;
	private final int adults;
	private final int children;
	private final int nightsStay;
	private TripPricerService tripPricerService;

	/**
    * Constructor
    * @param apiKey
    * @param attractionId
    * @param adults
    * @param children
    * @param nightsStay
    */
   public TripPricer(String apiKey, UUID attractionId, int adults, int children, int nightsStay) {
       this.apiKey = apiKey;
       this.attractionId = attractionId;
       this.adults = adults;
       this.children = children;
       this.nightsStay = nightsStay;
       this.tripPricerService = new TripPricerService(new HTTPRequestService(), "src/main/resources/application.properties");
   }

	/**
	 * Get Price for TripPricer Api
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Provider> call() throws Exception {
		return tripPricerService.getPrice(this.apiKey, this.attractionId, this.adults, this.children, this.nightsStay, 5);
	}
}
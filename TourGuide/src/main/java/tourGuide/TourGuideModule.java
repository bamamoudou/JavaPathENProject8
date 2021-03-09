package tourGuide;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tourGuide.service.GpsUtilService;
import tourGuide.service.HTTPRequestService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TripPricerService;

@Configuration
public class TourGuideModule {
	private String configurationFilePath = "src/main/resources/application.properties";

	@Bean
	public ExecutorService getExecutorService() {
		return Executors.newFixedThreadPool(1000);
	}

	@Bean
	public HTTPRequestService getHTTPRequestService() {
		return new HTTPRequestService();
	}

	@Bean
	public GpsUtilService getGpsUtilService() {
		return new GpsUtilService(this.getHTTPRequestService(), configurationFilePath);
	}

	@Bean
	public RewardCentralService getRewardCentralService() {
		return new RewardCentralService(this.getHTTPRequestService(), configurationFilePath);
	}

	@Bean
	public TripPricerService getTripPricerService() {
		return new TripPricerService(this.getHTTPRequestService(), configurationFilePath);
	}

	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(this.getGpsUtilService(), this.getRewardCentralService(), this.getExecutorService());
	}
}
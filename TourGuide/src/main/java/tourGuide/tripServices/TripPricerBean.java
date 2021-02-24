package tourGuide.tripServices;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tripPricer.TripPricer;

@Configuration
public class TripPricerBean {
	@Bean
	public TripPricer tripPricer() {
		return new TripPricer();
	}
}
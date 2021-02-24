package tourGuide.gpsservices;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;

@Configuration
public class GpsUtilBean {
	@Bean
	public GpsUtil gpsUtil() {
		Locale.setDefault(Locale.ENGLISH);
		return new GpsUtil();
	}
}
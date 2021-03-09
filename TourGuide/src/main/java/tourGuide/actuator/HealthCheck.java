package tourGuide.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class HealthCheck implements HealthIndicator {
	/**
	 * Actuactor Health
	 * 
	 * @return
	 */
	@Override
	public Health health() {
		return null;
	}
}
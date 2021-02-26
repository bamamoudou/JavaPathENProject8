package tourGuide.model;

import java.util.UUID;

public class ProviderData {
	public String name;
	public double price;
	public UUID tripId;

	public ProviderData(String attractionName, double attractionPrice, UUID idTrip) {
		name = attractionName;
		price = attractionPrice;
		tripId = idTrip;
	}

	public ProviderData() {
	}
}
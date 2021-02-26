package tourGuide.model;

public class UserPreferences {
	private int attractionProximity = Integer.MAX_VALUE;
	private double lowerPricePoint = 0;
	private double highPricePoint = Double.MAX_VALUE;
	private int tripDuration = 1;
	private int ticketQuantity = 1;
	private int numberOfAdults = 1;
	private int numberOfChildren = 0;

	public UserPreferences() {
	}

	public void setAttractionProximity(int attractionProximity) {
		this.attractionProximity = attractionProximity;
	}

	public int getAttractionProximity() {
		return attractionProximity;
	}

	public double getLowerPricePoint() {
		return lowerPricePoint;
	}

	public void setLowerPricePoint(double lowerPricePoint) {
		this.lowerPricePoint = lowerPricePoint;
	}

	public double getHighPricePoint() {
		return highPricePoint;
	}

	public void setHighPricePoint(double highPricePoint) {
		this.highPricePoint = highPricePoint;
	}

	public int getTripDuration() {
		return tripDuration;
	}

	public void setTripDuration(int tripDuration) {
		this.tripDuration = tripDuration;
	}

	public int getTicketQuantity() {
		return ticketQuantity;
	}

	public void setTicketQuantity(int ticketQuantity) {
		this.ticketQuantity = ticketQuantity;
	}

	public int getNumberOfAdults() {
		return numberOfAdults;
	}

	public void setNumberOfAdults(int numberOfAdults) {
		this.numberOfAdults = numberOfAdults;
	}

	public int getNumberOfChildren() {
		return numberOfChildren;
	}

	public void setNumberOfChildren(int numberOfChildren) {
		this.numberOfChildren = numberOfChildren;
	}
}
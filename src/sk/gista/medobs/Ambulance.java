package sk.gista.medobs;

public class Ambulance {

	private String name;
	private String street;
	
	public Ambulance(String name, String street) {
		this.name = name;
		this.street = street;
	}

	public String getName() {
		return name;
	}

	public String getStreet() {
		return street;
	}
}

package sk.gista.medobs;

public class Place {

	private int id;
	private String name;
	private String street;
	private String city;
	
	public Place(int id, String name, String street, String city) {
		this.id = id;
		this.name = name;
		this.street = street;
		this.city = city;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}
}

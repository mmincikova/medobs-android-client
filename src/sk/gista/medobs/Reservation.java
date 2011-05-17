package sk.gista.medobs;

public class Reservation {

	public enum Status {
		disabled(1),
		enabled(2),
		booked(3),
		in_held(4);
		
		public final int numCode;
		Status(int numCode) {
			this.numCode = numCode;
		}
		static Status valueOf(int num) {
			switch (num) {
			case 1:
				return disabled;
			case 2:
				return enabled;
			case 3:
				return booked;
			case 4:
				return in_held;
			default:
				throw new IndexOutOfBoundsException();
			}
		}
	}
	
	private String time;
	private Status status;
	private String patient;
	
	public Reservation(String time, Status status, String patient) {
		this.time = time;
		this.status = status;
		this.patient = patient;
	}

	public String getTime() {
		return time;
	}

	public Status getStatus() {
		return status;
	}

	public String getpatient() {
		return patient;
	}
}

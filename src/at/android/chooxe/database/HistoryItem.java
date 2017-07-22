package at.android.chooxe.database;

public class HistoryItem {

	private int key;
	private String name;
	private String url;
	private String dateTime;

	public HistoryItem(String name, String url, String dateTime) {
		super();
		this.name = name;
		this.url = url;
		this.dateTime = dateTime;
	}

	// public HistoryItem(String Name, String Url) {
	// super();
	// this.name = Name;
	// this.url = Url;
	// }

	// public HistoryItem(int Key, String Name, String Url) {
	// super();
	// this.key = Key;
	// this.name = Name;
	// this.url = Url;
	// }
	
	@Override
	public String toString() {
		return url;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int Key) {
		this.key = Key;
	}

	public String getName() {
		return name;
	}

	public void setName(String Name) {
		this.name = Name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String Url) {
		this.url = Url;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

}

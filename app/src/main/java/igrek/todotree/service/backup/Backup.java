package igrek.todotree.service.backup;


import java.util.Date;

public class Backup implements Comparable<Backup> {
	
	private String filename;
	private Date date;
	
	public Backup(String filename, Date date) {
		this.filename = filename;
		this.date = date;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public Date getDate() {
		return date;
	}
	
	@Override
	public int compareTo(Backup b) {
		// sort by date descending (from the newest)
		if (this.getDate() == null)
			return +1;
		if (b.getDate() == null)
			return -1;
		return -this.getDate().compareTo(b.getDate());
	}
}

package igrek.todotree.datatree;


public class ContentTrimmer {
	
	public ContentTrimmer() {
		
	}
	
	/**
	 * obcięcie białych znaków na początku i na końcu, usunięcie niedozwolonych znaków
	 * @param content zawartość elementu
	 * @return zawartość z obciętymi znakami
	 */
	public String trimContent(String content) {
		final String WHITE_CHARS = " ";
		final String INVALID_CHARS = "{}[]\n\t";
		//usunięcie niedozwolonych znaków ze środka
		for (int i = 0; i < content.length(); i++) {
			if (isCharInSet(content.charAt(i), INVALID_CHARS)) {
				content = content.substring(0, i) + content.substring(i + 1);
				i--;
			}
		}
		//obcinanie białych znaków na początku
		while (content.length() > 0 && isCharInSet(content.charAt(0), WHITE_CHARS)) {
			content = content.substring(1);
		}
		//obcinanie białych znaków na końcu
		while (content.length() > 0 && isCharInSet(content.charAt(content.length() - 1), WHITE_CHARS)) {
			content = content.substring(0, content.length() - 1);
		}
		return content;
	}
	
	private boolean isCharInSet(char c, String set) {
		for (int i = 0; i < set.length(); i++) {
			if (set.charAt(i) == c)
				return true;
		}
		return false;
	}
}

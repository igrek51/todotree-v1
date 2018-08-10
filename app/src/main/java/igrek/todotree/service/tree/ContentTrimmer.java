package igrek.todotree.service.tree;


public class ContentTrimmer {
	
	public final String WHITE_CHARS = " ";
	public final String INVALID_CHARS = "{}[]\n\t";
	
	public String trimContent(String content) {
		// remove unallowed characteres
		for (int i = 0; i < content.length(); i++) {
			if (isCharInSet(content.charAt(i), INVALID_CHARS)) {
				content = content.substring(0, i) + content.substring(i + 1);
				i--;
			}
		}
		// trim whitespaces from beginning
		while (content.length() > 0 && isCharInSet(content.charAt(0), WHITE_CHARS)) {
			content = content.substring(1);
		}
		// trim whitespaces from the end
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

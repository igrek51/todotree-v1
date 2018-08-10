package igrek.todotree.service.tree.persistence;


class IndentedLine {
	
	private String indentedLine;
	private int indentation;
	
	IndentedLine(String unindentedLine) {
		indentation = countIndentation(unindentedLine);
		indentedLine = unindentedLine.substring(indentation).trim();
	}
	
	public IndentedLine(String indentedLine, int indentation) {
		this.indentedLine = indentedLine;
		this.indentation = indentation;
	}
	
	public String getIndentedLine() {
		return indentedLine;
	}
	
	public int getIndentation() {
		return indentation;
	}
	
	private int countIndentation(String unindentedLine) {
		int indentation = 0;
		while (indentation < unindentedLine.length() && unindentedLine.charAt(indentation) == '\t') {
			indentation++;
		}
		return indentation;
	}
}

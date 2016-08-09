/**
 * 
 */
package ph.ryetech.pt;

/**
 * @author royce
 *
 */
public class ElementNameChecker {

	public boolean check(String elementName, String proposedSymbol) {

		if (elementName == null 
				|| elementName.length() < 2 
				|| proposedSymbol == null 
				|| proposedSymbol.length() != 2) {
			return false;
		}
		
		int counter = 0;
		int matchCount = 0;
		char[] elementNameCarr = elementName.toLowerCase().toCharArray(); 
		for (char symbolChar : proposedSymbol.toLowerCase().toCharArray()) {
			while (counter < elementNameCarr.length && elementNameCarr[counter] != symbolChar) {
				counter++;
			}
			if (counter < elementNameCarr.length && elementNameCarr[counter] == symbolChar) {
				matchCount++;
			}
			counter++;
		}
		return matchCount == 2;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.

				out.println("Start");

	}

}

/**
 * 
 */
package ph.ryetech.pt;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author royce
 *
 */
public class ElementNameCheckerTest {

	/**
	 * Test method for {@link ph.ryetech.pt.ElementNameChecker#check(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testCheck() {
		ElementNameChecker sut = new ElementNameChecker();
		
		assertFalse(sut.check("Boron", "B"));
		
		assertFalse(sut.check("Mercury", "Hg"));
		assertTrue(sut.check("Mercury", "Cy"));

		assertTrue(sut.check("Silver", "Vr"));
		assertFalse(sut.check("Silver", "Rv"));
		
		assertTrue(sut.check("Magnesium", "Ma"));
		assertTrue(sut.check("Magnesium", "Am"));
		
		assertTrue(sut.check("Xenon", "Nn"));
		assertFalse(sut.check("Xenon", "Xx"));
		assertFalse(sut.check("Xenon", "Oo"));
		
		
		assertTrue(sut.check("Spenglerium", "Ee"));
		assertTrue(sut.check("Zeddemorium", "Zr"));
		assertTrue(sut.check("Venkmine", "Kn"));

		assertFalse(sut.check("Stantzon", "Zt"));
		assertFalse(sut.check("Melintzum", "Nn"));
		assertFalse(sut.check("Tullium", "Ty"));		
	}

}

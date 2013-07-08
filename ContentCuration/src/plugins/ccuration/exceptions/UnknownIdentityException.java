/**
 * This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL.
 */
package plugins.ccuration.exceptions;

/**
 * Thrown when querying an Identity that doesn't exist in the database.
 * 
 * @author Julien Cornuwel (batosai@freenetproject.org)
 *
 */
public class UnknownIdentityException extends Exception {
	
	private static final long serialVersionUID = -1;

	public UnknownIdentityException(String message) {
		super(message);
	}

}

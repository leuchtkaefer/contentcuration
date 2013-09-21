/* Curator, Freenet plugin to curate content
 * Copyright (C) 2013 leuchtkaefer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
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

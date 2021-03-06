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
package plugins.ccuration;

/**
 * This works the same way as the node version system.
 * 
 * @author Artefact2
 */
public class Version {
	/**
	 * This will be parsed by ant at compile time.
	 */
	private static final String gitRevision = "@custom@";

	/**
	 * Don't forget to bump this when a new release is up.
	 */
	public static final long REVISION = 10;

	public static final long MAJOR = 0;
	public static final long MINOR = 1;
	public static final long RELEASE = 1;

	public static String getRevision() {
		return gitRevision;
	}

	/**
	 * Get the formatted version of this plugin.
	 * @return Formatted version.
	 */
	public static String getVersion() {
		return Long.toString(MAJOR)+ "." + Long.toString(MINOR) + "." + Long.toString(RELEASE) + "-" + gitRevision;
	}
}

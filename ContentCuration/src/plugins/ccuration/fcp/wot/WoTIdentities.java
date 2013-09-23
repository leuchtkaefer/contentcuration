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
package plugins.ccuration.fcp.wot;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import plugins.ccuration.fcp.ReceptorCore;
import plugins.ccuration.fcp.SyncPluginTalker;
import freenet.node.FSParseException;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.support.Logger;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;


/**
 * Used to manage WoT identities in general, particularly contexts and properties.
 *
 * @author Artefact2, leuchtkaefer
 */
public class WoTIdentities {
	public static final String CCURATION_CONTEXT = "Curator";

	/**
	 * Get a specific field from one specific WoT identity. This function doesn't work for getting Contexts or Properties
	 * @param field Field to get, eg "Nickname" or "InsertURI".
	 * @return Map of the requested data.
	 * @throws PluginNotFoundException
	 */
	public static  Map<String, String> getWoTIdentity(final String field, final String id) throws PluginNotFoundException {
		final HashMap<String, String> identity = new HashMap<String, String>();
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "GetIdentity");

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
				try {
					if (params.getString("Message").equals("Identity")) {
						for (final String s : params.toOrderedString().split("\n")) {
							if (s.startsWith(field)) {
								identity.put(field, s.split("=")[1]);
							}
						}
					} else {
						Logger.error(this, "Unexpected message : " + params.getString("Message"));
					}
				} catch (FSParseException ex) {
					Logger.error(this, "WoTIdentities : Parse error !");
				}
			}
		}, sfs, null);

		spt.run();

		return identity;
	}

	public static void addContext(String authorID) throws PluginNotFoundException {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "AddContext");
		sfs.putOverwrite("Identity", authorID);
		sfs.putOverwrite("Context", WoTIdentities.CCURATION_CONTEXT);

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
				//assert(params.get("Message").equals("ContextAdded"));
				if (params.get("Message").equals("ContextAdded"))
					Logger.debug(params.get("Message"), "Adding context succesfully");
				else
					Logger.debug(params.get("Message"), "No error while adding context");
			}
		}, sfs, null);

		spt.run();
	}

	public static void addProperty(String authorID, String propertyName, String propertyValue) throws PluginNotFoundException {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "SetProperty");   
		sfs.putOverwrite("Identity", authorID);
		sfs.putOverwrite("Property", propertyName);
		sfs.putOverwrite("Value", propertyValue);

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
				assert(params.get("Message").equals("PropertyAdded"));
			}
		}, sfs, null);

		spt.run();
	}

	public static String getProperty(String authorID, String propertyName) throws PluginNotFoundException {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "GetProperty");
		sfs.putOverwrite("Identity", authorID);
		sfs.putOverwrite("Property", propertyName);

		final String[] value = new String[1];

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
				if(params.get("Message").equals("Error")) {
					assert(params.get("Description").startsWith("plugins.WebOfTrust.exceptions.InvalidParameterException"));
					value[0] = null;
				} else {
					assert(params.get("Message").equals("PropertyValue"));
					value[0] = params.get("Property");
				}
			}
		}, sfs, null);

		spt.run();

		freenet.support.Logger.debug(WoTIdentities.class, authorID + " -> " + value[0]);

		return value[0];
	}

	public static void removeProperty(String authorID, String propertyName) throws PluginNotFoundException {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "RemoveProperty");
		sfs.putOverwrite("Identity", authorID);
		sfs.putOverwrite("Property", propertyName);

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
				assert(params.get("Message").equals("PropertyRemoved"));
			}
		}, sfs, null);

		spt.run();
	}
}

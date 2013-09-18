/* FlogHelper, Freenet plugin to create flogs
 * Copyright (C) 2009 Romain "Artefact2" Dalmaso
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.ccuration.fcp.wot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
 * Methods to communicate with WoT
 *
 * @author leuchtkaefer
 */
public class WoTOwnIdentities {

	public static final String CCURATION_CONTEXT = "Curator";
	public static final String IDENTITY_PROPERTY_INDEX_PREFIX = "Index.";
	/**
	 * Based on the format WoT stores identities'properties. Example: Properties0.Property1.Name=Index.culture
	 */
	public static final String IDENTITY_PROPERTY_PREFIX = "Properties";
	public static final String IDENTITY_PROPERTY_PREFIX_INTERNAL = ".Property";
	public static final String EMPTY_PUBLISHED_CATEGORIES_MSG = "This identity doesn't publish any index";
	
	
	/**
	 * Check if the identity has "Curator" in Contexts. 
	 * @param author The public key hash for an identity is referred to in the source code as the identity's ID, yet the field name for it is the identity's "Identity".
	 * @return
	 * @throws PluginNotFoundException 
	 */
	public static Boolean identityIsARegisteredPublisher(String author) throws PluginNotFoundException {
		List<String> contexts = getWoTIdentitiesContext().get(author);
		return contexts.contains(CCURATION_CONTEXT);
	}
	
	/**
	 * Check if the index is registered in WoT identity. 
	 * @param author The public key hash for an identity is referred to in the source code as the identity's ID, yet the field name for it is the identity's "Identity".
	 * @return
	 * @throws PluginNotFoundException 
	 */
	public static Boolean indexIsRegistered(String author, String indexName) throws PluginNotFoundException {
		List<String> contexts = getWoTIdentitiesCuratedCategories().get(author);
		return contexts.contains(indexName);
	}
	
	
	//TODO leuchtkaefer I need to update index with current version. Verify that the propertyValue 
	/**
	 * Sets the identity's property. It is use every time a new index is published by the identity. 
	 * 
	 * @param author The public key hash for an identity is referred to in the source code as the identity's ID, yet the field name for it is the identity's "Identity".
	 * @param indexFileName The index'name identifies the category, which classifies all content stored in the index. 
	 * @param propertyValue The full USK address of the index, including version.
	 * @throws PluginNotFoundException
	 */
	public static void registerIndex(String author, String indexFileName, String propertyValue) throws PluginNotFoundException {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "SetProperty");   
		sfs.putOverwrite("Identity", author);
		sfs.putOverwrite("Property", IDENTITY_PROPERTY_INDEX_PREFIX + indexFileName);
		sfs.putOverwrite("Value", propertyValue);

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
				assert(params.get("Message").equals("PropertyAdded"));
			}
		}, sfs, null);

		spt.run();
	}
	
	
	/**
	 * Sets the identity's context value. Identities that publish indexes need to include "Curator" in the context's values.  
	 * @param author The public key hash for an identity is referred to in the source code as the identity's ID, yet the field name for it is the identity's "Identity".
	 * @throws PluginNotFoundException
	 */
	public static void registerIdentity(String author) throws PluginNotFoundException {
			final SimpleFieldSet sfs = new SimpleFieldSet(true);
			sfs.putOverwrite("Message", "AddContext");
			sfs.putOverwrite("Identity", author);
			sfs.putOverwrite("Context", CCURATION_CONTEXT);

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
	
	/**
	 * Get the nicknames of WoT identities. The map is <"ID", "Nickname (ID)">.
	 * @return Map of WoT identities.
	 * @throws PluginNotFoundException
	 */
	public static Map<String, String> getWoTIdentities() throws PluginNotFoundException {
		return getWoTIdentities("Nickname");
	}

	/**
	 * Get the request URI of a given author ID.
	 * @param author The public key hash for an identity is referred to in the source code as the identity's ID, yet the field name for it is the identity's "Identity".
	 * @return Request URI of this identity.
	 */
	public static String getRequestURI(String author) {
		try {
			return getWoTIdentities("RequestURI").get(author);
		} catch (PluginNotFoundException ex) {
			return "**Error**";
		}
	}
	
	/**
	 * Get the insert URI of a given author ID.
	 * @param author The public key hash for an identity is referred to in the source code as the identity's ID, yet the field name for it is the identity's "Identity".
	 * @return Insert URI of this identity.
	 */
	public static String getInsertURI(String author) {
		try {
			return getWoTIdentities("InsertURI").get(author);
		} catch (PluginNotFoundException ex) {
			return "**Error**";
		}
	}
	
	/**
	 * Get all Contexts from WoTOwnidentities. 
	 * @return Map of the requested data.
	 * @throws PluginNotFoundException
	 */
	public static Map<String, List<String>> getWoTIdentitiesContext() throws PluginNotFoundException {
		final Map<String, List<String>> identities = new HashMap<String, List<String>>();
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "GetOwnIdentities");

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
				try {
					if (params.getString("Message").equals("OwnIdentities")) {
						Vector<String> identifiers = new Vector<String>();
						Vector<Integer> counter = new Vector<Integer>(); 
						Vector<String> contexts = new Vector<String>();				
						for (final String s : params.toOrderedString().split("\n")) {
							if (s.startsWith("Identity")) {
								identifiers.add(s.split("=")[1]);
								counter.add(0);
								System.out.println("ident" + s.split("=")[1]);
							} else if (s.startsWith("Contexts")) {
								String[] v = s.split("=");
								System.out.println("context0 " + v[0]);
								System.out.println("context1 " + v[1]);
								String[] w = v[0].split(".Context");
								System.out.println("contextv0 " + w[0].substring(8));
								System.out.println("contextv1 " + w[1]);		
								counter.set(Integer.parseInt(w[0].substring(8)), Integer.parseInt(w[1])); 
								contexts.add(v[1]);								
							}
						}

						assert (identifiers.size() == counter.size());

						for (int i = 0; i < identifiers.size(); ++i) {
							int qty = counter.elementAt(i)+1;
							int init = qty*i;	
							System.out.println("init " + qty*i);
							System.out.println("qty " + qty);
							identities.put(identifiers.get(i), contexts.subList(init, init + qty)); 
						}
					} else {
						Logger.error(this, "Unexpected message : " + params.getString("Message"));
					}
				} catch (FSParseException ex) {
					Logger.error(this, "WoTOwnIdentities : Parse error !");
				}
			}
		}, sfs, null);

		spt.run();

		return identities;
	}

	/**
	 * Get all categories curated by WoTOwnidentities. 
	 * @return Map of the requested data.
	 * @throws PluginNotFoundException
	 */
	public static Map<String, List<String>> getWoTIdentitiesCuratedCategories() throws PluginNotFoundException {
		final Map<String, List<String>> identities = new HashMap<String, List<String>>();
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "GetOwnIdentities");

		System.out.println("init getWoTIdentitiesCuratedCategories");
		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
				try {
					if (params.getString("Message").equals("OwnIdentities")) {
						Vector<String> identifiers = new Vector<String>();
						Vector<Integer> counter = new Vector<Integer>(); 
						Vector<String> contexts = new Vector<String>();	
						int ix, prev;
						for (final String s : params.toOrderedString().split("\n")) {
							if (s.startsWith("Identity")) {
								identifiers.add(s.split("=")[1]);
								counter.add(0);
							} else if (s.startsWith(IDENTITY_PROPERTY_PREFIX)) { //Properties0.Property1.Name=Index.culture
								String[] v = s.split("\\.Name=Index\\.");
								if (v.length==2) { //We only wants strings that contains names and not values
									String[] w = v[0].split(IDENTITY_PROPERTY_PREFIX_INTERNAL);
									ix = Integer.parseInt(w[0].substring(IDENTITY_PROPERTY_PREFIX.length()));
									prev = counter.get(ix);
									counter.set(ix, ++prev); 
									contexts.add(v[1]);	
								}
							}
						}
						
						assert (identifiers.size() == counter.size());
						int qty, init = 0;
						for (int i = 0; i < identifiers.size(); ++i) {
							qty = counter.elementAt(i);
							if (qty>0) {
								identities.put(identifiers.get(i), contexts.subList(init, init + qty)); 
							} else identities.put(identifiers.get(i), Arrays.asList(EMPTY_PUBLISHED_CATEGORIES_MSG));
							init = init + qty;
						}
					} else {
						Logger.error(this, "Unexpected message : " + params.getString("Message"));
					}
				} catch (FSParseException ex) {
					Logger.error(this, "WoTOwnIdentities : Parse error !");
				}
			}
		}, sfs, null);

		spt.run();
		
		return identities;
	}
	
	/**
	 * Get a specific field from WoT identities. This function doesn't work for getting Contexts or Properties
	 * @param field Field to get, eg "Nickname" or "InsertURI".
	 * @return Map of the requested data.
	 * @throws PluginNotFoundException
	 */
	public static Map<String, String> getWoTIdentities(final String field) throws PluginNotFoundException {
		final HashMap<String, String> identities = new HashMap<String, String>();
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "GetOwnIdentities");

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
				try {
					if (params.getString("Message").equals("OwnIdentities")) {
						Vector<String> identifiers = new Vector<String>();
						Vector<String> nicknames = new Vector<String>();
						for (final String s : params.toOrderedString().split("\n")) {
							if (s.startsWith("Identity")) {
								identifiers.add(s.split("=")[1]);
							} else if (s.startsWith(field)) {
								nicknames.add(s.split("=")[1]);
							}
							if (field == "Contexts") {
								System.out.println("what contexts "+ s);
							}
						}

						
						assert (identifiers.size() == nicknames.size());

						for (int i = 0; i < identifiers.size(); ++i) {
							identities.put(identifiers.get(i), nicknames.get(i));
						}
					} else {
						Logger.error(this, "Unexpected message : " + params.getString("Message"));
					}
				} catch (FSParseException ex) {
					Logger.error(this, "WoTOwnIdentities : Parse error !");
				}
			}
		}, sfs, null);

		spt.run();

		return identities;
	}

	/**
	 * Sends a "Ping" FCP message to the WoT plugin.
	 * @throws PluginNotFoundException If the plugin is not loaded/not
	 * responding/buggy/whatever.
	 */
	public static void sendPing() throws PluginNotFoundException {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "Ping");

		SyncPluginTalker spt = new SyncPluginTalker(new ReceptorCore() {

			public void onReply(String pluginname, String indentifier, SimpleFieldSet params, Bucket data) {
			}
		}, sfs, null);

		spt.run();
	}
}

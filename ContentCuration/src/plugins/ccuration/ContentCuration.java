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

import plugins.ccuration.ui.web.WebInterface;
import freenet.l10n.BaseL10n;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.l10n.PluginL10n;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginRealVersioned;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.Logger;

/**
 * This plugin helps you to organize Freenet data and share your collections (indexes) with others. 
 * Curator uses Web of Trust (WoT) identities and Library to create indexes. 
 * You can create multiple indexes for each anonymous identity that you own. An index is a collection 
 * of entries related to a category. For instance, you can create the categories linux and windows 
 * and maintain a collection of data related to such categories.
 * An entry can refer to a document or a web page in Freenet.
 * 
 * @author leuchtkaefer
 */

public class ContentCuration implements FredPlugin, FredPluginThreadless, FredPluginL10n, FredPluginBaseL10n, FredPluginVersioned, FredPluginRealVersioned {
	private static String PLUGIN_NAME;

	/* References from the node */

	/** The node's interface to connect the plugin with the node, needed for retrieval of all other interfaces */
	private static PluginRespirator pr;
	private static PluginL10n l10n;

	public static final String l10nFilesBasePath = "plugins/ccuration/l10n/";
	public static final String l10nFilesMask = "lang_${lang}.l10n";
	public static final String l10nOverrideFilesMask = "ccuration_lang_${lang}.override.l10n";

	/* User interfaces */

	private WebInterface mWebInterface;

	private LibraryTalker librarytalker;

	/*
	 * Boolean that is used for preventing the construction of log-strings if
	 * logging is disabled (for saving some cpu cycles)
	 */
	private static transient volatile boolean logDEBUG = false;

	/** The relative path of the plugin on Freenet's web interface */
	public static final String SELF_URI = "/ContentCuration";

	static {
		Logger.registerClass(ContentCuration.class);
	}

	/**
	 * Constructor for being used by the node and unit tests. Does not do
	 * anything.
	 */
	public ContentCuration() {
		super();
		if(logDEBUG) Logger.debug(this, "ContentCuration plugin constructed.");
	}

	public void terminate() {
		if (logDEBUG)
			Logger.debug(this, "ContentCuration terminating ...");

		try {
			if (mWebInterface != null)
				this.mWebInterface.unload();
		} catch (Exception e) {
			Logger.error(this, "Error during termination.", e);
		}

		if (logDEBUG)
			Logger.debug(this, "ContentCuration plugin terminated.");
	}

	public void runPlugin(PluginRespirator pr) {
		Logger.normal(this, "ContentCuration starting up...");
		ContentCuration.pr = pr;
		ContentCuration.PLUGIN_NAME = ContentCuration.getBaseL10n().getString("ContentCuration");

		mWebInterface = new WebInterface(this, SELF_URI);

		try {
			//Buffer for indexes
			librarytalker = new LibraryTalker(pr, this); 
			librarytalker.start();
		} catch (PluginNotFoundException e) {
			Logger.error(this, "Couldn't connect to Library. Please check if Library plugin is loaded", e);
			terminate();
		} 

		Logger.normal(this, "ContentCuration starting up completed.");

	}

	public String getString(String key) {
		try {
			return getBaseL10n().getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	public void setLanguage(LANGUAGE newLanguage) {
		l10n = new PluginL10n(this, newLanguage);
		Logger.debug(this, "Set LANGUAGE to: " + newLanguage.isoCode);
	}


	/**
	 * This is where our L10n files are stored.
	 * @return Path of our L10n files.
	 */
	@Override
	public String getL10nFilesBasePath() {
		return ContentCuration.l10nFilesBasePath;
	}


	@Override
	public String getL10nFilesMask() {
		return ContentCuration.l10nFilesMask;
	}

	@Override
	public String getL10nOverrideFilesMask() {
		return ContentCuration.l10nOverrideFilesMask;
	}

	@Override
	public ClassLoader getPluginClassLoader() {
		return ContentCuration.class.getClassLoader();
	}

	/**
	 * Get the (localized) name of the plugin.
	 *
	 * @return Name of the plugin
	 */
	public static String getName() {
		return ContentCuration.PLUGIN_NAME;
	}


	/**
	 * Access to the current L10n data.
	 * 
	 * @return L10n object.
	 */
	public static BaseL10n getBaseL10n() {
		return l10n.getBase();
	}

	public static PluginRespirator getPluginRespirator() {
		return ContentCuration.pr; 
	}

	public LibraryTalker getLibraryTalker() {
		return librarytalker;
	}

	/**
	 * Get the revision of this plugin.
	 * @return Revision
	 */
	@Override
	public long getRealVersion() {
		return Version.REVISION;
	}

	/**
	 * Get the formatted version of this plugin, for example "r0012" if revision 12.
	 * @return Formatted version.
	 */
	@Override
	public String getVersion() {
		return Version.getVersion();
	}

}

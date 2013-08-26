package plugins.ccuration;

import java.util.Date;

import plugins.ccuration.ui.web.WebInterface;
import freenet.l10n.BaseL10n;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.l10n.PluginL10n;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.Logger;

/**
 * A simple plugin based on Freenet.
 * 
 * @author leuchtkaefer
 */
//public class ContentCuration implements FredPlugin, FredPluginFCP, FredPluginThreadless, FredPluginL10n, FredPluginBaseL10n {
public class ContentCuration implements FredPlugin, FredPluginThreadless, FredPluginL10n, FredPluginBaseL10n {
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
	//private FCPInterface mFCPInterface;

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
		// goon = false;
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
	//	try {
			Logger.normal(this, "ContentCuration starting up...");
			ContentCuration.pr = pr;
			System.err.println("Heartbeat from ContentCuration: " + (new Date()));
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
	//	} catch (RuntimeException e) {
	//		Logger.error(this, "ContentCuration, error during startup", e);
			/* We call it so the database is properly closed */
	//		terminate();

	//		throw e;
	//	}

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
		return ContentCuration.pr; //leuchtkaefer see static reference
	}

	public LibraryTalker getLibraryTalker() {
		return librarytalker;
	}


	/**
	 * Event handler from FredPluginFCP, handled in <code>class FCPInterface</code>.
	 */
	/*
	@Override //TODO remove this method. i don't need FredPluginFCP
	public void handle(PluginReplySender replysender, SimpleFieldSet params,
			Bucket data, int accesstype) {	
		//TODO implement 
	//	mFCPInterface.handle(replysender, params, data, accesstype);
	}
*/
}

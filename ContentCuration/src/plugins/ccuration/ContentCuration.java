package plugins.ccuration;

import java.util.Date;

import plugins.ccuration.ui.web.WebInterface;
import freenet.l10n.BaseL10n;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.l10n.PluginL10n;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginFCP;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.PluginReplySender;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.Logger;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * A simple plugin based on Freenet.
 * 
 * @author leuchtkaefer
 */
public class ContentCuration implements FredPlugin, FredPluginFCP, FredPluginThreadless, FredPluginL10n, FredPluginBaseL10n {

	private static String PLUGIN_NAME;
	
	/* References from the node */
	
	/** The node's interface to connect the plugin with the node, needed for retrieval of all other interfaces */
	private static PluginRespirator pr;
	private static PluginL10n l10n;

	/* User interfaces */

	private WebInterface mWebInterface;
	//private FCPInterface mFCPInterface;

	/*
	 * Boolean that is used for preventing the construction of log-strings if
	 * logging is disabled (for saving some cpu cycles)
	 */
	private static transient volatile boolean logDEBUG = false;

	/** The relative path of the plugin on Freenet's web interface */
	public static final String SELF_URI = "/HelloWorld";

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
		try {
			Logger.normal(this, "ContentCuration starting up...");
			ContentCuration.pr = pr;
//			String mURI = "/Hello2";
			System.err.println("Heartbeat from ContentCuration: " + (new Date()));
			ContentCuration.PLUGIN_NAME = ContentCuration.getBaseL10n().getString("ContentCuration");
			
//			ToadletContainer container = pr.getToadletContainer();
//			final PageMaker pageMaker = pr.getPageMaker();

//			pageMaker.addNavigationCategory(mURI + "/",
	//				"WebInterface.HelloPluginMenuName",
		//			"WebInterface.HelloPluginMenuName.Tooltip", this, 1);

			mWebInterface = new WebInterface(this, SELF_URI);
			//mFCPInterface = new FCPInterface(this);

			
			Logger.normal(this, "ContentCuration starting up completed.");
		} catch (RuntimeException e) {
			Logger.error(this, "ContentCuration, error during startup", e);
			/* We call it so the database is properly closed */
			terminate();

			throw e;
		}

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

	@Override
	public String getL10nFilesBasePath() {
		return "plugins/ccuration/l10n/";
	}

	@Override
	public String getL10nFilesMask() {
		return "lang_${lang}.l10n";
	}

	@Override
	public String getL10nOverrideFilesMask() {
		return "ccuration_lang_${lang}.override.l10n";
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

	/**
	 * Event handler from FredPluginFCP, handled in <code>class FCPInterface</code>.
	 */
	@Override
	public void handle(PluginReplySender replysender, SimpleFieldSet params,
			Bucket data, int accesstype) {	
		//TODO implement 
	//	mFCPInterface.handle(replysender, params, data, accesstype);
	}

}

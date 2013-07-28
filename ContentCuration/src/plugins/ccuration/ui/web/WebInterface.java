/* This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.ccuration.ui.web;

import java.io.IOException;
import java.net.URI;

import plugins.ccuration.ContentCuration;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.RedirectException;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContainer;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.l10n.BaseL10n;
import freenet.node.NodeClientCore;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.api.HTTPRequest;

/**
 * The web interface of the Curator plugin.
 * 
 * @author xor (xor@freenetproject.org)
 * @author Bombe
 * @author Julien Cornuwel (batosai@freenetproject.org)
 * @author bback
 */
public class WebInterface {
	
	private final ContentCuration myCCur;
	private static PluginRespirator mPluginRespirator;
	private final PageMaker mPageMaker;
	
	// Visible
	private final WebInterfaceToadlet homeToadlet;
	private final WebInterfaceToadlet uploadToadlet;
	

	private final String mURI;
	
	/**
	 * Forward access to current l10n data.
	 * 
	 * @return current BaseL10n data
	 */
	public BaseL10n l10n() {
	    return myCCur.getBaseL10n();
	}

	public class HomeWebInterfaceToadlet extends WebInterfaceToadlet {

		protected HomeWebInterfaceToadlet(HighLevelSimpleClient client, WebInterface wi, NodeClientCore core, String pageTitle) {
			super(client, wi, core, pageTitle);
		}

		@Override
		WebPage makeWebPage(HTTPRequest req, ToadletContext context) {
			return new HomePage(this, req, context, l10n());
		}

		@Override
		public void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx)
				throws ToadletContextClosedException, IOException,
				RedirectException {
			super.handleMethodGET(uri, req, ctx);
		}

		@Override
		public void handleMethodPOST(URI uri, HTTPRequest req,
				ToadletContext ctx) throws ToadletContextClosedException,
				IOException, RedirectException {
			super.handleMethodPOST(uri, req, ctx);
		}
	}
	
	public class UploadSomethingWebInterfaceToadlet extends WebInterfaceToadlet {
	
		protected UploadSomethingWebInterfaceToadlet(HighLevelSimpleClient client, WebInterface wi, NodeClientCore core, String pageTitle) {
			super(client, wi, core, pageTitle);
		}
		
		@Override
		WebPage makeWebPage(HTTPRequest req, ToadletContext context) {
			return new UploadSomethingPage(this, req, context, l10n());
		}
		
		@Override
		public void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx)
				throws ToadletContextClosedException, IOException,
				RedirectException {
			final PageNode pageNode = ContentCuration.getPluginRespirator().getPageMaker().getPageNode(ContentCuration.getName(), ctx);
			if (!this.makeGlobalChecks(pageNode, uri, req, ctx)) {
				return;
			}
			super.handleMethodGET(uri, req, ctx);
			

		}

		@Override
		public void handleMethodPOST(URI uri, HTTPRequest req,
				ToadletContext ctx) throws ToadletContextClosedException,
				IOException, RedirectException {
			final PageNode pageNode = ContentCuration.getPluginRespirator().getPageMaker().getPageNode(ContentCuration.getName(), ctx);
			if (!this.makeGlobalChecks(pageNode, uri, req, ctx)) {
				return;
			}
			super.handleMethodPOST(uri, req, ctx);
			

		}

	}	
	
	
	public WebInterface(ContentCuration myWoT, String uri) {
		myCCur = myWoT;
		mURI = uri;
		
		mPluginRespirator = ContentCuration.getPluginRespirator();
		ToadletContainer container = mPluginRespirator.getToadletContainer();
		mPageMaker = mPluginRespirator.getPageMaker();
		
		mPageMaker.addNavigationCategory(mURI+"/", "WebInterface.HelloPlugin", "WebInterface.HelloPlugin.Tooltip", myCCur, mPluginRespirator.getNode().pluginManager.isPluginLoaded("plugins.Freetalk.Freetalk") ? 2 : 1);
		
		// Visible pages
		
		homeToadlet = new HomeWebInterfaceToadlet(null, this, ContentCuration.getPluginRespirator().getNode().clientCore, "");
		uploadToadlet = new UploadSomethingWebInterfaceToadlet(null, this, ContentCuration.getPluginRespirator().getNode().clientCore, "UploadSomething");
		
		container.register(homeToadlet, "WebInterface.HelloPlugin", mURI+"/", true, "WebInterface.HelloPluginMenuItem.Home", "WebInterface.HelloPluginMenuItem.Home.Tooltip", false, null);
		container.register(uploadToadlet, "WebInterface.HelloPlugin", mURI+"/UploadSomething", true, "WebInterface.HelloPluginMenuItem.UploadSomething", "WebInterface.HelloPluginMenuItem.UploadSomething.Tooltip", false, null);
	}
	
	public String getURI() {
		return mURI;
	}

	public PageMaker getPageMaker() {
		return mPageMaker;
	}
	
	public ContentCuration getCCur() {
		return myCCur;
	}
	
	public void unload() {
		ToadletContainer container = mPluginRespirator.getToadletContainer();
		for(Toadlet t : new Toadlet[] { 
				homeToadlet,
				uploadToadlet
		}) container.unregister(t);
		mPageMaker.removeNavigationCategory("WebInterface.HelloPlugin");
	}
}

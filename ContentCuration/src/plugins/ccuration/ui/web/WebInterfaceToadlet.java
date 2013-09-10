package plugins.ccuration.ui.web;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import plugins.ccuration.ContentCuration;
import plugins.ccuration.exceptions.UnknownIdentityException;
import plugins.ccuration.fcp.wot.WoTOwnIdentities;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.RedirectException;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.node.NodeClientCore;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.support.api.HTTPRequest;


public abstract class WebInterfaceToadlet extends Toadlet {
	
	final String pageTitle;
	final WebInterface webInterface;
	final NodeClientCore core;
	private Map<String, String> wotIdentities;
	
	protected WebInterfaceToadlet(HighLevelSimpleClient client, WebInterface wi, NodeClientCore core, String pageTitle) {
		super(client);
		this.pageTitle = pageTitle;
		this.webInterface = wi;
		this.core = core;
	}

	abstract WebPage makeWebPage(HTTPRequest req, ToadletContext context) throws UnknownIdentityException;
	
	@Override
	public String path() {
		return webInterface.getURI() + "/" + pageTitle;
	}

	public void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx) 
			throws ToadletContextClosedException, IOException, RedirectException {
		String ret = "OK";
		try {
			WebPage page = makeWebPage(req, ctx);
			page.make();
			ret = page.toHTML();
		} catch (UnknownIdentityException e) {
			ret = "ERROR";
			try {
				WebPage page = new ErrorPage(this, req, ctx, e, webInterface.l10n());
				page.make();
				ret = page.toHTML();
			}
			catch(Exception doubleFault) {
				ret = doubleFault.toString();
			}

		}
		writeHTMLReply(ctx, 200, "OK", ret);
	}

	public void handleMethodPOST(URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
		
		String pass = request.getPartAsString("formPassword", 32);
		if ((pass.length() == 0) || !pass.equals(core.formPassword)) {
			writeHTMLReply(ctx, 403, "Forbidden", "Invalid form password.");
			return;
		}

		String ret = "OK";
		try {
			
			WebPage page = makeWebPage(request, ctx);
			page.make();
			ret = page.toHTML();
		} catch (Exception e) {
			ret = "ERROR";
			try {
				WebPage page = new ErrorPage(this, request, ctx, e, webInterface.l10n());
				page.make();
				ret = page.toHTML();
			}
			catch(Exception doubleFault) {
				ret = doubleFault.toString();
			}

		}
		writeHTMLReply(ctx, 200, "OK", ret);
	}

	public String getURI() {
		return webInterface.getURI() + "/" + pageTitle;
	}
	
	public Map<String, String> getWoTIdentities() {
		return this.wotIdentities;
	}
	
	public boolean makeGlobalChecks(final PageNode pageNode, final URI uri, final HTTPRequest request, final ToadletContext ctx) throws ToadletContextClosedException, IOException {

		// Make sure WoT is there
		try {
			WoTOwnIdentities.sendPing();
		} catch (PluginNotFoundException ex) {
			this.getPM().getInfobox("infobox-error", ContentCuration.getBaseL10n().getString("MissingWoT"),
					pageNode.content).addChild("p", ContentCuration.getBaseL10n().getString("MissingWoTLong"));
			writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
			return false;
		}

		try {
			this.wotIdentities = WoTOwnIdentities.getWoTIdentities();
		} catch (PluginNotFoundException ex) {
			// Safe to ignore
		}

		// Make sure we have at least one identity
		if (this.wotIdentities.isEmpty()) {
			this.getPM().getInfobox("infobox-error", ContentCuration.getBaseL10n().getString("MissingWoTIdentity"),
					pageNode.content).addChild("p", ContentCuration.getBaseL10n().getString("MissingWoTIdentityLong"));
			writeHTMLReply(ctx, 200, "OK", null, pageNode.outer.generate());
			return false;
		}

		return true;
	}
	
	public PageMaker getPM() {
		return ContentCuration.getPluginRespirator().getPageMaker();
	}
	
}

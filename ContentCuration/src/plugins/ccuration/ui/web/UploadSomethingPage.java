/* This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.ccuration.ui.web;

import plugins.ccuration.ContentCuration;
import freenet.clients.http.ToadletContext;
import freenet.l10n.BaseL10n;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

/**
 * The HomePage of the plugin.
 * 
 * @author xor (xor@freenetproject.org)
 * @author Julien Cornuwel (batosai@freenetproject.org)
 */
public class UploadSomethingPage extends WebPageImpl {

	/**
	 * Creates a webpage.
	 * 
	 * @param toadlet A reference to the {@link WebInterfaceToadlet} which created the page, used to get resources the page needs.
	 * @param myRequest The request sent by the user.
	 */
	public UploadSomethingPage(WebInterfaceToadlet toadlet, HTTPRequest myRequest, ToadletContext context, BaseL10n _baseL10n) {
		super(toadlet, myRequest, context, _baseL10n);
	}

	public void make() {
		makeSummary();
	}

	/**
	 * Creates a short summary of what the plugin does.
	 */
	private void makeSummary() {
		HTMLNode box = addContentBox(l10n().getString("UploadSomething.SummaryBox.Header")); //Upload Content
		
		synchronized(cCur) {
		HTMLNode list = new HTMLNode("ul");
	//	list.addChild(new HTMLNode("li", l10n().getString("UploadSomething.SummaryBox.FirstRecord"))); //test
		for (final String identityID : this.getWotIdentities().keySet()) {
			
		//list.addChild(new HTMLNode("li", identityID == null? "Xnull":identityID)); //test
			list.addChild(new HTMLNode("li", this.getWotIdentities().get(identityID)));
		//list.addChild(new HTMLNode("li", String.valueOf(this.getWotIdentities().size()))); //test
		}
		box.addChild(list);
		
		//testing WoT connection
		//final HTMLNode form = ContentCuration.getPluginRespirator().addFormChild(contentNode, this.uri, "CreateOrEdit-" + "flog.getID");
		
		//final HTMLNode generalBox = this.pm.getInfobox(null, ContentCuration.getBaseL10n().getString("GeneralCCurData"), form, "GeneralFlogData", true);

		//generalBox.addChild("p").addChild("label", "for", "Title", ContentCuration.getBaseL10n().getString("TitleFieldDesc")).addChild("br").addChild("input", new String[]{"type", "size", "name", "value", "maxlength"},
			//	new String[]{"text", "50", "Title", DataFormatter.toString(flog.getTitle()), Integer.toString(TITLE_MAXLENGTH)});
/*
		final HTMLNode authorsBox = new HTMLNode("select", new String[]{"id", "name"}, new String[]{"Author", "Author"});
		for (final String identityID : this.getWotIdentities().keySet()) {
			final HTMLNode option = authorsBox.addChild("option", "value", identityID, this.getWotIdentities().get(identityID));
			if (true) {
				option.addAttribute("selected", "selected");
			}
			
		}*/
		
		
		}
		
		
	}
}

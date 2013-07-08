/* This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.ccuration.ui.web;

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
public class HomePage extends WebPageImpl {

	/**
	 * Creates a new HomePage.
	 * 
	 * @param toadlet A reference to the {@link WebInterfaceToadlet} which created the page, used to get resources the page needs.
	 * @param myRequest The request sent by the user.
	 */
	public HomePage(WebInterfaceToadlet toadlet, HTTPRequest myRequest, ToadletContext context, BaseL10n _baseL10n) {
		super(toadlet, myRequest, context, _baseL10n);
	}

	public void make() {
		makeSummary();
	}

	/**
	 * Creates a short summary of what the plugin knows of the HelloWorld.
	 */
	private void makeSummary() {
		HTMLNode box = addContentBox(l10n().getString("WebInterface.HomePage.SummaryBox.Header"));
		
		synchronized(cCur) {
		HTMLNode list = new HTMLNode("ul");
		list.addChild(new HTMLNode("li", l10n().getString("WebInterface.HomePage.SummaryBox.FirstRecord")));
		box.addChild(list);
		}
		
		
	}
}

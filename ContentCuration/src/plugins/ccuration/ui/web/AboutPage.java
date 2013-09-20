/* This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.ccuration.ui.web;

import freenet.clients.http.ToadletContext;
import freenet.l10n.BaseL10n;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

/**
 * The HomePage of the curator plugin.
 * 
 * @author leuchtkaefer 
 */
public class AboutPage extends WebPageImpl {

	/**
	 * Creates a new About Page for curator plugin.
	 * 
	 * @param toadlet A reference to the {@link WebInterfaceToadlet} which created the page, used to get resources the page needs.
	 * @param myRequest The request sent by the user.
	 */
	public AboutPage(WebInterfaceToadlet toadlet, HTTPRequest myRequest, ToadletContext context, BaseL10n _baseL10n) {
		super(toadlet, myRequest, context, _baseL10n);
	}

	public void make() {
		makeSummary();
	}

	/**
	 * Creates a short summary of the plugin functionality.
	 * Explain the user the steps to create a bookmarklet button to start curating items.
	 */
	private void makeSummary() {
		HTMLNode section1 = addContentBox(l10n().getString("AboutPage.SummaryBox.HeaderSection1"));
		HTMLNode list1 = new HTMLNode("ul");
		list1.addChild(new HTMLNode("li", l10n().getString("AboutPage.SummaryBox.Section1FirstRecord")));
		list1.addChild(new HTMLNode("li", l10n().getString("AboutPage.SummaryBox.Section1SecondRecord")));
		section1.addChild(list1);
		
		HTMLNode section2 = addContentBox(l10n().getString("AboutPage.SummaryBox.HeaderSection2"));
		HTMLNode list2 = new HTMLNode("ul");
		list2.addChild(new HTMLNode("li", l10n().getString("AboutPage.SummaryBox.Section2FirstRecord")));
		list2.addChild(new HTMLNode("li", l10n().getString("AboutPage.SummaryBox.Section2SecondRecord")));
		section2.addChild(list2);
		
		HTMLNode section3 = addContentBox(l10n().getString("AboutPage.SummaryBox.HeaderSection3"));
		HTMLNode list3 = new HTMLNode("ul");
		list3.addChild(new HTMLNode("li", l10n().getString("AboutPage.SummaryBox.Section3FirstRecord")));
		list3.addChild(new HTMLNode("li", l10n().getString("AboutPage.SummaryBox.Section3SecondRecord")));
		list3.addChild(new HTMLNode("li", l10n().getString("AboutPage.SummaryBox.Section3ThirdRecord")));
		section3.addChild(list3);
		
		HTMLNode section4 = addContentBox(l10n().getString("AboutPage.SummaryBox.HeaderSection4"));
		HTMLNode list4 = new HTMLNode("ul");
		list4.addChild(new HTMLNode("li", l10n().getString("AboutPage.SummaryBox.Section4FirstRecord")));
		list4.addChild(new HTMLNode("li", l10n().getString("AboutPage.SummaryBox.Section4SecondRecord")));
		section4.addChild(list4);
		
		HTMLNode section5 = addContentBox(l10n().getString("AboutPage.SummaryBox.HeaderSection5"));
		HTMLNode list5 = new HTMLNode("ul");
		list5.addChild(new HTMLNode("li", l10n().getString("AboutPage.SummaryBox.Section5FirstRecord")));
		list5.addChild(new HTMLNode("li", l10n().getString("AboutPage.SummaryBox.Section5SecondRecord")));
		section5.addChild(list5);
	}
	
}

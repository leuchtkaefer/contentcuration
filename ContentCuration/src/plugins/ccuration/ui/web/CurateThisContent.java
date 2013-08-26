/* This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.ccuration.ui.web;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import plugins.ccuration.ContentCuration;
import plugins.ccuration.LibraryTalker;
import plugins.ccuration.fcp.wot.WoTContexts;
import plugins.ccuration.fcp.wot.WoTOwnIdentities;
import plugins.ccuration.index.InputEntry;
import plugins.ccuration.utils.Utils;
import freenet.clients.http.ToadletContext;
import freenet.keys.FreenetURI;
import freenet.l10n.BaseL10n;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.support.HTMLNode;
import freenet.support.Logger;
import freenet.support.api.HTTPRequest;

/**
 * The Input Form Page of the curator plugin.
 * 
 * @author leuchtkaefer
 */
public class CurateThisContent extends WebPageImpl {

	/**
	 * Creates a webpage.
	 * 
	 * @param toadlet
	 *            A reference to the {@link WebInterfaceToadlet} which created
	 *            the page, used to get resources the page needs.
	 * @param myRequest
	 *            The request sent by the user.
	 */
	public CurateThisContent(WebInterfaceToadlet toadlet,
			HTTPRequest myRequest, ToadletContext context, BaseL10n _baseL10n) {
		super(toadlet, myRequest, context, _baseL10n);

	}

	public void make() {
//		String indexOwner = null;
		String content = null;
		int nbOwnIdentities = 0;
//		String ownerID = request.getPartAsStringFailsafe("OwnerID", 128);
		String buttonNewContentValue = request.getPartAsStringFailsafe(
				"buttonNewContent", 128);
		//String identity = request
		//		.getPartAsStringFailsafe("chosenIdentity", 128);
		InputEntry entry; //TODO leuchtkaefer
		
		
		//Inputs coming from bookmarklet button
		String bookmarkletURI = request.getParam("addNewURI");
		String docTitle = request.getParam("addDocTitle");
		
		synchronized (cCur) {
			Set<String> allOwnIdentities;
			try {
				allOwnIdentities = WoTOwnIdentities.getWoTIdentities()
						.keySet();
				nbOwnIdentities = allOwnIdentities.size();
/*				if (nbOwnIdentities == 1) {
					Iterator<String> iterator = allOwnIdentities.iterator();
					indexOwner = iterator.next();
				}*/
			} catch (PluginNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (nbOwnIdentities > 0) {
		//if (this.wotIdentities.size() > 0) {
			try {
			curateIt(bookmarkletURI,docTitle);
			} catch (MalformedURLException e) {
				Logger.error(this, "Error", e);
				addErrorBox("Error", e);
			}
		}	

		
		if (!buttonNewContentValue.equals("")) { // TODO leuchtkaefer check
			content = buttonNewContentValue;
		}

		// Actions done when addingNewContent button is pressed
		if (content != null) {
			String category = request.getPartAsStringFailsafe("term", 128);
			String docURI = request.getPartAsStringFailsafe("newContentURI", 128);
			String identity = request.getPartAsStringFailsafe("OwnerID", 128);
			String pageTitle = request.getPartAsStringFailsafe("title", 65);
			
			Collection<String> tags = new ArrayList<String>();
			tags.add(request.getPartAsStringFailsafe("tag1", 155));
			tags.add(request.getPartAsStringFailsafe("tag2", 155));
			tags.add(request.getPartAsStringFailsafe("tag3", 155));
			
			if (Utils.validString(category) && Utils.validString(docURI)) {
				try {
					final String insertURI = WoTOwnIdentities
							.getInsertURI(identity);
					FreenetURI privURI = new FreenetURI(insertURI);
					final String requestURI = WoTOwnIdentities
							.getRequestURI(identity);
					FreenetURI pubURI = new FreenetURI(requestURI);
					pubURI = pubURI.setDocName("index").setSuggestedEdition(0);
					privURI = privURI.setDocName("index").setSuggestedEdition(0);
					
					//TODO leuchtkaefer check that all needed inputs are not empty pageTitle, category. Tags are optional
					
					entry = new InputEntry(privURI, pubURI, category, new FreenetURI(docURI), pageTitle, tags, null);
					
				} catch (MalformedURLException e1) {
					Logger.error(this, "Error while forming the URI", e1);
					System.out.println("Leuchtkaefer MalformedURLException "
							+ e1);
					return;
				}
				//send user's input to Library
				LibraryTalker ltalker = cCur.getLibraryTalker();
				ltalker.sendInput(entry);
			}else {
				HTMLNode listBoxContent2 = addContentBox("Please include some data to publish in your index.");
			}
		}
	}

	private void curateIt(String uriContent, String title) throws MalformedURLException{
		
		HTMLNode listBoxContent = addContentBox(l10n().getString(
				"CurateThisContentPage.SelectWoTIdentity.Header"));
		HTMLNode inputForm = pr.addFormChild(listBoxContent, uri,
				"CurateItForm"); 
		HTMLNode selectBox = new HTMLNode("select", "name", "OwnerID");

		System.out.println("curateIt receives "+ (title.length()>0) != null ? title:"title is empty");
		
		synchronized (cCur) {
			for (final String identityID : this.getWotIdentities().keySet()){
				selectBox.addChild("option", "value", identityID, this
						.getWotIdentities().get(identityID));
			}
			selectBox.getChildren().get(0).addAttribute("selected", "selected"); //default value
			
		}
		inputForm.addChild("p").addChild("label", "for", "Author",l10n().getString("CurateThisContentPage.AuthorLabel")).addChild("br")
			.addChild(selectBox);	
		inputForm.addChild("br");
		
		inputForm.addChild("p").addChild("label", "for", "Term",l10n().getString("CurateThisContentPage.TermLabel")).addChild("br")
			.addChild("input", new String[] { "type", "name", "size" },
				new String[] { "text", "term", "30" });
		inputForm.addChild("br");
		
		HTMLNode uriBox = inputForm.addChild("p").addChild("label", "for", "URI",l10n().getString("CurateThisContentPage.URILabel")).addChild("br")
			.addChild("input", new String[] { "type", "name", "size" },
				new String[] { "text", "newContentURI", "128" });
		
		if (uriContent.length()>0) {
			FreenetURI fURI = new FreenetURI(uriContent);
			String uriSSK = fURI.sskForUSK().toASCIIString(); //TODO leuchtkaefer support for CHK!		
			uriBox.addAttribute("value", uriSSK);
		}
		inputForm.addChild("br");
		
		HTMLNode titleBox = inputForm.addChild("p").addChild("label", "for", "Title",l10n().getString("CurateThisContentPage.TitleLabel")).addChild("br")
			.addChild("input", new String[] { "type", "name", "size" },
					new String[] { "text", "title", "65" });
		if (title.length()>0) {
			titleBox.addAttribute("value", title);
		}
		inputForm.addChild("br");
	
		inputForm.addChild("p").addChild("label", "for", "Term",l10n().getString("CurateThisContentPage.TagsLabel"));
		inputForm.addChild("input", new String[] { "type", "name", "size" },
					new String[] { "text", "tag1", "155" });
		inputForm.addChild("input", new String[] { "type", "name", "size" },
				new String[] { "text", "tag2", "155" });
		inputForm.addChild("input", new String[] { "type", "name", "size" },
				new String[] { "text", "tag3", "155" });
		inputForm.addChild("br");
		
		inputForm.addChild("p").
				addChild("input", new String[] { "type", "name", "value" },
						new String[] { "submit", "buttonNewContent",
								"addingNewContent" });
	}


	private void makeNoOwnIdentityWarning() {
		addErrorBox(
				l10n().getString(
						"UploadSomethingPage.NoOwnIdentityWarning.Header"),
				l10n().getString(
						"UploadSomethingPage.NoOwnIdentityWarning.Text"));
	}
}

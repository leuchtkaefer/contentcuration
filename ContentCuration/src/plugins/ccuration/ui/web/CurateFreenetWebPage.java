/* This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.ccuration.ui.web;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import plugins.ccuration.ContentCuration;
import plugins.ccuration.LibraryTalker;
import plugins.ccuration.fcp.wot.WoTContexts;
import plugins.ccuration.fcp.wot.WoTOwnIdentities;
import plugins.ccuration.index.InputEntry;
import plugins.ccuration.index.TermEntry;
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
public class CurateFreenetWebPage extends WebPageImpl {
	
	/**
	 * Creates a webpage.
	 * 
	 * @param toadlet
	 *            A reference to the {@link WebInterfaceToadlet} which created
	 *            the page, used to get resources the page needs.
	 * @param myRequest
	 *            The request sent by the user.
	 */
	public CurateFreenetWebPage(WebInterfaceToadlet toadlet,
			HTTPRequest myRequest, ToadletContext context, BaseL10n _baseL10n) {
		super(toadlet, myRequest, context, _baseL10n);

	}

	public void make() {
		String content = null;
		String activeIdentity = null;
		int nbOwnIdentities = 0;
		String selectedIdentity = request.getPartAsStringFailsafe(
				"OwnerID", 128);
		String buttonChosenIdentity = request.getPartAsStringFailsafe(
				"buttonChooseIdentity", 128);
		String buttonNewContentValue = request.getPartAsStringFailsafe(
				"buttonNewContent", 128);
		InputEntry entry; 
				
		//Inputs coming from bookmarklet button
		String bookmarkletURI = request.getParam("addNewURI");
		String docTitle = request.getParam("addDocTitle");
		
		synchronized (cCur) {
			Set<String> allOwnIdentities;
			try {
				allOwnIdentities = WoTOwnIdentities.getWoTIdentities()
						.keySet();
				nbOwnIdentities = allOwnIdentities.size();
			} catch (PluginNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (!buttonNewContentValue.equals("")) { // TODO leuchtkaefer check
			content = buttonNewContentValue;
		}
		
		if (!buttonChosenIdentity.equals("")) {
			activeIdentity = selectedIdentity;
		} 

		try {
			if (nbOwnIdentities > 0) {
				selectIdentity(activeIdentity);
			} else {
			makeNoOwnIdentityWarning();
			}
			if (activeIdentity != null) {
				curateIt("", "", activeIdentity);
			}
		} catch (MalformedURLException e) {
			makeNoURLWarning();
		}
		
		// Actions done when addingNewContent button is pressed
		if (content != null) {
			String category = request.getPartAsStringFailsafe("term", 128);
			String docURI = request.getPartAsStringFailsafe("newContentURI", 128);
			String activeID = request.getPartAsStringFailsafe("currentID", 128);
			String pageTitle = request.getPartAsStringFailsafe("title", 65);
			Collection<String> tags = new ArrayList<String>();
			tags.add(request.getPartAsStringFailsafe("tag1", 155));
			tags.add(request.getPartAsStringFailsafe("tag2", 155));
			tags.add(request.getPartAsStringFailsafe("tag3", 155));
			
			if (Utils.validString(category) && Utils.validString(docURI)) {
				try {
					final String insertURI = WoTOwnIdentities
							.getInsertURI(activeID);
					FreenetURI privURI = new FreenetURI(insertURI);
					final String requestURI = WoTOwnIdentities
							.getRequestURI(activeID);
					FreenetURI pubURI = new FreenetURI(requestURI);
					pubURI = pubURI.setDocName("index").setSuggestedEdition(0);
					privURI = privURI.setDocName("index").setSuggestedEdition(0);
					
					//TODO category is NOT used. I need another index file!!!!
					//TODO leuchtkaefer check that all needed inputs are not empty pageTitle, category. Tags are optional					
					entry = new InputEntry.Builder(privURI, pubURI, new FreenetURI(docURI), TermEntry.EntryType.PAGE,tags).title(pageTitle).build();
					
					//publish the identity'index
					if (!WoTOwnIdentities.identityIsARegisteredPublisher(activeID)) {
							WoTOwnIdentities.registerIdentity(activeID);				
					}
					WoTOwnIdentities.registerIndex(activeID, category, pubURI.toASCIIString());
					
				} catch (MalformedURLException e1) {
					Logger.error(this, "Error while forming the URI", e1);
					makeNoURLWarning();
					return;
				} catch (PluginNotFoundException e) {
					Logger.error(this, "WoT plugin not found", e);
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

	/**
	 * Makes a form to select a WoT identity
	 * @param defaultSelectedValue
	 * @throws MalformedURLException
	 */
	private void selectIdentity(String defaultSelectedValue)  throws MalformedURLException {
		HTMLNode listBoxContent = addContentBox(l10n().getString(
				"CurateThisContentPage.SelectWoTIdentity.Header"));
		HTMLNode inputForm = pr.addFormChild(listBoxContent, uri,
				"SelectIdentityForm"); 
		HTMLNode selectBox = new HTMLNode("select", "name", "OwnerID");		
		synchronized (cCur) {
			for (final String identityID : this.getWotIdentities().keySet()){
				HTMLNode selectBoxElement = selectBox.addChild("option", "value", identityID, this
						.getWotIdentities().get(identityID));
				if (defaultSelectedValue != null && defaultSelectedValue.equals(identityID.toString())) {
					selectBoxElement.addAttribute("selected", "selected");
				}
			}
			
			if (defaultSelectedValue == null) {
				selectBox.getChildren().get(0).addAttribute("selected", "selected"); //default value
			}
			inputForm.addChild("br"); 
		}
		inputForm.addChild("p").addChild("label", "for", "Author",l10n().getString("CurateThisContentPage.AuthorLabel")).addChild("br")
		.addChild(selectBox);
		
		inputForm.addChild("p").
		addChild("input", new String[] { "type", "name", "value" },
				new String[] { "submit", "buttonChooseIdentity",
						"Use this identity" });
	}
	
	/**
	 * Creates the complete form to publish an element in a curated index.
	 * @param uriContent
	 * @param title
	 * @param selectedIdentity
	 * @throws MalformedURLException
	 */
	private void curateIt(String uriContent, String title, String selectedIdentity) throws MalformedURLException{
		HTMLNode inputForm = makeMetadataForm(selectedIdentity);
		
		makeDataForm(uriContent, title, inputForm);
		
		inputForm.addChild("p").
				addChild("input", new String[] { "type", "name", "value" },
						new String[] { "submit", "buttonNewContent",
								"addingNewContent" });
	}

	/**
	 * Creates the data section form. The content of this section is different for web and document contents.
	 * @param uriContent
	 * @param title
	 * @param inputForm
	 * @throws MalformedURLException
	 */
	private void makeDataForm(String uriContent, String title,
			HTMLNode inputForm) throws MalformedURLException {
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
	}

	/**
	 * Creates the metadata section form. The content of this section is share by web and document contents
	 * @param selectedIdentity
	 * @return
	 */
	private HTMLNode makeMetadataForm(String selectedIdentity) {
		HTMLNode listBoxContent2 = addContentBox(l10n().getString(
				"CurateThisContentPage.WebContentForm.Header"));
		HTMLNode inputForm = pr.addFormChild(listBoxContent2, uri,
				"CurateItForm"); 
		
		HTMLNode currentIdentity = inputForm.addChild("input", new String[] { "type", "name", "value" },
						new String[] { "hidden", "currentID", "128" });
			if (selectedIdentity.length()>0) {
				currentIdentity.addAttribute("value", selectedIdentity.toString());
			}

		HTMLNode selectCategory = new HTMLNode("select", "name", "term");
		
		synchronized (cCur) {
			inputForm.addChild("br"); 
			
			List<String> lis = this.getWotIndexCategories().get(selectedIdentity);
			
			for (final String categoryID : lis) {
				selectCategory.addChild("option", "value", categoryID, categoryID); 
			}
			selectCategory.getChildren().get(0).addAttribute("selected", "selected");			
		}
			
		inputForm.addChild("p").addChild("label", "for", "Term",l10n().getString("CurateThisContentPage.TermLabel")).addChild("br")
		.addChild(selectCategory);
		return inputForm;
	}


	private void makeNoOwnIdentityWarning() {
		addErrorBox(
				l10n().getString(
						"CurateThisContentPage.NoOwnIdentityWarning.Header"),
				l10n().getString(
						"CurateThisContentPage.NoOwnIdentityWarning.Text"));
	}
	
	private void makeNoURLWarning() {
		addErrorBox(
				l10n().getString(
						"CurateThisContentPage.NoURLWarning.Header"),
				l10n().getString(
						"CurateThisContentPage.NoURLWarning.Text"));
	}
}

/* This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.ccuration.ui.web;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import plugins.ccuration.LibraryTalker;
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
abstract class CurateForm extends WebPageImpl{
	
	/**
	 * Creates a webpage.
	 * 
	 * @param toadlet
	 *            A reference to the {@link WebInterfaceToadlet} which created
	 *            the page, used to get resources the page needs.
	 * @param myRequest
	 *            The request sent by the user.
	 */
	public CurateForm(WebInterfaceToadlet toadlet,
			HTTPRequest myRequest, ToadletContext context, BaseL10n _baseL10n) {
		super(toadlet, myRequest, context, _baseL10n);

	}

	public void make() {
		String content = null;
		String activeIdentity = null;
		String newIndex = null;
		int nbOwnIdentities = 0;
		String selectedIdentity = request.getPartAsStringFailsafe(
				"OwnerID", 128);
		String buttonChosenIdentityValue = request.getPartAsStringFailsafe(
				"buttonChooseIdentity", 128);
		String buttonNewContentValue = request.getPartAsStringFailsafe(
				"buttonNewContent", 128);
		String buttonNewCategoryValue = request.getPartAsStringFailsafe(
				"newCategory", 155);
		String addedCategoryRequest = request.getPartAsStringFailsafe(
				"addedCategory", 155);
		String currentPreviousIdentity = request.getPartAsStringFailsafe(
				"currentID", 128);
		
		InputEntry entry; 
						
		synchronized (cCur) {
			Set<String> allOwnIdentities;
			try {
				allOwnIdentities = WoTOwnIdentities.getWoTIdentities()
						.keySet();
				nbOwnIdentities = allOwnIdentities.size();
			} catch (PluginNotFoundException e) {
				Logger.error(this, "WoT plugin not found", e);
				return;
			}
		}
		
		if (!buttonNewContentValue.equals("")) { // TODO leuchtkaefer check
			content = buttonNewContentValue;
		}
		
		if (!buttonChosenIdentityValue.equals("")) {
			activeIdentity = selectedIdentity;
		} 
		if (!buttonNewCategoryValue.equals("")) {
			activeIdentity = currentPreviousIdentity;
			newIndex = addedCategoryRequest; //a new category implies a new index for the active identity
			//Register the new index on WoT
			synchronized (cCur) {
				String indexURI = WoTOwnIdentities.getRequestURI(activeIdentity).split("/")[0];
				try {
					WoTOwnIdentities.registerIndex(activeIdentity, newIndex, indexURI);
				} catch (PluginNotFoundException e) {
					Logger.error(this, "WoT plugin not found", e);
					return;
				}
			}
		}
		
		try {
			if (nbOwnIdentities > 0) {
				selectIdentity(activeIdentity);
			} else {
			makeNoOwnIdentityWarning();
			}
			if ((activeIdentity != null) && (activeIdentity.length()>0)) { //the user must set an active identity to get the CurateIt form 
				curateIt("", "", activeIdentity,newIndex);
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
					FreenetURI privURI = new FreenetURI(WoTOwnIdentities
							.getInsertURI(activeID));
					FreenetURI pubURI = new FreenetURI(WoTOwnIdentities
							.getRequestURI(activeID));
					pubURI = pubURI.setDocName(category).setSuggestedEdition(0);
					privURI = privURI.setDocName(category).setSuggestedEdition(0);
									
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
	protected void selectIdentity(String defaultSelectedValue)  throws MalformedURLException {
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
	protected void curateIt(String uriContent, String title, String selectedIdentity, String index) throws MalformedURLException{
		HTMLNode inputForm = makeMetadataForm(selectedIdentity, index);
		if (inputForm != null) {
			makeDataForm(uriContent, title, inputForm);
			inputForm.addChild("p").
					addChild("input", new String[] { "type", "name", "value" },
							new String[] { "submit", "buttonNewContent",
									"addingNewContent" });
		}
	}

	/**
	 * Creates the data section form. The content of this section is different for web and document contents.
	 * @param uriContent
	 * @param title
	 * @param inputForm
	 * @throws MalformedURLException
	 */
	abstract protected HTMLNode makeDataForm(String uriContent, String title, HTMLNode inputForm) throws MalformedURLException; 

	
	/**
	 * Creates the metadata section form. The content of this section is share by web and document contents
	 * @param selectedIdentity
	 * @return
	 */
	private HTMLNode makeMetadataForm(String selectedIdentity, String newAddedCategory) {
		HTMLNode listBoxContent2 = addContentBox(l10n().getString(
				"CurateThisContentPage.DataInputForm.Header"));
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
			List<String> lis;
			try {
				//this.getWotIndexCategories().get(selectedIdentity);
				lis = WoTOwnIdentities.getWoTIdentitiesCuratedCategories().get(selectedIdentity);
			} catch (PluginNotFoundException e) {
				Logger.error(this, "WoT plugin not found", e);
				return null;
			}			
			for (final String categoryID : lis) {
				selectCategory.addChild("option", "value", categoryID, categoryID); 
				if ((newAddedCategory != null) && newAddedCategory.equals(categoryID.toString())) {
					selectCategory.addAttribute("selected", "selected");
				}
			}		
			if (newAddedCategory == null) {
				selectCategory.getChildren().get(0).addAttribute("selected", "selected"); //default value
			}						
		}
			
		inputForm.addChild("p").addChild("label", "for", "Term",l10n().getString("CurateThisContentPage.TermLabel"));
		inputForm.addChild(selectCategory);
		inputForm.addChild("input", new String[] { "type", "name", "size" },
					new String[] { "text", "addedCategory", "155" });
		final HTMLNode buttonAddCat = inputForm.addChild("input", new String[]{"type", "name", "value"},
				new String[]{"submit", "newCategory", " + "});
		buttonAddCat.addChild("label", "for", "buttonAddCat","Add a new category");//l10n().getString("CurateThisContentPage.TitleLabel"));
		return inputForm;
	}


	protected void makeNoOwnIdentityWarning() {
		addErrorBox(
				l10n().getString(
						"CurateThisContentPage.NoOwnIdentityWarning.Header"),
				l10n().getString(
						"CurateThisContentPage.NoOwnIdentityWarning.Text"));
	}
	
	protected void makeNoURLWarning() {
		addErrorBox(
				l10n().getString(
						"CurateThisContentPage.NoURLWarning.Header"),
				l10n().getString(
						"CurateThisContentPage.NoURLWarning.Text"));
	}
}

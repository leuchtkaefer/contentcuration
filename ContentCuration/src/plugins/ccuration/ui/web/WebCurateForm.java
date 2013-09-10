package plugins.ccuration.ui.web;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
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

public class WebCurateForm extends CurateForm{

	public WebCurateForm(WebInterfaceToadlet toadlet,
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
	
	protected HTMLNode makeDataForm(String uriContent, String title, HTMLNode inputForm) throws MalformedURLException { //TODO remove inputForm2
		
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
		
		return inputForm;
	}

}

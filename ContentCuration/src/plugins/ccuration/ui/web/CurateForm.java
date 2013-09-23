/* This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.ccuration.ui.web;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

	public static String INDEX_FILENAME_EXTENSION = ".yml";
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

	abstract public void make();
	
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
			Map<String, String> m = this.getWotIdentities();
			for (final String identityID : m.keySet()){ 
				HTMLNode selectBoxElement = selectBox.addChild("option", "value", identityID, m.get(identityID));
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
				l10n().getString("CurateForm.InputForm.Button.SelectIdentity")});
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
							l10n().getString(
									"CurateForm.InputForm.Button.AddNewContent") });
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
			List<String> los;
			try {
				los = WoTOwnIdentities.getCuratedCategories().get(selectedIdentity); 
				Collections.sort(los); //No idea how it works with arabic or chinese characters
			} catch (PluginNotFoundException e) {
				Logger.error(this, "WoT plugin not found", e);
				return null;
			}			
			for (final String categoryID : los) {
				HTMLNode childCategory = new HTMLNode("option", "value", categoryID, categoryID);
				if ((newAddedCategory != null) && newAddedCategory.equals(categoryID.toString())) {
					childCategory.addAttribute("selected", "selected");
				}
				selectCategory.addChild(childCategory);
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
		buttonAddCat.addChild("label", "for", "buttonAddCat",l10n().getString("CurateThisContentPage.buttonAddCat"));
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

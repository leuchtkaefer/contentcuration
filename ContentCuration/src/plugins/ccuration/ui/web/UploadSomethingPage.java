/* This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.ccuration.ui.web;

import java.net.MalformedURLException;
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
 * The Input Form Page of the plugin.
 * 
 * @author leuchtkaefer
 */
public class UploadSomethingPage extends WebPageImpl {

	/**
	 * Creates a webpage.
	 * 
	 * @param toadlet
	 *            A reference to the {@link WebInterfaceToadlet} which created
	 *            the page, used to get resources the page needs.
	 * @param myRequest
	 *            The request sent by the user.
	 */
	public UploadSomethingPage(WebInterfaceToadlet toadlet,
			HTTPRequest myRequest, ToadletContext context, BaseL10n _baseL10n) {
		super(toadlet, myRequest, context, _baseL10n);

	}

	public void make() {
		makeSummary();
		String indexOwner = null;
		String content = null;
		int nbOwnIdentities = 1;
		String ownerID = request.getPartAsStringFailsafe("OwnerID", 128);
		String buttonNewContentValue = request.getPartAsStringFailsafe(
				"buttonNewContent", 128);
		String identity = request
				.getPartAsStringFailsafe("chosenIdentity", 128);
		InputEntry entry;

		if (!ownerID.equals("")) {
			try {
				indexOwner = ownerID; // Uses the selected Identity
			} catch (Exception e) {
				Logger.error(this, "Error while selecting the OwnIdentity", e);
				addErrorBox(
						l10n().getString(
								"UploadSomething.SelectOwnIdentity.Failed"), e);
			}
		} else {
			synchronized (cCur) {
				Set<String> allOwnIdentities;
				try {
					allOwnIdentities = WoTOwnIdentities.getWoTIdentities()
							.keySet();
					nbOwnIdentities = allOwnIdentities.size();
					if (nbOwnIdentities == 1) {
						Iterator<String> iterator = allOwnIdentities.iterator();
						indexOwner = iterator.next();
					}
				} catch (PluginNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// Actions done when UseThisIdentity button is pressed?//TODO check
		if (indexOwner != null) {
			try {
				makeInputNewContentForm(indexOwner);
				// Sends to WoT Plugin Context and Properties.
				WoTContexts.addContext(indexOwner);
				WoTContexts.addProperty(
						indexOwner,
						"IndexRoot",
						WoTOwnIdentities.getRequestURI(indexOwner).substring(
								0,
								WoTOwnIdentities.getRequestURI(indexOwner)
										.indexOf(' ')));
			} catch (PluginNotFoundException e) {
				Logger.error(this, "Error", e);
				addErrorBox("Error", e);
			}
		} else if (nbOwnIdentities > 1) {
			makeSelectOwnIdentityForm();
			// makeInputNewContentForm();
		} else {
			makeNoOwnIdentityWarning();
		}

		if (!buttonNewContentValue.equals("")) { // TODO leuchtkaefer check
			content = buttonNewContentValue;
		}

		// Actions done when addingNewContent button is pressed
		if (content != null) {

			System.out.println("entro al InputEntry");
			String word = request.getPartAsStringFailsafe("term", 128);
			String uri = request.getPartAsStringFailsafe("newContentURI", 128);

			if (Utils.validString(word) && Utils.validString(uri)) {
				try {
					FreenetURI privURI = new FreenetURI(WoTOwnIdentities
							.getInsertURI(identity).substring(
									0,
									WoTOwnIdentities.getInsertURI(identity)
											.indexOf(' ')));
					FreenetURI pubURI = new FreenetURI(WoTOwnIdentities
							.getRequestURI(identity).substring(
									0,
									WoTOwnIdentities.getRequestURI(identity)
											.indexOf(' ')));
					System.out.println("recovered Identity "
							+ WoTOwnIdentities.getRequestURI(identity));
					System.out.println(pubURI.toString());
					System.out.println(privURI.toString());

					entry = new InputEntry(privURI, pubURI);
					entry.setTermClassif(word);
					entry.setUri(new FreenetURI(uri));
					System.out.println("salgo del InputEntry");
				} catch (MalformedURLException e1) {
					Logger.error(this, "Error while forming the URI", e1);
					System.out.println("Leuchtkaefer MalformedURLException "
							+ e1);
					return;
				}
				// LibraryTalker will send user's input to Library
				LibraryTalker ltalker = cCur.getLibrarytalker();
				ltalker.maybeSend(entry);
			}else {
				HTMLNode listBoxContent2 = addContentBox("Please include some data to publish in your index.");
			}
		} 

	}

	/**
	 * Creates a short summary of what the plugin does.
	 */
	private void makeSummary() {
		// testing WoT connection
		final HTMLNode form = ContentCuration.getPluginRespirator()
				.addFormChild(contentNode, this.uri, "UploadSth");
		final HTMLNode generalBox = this.pm.getInfobox(null, ContentCuration
				.getBaseL10n().getString("UploadSomething.GeneralCCurData"),
				form, "IdentitySelection", true); // title

	}

	private void makeSelectOwnIdentityForm() {

		HTMLNode listBoxContent = addContentBox(l10n().getString(
				"UploadSomethingPage.SelectWoTIdentity.Header"));
		HTMLNode selectForm = pr.addFormChild(listBoxContent, uri,
				"ViewIdentity"); // TODO do i need
									// ContentCuration.getPluginRespirator?
		HTMLNode selectBox = selectForm.addChild("select", "name", "OwnerID");

		synchronized (cCur) {
			for (final String identityID : this.getWotIdentities().keySet())
				selectBox.addChild("option", "value", identityID, this
						.getWotIdentities().get(identityID));
		}

		selectForm
				.addChild(
						"input",
						new String[] { "type", "name", "value" },
						new String[] {
								"submit",
								"select",
								l10n().getString(
										"UploadSomethingPage.SelectWoTIdentity.SelectIdentityButton") });

	}

	private void makeInputNewContentForm(String indexOwner) {

		System.out.println("indexOwner" + indexOwner);
		HTMLNode inputNode = addContentBox(l10n().getString(
				"UploadSomethingPage.InputContent.Header"));
		HTMLNode inputForm = pr.addFormChild(inputNode, uri, "ViewIdentity"); // TODO
																				// name
																				// ViewIdentity
																				// why?
		inputForm.addChild("input", new String[] { "type", "name", "size" },
				new String[] { "text", "term", "30" });
		inputForm.addChild("input", new String[] { "type", "name", "size" },
				new String[] { "text", "newContentURI", "128" });
		inputForm.addChild("br");
		inputForm.addChild("input", new String[] { "type", "name", "size",
				"value" }, new String[] { "hidden", "chosenIdentity", "128",
				indexOwner });
		inputForm.addChild("br");
		inputForm
				.addChild("input", new String[] { "type", "name", "value" },
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

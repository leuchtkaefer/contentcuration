/* This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.ccuration.ui.web;

import java.net.MalformedURLException;

import freenet.support.HTMLNode;

/**
 * The Input Form Page of the curator plugin.
 * 
 * @author leuchtkaefer
 */
public interface CurateFormBackup {
	

	/**
	 * Makes a form to select a WoT identity
	 * @param defaultSelectedValue
	 * @throws MalformedURLException
	 */
	public void selectIdentity(String defaultSelectedValue)  throws MalformedURLException;
	
	/**
	 * Creates the complete form to publish an element in a curated index.
	 * @param uriContent
	 * @param title
	 * @param selectedIdentity
	 * @throws MalformedURLException
	 */
	public void curateIt(String uriContent, String title, String selectedIdentity, String index) throws MalformedURLException;
	

	/**
	 * Creates the data section form. The content of this section is different for web and document contents.
	 * @param uriContent
	 * @param title
	 * @param inputForm
	 * @throws MalformedURLException
	 */
	public HTMLNode makeDataForm(String uriContent, String title, HTMLNode inputForm) throws MalformedURLException;
		
	/**
	 * Creates the metadata section form. The content of this section is share by web and document contents
	 * @param selectedIdentity
	 * @return
	 */
	public HTMLNode makeMetadataForm(String selectedIdentity, String newAddedCategory);
	
	public void makeNoOwnIdentityWarning();
	
	public void makeNoURLWarning();
	
}

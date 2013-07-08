/* This code is part of CCuration, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.ccuration.fcp;

import java.util.Map.Entry;

import plugins.ccuration.ContentCuration;
import plugins.ccuration.exceptions.InvalidParameterException;
import plugins.ccuration.exceptions.UnknownIdentityException;
import freenet.pluginmanager.FredPluginFCP;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginReplySender;
import freenet.support.Logger;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * @author xor (xor@freenetproject.org), Julien Cornuwel
 *         (batosai@freenetproject.org)
 */
public final class FCPInterface implements FredPluginFCP {

	private final ContentCuration mCC;

	public FCPInterface(final plugins.ccuration.ContentCuration myCC) {
		mCC = myCC;
	}

	public void handle(final PluginReplySender replysender,
			final SimpleFieldSet params, final Bucket data, final int accesstype) {

		try {
			final String message = params.get("Message");

			if (message.equals("GetOwnIdentities")) {
				replysender.send(handleGetOwnIdentities(params), data);
			} else {
				throw new Exception("Unknown message (" + message + ")");
			}
		} catch (final Exception e) {
			// TODO: This might miss some stuff which are errors. Find a better
			// way of detecting which exceptions are okay.
			boolean dontLog = e instanceof UnknownIdentityException;

			if (!dontLog)
				Logger.error(this, "FCP error", e);

			try {
				replysender.send(errorMessageFCP(params.get("Message"), e),
						data);
			} catch (final PluginNotFoundException e1) {
				Logger.normal(this, "Connection to request sender lost", e1);
			}
		}
	}

	private String getMandatoryParameter(final SimpleFieldSet sfs,
			final String name) throws InvalidParameterException {
		final String result = sfs.get(name);
		if (result == null)
			throw new IllegalArgumentException("Missing mandatory parameter: "
					+ name);

		return result;
	}

	private SimpleFieldSet handleGetOwnIdentities(final SimpleFieldSet params) {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "OwnIdentities");

		synchronized (mCC) {
			// final ObjectSet<OwnIdentity> result = mCC.getAllOwnIdentities();
			final String result = "myIdentity";

			int i = 0;

			String routingKey = "wqtJyhqUdfoEX1Wdg5Ulebg3DRnNxH2SXK439QD~pWk";
			sfs.putOverwrite("Identity" + i, routingKey);
			sfs.putOverwrite(
					"RequestURI" + i,
					"SSK@wqtJyhqUdfoEX1Wdg5Ulebg3DRnNxH2SXK439QD~pWk,Pi2V~QCik4kvW5uBx-dB~A1Uv7eBdLYoT5CBL6aI8Hk,AQACAAE/");
			sfs.putOverwrite(
					"InsertURI" + i,
					"SSK@Rmk5BnyXFXRsxY4yt6f8KzvlvRWe9anNeZZJE-wjoI0,Pi2V~QCik4kvW5uBx-dB~A1Uv7eBdLYoT5CBL6aI8Hk,AQECAAE/");
			sfs.putOverwrite("Nickname" + i, "Oliver_Chaudhry");
			int contextCounter = 0;

			/**
			 * A context is a string, the identities contexts are a set of strings - no context will be added more than once.
			 * Contexts are used by client applications to identify what identities are relevant for their use.
			 */

			sfs.putOverwrite("Contexts" + i + ".Context" + contextCounter++,
					"ContentCurator");
			/**
			 * Sets a custom property on this Identity. Custom properties keys
			 * have to be unique. This can be used by client applications that
			 * need to store additional informations on their Identities (crypto
			 * keys, avatar, whatever...).
			 */
			int propertiesCounter = 0;
			sfs.putOverwrite("Properties" + i + ".Property" + propertiesCounter
					+ ".Name", "PropertyKey");
			sfs.putOverwrite("Properties" + i + ".Property"
					+ propertiesCounter++ + ".Value", "PropertyValue");
			++i;
			sfs.putOverwrite("Amount", Integer.toString(i));
		}

		return sfs;
	}

	private SimpleFieldSet handlePing() {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "Pong");
		return sfs;
	}

	private SimpleFieldSet errorMessageFCP(final String originalMessage,
			final Exception e) {
		final SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "Error");
		sfs.putOverwrite("OriginalMessage", originalMessage);
		sfs.putOverwrite("Description", e.toString());
		return sfs;
	}

}

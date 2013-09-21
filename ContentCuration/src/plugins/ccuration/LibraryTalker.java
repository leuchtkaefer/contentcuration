/* Curator, Freenet plugin to curate content
 * Copyright (C) 2013 leuchtkaefer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
package plugins.ccuration;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import plugins.ccuration.fcp.wot.WoTOwnIdentities;
import plugins.ccuration.index.InputEntry;
import plugins.ccuration.index.TermEntry;
import plugins.ccuration.index.TermEntryWriter;
import freenet.keys.FreenetURI;
import freenet.pluginmanager.FredPluginTalker;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.pluginmanager.PluginTalker;
import freenet.support.Base64;
import freenet.support.Logger;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * It prepares a buffer to keep TermPageEntries and TermFileEntries as they are submitted by one unique WoT Identity. 
 * The buffer is immediately serialized into a Bucket and sent to the Library. 
 * It is a simplified version of LibraryBuffer for Spider (@author MikeB).
 * 
 * @author leuchtkaefer
 */

public class LibraryTalker implements FredPluginTalker, ContentCurationConstants{


	private PluginRespirator pr;
	private boolean shutdown;

	private ContentCuration cCur; 

	LibraryTalker(PluginRespirator pr, ContentCuration cCur) {
		this.pr = pr;
		this.cCur = cCur;
	}



	/**
	 * 
	 * @param input
	 */
	public void sendInput(InputEntry i){ 
		System.out.println("Leuchtkaefer master of curator, I reached sendInput");
		try {
			Bucket bucket = pr.getNode().clientCore.tempBucketFactory.makeBucket(3000000);
			fillBucket(bucket, i);
			innerSend(bucket, i);
		} catch (IOException e) {
			Logger.error(this, "Could not make bucket to transfer buffer", e);
		}
		synchronized(this) {
			if(shutdown) return;
		}

	}


	/**
	 * It gets all identities and send a message to Library in order to finalize any pending on disk merging job. 
	 * @throws PluginNotFoundException
	 */
	public void start() throws PluginNotFoundException {
		PluginTalker libraryTalker;
		SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putSingle(COMMAND, PUSH_BUFFER);

		//Get all identities
		Map<String, String> identityList = WoTOwnIdentities.getWoTIdentities("InsertURI");

		libraryTalker = pr.getPluginTalker(this, PLUGINS_LIBRARY_MAIN, CONTENT_CURATOR);

		//Make sure there is at least one identity
		if (identityList.isEmpty()) {
			return;
		}

		Iterator<String> WoTId = identityList.keySet().iterator();
		FreenetURI insertURI;
		while (WoTId.hasNext()) {
			String identityID = WoTId.next();
			synchronized (cCur) { 
				List<String> lis;
				try {
					lis = WoTOwnIdentities.getWoTIdentitiesCuratedCategories().get(identityID);
					for (final String categoryID : lis) {
						insertURI = new FreenetURI(identityList.get(identityID));
						insertURI = insertURI.setDocName(categoryID).setSuggestedEdition(0);
						sfs.putOverwrite(HASH_PUBKEY, identityID);
						sfs.putOverwrite(INSERT_URI, insertURI.toASCIIString());
						libraryTalker.sendSyncInternalOnly(sfs, null);
					}			
				} catch (PluginNotFoundException e) {
					Logger.error(this, "WoT plugin not found", e);
				} catch (MalformedURLException e) {
					Logger.error(this,"MalformedURL insertURI" + identityID, e);
				}	
			}
		}

	}


	private void innerSend(Bucket bucket, InputEntry input) {
		System.out.println("Leuchtkaefer master of curator, I reached innerSend");
		System.out.println("Leuchtkaefer input size "+ input.getTpe().size());
		SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putSingle(COMMAND, PUSH_BUFFER);
		sfs.putSingle(INSERT_URI, input.getPrivKey().toASCIIString());
		sfs.putSingle(HASH_PUBKEY, Base64.encode(input.getPubKey().getRoutingKey()));
		sfs.putSingle(INDEX_NAME, input.getPubKey().getDocName());

		PluginTalker libraryTalker;
		try {
			libraryTalker = pr.getPluginTalker(this, PLUGINS_LIBRARY_MAIN, CONTENT_CURATOR);
			libraryTalker.sendSyncInternalOnly(sfs, bucket);
			bucket.free();
		} catch (PluginNotFoundException e) {
			Logger.error(this, "Couldn't connect buffer to Library", e);
		}
	}

	/**
	 * Fills a bucket with index'metadata and the TermEntries contain in InputEntry. TermEntries are related to 
	 * one unique Freenet object, that could be a web page or a file (pdf, jpg, etc).
	 * 
	 * @param bucket
	 * @param input
	 * @throws IOException
	 */
	private void fillBucket(Bucket bucket, InputEntry input) throws IOException {
		OutputStream os = bucket.getOutputStream();
		SimpleFieldSet meta = new SimpleFieldSet(true); // Stored with data to make things easier.
		String indexOwner = Base64.encode(input.getPubKey().getRoutingKey());
		meta.putSingle("index.title", input.getPubKey().getDocName());
		if(indexOwner != null) {
			meta.putSingle("index.owner.name", indexOwner);
		}
		meta.putSingle("index.owner.email", "private");
		meta.writeTo(os);

		for (TermEntry termEntry : input.getTpe()) {
			TermEntryWriter.getInstance().writeObject(termEntry, os); 
		}
		os.close();
		bucket.setReadOnly();
	}

	@Override
	public void onReply(String pluginname, String indentifier,
			SimpleFieldSet params, Bucket data) {
		// TODO Auto-generated method stub

	}

}

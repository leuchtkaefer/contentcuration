package plugins.ccuration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import plugins.ccuration.fcp.wot.WoTOwnIdentities;
import plugins.ccuration.index.InputEntry;
import plugins.ccuration.index.TermEntry;
import plugins.ccuration.index.TermEntryWriter;
import plugins.ccuration.index.TermFileEntry;
import plugins.ccuration.index.TermPageEntry;
import freenet.keys.FreenetURI;
import freenet.pluginmanager.FredPluginTalker;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.pluginmanager.PluginTalker;
import freenet.support.Base64;
import freenet.support.Logger;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;
import freenet.support.io.FileBucket;

/**
 * Buffer which stores TermPageEntrys as they are submitted by one unique WoT Identity. When the
 * buffer's estimated size gets up to bufferMax, the buffer is serialized into a
 * Bucket and sent to the Library...//TODO leuchtkaefer not sure if it will work on this way
 * 
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
		/*
		InputStream is = null; //TODO leuchtkaefer remove all this try
		try{
			is = bucket.getInputStream();
			SimpleFieldSet fs = new SimpleFieldSet(new LineReadingInputStream(is), 1024, 512, true, true, true);
		} catch (IOException ex) {
			Logger.error(this,"IO Exception", ex);
		} finally {
			Closer.close(is);
		}*/
		PluginTalker libraryTalker;
		try {
			libraryTalker = pr.getPluginTalker(this, PLUGINS_LIBRARY_MAIN, CONTENT_CURATOR);
			libraryTalker.sendSyncInternalOnly(sfs, bucket);
			bucket.free();
		} catch (PluginNotFoundException e) {
			Logger.error(this, "Couldn't connect buffer to Library", e);
		}
	}

	private void fillBucket(Bucket bucket, InputEntry input) throws IOException {
		System.out.println("Leuchtkaefer master of curator, I reached fillBucket");
		
		OutputStream os = bucket.getOutputStream();
		SimpleFieldSet meta = new SimpleFieldSet(true); // Stored with data to make things easier.
		String indexOwner = Base64.encode(input.getPubKey().getRoutingKey());
		
		meta.putSingle("index.title", input.getPrivKey().getDocName());
		if(indexOwner != null) {
			meta.putSingle("index.owner.name", indexOwner);
		}
		meta.putSingle("index.owner.email", "private");
		meta.writeTo(os);
/*	
		if (input.getInputType()==TermEntry.EntryType.FILE) {
			for (TermFileEntry termFileEntry : input.getTpe()) {
				TermEntryWriter.getInstance().writeObject(termFileEntry, os); //TODO leuchtkaefer I need to modify TermEntry to accept FIL
			}	
		} else if (input.getInputType()==TermEntry.EntryType.PAGE) {
			for (TermPageEntry termPageEntry : input.getTpe()) {
				TermEntryWriter.getInstance().writeObject(termPageEntry, os); 
			}
		}
*/			
		for (TermEntry termEntry : input.getTpe()) {
			TermEntryWriter.getInstance().writeObject(termEntry, os); //TODO leuchtkaefer I need to modify TermEntry to accept FIL
		}
		os.close();
		bucket.setReadOnly();
		System.out.println("Leuchtkaefer master of curator, fillBucket ended");
	}
	
	@Override
	public void onReply(String pluginname, String indentifier,
			SimpleFieldSet params, Bucket data) {
		// TODO Auto-generated method stub
		
	}

}

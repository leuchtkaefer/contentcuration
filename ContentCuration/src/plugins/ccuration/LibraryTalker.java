package plugins.ccuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.TreeMap;

import plugins.ccuration.index.InputEntry;
import plugins.ccuration.index.TermEntryWriter;
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
import freenet.support.io.Closer;
import freenet.support.io.FileBucket;
import freenet.support.io.LineReadingInputStream;

/**
 * Buffer which stores TermPageEntrys as they are submitted by one unique WoT Identity. When the
 * buffer's estimated size gets up to bufferMax, the buffer is serialized into a
 * Bucket and sent to the Library...//TODO leuchtkaefer not sure if it will work on this way
 * 
 *
 * @author MikeB, leuchtkaefer
 */

public class LibraryTalker implements FredPluginTalker, ContentCurationConstants{


	private PluginRespirator pr;
	private long timeStalled = 0;
	private long timeNotStalled = 0;
	private long timeLastNotStalled = System.currentTimeMillis();
	private boolean shutdown;
	
	private ContentCuration cCur;
	private TreeMap<TermPageEntry, TermPageEntry> termPageBuffer = new TreeMap<TermPageEntry, TermPageEntry>();
	private Collection<TermPageEntry> pushing = null; //Leuchtkaefer Pushing may content TermPageEntry from different indexes

	static final File SAVE_FILE = new File("curator.saved.data"); //TODO leuchtkaefer I am not sure when this file is created, 
	//the file should be modified if is used and multiple identities create indexes
	
	private int bufferUsageEstimate = 0;
	private int bufferMax;
	
	
	LibraryTalker(PluginRespirator pr, ContentCuration cCur) {
		this.pr = pr;
		this.cCur = cCur;
	}
	
	synchronized void setBufferSize(int maxSize) {
		if(maxSize <= 0) throw new IllegalArgumentException();
		bufferMax = maxSize;
	}
	
	/**
	 * Increments the estimate by specified amount.
	 * @param increment
	 */
	private synchronized void increaseEstimate(int increment) {
		bufferUsageEstimate += increment;
	}
	
	public synchronized int bufferUsageEstimate() {
		return bufferUsageEstimate;
	}
	
	public void terminate() { //leuchtkaefer it should be called by plugin. what i do with db??
		synchronized(this) {
			if(shutdown) {
				Logger.error(this, "Shutdown called twice", new Exception("error"));
				return;
			}
			shutdown = true;
			pushing = termPageBuffer.values();
			termPageBuffer = new TreeMap();
			bufferUsageEstimate = 0;
		}
		System.out.println("Writing pending data to "+SAVE_FILE);
		FileBucket bucket = new FileBucket(SAVE_FILE, false, false, false, false, false);
		/*TODO leuchtkaefer I don't use db or totalPagesIndexes
		long totalPagesIndexed;
		try {
			totalPagesIndexed = spider.getRoot().getPageCount(Status.INDEXED);
		} catch (Throwable t) {
			totalPagesIndexed = -1;
			// FIXME I don't understand why this (ClassNotFoundException) happens, we have not closed the class loader yet.
			System.err.println("Failed to determine page size during writing data to "+SAVE_FILE+": "+t);
			t.printStackTrace();
		}
		try {
			writeToPush(totalPagesIndexed, bucket);
		} catch (IOException e) {
			System.err.println("Failed to write pending data: "+e); // Hopefully no data loss due to NOT_PUSHED vs INDEXED.
			e.printStackTrace();
		}*/
		System.out.println("Written pending data to "+SAVE_FILE);
	}
	
	
	/**
	 * 
	 * @param input
	 */
	public void sendInput(InputEntry i){ 
		System.out.println("Leuchtkaefer master of curator, I reached sendInput");
		long tStart = System.currentTimeMillis();
		try {
			Bucket bucket = pr.getNode().clientCore.tempBucketFactory.makeBucket(3000000);
			fillBucket(bucket, i);
			innerSend(bucket, i);
		} catch (IOException e) {
			Logger.error(this, "Could not make bucket to transfer buffer", e);
		}
		long tEnd = System.currentTimeMillis();
		synchronized(this) {
			timeNotStalled += (tStart - timeLastNotStalled);
			timeLastNotStalled = tEnd;
			timeStalled += (tEnd - tStart);
			if(shutdown) return;
		}
		
		//TODO leuchtkaefer - do sth with this SAVE_FILE does not exists
		// Robustness: Send SAVE_FILE *after* sending new data, because *it is already on disk*, whereas the new data is not.
		if(SAVE_FILE.exists()) {
			System.out.println("Restoring data from last time from "+SAVE_FILE);
			Bucket bucket = new FileBucket(SAVE_FILE, true, false, false, false, true);
			innerSend(bucket, i);
			System.out.println("Restored data from last time from "+SAVE_FILE);
		}
	}
	
	/**
	 * TermPageEntry are created and filled with user input. 
	 * maybeSend is called by the web ui. 
	 *
	 * @param input Contains the data enter by user from the web ui
	 */
	/*
	public void maybeSend(InputEntry input) { //TODO leuchtkaefer maybesend should be send directly
	setBufferSize(1024*1024); //TODO leuchtkaefer which size for balance?
	bufferUsageEstimate = bufferMax+1; //TODO leuchtkaefer force everything
	System.out.println("Leuchtkaefer master of curator, I reached maybeSend");

	boolean push = false;  
	synchronized(this) {
		if (bufferMax == 0) return;
		if (bufferUsageEstimate > bufferMax) {
			if(pushing != null) {
				throw new IllegalStateException("Still pushing?!");
			}
			TermPageEntry tpe = input.getTpe(); 
			termPageBuffer.put(tpe, tpe);
			pushing = termPageBuffer.values();
			push = true; //TODO leuchtkaefer remove all push stuff
			termPageBuffer = new TreeMap<TermPageEntry,TermPageEntry>();
			bufferUsageEstimate = 0;
		}
	}
	if(push) sendInput();
	}*/
	
	
	
	private void innerSend(Bucket bucket, InputEntry input) {
		System.out.println("Leuchtkaefer master of curator, I reached innerSend");
		SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putSingle(COMMAND, PUSH_BUFFER);
		sfs.putSingle(INSERT_URI, input.getPrivKey().toASCIIString());
		sfs.putSingle(REQUEST_URI, input.getPubKey().toASCIIString());
		InputStream is = null;
		try{
			is = bucket.getInputStream();
			SimpleFieldSet fs = new SimpleFieldSet(new LineReadingInputStream(is), 1024, 512, true, true, true);
		} catch (IOException ex) {
			Logger.error(this,"IO Exception", ex);
		} finally {
			Closer.close(is);
		}
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
		String indexTitle = "Content curated by WoT identity";
	//	TermPageEntry firstEntry = pushing.iterator().next();

//		FreenetURI insertURI = firstEntry.getInsertURIOwner(); //leuchtkaefer do i need it?
//		String indexOwner = Base64.encode(firstEntry.getRequestURIOwner().getRoutingKey());
		String indexOwner = Base64.encode(input.getPubKey().getRoutingKey());
		String indexOwnerEmail = "private";
		
		if(indexTitle != null) {
			meta.putSingle("index.title", indexTitle);
		}
		if(indexOwner != null) {
			meta.putSingle("index.owner.name", indexOwner);
		}
		if(indexOwnerEmail != null) {
			meta.putSingle("index.owner.email", indexOwnerEmail);
		}	
		meta.writeTo(os);
	
		TermEntryWriter.getInstance().writeObject(input.getTpe(), os);
		/*
		for (TermPageEntry termPageEntry : pushing) {
			TermEntryWriter.getInstance().writeObject(termPageEntry, os); 
			System.out.println(termPageEntry.subj);
		}
	
		System.out.println("Leuchtkaefer master of curator, sali del for");
		pushing = null;
		*/
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

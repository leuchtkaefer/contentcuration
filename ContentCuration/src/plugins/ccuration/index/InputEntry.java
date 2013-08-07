package plugins.ccuration.index;

import java.net.MalformedURLException;
import java.util.Map;

import freenet.keys.FreenetURI;
import freenet.support.api.HTTPRequest;

public class InputEntry {

	private final FreenetURI privKey; // insertURI
	private final FreenetURI pubKey;
	private final TermPageEntry tpe;
	
	public FreenetURI getPrivKey() {
		return privKey;
	}


	public FreenetURI getPubKey() {
		return pubKey;
	}


	public TermPageEntry getTpe() {
		return tpe;
	}


	
	
	/**
	** Constructor.
	**
	** @param privK InsertURI of Curator Identity
	** @param pubK RequestURI of Curator Identity
	** @param s Subject of the entry
	** @param u {@link FreenetURI} of the page/document //TODO leuchtkaefer u is different for docs or pages?
	** @param t Title
	** @param p Map of positions (where the term appears) //TODO leuchtkaefer adapt p for tags
	**          
	*/
	public InputEntry(FreenetURI privK, FreenetURI pubK, String s, FreenetURI u, String t, Map<Integer, String> p) {
		tpe = new TermPageEntry(s, 0f, u, t, p); //TODO leuchtkaefer validate data
		this.privKey = privK;
		this.pubKey = pubK;
	}
	
	

}
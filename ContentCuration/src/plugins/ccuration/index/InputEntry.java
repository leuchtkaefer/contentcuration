package plugins.ccuration.index;

import java.net.MalformedURLException;

import freenet.keys.FreenetURI;
import freenet.support.api.HTTPRequest;

public class InputEntry {
	/*
	public final String URItoAdd;
	public final FreenetURI uri;
	public final String termClassif;
	private String insertURI;

	public String getInsertURI() {
		return insertURI;
	}

	public void setInsertURI(String insertURI) {
		//assert this.insertURI == null:"insertURI is set";
		this.insertURI = insertURI;
	}

	public InputEntry(HTTPRequest request) throws MalformedURLException {
		URItoAdd = request.getPartAsStringFailsafe("newContentURI",128);
		System.out.println("Value of URItoAdd inside InputEntry"+URItoAdd);
		uri = new FreenetURI(URItoAdd);
		termClassif = request.getPartAsStringFailsafe("term", 30);
		insertURI = null;
	}
	*/
	
	public final FreenetURI privKey; // insertURI
	public final FreenetURI pubKey;
	private FreenetURI uri;
	private String termClassif;
	

	public InputEntry(FreenetURI privKeyIdentity, FreenetURI pubKeyIdentity) throws MalformedURLException {
		privKey = privKeyIdentity;
		pubKey = pubKeyIdentity;
		uri = new FreenetURI("CHK@");
		termClassif = "general";
	}
	
	public FreenetURI getUri() {
		return uri;
	}

	public void setUri(FreenetURI uri) {
		this.uri = uri;
	}

	public String getTermClassif() {
		return termClassif;
	}

	public void setTermClassif(String termClassif) {
		this.termClassif = termClassif;
	}

}
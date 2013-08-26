package plugins.ccuration.index;

import static plugins.ccuration.utils.SearchUtil.isStopWord;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import plugins.ccuration.ContentCurationConstants;
import plugins.ccuration.utils.SearchTokenizer;
import plugins.ccuration.utils.Utils;
import freenet.keys.FreenetURI;
import freenet.support.Logger;

/**
 * This container stores all the tpe (TermPageEntry) related to one document and the identity who publishes it.
 * The constructor extract the terms from title, document's name, and keywords and generate one tpe for each.
 * @author leuchtkaefer
 *
 */
public class InputEntry implements ContentCurationConstants{

	private final FreenetURI privKey; // insertURI
	private final FreenetURI pubKey;
//	private final Collection<TermPageEntry> tpeCollection = null;
	private Integer lastPosition = null;
	private final String title;
	private int totalWords = 0;
	private final FreenetURI uri;
	
	private final HashMap<String, TermPageEntry> tpes = new HashMap<String, TermPageEntry>();
	
	/**
	 * Creates a tpe for words that are not stop words
	 * @param word
	 * @param position
	 * @throws java.lang.Exception
	 */
	private void addWord(String word, int position) {


		TermPageEntry tp = getEntry(word);
		tp.putPosition(position);
	}
	
	/**
	 * Gets the correspondent TermPageEntry or makes a new one for this term 
	 * @param word
	 * @return
	 */
	private TermPageEntry getEntry(String word) {
		TermPageEntry tp = tpes.get(word);
		if (tp == null) {
			tp = new TermPageEntry(word, 0, uri, title, null);
			tpes.put(word, tp);
		}
		return tp;
	}

		
	/**
	 * Process title 
	 * @param t
	 */
	private void processEntryItem(String e, ContentCurationConstants.EntryType t) {
		switch (t) {
		case DOC_NAME:
			if (lastPosition == null) {
				lastPosition = 1;
			}
			break;
		case TITLE:
			lastPosition = 1001;
			break;
		case TAG:
			if (lastPosition < 2000) {
				lastPosition = 2001;				
			} else if (lastPosition < 3000) {
				lastPosition = 3001;
			} else {
				lastPosition = 4001;
			}
			break;
		default:
			// TODO leuchtkaefer it may handle category words
			break;
		}

		// Tokenise. Do not use the pairs-of-CJK-chars option because we
		// need
		// accurate word index numbers.
		SearchTokenizer tok = new SearchTokenizer(e, false);

		int i = 0;
		for (String word : tok) {
			// Skip word if it is a stop word
			if (!isStopWord(word)) {

				incrementTotalWords();
				try {
					addWord(word, lastPosition + i);
				} catch (Exception exc) {
					// If a word fails continue
					Logger.error(this, "Word failed: " + exc, exc);
				}
				i++;
			}
		}
		lastPosition = lastPosition + i;
	}
	
	/**
	** Constructor.
	**
	** @param privK InsertURI of Curator Identity
	** @param pubK RequestURI of Curator Identity
	** @param s Subject of the entry
	** @param u {@link FreenetURI} of the page/document //TODO leuchtkaefer u is different for docs or pages?
	** @param t Title
	** @param tags List of keyphrases
	** @param p Map of positions (where the term appears) //TODO leuchtkaefer adapt p for tags
	**          
	*/
	public InputEntry(FreenetURI privK, FreenetURI pubK, String s, FreenetURI u, String t, Collection<String> tags, Map<Integer, String> p) {
		this.uri = u;
		this.title = t;
		processEntryItem(u.getDocName(), EntryType.DOC_NAME);
		processEntryItem(t, EntryType.TITLE);
		for (String keyphrase: tags) {
			if (Utils.validString(keyphrase)) {
				processEntryItem(keyphrase, EntryType.TAG);
			}
		}

//		this.tpeCollection.add(new TermPageEntry(s, 0f, u, t, p)); //TODO leuchtkaefer validate data title and category cannot be null
		this.privKey = privK;
		this.pubKey = pubK;
	}

	public FreenetURI getPrivKey() {
		return privKey;
	}


	public FreenetURI getPubKey() {
		return pubKey;
	}

	public String getTitle() {
		return title;
	}


	public int getTotalWords() {
		return totalWords;
	}


	public void incrementTotalWords() {
		totalWords++;
	}

	public Collection<TermPageEntry> getTpe() {
		return tpes.values();
//		return tpeCollection;
	}


	
	

}
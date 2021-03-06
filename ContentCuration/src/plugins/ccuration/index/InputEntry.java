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
package plugins.ccuration.index;

import static plugins.ccuration.utils.SearchUtil.isStopWord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import plugins.ccuration.ContentCurationConstants;
import plugins.ccuration.utils.SearchTokenizer;
import plugins.ccuration.utils.Utils;
import freenet.client.DefaultMIMETypes;
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
	private final String title;
	private final FreenetURI uri;
	private final TermEntry.EntryType inputType;
	private final String description;
	private final String mime;
	private Integer lastPosition = null;
	private int totalWords = 0;
	private final Map<String, TermEntry> tpes;

	/**
	 * Constructor for InputEntry.
	 * <p>Instantiating InputEntry for Freenet webpages</p>
	 * InputEntry i = new InputEntry.Builder(privK,pubK,u,PAGE,tags).title(t).build()
	 * <p>Instantiating InputEntry for Freenet documents</p>
	 * InputEntry i = new InputEntry.Builder(privK,pubK,u,FILE,tags).description(d).mime(m).build()
	 * 
	 * @param privK InsertURI of Curator Identity
	 * @param pubK RequestURI of Curator Identity
	 * @param s Subject of the entry
	 * @param u {@link FreenetURI} of the page/document 
	 * @param type FILE or PAGE
	 * @param t Title
	 * @param d Description
	 * @param m Mime type. If null, guess a mime type from file name
	 * @param tags List of keyphrases
	 * @param p Map of positions (where the term appears) //TODO leuchtkaefer adapt p for tags         
	 */
	private InputEntry(Builder builder) {
		this.privKey = builder.privKey;
		this.pubKey = builder.pubKey;
		this.title = builder.title;
		this.uri = builder.uri;
		this.inputType = builder.inputType;	
		this.lastPosition = builder.lastPosition;
		this.totalWords = builder.totalWords;
		this.tpes = builder.tpes;
		this.description = builder.description;
		this.mime = builder.mime;
	}

	public static class Builder {
		//Required params
		private final FreenetURI privKey; // insertURI
		private final FreenetURI pubKey;
		private final FreenetURI uri;
		private final TermEntry.EntryType inputType;
		private Integer lastPosition = null;
		private int totalWords = 0;
		private final Map<String, TermEntry> tpes = new HashMap<String, TermEntry>();

		//Optional params web pages
		private String title = null; 
		//Optional params files
		private String description = null; 
		private String mime = null; //mime can be guess from document name

		/**
		 ** Builder for InputEntry
		 ** Mandatories parameters
		 ** @param privK InsertURI of Curator Identity
		 ** @param pubK RequestURI of Curator Identity
		 ** @param uri {@link FreenetURI} of the page/document
		 ** @param inputType FILE or PAGE
		 ** @param s Subject of the entry
		 ** @param tags List of keyphrases
		 */
		public Builder(FreenetURI privKey, FreenetURI pubKey, FreenetURI uri, TermEntry.EntryType inputType, Collection<String> tags) {  
			this.privKey = privKey;  
			this.pubKey = pubKey;
			if (uri.isUSK()) {
				this.uri = uri.sskForUSK();
			} else {
				this.uri = uri; 	        	 
			}
			this.inputType = inputType;

			processEntryItem(uri.getDocName(), EntryType.DOC_NAME);

			for (String keyphrase: tags) {
				if (Utils.validString(keyphrase)) {
					processEntryItem(keyphrase, EntryType.TAG);
				}
			}

		}

		/**
		 * Sets optional parameter title. Used for TermPageEntries only.
		 * @param val
		 * @return
		 */
		public Builder setTitle(String val) {
			title = val;
			processEntryItem(val, EntryType.TITLE);
			return this;  
		}
		
		/**
		 * Sets optional parameter description. Used for TermFileEntries only.
		 * @param val
		 * @return
		 */
		public Builder setDescription(String val) {  
			description = val;
			processEntryItem(val, EntryType.DESCRIPTION);
			return this;
		} 
		
		/**
		 * Sets optional parameter mime type. Used for TermFileEntries only.
		 * When an empty val is provided as argument, the method guess the mime type
		 * using the document name. When mime type cannot be recognized DEFAULT_MIME_TYPE is returned.
		 * @param val
		 * @return
		 */
		public Builder setMimeType(String val) {
			if (val.length()==0){
				mime = DefaultMIMETypes.guessMIMEType(uri.getDocName(),false);
			} else {
				mime = val;
			}
			return this;
		}

		public InputEntry build() {
			return new InputEntry(this);
		}

		/**
		 * Process doc name, title, tags 
		 * @param t
		 */
		private void processEntryItem(String e, EntryType t) {
			switch (t) {
			case DOC_NAME:
				if (lastPosition == null) {
					lastPosition = 1;
				}
				if (mime==null) {
					mime = DefaultMIMETypes.guessMIMEType(uri.getDocName(),false);
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
			case DESCRIPTION:
				lastPosition = 5001;
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

		public void incrementTotalWords() {
			totalWords++;
		}

		/**
		 * Creates a tpe for words that are not stop words
		 * @param word
		 * @param position
		 * @throws java.lang.Exception
		 */
		private void addWord(String word, int position) {
			if (inputType == TermEntry.EntryType.PAGE) {
				TermPageEntry tp = getPageEntry(word);
				tp.putPosition(position);
			} else if (inputType == TermEntry.EntryType.FILE) {
				TermFileEntry tp = getFileEntry(word);
				tp.putPosition(position);
			}	
		}

		/**
		 * Gets the correspondent TermPageEntry or makes a new one for this term 
		 * @param word
		 * @return
		 */
		private TermPageEntry getPageEntry(String word) {
			TermPageEntry tp = (TermPageEntry) tpes.get(word);
			if (tp == null) {
				tp = new TermPageEntry(word, 0, uri, title, null);
				tpes.put(word, tp);
			}
			return tp;
		}

		/**
		 * Gets the correspondent TermFileEntry or makes a new one for this term 
		 * @param word
		 * @return
		 */
		private TermFileEntry getFileEntry(String word) {
			TermFileEntry tp = (TermFileEntry) tpes.get(word);
			if (tp == null) {
				tp = new TermFileEntry(word, 0, uri, mime, description, null);
				tpes.put(word, tp);
			}
			return tp;
		}
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

	public TermEntry.EntryType getInputType() {
		return inputType;
	}

	public int getTotalWords() {
		return totalWords;
	}

	public Collection<TermEntry> getTpe() {
		return tpes.values();
	}





}
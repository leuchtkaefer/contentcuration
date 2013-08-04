/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package plugins.ccuration.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import freenet.keys.FreenetURI;
import freenet.support.SortedIntSet;

/**
** A {@link TermEntry} that associates a subject term with a final target
** {@link FreenetURI} that satisfies the term.
** 
** NOTE: This is different to the code in Library! Most notably it is modifiable.
** 
** @author infinity0
*/
public class TermPageEntry extends TermEntry {

	/**
	** URI of the target
	*/
	final public FreenetURI page;

	//TODO leuchtkaefer remove things that i don't use. Positions are not relevant
	/** Positions where the term occurs. May be null if we don't have that data. */
	private SortedIntSet positions;
	
	/**
	** Map from positions in the text to a fragment of text around where it occurs.
	** Only non-null if we have the fragments of text (we may have positions but not details), 
	** to save memory.
	*/
	private Map<Integer, String> posFragments;

	/*
	private FreenetURI insertURIOwner;
	private FreenetURI requestURIOwner;
	 */
	
	public String title;

	/**
	** Standard constructor.
	**
	** @param s Subject of the entry
	** @param r Relevance of the entry
	** @param u {@link FreenetURI} of the page
	** @param p Map of positions (where the term appears) to context (fragment
	**          surrounding it).
	*/
	public TermPageEntry(String s, float r, FreenetURI u, Map<Integer, String> p) {
		this(s, r, u, (String)null, p);
	}

	/*	
	public TermPageEntry(InputEntry input) {
		this(input.getTermClassif(),0,input.getUri(),(String)null,null);
		this.insertURIOwner = input.privKey;
		this.requestURIOwner = input.pubKey;
	}
*/
	
	/*
	public FreenetURI getInsertURIOwner() {
		return insertURIOwner;
	}

	public FreenetURI getRequestURIOwner() {
		return requestURIOwner;
	}
*/
	//TODO leuchtkaefer remove compatibility with old indexes
	/**
	** Extended constructor with additional {@code title} field for old-style
	** indexes.
	**
	** @param s Subject of the entry
	** @param r Relevance of the entry
	** @param u {@link FreenetURI} of the page
	** @param t Title or description of the page
	** @param p Map of positions (where the term appears) to context (fragment
	**          surrounding it).
	*/
	public TermPageEntry(String s, float r, FreenetURI u, String t, Map<Integer, String> p) {
		super(s, r);
		if (u == null) {
			throw new IllegalArgumentException("can't have a null page");
		}
		page = u.intern(); // OPT LOW make the translator use the same URI object as from the URI table?
		title = t == null ? null : t.intern();
		if(p == null) {
			posFragments = null;
			positions = null;
		} else {
			posFragments = p;
			int[] pos = new int[p.size()];
			int x = 0;
			for(Integer i : p.keySet())
				pos[x++] = i;
			Arrays.sort(pos);
			positions = new SortedIntSet(pos);
		}
	}

	/**
	** For serialisation.
	*/
	public TermPageEntry(String s, float r, FreenetURI u, SortedIntSet pos, Map<Integer, String> frags) {
		super(s, r);
		if (u == null) {
			throw new IllegalArgumentException("can't have a null page");
		}
		page = u.intern(); // OPT LOW make the translator use the same URI object as from the URI table?
		title = null;
		this.positions = pos;
		this.posFragments = frags;
	}

	/*========================================================================
	  abstract public class TermEntry
	 ========================================================================*/

	@Override public EntryType entryType() {
		assert(getClass() == TermPageEntry.class);
		return EntryType.PAGE;
	}

	// we discount the "pos" field as there is no simple way to compare a map.
	// this case should never crop up anyway.
	@Override public int compareTo(TermEntry o) {
		int a = super.compareTo(o);
		if (a != 0) { return a; }
		// OPT NORM make a more efficient way of comparing these
		return page.toString().compareTo(((TermPageEntry)o).page.toString());
	}

	@Override public boolean equals(Object o) {
		return o == this || super.equals(o) && page.equals(((TermPageEntry)o).page);
	}

	@Override public boolean equalsTarget(TermEntry entry) {
		return entry == this || (entry instanceof TermPageEntry) && page.equals(((TermPageEntry)entry).page);
	}

	@Override public int hashCode() {
		return super.hashCode() ^ page.hashCode();
	}
	
	public int sizeEstimate() {
		int s = 0;
		s += page.toString().length();
		s += (title==null)?0:title.length();
		s += (subj==null)?0:subj.length();
		s += (positions==null)?0:positions.size() * 4;
		return s;
	}

	/** Do we have term positions? Just because we do doesn't necessarily mean we have fragments. */
	public boolean hasPositions() {
		return positions != null;
	}

	/** Get the positions to fragments map. If we don't have fragments, create this from the positions list. */
	public Map<Integer, String> positionsMap() {
		if(positions == null) return null;
		if(posFragments != null) return posFragments;
		HashMap<Integer, String> ret = new HashMap<Integer, String>(positions.size());
		int[] array = positions.toArrayRaw();
		for(int x : array)
			ret.put(x, null);
		return ret;
	}

	public boolean hasPosition(int i) {
		return positions.contains(i);
	}

	public ArrayList<Integer> positions() {
		int[] array = positions.toArrayRaw();
		ArrayList<Integer> pos = new ArrayList<Integer>(array.length);
		for(int x : array)
			pos.add(x);
		return pos;
	}

	public int[] positionsRaw() {
		return positions.toArrayRaw();
	}

	public int positionsSize() {
		if(positions == null) return 0;
		return positions.size();
	}

	public boolean hasFragments() {
		return posFragments != null;
	}

	public void putPosition(int position) {
		if(positions == null) positions = new SortedIntSet();
		positions.add(position);
		if(posFragments != null)
			posFragments.put(position, null);
	}

}

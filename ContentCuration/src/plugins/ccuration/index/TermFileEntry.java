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
** @author leuchtkaefer
*/
public class TermFileEntry extends TermEntry {

	/**
	** URI of the target
	*/
	final public FreenetURI file;

	/** Positions where the term occurs. May be null if we don't have that data. */
	private SortedIntSet positions;
	
	/**
	** Map from positions in the text to a fragment of text around where it occurs.
	** Only non-null if we have the fragments of text (we may have positions but not details), 
	** to save memory.
	*/
	private Map<Integer, String> posFragments;
	
	public final String mimeType;
	public final String description;

	/**
	** Constructor
	**
	** @param s Subject of the entry
	** @param r Relevance of the entry
	** @param u {@link FreenetURI} of the page
	** @param m MIME type of the document
	** @param d Description of the document
	** @param p Map of positions (where the term appears) to context (fragment
	**          surrounding it).
	*/
	public TermFileEntry(String s, float r, FreenetURI u, String m, String d, Map<Integer, String> p) {
		super(s, r);
		if (u == null) {
			throw new IllegalArgumentException("can't have a null uri");
		}
		file = u.intern(); // OPT LOW make the translator use the same URI object as from the URI table?
		mimeType = m == null ? null : m.intern();
		description = d == null ? null : d.intern();
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


	/*========================================================================
	  abstract public class TermEntry
	 ========================================================================*/

	@Override public EntryType entryType() {
		assert(getClass() == TermFileEntry.class);
		return EntryType.FILE;
	}

	// we discount the "pos" field as there is no simple way to compare a map.
	// this case should never crop up anyway.
	@Override public int compareTo(TermEntry o) {
		int a = super.compareTo(o);
		if (a != 0) { return a; }
		// OPT NORM make a more efficient way of comparing these
		return file.toString().compareTo(((TermFileEntry)o).file.toString());
	}

	//TODO leuchtkaefer add new field members
	@Override public boolean equals(Object o) {
		return o == this || super.equals(o) && file.equals(((TermFileEntry)o).file);
	}

	//TODO leuchtkaefer add new field members
	@Override public boolean equalsTarget(TermEntry entry) {
		return entry == this || (entry instanceof TermFileEntry) && file.equals(((TermFileEntry)entry).file);
	}

	//TODO leuchtkaefer add new field members
	@Override public int hashCode() {
		return super.hashCode() ^ file.hashCode();
	}
	
	public int sizeEstimate() {
		int s = 0;
		s += file.toString().length();
		s += ((mimeType)==null)?0:mimeType.length();
		s += (subj==null)?0:subj.length();
		s += (description==null)?0:description.length();
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
		if(posFragments != null)  //we don't use text fragments
			posFragments.put(position, null);
	}

}

package plugins.ccuration.index;

/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */


import java.util.Map;

import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
** Reads and writes {@link TermEntry}s in binary form, for performance.
**
** @author infinity0
*/
public class TermEntryWriter {

	final private static TermEntryWriter instance = new TermEntryWriter();

	protected TermEntryWriter() {}

	public static TermEntryWriter getInstance() {
		return instance;
	}

	/*@Override**/ public void writeObject(TermEntry en, OutputStream os) throws IOException {
		writeObject(en, new DataOutputStream(os));
	}
	
	public void writeObject(TermEntry en, DataOutputStream dos) throws IOException {
		dos.writeLong(TermEntry.serialVersionUID);
		System.out.println("Paso 1");
		TermEntry.EntryType type = en.entryType();
		System.out.println("Paso 2");
		dos.writeInt(type.ordinal());
		System.out.println("Paso 3");
		dos.writeUTF(en.subj);
		System.out.println("Paso 4");
		dos.writeFloat(en.rel);
		System.out.println("Paso 5");
		switch (type) {
		case PAGE:
			System.out.println("Paso 6");
			TermPageEntry enn = (TermPageEntry)en;
			System.out.println("Paso 7");
			System.out.println("enn.page "+ enn.page);
			enn.page.writeFullBinaryKeyWithLength(dos);
			System.out.println("Paso 8");
			int size = enn.hasPositions() ? enn.positionsSize() : 0;
			System.out.println("Paso 9");
			if(enn.title == null) {
				System.out.println("Paso 10");
				dos.writeInt(size);
				System.out.println("Paso 11");
			}
			else {
				System.out.println("Paso 12");
				dos.writeInt(~size); // invert bits to signify title is set
				System.out.println("Paso 13");
				dos.writeUTF(enn.title);
				System.out.println("Paso 14");
			}
			if(size != 0) {
				System.out.println("Paso 15");
				if(enn.hasFragments()) {
					System.out.println("Paso 16");
					for(Map.Entry<Integer, String> p : enn.positionsMap().entrySet()) {
						System.out.println("Paso 17");
						dos.writeInt(p.getKey());
						System.out.println("Paso 18");
						if(p.getValue() == null) {
							System.out.println("Paso 19");
							dos.writeUTF("");
							System.out.println("Paso 20");
						}
						else {
							System.out.println("Paso 21");
							dos.writeUTF(p.getValue());
							System.out.println("Paso 22");
						}
					}
				} else {
					System.out.println("Paso 23");
					for(int x : enn.positionsRaw()) {
						System.out.println("Paso 24");
						dos.writeInt(x);
						System.out.println("Paso 25");
						dos.writeUTF("");
						System.out.println("Paso 26");
					}
				}
			}
			return;
		}
	}

}

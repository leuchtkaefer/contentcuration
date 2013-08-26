package plugins.ccuration;

import java.io.File;

public interface ContentCurationConstants {
	public static final String PLUGINS_LIBRARY_MAIN = "plugins.Library.Main";
	public static final String COMMAND = "command";
	public static final String PUSH_BUFFER = "pushBufferCur";
	public static final String INSERT_URI = "insertURI";
	public static final String HASH_PUBKEY = "hashPubKey";
	public static final String REQUEST_URI = "requestURI";
	public static final String CONTENT_CURATOR = "ContentCurator";
	
	enum EntryType {TITLE, DOC_NAME, CATEGORY, TAG};
}

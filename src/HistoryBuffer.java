// for now just caching the past N values

import java.util.*;

public class HistoryBuffer {
	public LinkedList<HistoryBufferEntry> linkedlist;
	private int maxBufferSize;

	HistoryBuffer(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
		linkedlist = new LinkedList<HistoryBufferEntry>();
	}

	public void add(HistoryBufferEntry h) {
		linkedlist.addFirst(h);
		if (linkedlist.size() > maxBufferSize) {
			linkedlist.removeLast();
		}
	}

}

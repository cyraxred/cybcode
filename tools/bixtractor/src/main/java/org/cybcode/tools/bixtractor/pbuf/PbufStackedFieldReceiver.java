package org.cybcode.tools.bixtractor.pbuf;

import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.BiXourceLink;

class PbufStackedFieldReceiver implements PbufFieldReceiver
{
	private final BiXourceLink receiver;
	private PbufStackedFieldReceiver next;
	
	public PbufStackedFieldReceiver(BiXourceLink receiver)
	{
		this.receiver = receiver;
	}

	public void merge(PbufStackedFieldReceiver prev)
	{
		if (prev == null) return;
		if (this.next != null) throw new IllegalStateException();
		this.next = prev;
	}

	@Override public boolean pushFieldValue(XecutionContext context, PbufFieldValue value)
	{
		PbufStackedFieldReceiver current = this;
		do {
			if (receiver.pushAndEvaluate(context, value)) return true;
			current = current.next;
		} while (current != null);
		
		return false;
	}
}
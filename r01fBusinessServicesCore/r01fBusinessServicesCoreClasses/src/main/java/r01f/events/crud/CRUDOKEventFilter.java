package r01f.events.crud;

import r01f.events.COREServiceMethodExecEvents.COREServiceMethodExecOKEvent;

public interface CRUDOKEventFilter {
	public boolean hasTobeHandled(COREServiceMethodExecOKEvent opEvent);
}

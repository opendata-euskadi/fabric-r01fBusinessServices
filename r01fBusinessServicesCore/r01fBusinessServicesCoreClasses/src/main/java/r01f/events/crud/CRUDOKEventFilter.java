package r01f.events.crud;

import r01f.events.COREServiceMethodCallEvents.COREServiceMethodCallOKEvent;

public interface CRUDOKEventFilter {
	public boolean hasTobeHandled(COREServiceMethodCallOKEvent opEvent);
}

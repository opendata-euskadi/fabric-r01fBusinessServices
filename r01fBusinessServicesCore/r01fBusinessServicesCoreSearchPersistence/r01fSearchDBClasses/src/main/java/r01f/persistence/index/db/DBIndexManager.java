package r01f.persistence.search.db;

import javax.inject.Inject;

import lombok.experimental.Accessors;
import r01f.persistence.index.IndexManager;
import r01f.securitycontext.SecurityContext;

/**
 * Base DB index manager 
 */
@Accessors(prefix="_")
public final class DBIndexManager 
		implements IndexManager {
/////////////////////////////////////////////////////////////////////////////////////////
//  FINAL STATUS
/////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////////
//  INJECTED CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public DBIndexManager() {
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX CONTROL METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void open(final SecurityContext securityContext) {
		// do nothing
	}
	@Override
	public void close(final SecurityContext securityContext) {
		// do nothing
	}
	@Override
	public void optimize(final SecurityContext securityContext) {
		// do nothing
	}
	@Override
	public void truncate(final SecurityContext securityContext) {
		// do nothing
	}
}

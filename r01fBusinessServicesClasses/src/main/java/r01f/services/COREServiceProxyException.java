package r01f.services;

import lombok.experimental.Accessors;


/**
 * Exception thrown by the proxies because the server layer could not be reached or 
 * because the server response is not valid
 */
@Accessors(prefix="_")
public class COREServiceProxyException 
     extends RuntimeException {

	private static final long serialVersionUID = -4968119097697717368L;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public COREServiceProxyException() {
		super();
	}
	public COREServiceProxyException(final String msg) {
		super(msg);
	}
	public COREServiceProxyException(final Throwable otherEx) {
		super(otherEx);
	}
	public COREServiceProxyException(final String msg,
								 final Throwable otherEx) {
		super(msg,
			  otherEx);
	}
}

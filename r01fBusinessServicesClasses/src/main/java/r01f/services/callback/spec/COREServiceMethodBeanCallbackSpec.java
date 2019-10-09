package r01f.services.callback.spec;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.objectstreamer.annotations.MarshallField;
import r01f.objectstreamer.annotations.MarshallType;
import r01f.services.callback.COREMethodCallback;

@MarshallType(as="beanCallbackSpec")
@Accessors(prefix="_")
public class COREServiceMethodBeanCallbackSpec
     extends COREServiceMethodCallbackSpecBase {

	private static final long serialVersionUID = -169301594778012524L;
/////////////////////////////////////////////////////////////////////////////////////////
// 	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@MarshallField(as="implType")
	@Getter @Setter private Class<? extends COREMethodCallback> _implType;
/////////////////////////////////////////////////////////////////////////////////////////
// 	
/////////////////////////////////////////////////////////////////////////////////////////
	public COREServiceMethodBeanCallbackSpec() {
		// default no-args constructor
	}
	public COREServiceMethodBeanCallbackSpec(final Class<? extends COREMethodCallback> callbackType) {
		_implType = callbackType;
	}
}

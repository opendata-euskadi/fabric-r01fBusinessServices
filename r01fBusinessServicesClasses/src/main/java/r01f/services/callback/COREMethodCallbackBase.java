package r01f.services.callback;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.model.services.COREServiceMethodExecResult;

@ConvertToDirtyStateTrackable
@Accessors(prefix="_")
@NoArgsConstructor
public abstract class COREMethodCallbackBase 
  		   implements COREMethodCallback,
  		   			  Serializable {
	
	private static final long serialVersionUID = -4720498929631480441L;
/////////////////////////////////////////////////////////////////////////////////////////
// 	
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter @Setter protected COREServiceMethodExecResult<?> _COREServiceMethodExecResult;
	
/////////////////////////////////////////////////////////////////////////////////////////
// 	ACCESSOR METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean hasSucceeded() {
		return _COREServiceMethodExecResult.hasSucceeded();
	}
	@Override
	public boolean hasFailed() {
		return _COREServiceMethodExecResult.hasFailed();
	}
}

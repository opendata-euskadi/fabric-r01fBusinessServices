package r01f.test.persistence;

import r01f.guids.PersistableObjectOID;
import r01f.model.ModelObjectRef;
import r01f.model.PersistableModelObject;

public interface ManagesTestMockDependentModelObjsLifeCycle<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
													 		P extends PersistableModelObject<?>> 
         extends ManagesTestMockModelObjsLifeCycle<O,M> {
	public P getParentModelObject();
	public <PR extends ModelObjectRef<P>> PR getParentModelObjectRef();
}

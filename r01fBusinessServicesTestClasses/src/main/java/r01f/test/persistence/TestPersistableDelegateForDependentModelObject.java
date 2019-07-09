package r01f.test.persistence;

import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectFindServices;

/**
 * JVM arguments:
 * -javaagent:D:/develop/local_libs/aspectj/lib/aspectjweaver.jar -Daj.weaving.verbose=true
 */
public abstract class TestPersistableDelegateForDependentModelObject<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
							  		 				 		  		 P extends PersistableModelObject<?>> 
			  extends TestPersistableModelObjectBase<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	protected TestPersistableDelegateForDependentModelObject(final ManagesTestMockDependentModelObjsLifeCycle<O,M,P> managesTestMockObjects,
					  								  		 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudApi,final ClientAPIDelegateForModelObjectFindServices<O,M> findApi) {
		super(managesTestMockObjects,
			  crudApi,findApi);
	}
}

package r01f.test.persistence;

import java.util.Collection;

import r01f.debug.Debuggable;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.patterns.Factory;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;

public interface ManagesTestMockModelObjsLifeCycle<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
		 extends Debuggable {
	/**
	 * @return the managed model object type
	 */
	public Class<M> getModelObjType();
	/**
	 * @return the crud api
	 */
	public ClientAPIDelegateForModelObjectCRUDServices<O,M> getCRUDApi();
	/**
	 * @return the factory of mock objets
	 */
	public Factory<? extends M> getMockObjectsFactory();
	/**
	 * Creates a number of model objects using the provided factory
	 * @param numOfObjectsToCreate
	 * @return the created objects
	 */
	public Collection<M> setUpMockObjs(final int numOfObjsToCreate);
	/**
	 * Creates a single model object using the provided factory
	 * @return the created obj
	 */
	public M setUpSingleMockObj();
	/**
	 * Deletes a {@link Collection} of previously created objects
	 * @param createdObjs
	 */
	public void tearDownCreatedMockObjs();
	/**
	 * Reset the state removing the stored created mock objs oids
	 */
	public void reset();

	/**
	 * Returns {@link Collection} of the oids of the created model objects after calling {@link #setUpMockObjs(int)}
	 * @return
	 */
	public O getAnyCreatedMockObjOid();
	/**
	 * Returns {@link Collection} of the oids of the created model objects after calling {@link #setUpMockObjs(int)}
	 * @return
	 */
	public O getAnyCreatedMockObjOidOrCreateNew();
	/**
	 * @return a {@link Collection} of the created model objects after calling {@link #setUpMockObjs(int)}
	 */
	public M getAnyCreatedMockObj();
	/**
	 * @return a {@link Collection} of the created model objects after calling {@link #setUpMockObjs(int)}
	 */
	public M getAnyCreatedMockObjOrCreateNew();
	/**
	 * @return the created model objects after calling after calling {@link #setUpMockObjs(int)}
	 */
	public Collection<M> getCreatedMockObjs();
	/**
	 * @return the oids of the created model objects
	 */
	public Collection<O> getCreatedMockObjsOids();
}

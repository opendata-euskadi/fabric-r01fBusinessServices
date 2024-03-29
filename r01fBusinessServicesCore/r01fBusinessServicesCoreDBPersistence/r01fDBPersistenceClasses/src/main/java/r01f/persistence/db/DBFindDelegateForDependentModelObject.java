package r01f.persistence.db;

import javax.persistence.Tuple;

import com.google.common.base.Function;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.PersistableObjectOID;
import r01f.locale.Language;
import r01f.model.PersistableModelObject;
import r01f.model.SummarizedModelObject;
import r01f.model.persistence.FindOIDsResult;
import r01f.model.persistence.FindResult;
import r01f.model.persistence.FindSummariesResult;
import r01f.persistence.db.DBFindForModelObjectBase.QueryDBEntityWrapper;
import r01f.persistence.db.DBFindForModelObjectBase.QueryWrapper;
import r01f.persistence.db.entities.DBEntityForModelObject;
import r01f.persistence.db.entities.annotations.DBEntitySummaryField;
import r01f.persistence.db.entities.annotations.ParentOidDBEntityField;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.reflection.ReflectionUtils;
import r01f.reflection.ReflectionUtils.FieldAnnotated;
import r01f.securitycontext.SecurityContext;
import r01f.util.types.collections.CollectionUtils;

/**
 * Base type for every persistence layer type
 * @param <O>
 * @param <M>
 * @param <PK>
 * @param <DB>
 */
@Slf4j
@Accessors(prefix="_")
public abstract class DBFindDelegateForDependentModelObject<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
															PO extends PersistableObjectOID,P extends PersistableModelObject<PO>,
							     			   				PK extends DBPrimaryKeyForModelObject,DB extends DBEntity & DBEntityForModelObject<PK>>
	       implements DBFindForDependentModelObject<O,M,PO> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected final Class<? extends PersistableModelObject<PO>> _parentObjType;
	@Getter protected final DBFindForModelObjectBase<O,M,PK,DB> _dbFind;

			protected final Function<Tuple,? extends SummarizedModelObject<M>> _dbTupleToSummarizedObj;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBFindDelegateForDependentModelObject(final Class<P> parentModelObjType,
												 final DBFindForModelObjectBase<O,M,PK,DB> dbFind) {
		this(parentModelObjType,
			 dbFind,
			 null);		// not using db tuple to summarized obj
	}
	public DBFindDelegateForDependentModelObject(final Class<P> parentModelObjType,
												 final DBFindForModelObjectBase<O,M,PK,DB> dbFind,
												 final Function<Tuple,? extends SummarizedModelObject<M>> dbTupleToSummarizedObj) {
		_parentObjType = parentModelObjType;
		_dbFind = dbFind;
		_dbTupleToSummarizedObj = dbTupleToSummarizedObj;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings({ "unchecked","rawtypes" })
	public FindOIDsResult<O> findOidsOfDependentsOf(final SecurityContext securityContext,
											  		final PO parentOid) {
		QueryWrapper qry = _dbFind.new QueryWrapper();
		qry.addFilterByOidPredicate(parentOid,_getParentOidDBEntityFieldName());
		return qry.findOidsUsing(securityContext);
	}
	@Override @SuppressWarnings({ "unchecked","rawtypes" })
	public FindResult<M> findDependentsOf(final SecurityContext securityContext,
										  final PO parentOid) {
		QueryDBEntityWrapper qry = _dbFind.new QueryDBEntityWrapper();
		qry.addFilterByOidPredicate(parentOid,_getParentOidDBEntityFieldName());
		return qry.findUsing(securityContext);
	}
	@Override @SuppressWarnings({ "unchecked","rawtypes" })
	public FindSummariesResult<M> findSummariesOfDependentsOf(final SecurityContext securityContext,
															  final PO parentOid,
															  final Language lang) {
		if (_dbTupleToSummarizedObj == null) throw new IllegalStateException("A [db tuple] to [summarized object] is needed; otherwise override this function!");
		QueryWrapper qry = _dbFind.new QueryWrapper(_summaryDBRowCols())
											.addFilterByOidPredicate(parentOid,_getParentOidDBEntityFieldName());
		return qry.findSummariesUsing(securityContext)
				  .convertingTuplesUsing(_dbTupleToSummarizedObj);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ABSTRACT METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the name of the {@link DBEntity} field that acts as parent {@link DBEntity} reference
	 * @return
	 */
	protected String _getParentOidDBEntityFieldName() {
		// use reflection to find the field annotated with @ParentOidDBEntityField
		FieldAnnotated<ParentOidDBEntityField>[] fields = ReflectionUtils.fieldsAnnotated(_dbFind.getDBEntityType(),
																						  ParentOidDBEntityField.class);
		if (CollectionUtils.isNullOrEmpty(fields)) throw new IllegalStateException("The db entity " + _dbFind.getDBEntityType() + " DOES NOT have a field annotated with @" + ParentOidDBEntityField.class.getSimpleName() + " that stores the parent entity's oid");
		if (fields.length > 1) throw new IllegalStateException("The db entity " + _dbFind.getDBEntityType() + " HAS MORE THAN A SINGLE FIELD annotated with @" + ParentOidDBEntityField.class.getSimpleName() + " that stores the parent entity's oid");

		return fields[0].getField().getName();
	}
	/**
	 * Returns the cols to be returned when returning a summarized object
	 * @return
	 */
	protected String[] _summaryDBRowCols() {
		// use reflection to find the fields annotated with @ParentOidDBEntityField
		FieldAnnotated<ParentOidDBEntityField>[] parentFields = ReflectionUtils.fieldsAnnotated(_dbFind.getDBEntityType(),
																								ParentOidDBEntityField.class);

		// use reflection to find the fields annotated with @DBEntitySummaryField
		FieldAnnotated<DBEntitySummaryField>[] fields = ReflectionUtils.fieldsAnnotated(_dbFind.getDBEntityType(),
																						DBEntitySummaryField.class);
		if (CollectionUtils.isNullOrEmpty(fields)) log.warn("The db entity {} DOES NOT have any field annotated with @{} to be included at the SELECT clause when querying for summaries",
															_dbFind.getDBEntityType(),DBEntitySummaryField.class.getSimpleName());

		int colNum = (CollectionUtils.hasData(parentFields) ? parentFields.length : 0) +
				   	 fields.length;
		String[] outCols = new String[colNum];
		int i=0;

		// the cols for the fields annotated with @DBEntitySummaryField
		for (FieldAnnotated<DBEntitySummaryField> field : fields) {
			outCols[i] = field.getField().getName();
			i++;
		}
		// the cols for the fields annotated with @ParentOidDBEntityField
		if (CollectionUtils.hasData(parentFields)) {
			for (FieldAnnotated<ParentOidDBEntityField> parentField : parentFields) {
				outCols[i] = parentField.getField().getName();
				i++;
			}
		}
		return outCols;
	}
//	/**
//	 * Returns a function that transforms a db row to a summarized model object
//	 * @return
//	 */
//	protected abstract <S extends SummarizedModelObject<M>> Function<Tuple,S> _dbRowToSummarizedModelObjectTranformFunction(SecurityContext securityContext);
}

package r01f.persistence.search.db;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.model.metadata.FieldID;
import r01f.model.metadata.HasMetaDataForHasOIDModelObject;
import r01f.model.metadata.TypeMetaDataForModelObjectBase;
import r01f.model.search.SearchFilter;
import r01f.model.search.query.BooleanQueryClause;
import r01f.model.search.query.BooleanQueryClause.QualifiedQueryClause;
import r01f.model.search.query.BooleanQueryClause.QueryClauseOccur;
import r01f.model.search.query.ContainedInQueryClause;
import r01f.model.search.query.ContainsTextQueryClause;
import r01f.model.search.query.EqualsQueryClause;
import r01f.model.search.query.NullQueryClause;
import r01f.model.search.query.QueryClause;
import r01f.model.search.query.RangeQueryClause;
import r01f.model.search.query.SearchResultsOrdering;
import r01f.patterns.FactoryFrom;
import r01f.persistence.db.DBEntity;
import r01f.persistence.db.config.DBModuleConfig;
import r01f.types.CanBeRepresentedAsString;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Translates a search filter to JPQL
 * @param <F>
 * @param <DB>
 */
@Slf4j
@Accessors(prefix="_")
public class DBSearchQueryToJPQLTranslator<F extends SearchFilter,
										   DB extends DBEntity> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final Class<DB> _dbEntityType;
	protected final DBModuleConfig _dbModuleConfig;
	protected final EntityManager _entityManager;		
	protected FactoryFrom<F,TranslatesIndexableFieldIDToDBEntityField> _translatesFieldToDBEntityFieldFactory;
	protected FactoryFrom<F,TranslatesSearchFilterClauseToJPQLWherePredicate> _translatesFilterClauseToJpqlPredicateFactory;
	protected FactoryFrom<Query,SetsJPQLWherePredicateParamFromSearchFilterClauseValue> _setsJpqlWherePredicateParamsFromFilterClauseValueFactory;
	
	@Getter public String _dbEntityAlias = "entity";
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBSearchQueryToJPQLTranslator(final Class<DB> dbEntityType,
						 				 final DBModuleConfig dbModuleConfig,
						 				 final EntityManager entityManager) {
		this(dbEntityType,
			 dbModuleConfig,
			 entityManager,
			 // field to db entity field translator
			 new FactoryFrom<F,TranslatesIndexableFieldIDToDBEntityField>() {
					@Override
					public TranslatesIndexableFieldIDToDBEntityField from(final F filter) {
						return new IndexableFieldIDToDBEntityFieldTranslatorByDefault<F>(filter);	// default translator
					}
			 });
	}
	public DBSearchQueryToJPQLTranslator(final Class<DB> dbEntityType,
						 				 final DBModuleConfig dbModuleConfig,
						 				 final EntityManager entityManager,
						 				 final FactoryFrom<F,TranslatesIndexableFieldIDToDBEntityField> indexableFieldToDBEntityFieldTranslatorFactory) {
		this(dbEntityType,
			 dbModuleConfig,
			 entityManager,
			 indexableFieldToDBEntityFieldTranslatorFactory,	// fieldId to dbentity field translator 
			 null, 	// query clause to jpql predicate
			 null);	// set jpa query param from query clause
	}
	public DBSearchQueryToJPQLTranslator(final Class<DB> dbEntityType,
						 				 final DBModuleConfig dbModuleConfig,
						 				 final EntityManager entityManager,
						 				 final FactoryFrom<F,TranslatesIndexableFieldIDToDBEntityField> indexableFieldToDBEntityFieldTranslatorFactory,
						 				 final FactoryFrom<F,TranslatesSearchFilterClauseToJPQLWherePredicate> filterClauseToJpqlPredicateFactory) {
		this(dbEntityType,
			 dbModuleConfig,
			 entityManager,
			 indexableFieldToDBEntityFieldTranslatorFactory,	// fieldId to dbentity field translator 
			 filterClauseToJpqlPredicateFactory, 				// query clause to jpql predicate
			 null);	// set jpa query param from query clause
	}
	public DBSearchQueryToJPQLTranslator(final Class<DB> dbEntityType,
						 				 final DBModuleConfig dbModuleConfig,
						 				 final EntityManager entityManager,
						 				 final FactoryFrom<F,TranslatesIndexableFieldIDToDBEntityField> indexableFieldToDBEntityFieldTranslatorFactory,
						 				 final FactoryFrom<F,TranslatesSearchFilterClauseToJPQLWherePredicate> filterClauseToJpqlPredicateFactory,
						 				 final FactoryFrom<Query,SetsJPQLWherePredicateParamFromSearchFilterClauseValue> jpqlWherePredicateParamsFromFilterClauseValueFactory) {
		_dbEntityType = dbEntityType;
		_dbModuleConfig = dbModuleConfig;
		_entityManager = entityManager;
		_translatesFieldToDBEntityFieldFactory = indexableFieldToDBEntityFieldTranslatorFactory;
		_translatesFilterClauseToJpqlPredicateFactory = filterClauseToJpqlPredicateFactory;
		_setsJpqlWherePredicateParamsFromFilterClauseValueFactory = jpqlWherePredicateParamsFromFilterClauseValueFactory;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	                                                                          
/////////////////////////////////////////////////////////////////////////////////////////
	protected TranslatesIndexableFieldIDToDBEntityField _createTranslatesIndexableFieldIDToDBEntityField(final F filter) {
		return _translatesFieldToDBEntityFieldFactory != null
						? _createTranslatesIndexableFieldIDToDBEntityField(_translatesFieldToDBEntityFieldFactory,
																		   filter)
						: _createTranslatesIndexableFieldIDToDBEntityField(new FactoryFrom<F,TranslatesIndexableFieldIDToDBEntityField>() {
																					@Override
																					public TranslatesIndexableFieldIDToDBEntityField from(final F theFilter) {
																						return new IndexableFieldIDToDBEntityFieldTranslatorByDefault<F>(theFilter);	// default translator
																					}
																			},
																		    filter);
	}
	protected TranslatesIndexableFieldIDToDBEntityField _createTranslatesIndexableFieldIDToDBEntityField(final FactoryFrom<F,TranslatesIndexableFieldIDToDBEntityField> translatesFieldToDBEntityFieldFactory,
																										 final F filter) {
		if (translatesFieldToDBEntityFieldFactory == null) throw new IllegalArgumentException();
		return translatesFieldToDBEntityFieldFactory.from(filter);
	}
	protected TranslatesSearchFilterClauseToJPQLWherePredicate _createTranslatesSearchFilterClauseToJPQLWherePredicate(final F filter) {
		return _translatesFilterClauseToJpqlPredicateFactory != null 
					? _createTranslatesSearchFilterClauseToJPQLWherePredicate(_translatesFilterClauseToJpqlPredicateFactory,
																			  filter)
					: _createTranslatesSearchFilterClauseToJPQLWherePredicate(new FactoryFrom<F,TranslatesSearchFilterClauseToJPQLWherePredicate>() {	// default clause to jpql where predicate factory
																					@Override
																					public SearchFilterClauseToJPQLWherePredicate from(final F theFilter) {
																						return new SearchFilterClauseToJPQLWherePredicate(theFilter);
																					}
																		      },
																			  filter);
	}
	protected TranslatesSearchFilterClauseToJPQLWherePredicate _createTranslatesSearchFilterClauseToJPQLWherePredicate(final FactoryFrom<F,TranslatesSearchFilterClauseToJPQLWherePredicate> translatesFilterClauseToJpqlPredicateFactory,
																													   final F filter) {
		if (translatesFilterClauseToJpqlPredicateFactory == null) throw new IllegalArgumentException();
		return translatesFilterClauseToJpqlPredicateFactory.from(filter);
	}
	protected SetsJPQLWherePredicateParamFromSearchFilterClauseValue _createSetsJPQLWherePredicateParamFromSearchFilterClauseValue(final Query qry) {
		return _setsJpqlWherePredicateParamsFromFilterClauseValueFactory != null
						? _createSetsJPQLWherePredicateParamFromSearchFilterClauseValue(_setsJpqlWherePredicateParamsFromFilterClauseValueFactory,
																						qry)
						: _createSetsJPQLWherePredicateParamFromSearchFilterClauseValue(new FactoryFrom<Query,SetsJPQLWherePredicateParamFromSearchFilterClauseValue>() {
																								@Override
																								public SetsJPQLWherePredicateParamFromSearchFilterClauseValue from(final Query query) {
																									return new SearchFilterClauseValueToJPQLWherePredicateParam(query);
																								}
																						},	// default
																						qry);
	}
	protected static SetsJPQLWherePredicateParamFromSearchFilterClauseValue _createSetsJPQLWherePredicateParamFromSearchFilterClauseValue(final FactoryFrom<Query,SetsJPQLWherePredicateParamFromSearchFilterClauseValue> setsJpqlWherePredicateParamsFromFilterClauseValueFactory,
																																   		  final Query qry) {
		if (setsJpqlWherePredicateParamsFromFilterClauseValueFactory == null) throw new IllegalArgumentException();
		return setsJpqlWherePredicateParamsFromFilterClauseValueFactory.from(qry);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  META-DATA TO DB COLUMN TRANSLATION
/////////////////////////////////////////////////////////////////////////////////////////
	protected static String _dbEntityFieldNameForOid(final TranslatesIndexableFieldIDToDBEntityField translatesFieldToDBEntityField) {
		FieldID fieldId = FieldID.from(HasMetaDataForHasOIDModelObject.SEARCHABLE_METADATA.OID);
		return translatesFieldToDBEntityField.dbEntityFieldNameFor(fieldId);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  JPQL QUERY COMPOSING
/////////////////////////////////////////////////////////////////////////////////////////
	public String composeCountJPQL(final F filter) {
		TranslatesIndexableFieldIDToDBEntityField translatesFieldToDBEntityField = _createTranslatesIndexableFieldIDToDBEntityField(filter);
		String jpql = _composeJPQL("COUNT(" + _dbEntityAlias + ")",	// count query
								   filter,null,		// no ordering color
								   translatesFieldToDBEntityField);
		return jpql;
	}
	public String composeRetrieveJPQL(final F filter,final Collection<SearchResultsOrdering> ordering) {
		TranslatesIndexableFieldIDToDBEntityField translatesFieldToDBEntityField = _createTranslatesIndexableFieldIDToDBEntityField(filter);
		String jpql = _composeJPQL(_dbEntityAlias,	// not a count query
						    	   filter,ordering,
						    	   translatesFieldToDBEntityField);
		return jpql;
	}
	public String composeRetrieveOidsJPQL(final F filter) {
		TranslatesIndexableFieldIDToDBEntityField translatesFieldToDBEntityField = _createTranslatesIndexableFieldIDToDBEntityField(filter);
		String oidDBEntityField = _dbEntityFieldNameForOid(translatesFieldToDBEntityField);
		String jpql = _composeJPQL(Strings.customized(_dbEntityAlias+".{}",
											   		  oidDBEntityField),
								   			   		  filter,null,	// no ordering
								   			   		  translatesFieldToDBEntityField);
		return jpql;
	}
	protected String _composeJPQL(final String colSpec,
							      final F filter,
							      final Collection<SearchResultsOrdering> ordering) {
		TranslatesIndexableFieldIDToDBEntityField translatesFieldToDBEntityField = _createTranslatesIndexableFieldIDToDBEntityField(filter);
		String jpql = _composeJPQL(colSpec,
								   filter,
								   ordering,
								   translatesFieldToDBEntityField);
		return jpql;
	}
	protected String _composeJPQL(final String colSpec,
							      final F filter,
							      final Collection<SearchResultsOrdering> ordering,
							      final TranslatesIndexableFieldIDToDBEntityField translatesFieldToDBEntityField) {
		// [0] - SELECT
		StringBuilder jpql = new StringBuilder(Strings.customized("SELECT {} " +
										  		  					"FROM {} " + _dbEntityAlias + " ",
										  		  			      colSpec,		// "COUNT(entity)" : "entity",
										  		  			      _dbEntityTypeNameFor(filter)));
		// [1] - WHERE
		TranslatesSearchFilterClauseToJPQLWherePredicate filterClauseToJpqlPredicate = _createTranslatesSearchFilterClauseToJPQLWherePredicate(filter);
		String jpqlWhere = _composeWhereJpqlPredicates(filter,
													   filterClauseToJpqlPredicate);
		if (Strings.isNOTNullOrEmpty(jpqlWhere)) {
			jpql.append("WHERE ");
			jpql.append(jpqlWhere);
		}
		
		// [2] - ORDER
		String orderClause = _composeJpqlOrderByClause(filter,
													   ordering,
													   translatesFieldToDBEntityField);
		if (Strings.isNOTNullOrEmpty(orderClause)) {
			jpql.append(orderClause);
		}
		log.debug("JPQL: {}",jpql);
		
		return jpql.toString();
	}
	@SuppressWarnings("unused")
	protected String _dbEntityTypeNameFor(final F filter) {
		return _dbEntityType.getSimpleName();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  QUERY FILTER
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Creates a collection of JPQL clauses from the search query
	 * @param filter
	 * @param filterClauseToJpqlPredicate
	 * @return
	 */
    protected String _composeWhereJpqlPredicates(final F filter,
    											 final TranslatesSearchFilterClauseToJPQLWherePredicate filterClauseToJpqlPredicate) {
		BooleanQueryClause qryClause = filter != null ? filter.getBooleanQuery()
													  : null;
		if (qryClause == null || CollectionUtils.isNullOrEmpty(qryClause.getClauses())) {
			log.warn("A filter with NO filter parameters was received... al records will be returned");
			return null;
		}
		
		Set<QualifiedQueryClause<? extends QueryClause>> clauses = qryClause.getClauses();
		
		// recursively create the predicates
		StringBuilder outJPQL = new StringBuilder();
		_recurseComposeWhereJpqlPredicates(filterClauseToJpqlPredicate,
										   clauses,
										   outJPQL);
		// return
		return outJPQL.length() > 0 ? outJPQL.insert(0,"(")
											 .append(")").toString()
									: null;
    }
    private void _recurseComposeWhereJpqlPredicates(final TranslatesSearchFilterClauseToJPQLWherePredicate filterClauseToJpqlPredicate,
    											    final Set<QualifiedQueryClause<? extends QueryClause>> clauses,
    											    final StringBuilder outJPQL) {
		QueryClauseOccur prevClauseOccur = null;
		for (Iterator<QualifiedQueryClause<? extends QueryClause>> clauseIt = clauses.iterator(); clauseIt.hasNext(); ) {
			QualifiedQueryClause<? extends QueryClause> clause = clauseIt.next();
			
			// If it's a BooleanQueryClause: recurse!
			if (clause.getClause() instanceof BooleanQueryClause) {
				String jpqlJoin = _jpqlJoinFor(clause.getOccur());
				if (prevClauseOccur != null) {
					outJPQL.append(jpqlJoin);
				} else {
					String jpqlPrevOp = _jpqlPrevOpFor(clause.getOccur());
					outJPQL.append(jpqlPrevOp);
				}
				if (clauseIt.hasNext()) prevClauseOccur = clause.getOccur();
				outJPQL.append("(");
				
				BooleanQueryClause boolClause = (BooleanQueryClause)clause.getClause();
				Set<QualifiedQueryClause<? extends QueryClause>> otherClauses = boolClause.getClauses();
				_recurseComposeWhereJpqlPredicates(filterClauseToJpqlPredicate,
												   otherClauses, 
												   outJPQL);			// BEWARE!!! recursion
				
				outJPQL.append(")");
				continue;
			} 
			
			// some indexable fields are NOT supported when the search engine 
			// is DB based
			boolean isDBEntitySupportedField = _isSupportedDBEntityField(clause.getClause());
			if (!isDBEntitySupportedField)  continue;

			String jpqlQuery = filterClauseToJpqlPredicate.wherePredicateFrom(clause.getClause());
			if (jpqlQuery == null) {
				log.error("A null query clause was returned for field id={}",
						  clause.getClause().getFieldId());
				continue;
			}
			
			String jpqlJoin = _jpqlJoinFor(clause.getOccur());
			if (prevClauseOccur != null) {
				outJPQL.append(jpqlJoin);
			} else {
				String jpqlPrevOp = _jpqlPrevOpFor(clause.getOccur());
				outJPQL.append(jpqlPrevOp);
			}
			if (clauseIt.hasNext()) prevClauseOccur = clause.getOccur();
			
			outJPQL.append("(");
			outJPQL.append(jpqlQuery);	// The clause
			outJPQL.append(")");
		}		
    }
	protected static String _jpqlJoinFor(final QueryClauseOccur occur) {
		return DBSearchJPQUtil.jpqlJoinFor(occur);
	}
	protected static String _jpqlPrevOpFor(final QueryClauseOccur occur) {
		String outJPQL = null;
		switch(occur) {
		case MUST:
			outJPQL = "";
			break;
		case MUST_NOT:
			outJPQL = " NOT ";
			break;
		case SHOULD:
			outJPQL = "";
			break;
		default:
			throw new IllegalArgumentException();
		}
		return outJPQL;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FILTER CLAUSE TO JPQL WHERE PREDICATE
/////////////////////////////////////////////////////////////////////////////////////////
	protected class SearchFilterClauseToJPQLWherePredicate 
		 implements TranslatesSearchFilterClauseToJPQLWherePredicate {
		
		protected final F _filter;
		protected final TranslatesIndexableFieldIDToDBEntityField _translatesFieldToDBEntityField;
		
		protected SearchFilterClauseToJPQLWherePredicate(final F filter) {
			_filter = filter;
			_translatesFieldToDBEntityField = _createTranslatesIndexableFieldIDToDBEntityField(filter);
		}
		
		@Override
		public <Q extends QueryClause> String wherePredicateFrom(final Q clause) {
			if (clause == null) return null;
			
			String outJPQL = null;
			if (clause instanceof BooleanQueryClause) {
				outJPQL = this.wherePredicateFrom((BooleanQueryClause)clause);
			} 
			else if (clause instanceof EqualsQueryClause<?>) {
				outJPQL = this.wherePredicateFrom((EqualsQueryClause<?>)clause);
			} 
			else if (clause instanceof NullQueryClause) {
				outJPQL = this.wherePredicateFrom((NullQueryClause)clause);
			} 
			else if (clause instanceof ContainsTextQueryClause) {
				outJPQL = this.wherePredicateFrom((ContainsTextQueryClause)clause);
			} 
			else if (clause instanceof RangeQueryClause<?>) {
				outJPQL = this.wherePredicateFrom((RangeQueryClause<?>)clause);
			} 
			else if (clause instanceof ContainedInQueryClause<?>) {
				outJPQL = this.wherePredicateFrom((ContainedInQueryClause<?>)clause);
			}
			return outJPQL;
		}	
		@Override
		public String wherePredicateFrom(final EqualsQueryClause<?> eqQry) {
			if (eqQry == null || eqQry.getValue() == null) return null;
			
			String dbFieldId = _translatesFieldToDBEntityField.dbEntityFieldNameFor(eqQry.getFieldId()); 
			String outJPQL = Strings.customized(_dbEntityAlias + ".{} = :{}",
								    			dbFieldId,eqQry.getFieldId());
			return outJPQL;
		}
		@Override
		public String wherePredicateFrom(final ContainsTextQueryClause containsTextQry) {
			String dbFieldId = _translatesFieldToDBEntityField.dbEntityFieldNameFor(containsTextQry.getFieldId());
			String fullTextWherePredicate = DBSearchJPQUtil.fullTextWherePredicateFrom(_dbModuleConfig,_entityManager,
																			   		   _dbEntityAlias,dbFieldId,
																			   		   containsTextQry);
			return fullTextWherePredicate;
		}
		@Override
		public String wherePredicateFrom(final RangeQueryClause<?> rangeQry) {
			String dbFieldId = _translatesFieldToDBEntityField.dbEntityFieldNameFor(rangeQry.getFieldId()); 
			
			String outJPQL = null;
			// TODO mind the bound types... now only CLOSED (inclusive) bounds are being having into account 
			if (rangeQry.getRange().hasLowerBound() && rangeQry.getRange().hasUpperBound()) {
				outJPQL = Strings.customized(_dbEntityAlias+".{} BETWEEN :{}Start AND :{}End",		// SQL between is INCLUSIVE (>= lower and <= lower)
											 dbFieldId, rangeQry.getFieldId(),rangeQry.getFieldId());
			} else if (rangeQry.getRange().hasLowerBound()) {
				outJPQL = Strings.customized(_dbEntityAlias+".{} >= :{}",
											 dbFieldId, rangeQry.getFieldId());
			} else if (rangeQry.getRange().hasUpperBound()) {
				outJPQL = Strings.customized(_dbEntityAlias+".{} <= :{}",
											 dbFieldId, rangeQry.getFieldId());
			}
			return outJPQL;
		}
		@Override
		public String wherePredicateFrom(final ContainedInQueryClause<?> containedInQry) {
			String dbFieldId = _translatesFieldToDBEntityField.dbEntityFieldNameFor(containedInQry.getFieldId());
			String outJPQL = Strings.customized(_dbEntityAlias+".{} IN :{}",
												dbFieldId,containedInQry.getFieldId());
			return outJPQL;
		}
		@Override
		public String wherePredicateFrom(final NullQueryClause nullQry) {
			String dbFieldId = _translatesFieldToDBEntityField.dbEntityFieldNameFor(nullQry.getFieldId());
			String nullStr = "";
			if (!nullQry.isNull()) {
				nullStr = "NOT";
			}
			String outJPQL = Strings.customized(_dbEntityAlias+".{} IS {} NULL",
												dbFieldId,nullStr);
			return outJPQL;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  QUERY PARAMETERS
/////////////////////////////////////////////////////////////////////////////////////////    
	/**
	 * Sets the JPA query parameters
	 * @param filter
	 * @param qry
	 */
	public void setJPAQueryParameters(final F filter,final Query qry) {
		if (filter == null || filter.getBooleanQuery() == null) return;
		SetsJPQLWherePredicateParamFromSearchFilterClauseValue jpqlWhereParamFromFilterClause = _createSetsJPQLWherePredicateParamFromSearchFilterClauseValue(qry);
		_recurseSetJPAQueryParameters(filter.getBooleanQuery(),
									  jpqlWhereParamFromFilterClause);
	}
	protected void _recurseSetJPAQueryParameters(final BooleanQueryClause qryClause,
												 final SetsJPQLWherePredicateParamFromSearchFilterClauseValue clauseValueToJPQLPredicateParam) {
												 //final Query qry) {
		if (CollectionUtils.isNullOrEmpty(qryClause.getClauses())) return;
		Set<QualifiedQueryClause<? extends QueryClause>> clauses = qryClause.getClauses();

		for (Iterator<QualifiedQueryClause<? extends QueryClause>> clauseIt = clauses.iterator(); clauseIt.hasNext(); ) {
			QueryClause clause = clauseIt.next().getClause();
			
			// If it's a BooleanQueryClause...
			if (clause instanceof BooleanQueryClause) {
				BooleanQueryClause boolClause = (BooleanQueryClause)clause;
				_recurseSetJPAQueryParameters(boolClause,
											  clauseValueToJPQLPredicateParam);			// BEWARE!!! recursion
				continue;
			} 

			// some indexable fields are NOT supported when the search engine 
			// is DB based
			boolean isDBEntitySupportedField = _isSupportedDBEntityField(clause);
			if (!isDBEntitySupportedField)  continue;
			
			_setJPAQueryParameter(clause,
								  clauseValueToJPQLPredicateParam);
		}
	}
	/**
	 * Some indexable fields are NOT supported when the search engine is DB based
	 * @param clause
	 * @return
	 */
	protected static boolean _isSupportedDBEntityField(final QueryClause clause) {
		FieldID facetsField = FieldID.from(TypeMetaDataForModelObjectBase.SEARCHABLE_METADATA.TYPE_FACETS);
		if (clause.getFieldId().is(facetsField)) return false;
		return true;
	}
	protected static void _setJPAQueryParameter(final QueryClause clause,
										 	    final SetsJPQLWherePredicateParamFromSearchFilterClauseValue clauseValueToJPQLPredicateParam) {
		String dbFieldId = clause.getFieldId().asString();
		
		if (clause instanceof EqualsQueryClause<?>) {
			EqualsQueryClause<?> eqClause = (EqualsQueryClause<?>)clause;
			clauseValueToJPQLPredicateParam.setWherePredicateParamFor(eqClause,
																	  dbFieldId);
		} 
		else if (clause instanceof ContainsTextQueryClause) {
			ContainsTextQueryClause containsTextClause = (ContainsTextQueryClause)clause;
			clauseValueToJPQLPredicateParam.setWherePredicateParamFor(containsTextClause,
																	  dbFieldId);
		} 
		else if (clause instanceof RangeQueryClause<?>) {
			RangeQueryClause<?> rangeClause = (RangeQueryClause<?>)clause;
			clauseValueToJPQLPredicateParam.setWherePredicateParamFor(rangeClause,
																	  dbFieldId);
		} 
		else if (clause instanceof ContainedInQueryClause<?>) {
			ContainedInQueryClause<?> containedInClause = (ContainedInQueryClause<?>)clause;
			clauseValueToJPQLPredicateParam.setWherePredicateParamFor(containedInClause,
																	  dbFieldId);
		}		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	FILTER CLAUSE VALUE TO JPQL WHERE PREDICATE PARAM 
/////////////////////////////////////////////////////////////////////////////////////////	
	@RequiredArgsConstructor
	protected class SearchFilterClauseValueToJPQLWherePredicateParam 
		 implements SetsJPQLWherePredicateParamFromSearchFilterClauseValue {
		
		protected final Query _qry;
		
		@Override
		public void setWherePredicateParamFor(final EqualsQueryClause<?> eqClause,
											  final String dbFieldId) {
			if (log.isDebugEnabled()) log.debug("... set {} to {}",
												dbFieldId,eqClause.getValue());
			
			if (eqClause.getValue() instanceof Boolean
			 || eqClause.getValue() instanceof Enum) {
				_qry.setParameter(dbFieldId,
							      eqClause.getValue());
			} 
			else if (eqClause.getValue() instanceof OID) {
				_qry.setParameter(dbFieldId,
							      ((OID)eqClause.getValue()).asString());
			}
			else if (eqClause.getValue() instanceof CanBeRepresentedAsString) {
				_qry.setParameter(dbFieldId,
								  ((CanBeRepresentedAsString)eqClause.getValue()).asString());
			}
			else {
				_qry.setParameter(dbFieldId,
							      eqClause.getValue());
			}
		}
		@Override
		public void setWherePredicateParamFor(final ContainsTextQueryClause containsTextClause,
											  final String dbFieldId) {
			// The contains text query clause DOES NOT USE jpa params: the param value is directly set
			// when creating the jpql where clause (see wherePredicateFrom(ContainsTextQueryClause)
//			qry.setParameter(dbFieldId,
//							 containsTxtClause.getText());
		}
		@Override
		public void setWherePredicateParamFor(final RangeQueryClause<?> rangeClause,
											  final String dbFieldId) {
			if (rangeClause.getRange().hasLowerBound() && rangeClause.getRange().hasUpperBound()) {
				_qry.setParameter(dbFieldId + "Start",rangeClause.getRange().lowerEndpoint());
				_qry.setParameter(dbFieldId + "End",rangeClause.getRange().upperEndpoint());
			} else if (rangeClause.getRange().hasLowerBound()) {
				_qry.setParameter(dbFieldId,rangeClause.getRange().lowerEndpoint());
			} else if (rangeClause.getRange().hasUpperBound()) {
				_qry.setParameter(dbFieldId,rangeClause.getRange().upperEndpoint());
			}
		}
		@Override
		public void setWherePredicateParamFor(final ContainedInQueryClause<?> containedInClause,
											  final String dbFieldId) {
			Collection<?> spectrum = Lists.newArrayList(containedInClause.getSpectrum());
			_qry.setParameter(dbFieldId,spectrum);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ORDER
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Composes the order by clause
	 * @param filter
	 * @param ordering
	 * @return
	 */
	protected String _composeJpqlOrderByClause(final F filter,
											   final Collection<SearchResultsOrdering> ordering,
											   final TranslatesIndexableFieldIDToDBEntityField translatesFieldToDBEntityField) {
		if (CollectionUtils.isNullOrEmpty(ordering)) return null;
		
		StringBuilder orderBy = new StringBuilder();
		orderBy.append("ORDER BY ");
		for (SearchResultsOrdering ord : ordering) {
			String ordDBEntityFieldName = translatesFieldToDBEntityField.dbEntityFieldNameFor(ord.getFieldId());
			orderBy.append(Strings.customized(_dbEntityAlias+".{} {}",
											  ordDBEntityFieldName,ord.getDirection().getCode()));
		}
		// BEWARE!!! 	ORACLE BUG with paging & ordering: the order clause MUST include the primary key
		//				see: http://adfinmunich.blogspot.com.es/2012/03/problem-with-pagination-and-ordering-in.html
		//					 http://www.eclipse.org/forums/index.php/m/638599/
		orderBy.append(Strings.customized(","+_dbEntityAlias+".{} ASC",
										  _dbEntityFieldNameForOid(translatesFieldToDBEntityField)));
		return orderBy.toString();
	}
}

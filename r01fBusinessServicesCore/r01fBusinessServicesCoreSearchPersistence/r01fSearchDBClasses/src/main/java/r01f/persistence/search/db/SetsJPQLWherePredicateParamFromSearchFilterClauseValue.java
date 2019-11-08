package r01f.persistence.search.db;

import r01f.model.search.query.ContainedInQueryClause;
import r01f.model.search.query.ContainsTextQueryClause;
import r01f.model.search.query.EqualsQueryClause;
import r01f.model.search.query.RangeQueryClause;

public interface SetsJPQLWherePredicateParamFromSearchFilterClauseValue {
	public void setWherePredicateParamFor(final EqualsQueryClause<?> eqClause,
										  final String dbFieldId);
	
	public void setWherePredicateParamFor(final ContainsTextQueryClause containsTextClause,
										  final String dbFieldId);
	
	public void setWherePredicateParamFor(final RangeQueryClause<?> rangeClause,
										  final String dbFieldId);
	
	public void setWherePredicateParamFor(final ContainedInQueryClause<?> containedInClause,
										  final String dbFieldId);

}

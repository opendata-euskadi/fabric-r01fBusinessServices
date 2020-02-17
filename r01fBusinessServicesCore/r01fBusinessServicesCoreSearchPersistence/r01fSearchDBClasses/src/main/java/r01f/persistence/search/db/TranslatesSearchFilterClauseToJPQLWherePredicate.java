package r01f.persistence.search.db;

import r01f.model.search.query.ContainedInQueryClause;
import r01f.model.search.query.ContainsTextQueryClause;
import r01f.model.search.query.EqualsQueryClause;
import r01f.model.search.query.NullQueryClause;
import r01f.model.search.query.QueryClause;
import r01f.model.search.query.RangeQueryClause;

public interface TranslatesSearchFilterClauseToJPQLWherePredicate {
	public <Q extends QueryClause> String wherePredicateFrom(final Q clause);
	public String wherePredicateFrom(final EqualsQueryClause<?> eqQry);
	public String wherePredicateFrom(final NullQueryClause nullQry);
	public String wherePredicateFrom(final ContainsTextQueryClause containsTextQry);
	public String wherePredicateFrom(final RangeQueryClause<?> rangeQry);
	public String wherePredicateFrom(final ContainedInQueryClause<?> containedInQry);
}

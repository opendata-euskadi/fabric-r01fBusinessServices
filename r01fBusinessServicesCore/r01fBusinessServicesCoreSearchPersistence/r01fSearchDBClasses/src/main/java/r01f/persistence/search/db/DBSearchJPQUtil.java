package r01f.persistence.search.db;

import java.util.Collection;
import java.util.Iterator;

/**
 * Utility type to compose JPQL sentences
 */
public class DBSearchJPQUtil {
/////////////////////////////////////////////////////////////////////////////////////////
//  JPQL COMPOSING
/////////////////////////////////////////////////////////////////////////////////////////
	public static String joinWhereClauses(final Collection<String> whereClauses) {
		StringBuilder whereClausesJoined = new StringBuilder();
		for (Iterator<String> clauseIt = whereClauses.iterator(); clauseIt.hasNext(); ) {
			whereClausesJoined.append(clauseIt.next());
			if (clauseIt.hasNext()) whereClausesJoined.append(" AND ");
		}
		return whereClausesJoined.toString();
	}
}

package r01f.persistence.search.db;

import java.util.Collection;
import java.util.Iterator;

import javax.persistence.EntityManager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.model.search.query.BooleanQueryClause.QueryClauseOccur;
import r01f.model.search.query.ContainsTextQueryClause;
import r01f.persistence.db.config.DBModuleConfig;
import r01f.persistence.db.config.DBVendor;
import r01f.util.types.Strings;

/**
 * Utility type to compose JPQL sentences
 */
@Slf4j
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class DBSearchJPQUtil {
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
	public static String jpqlJoinFor(final QueryClauseOccur occur) {
		String outJPQL = null;
		switch(occur) {
		case MUST:
			outJPQL = " AND ";
			break;
		case MUST_NOT:
			outJPQL = " AND NOT ";
			break;
		case SHOULD:
			outJPQL = " OR ";
			break;
		default:
			throw new IllegalArgumentException();
		}
		return outJPQL;
	}
	public static String sanitizeFullTextQueryText(final String filteringText) {
		if (filteringText == null) return null;
		return filteringText			// it's already in UPPERCASE					
   				  .replaceAll("%","")		// remove all %
				  .replaceAll("'","")		// remove all '
				  .replaceAll("\"","")		// remove all "
				  .replaceAll("SELECT","")
				  .replaceAll("ALTER","")	
				  .replaceAll("DROP","")	
				  .replaceAll("DELETE","")
				  .replaceAll("INSERT","")
				  .replaceAll("UPDATE","")
				  .replaceAll("EXPORT","")
				  .replaceAll("IMPORT","");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	FULL TEXT
/////////////////////////////////////////////////////////////////////////////////////////	
	public static String fullTextWherePredicateFrom(final DBModuleConfig dbModuleConfig,
													final EntityManager entityManager,
													final String dbEntityName,final String dbFieldSpec,
													final ContainsTextQueryClause containsTextQry) {
		if (Strings.isNullOrEmpty(containsTextQry.getText())) return null;
		
		String template = null;
		if (containsTextQry.isBegining()) {
			template = "upper(" + dbEntityName + ".{}) LIKE '{}%'";
		} else if (containsTextQry.isEnding()) {
			template = "upper(" + dbEntityName + ".{}) LIKE '%{}'";			
		} else if (containsTextQry.isContaining()) {
			template = "upper(" + dbEntityName + ".{}) LIKE '%{}%'";
		} else if (containsTextQry.isFullText()) {
			boolean fullTextSearchEnabled = dbModuleConfig.isFullTextSearchSupported(entityManager); 
			log.info("FullText search enabled: {}",
					 fullTextSearchEnabled);
			if (fullTextSearchEnabled) {
				// Full text search is ENABLED: the filter expression is db platform-dependent
				// 								use SQL operator available since eclipselink 2.5
				// 								see http://wiki.eclipse.org/EclipseLink/UserGuide/JPA/Basic_JPA_Development/Querying/Support_for_Native_Database_Functions#SQL
				if (dbModuleConfig.getDbSpec().getVendor()
											   .is(DBVendor.MySQL)) {
				    // IMPORTANT!! see: http://dev.mysql.com/doc/refman/5.0/en/fulltext-search.html / http://devzone.zend.com/26/using-mysql-full-text-searching/
				    //		Tables MUST be MyISAM (InnoDB)) type; to change the table type:
				    //			ALTER TABLE [table] engine=MyISAM;
				    //
				    //		also a FULLTEXT index must be added to the cols:
				    //			ALTER TABLE [table] ADD FULLTEXT [NOMBRE INDICE](col1,col2,...);
				    //		
				    //		Once the above is done, a FULL-TEXT search can be executed like:
				    //			select * 
				    //			  from [table]
				    //			 where MATCH(col1,col2) AGAINST ('[text]');
					
					// Generate:  SQL(   'MATCH(colXX) 
					//				     AGAINST(? IN BOOLEAN MODE)',':text')
					template = "SQL('MATCH({}) " + 
					  			  "AGAINST(? IN BOOLEAN MODE)','{}')";
				} 
				else if (dbModuleConfig.getDbSpec().getVendor()
													.is(DBVendor.ORACLE)) {
					// IMPORTANT!! see: http://docs.oracle.com/cd/B28359_01/text.111/b28304/csql.htm#i997503
					// 		Oracle Text MUST be enabled
					
					// Generate: SQL('CONTAINS(?,?,1) > 0,colXX,:text)
					template = "SQL('CONTAINS(?,?,1) > 0',{},'{}')";
				}
			}
			else {
				// simulate full text
				template = "upper(" + dbEntityName + ".{}) LIKE '%{}%'";
			}
		}
		String text = containsTextQry.getText()
									 .trim().toUpperCase();	// important!!
		String filteringText = null;
		if (containsTextQry.isFullText()
		 && dbModuleConfig.isFullTextSearchSupported(entityManager)) {
			// when full text search is enabled, multiple words are supported
			filteringText = text;
		} 
		else {
			// when full text search is NOT enabled, LIKE operations ONLY supports a single word
			// (otherwise multiple LIKE clauses are needed:  X LIKE %..% OR X LIKE %..%) 
			filteringText = text.split(" ")[0];			// use only the FIRST word (no multiple word is allowed)
		}
		filteringText = DBSearchJPQUtil.sanitizeFullTextQueryText(filteringText); 	// a minimal sanitization of the filtering text
		
		String theDBFieldSpec = dbFieldSpec.startsWith("_") ? dbFieldSpec.substring(1) : dbFieldSpec;	// remove the _ (WTF!!!!)
		theDBFieldSpec = dbModuleConfig.isFullTextSearchSupported(entityManager) ? dbFieldSpec			// SQL(...) queries requires the PHYSICAL column
																				 : "_" + dbFieldSpec;	// LIKE queries requires the MAPPED property
		String outPredStr = Strings.customized(template,
			          			  			   theDBFieldSpec,filteringText);	// the field and the value!!!
		return outPredStr;
	}
}

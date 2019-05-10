package r01f.test.search;

import java.util.Iterator;

import r01f.facets.Facetable;
import r01f.facets.HasName;
import r01f.facets.HasOID;
import r01f.facets.LangDependentNamed;
import r01f.facets.LangDependentNamed.HasLangDependentNamedFacet;
import r01f.facets.LangInDependentNamed;
import r01f.facets.LangInDependentNamed.HasLangInDependentNamedFacet;
import r01f.facets.Summarizable.HasSummaryFacet;
import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.model.search.SearchFilterForModelObject;
import r01f.model.search.SearchResultItemContainsPersistableObject;
import r01f.model.search.SearchResultItemForModelObject;
import r01f.model.search.SearchResults;
import r01f.types.summary.LangDependentSummary;
import r01f.types.summary.LangIndependentSummary;
import r01f.types.summary.Summary;

public class TestSearchUtil {
	/**
	 * Prints info about search results
	 * @param results
	 */
	public static <F extends SearchFilterForModelObject,
				   I extends SearchResultItemForModelObject<?>> void debugSearchResults(final SearchResults<F,I> results) {
		if (results.hasData()) {
			System.out.println(">>>>Found " +  results.getTotalItemsCount() + ": results");
			for (I item : results.getPageItems()) {
				HasOID<?> itemHasOid = (HasOID<?>)item;
				
				StringBuilder sb = new StringBuilder();
				sb.append(itemHasOid.getOid() + " (" + item.getClass() + ") > ");
				
				String itemSum = null; 
				
				if (item instanceof HasName) {
					if (item instanceof HasLangDependentNamedFacet) {
						HasLangDependentNamedFacet langDepNamedItem = (HasLangDependentNamedFacet)item;
						LanguageTexts langTexts = langDepNamedItem.getNameByLanguage();
						for (Iterator<Language> langIt = langTexts.getDefinedLanguages().iterator(); langIt.hasNext(); ) {
							Language lang = langIt.next();
							sb.append("[" + lang + "]: " + langTexts.get(lang));
							if (langIt.hasNext()) sb.append(", ");
						}
					} else if (item instanceof HasLangInDependentNamedFacet) {
						HasLangInDependentNamedFacet langIndepNamedItem = (HasLangInDependentNamedFacet)item;
						String name = langIndepNamedItem.getName();
						sb.append(name);
					} else {
						throw new IllegalStateException();
					}
				}
				// There's a summary at the search result item
				else if (item instanceof HasSummaryFacet) {
					HasSummaryFacet itemHasSumm = (HasSummaryFacet)item;
					Summary sum = itemHasSumm.asSummarizable()
									  		 .getSummary();
					if (sum.isLangDependent()) {
						LangDependentSummary langDepSum = sum.asLangDependent();
						for (Iterator<Language> langIt = langDepSum.getAvailableLanguages().iterator(); langIt.hasNext(); ) {
							Language lang = langIt.next();
							sb.append("[" + lang + "]: " + langDepSum.asString(lang));
							if (langIt.hasNext()) sb.append(", ");
						}
					} else {
						LangIndependentSummary langIndepSum = sum.asLangIndependent();
						sb.append(langIndepSum.asString());
					}
					itemSum = sb.toString();
					System.out.println("\t-" + itemSum);					
				}
				// The item's summary is NOT available... try to print something about the model object's name
				else if (item instanceof SearchResultItemContainsPersistableObject
					 &&  ((SearchResultItemContainsPersistableObject<?,?>)item).getModelObject() != null
					 && ((SearchResultItemContainsPersistableObject<?,?>)item).getModelObject().hasFacet(HasName.class)) {
					Facetable modelObj = ((SearchResultItemContainsPersistableObject<?,?>)item).getModelObject();
					if (modelObj.hasFacet(HasLangDependentNamedFacet.class)) {
						LangDependentNamed langNames = modelObj.asFacet(HasLangDependentNamedFacet.class)
														   	   .asLangDependentNamed();
						for (Iterator<Language> langIt = langNames.getAvailableLanguages().iterator(); langIt.hasNext(); ) {
							Language lang = langIt.next();
							sb.append("[" + lang + "]: " + langNames.getNameIn(lang));
							if (langIt.hasNext()) sb.append(", ");
						}
					} else {
						LangInDependentNamed name = modelObj.asFacet(HasLangInDependentNamedFacet.class)
															.asLangInDependentNamed();
						sb.append(name.getName());
					}
					itemSum = sb.toString();
					System.out.println("\t-" + itemSum);
				} 
				// There's nowhere to get a summary
				else {
					System.out.println("\t-->NO summary");
				}
			}
		} else {
			System.out.println(">> NO results");
		}
	}
}

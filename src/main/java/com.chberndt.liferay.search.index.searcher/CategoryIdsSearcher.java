package com.chberndt.liferay.search.index.searcher;

import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christian Berndt
 */
@Component(immediate = true, service = CategoryIdsSearcher.class)
public class CategoryIdsSearcher {

	@Activate
	protected void activate() {
		_log.info("activate() modified 12 times");

		// String[] assetCategoryIds = {"38809"}; // Topic A
		String[] assetCategoryIds = {"38809", "38810"}; // Topic A, Topic B

		long companyId = 20097; // which index to query
		String[] entryClassNames = {User.class.getName()};
		// String[] entryClassNames = {User.class.getName(), JournalArticle.class.getName()};
		boolean allCategories = false; // match all categories?

		_countByAssetCategoryIds(
			companyId, entryClassNames, assetCategoryIds, allCategories);
	}

	private void _countByAssetCategoryIds(
		long companyId, String[] entryClassNames, String[] assetCategoryIds,
		boolean allCategories) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		if ((assetCategoryIds == null) || (assetCategoryIds.length == 0)) {
			_log.info("No assetCategoryId provided");
		}
		else if (allCategories) {
			_log.info("all categories");

			TermsQuery[] termsQueries = new TermsQuery[assetCategoryIds.length];

			for (int i = 0; i < assetCategoryIds.length; i++) {
				TermsQuery termsQuery = _queries.terms(
					Field.ASSET_CATEGORY_IDS);

				termsQuery.addValue(assetCategoryIds[i]);

				termsQueries[i] = termsQuery;
			}

			booleanQuery.addFilterQueryClauses(termsQueries);
		}
		else {
			_log.info("any category");

			TermsQuery termsQuery = _queries.terms(Field.ASSET_CATEGORY_IDS);

			termsQuery.addValues(assetCategoryIds);

			booleanQuery.addFilterQueryClauses(termsQuery);
		}

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder();

		searchRequestBuilder.emptySearchEnabled(true);

		searchRequestBuilder.withSearchContext(
			searchContext -> {
				searchContext.setCompanyId(companyId);
				searchContext.setEntryClassNames(entryClassNames);
			});

		SearchRequest searchRequest = searchRequestBuilder.query(
			booleanQuery
		).build();

		SearchResponse searchResponse = _searcher.search(searchRequest);

		SearchHits searchHits = searchResponse.getSearchHits();

		_log.info("searchHits.getTotalHits: " + searchHits.getTotalHits());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CategoryIdsSearcher.class);

	@Reference
	private Queries _queries;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

}
package net.es.jsnow.parameters;

import org.apache.http.message.BasicNameValuePair;

import java.util.List;

/**
 * Request parameters for querying tables in the Table API
 */
public class TableQueryParams extends TableParams {

    protected String query = null;
    protected Integer limit = null;
    protected Integer offset = null;

    /**
     * @return the query string to perform passed to sysparm_query. See ServiceNow docs for format
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query string to perform passed to sysparm_query. See ServiceNow docs for format
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the maximum number of results to return (sysparm_limit)
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit the maximum number of results to return (sysparm_limit)
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * @return the first record to display (sysparm_offset)
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @param offset the first record to display (sysparm_offset)
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    protected List<BasicNameValuePair> buildGetParams() {
        List<BasicNameValuePair> getParams = super.buildGetParams();

        if(query != null){
            getParams.add(new BasicNameValuePair("sysparm_query", this.query));
        }

        if(limit != null){
            getParams.add(new BasicNameValuePair("sysparm_limit", this.limit + ""));
        }

        if(offset != null){
            getParams.add(new BasicNameValuePair("sysparm_offset", this.offset + ""));
        }

        return getParams;
    }
}

package com.xiaof.framework.model;

/**
 * Created by Chaoyun.Yep on 18/8/31.
 */
public interface ElasticSearchModel {

    /**
     * 在ElasticSearch中的Index
     * @return
     */
    String elasticSearchIndex();

    /**
     * 在ElasticSearch中的Type
     * @return
     */
    String elasticSearchType();

    /**
     * 获取ID
     * @return
     */
    String elasticSearchId();

}

package org.spider.es;

import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.client.Client;

import java.util.Map;

/**
 * Created by tianxudong on 2017/10/27.
 */
public interface IElasticSearchService {
    Client getClient();

    /**
     * 单个索引创建
     *
     * @param indexName 索引库
     * @param indexType 索引表
     * @param id        索引id
     * @param object    索引对象
     * @return
     */
    boolean addIndex(String indexName, String indexType, String id, JSONObject object);

    /**
     * 获取某条索引信息
     *
     * @param indexName 索引库
     * @param indexType 索引表
     * @param id        索引id
     * @return
     */
    Map<String, Object> getIndexById(String indexName, String indexType, String id);

    /**
     * 根据id删除索引
     *
     * @param indexName 索引库
     * @param indexType 索引表
     * @param id        索引id
     * @return
     */
    boolean delIndexById(String indexName, String indexType, String id);

    /**
     * 根据索引名称删除索引
     *
     * @param indexName 索引库
     * @return
     */
    void delIndexByIndexName(String indexName);

//    String addIndexByListAlbum(String indexName, String inexType, List<AlbumIndex> list);
//
//    String updateIndexByListAlbum(String indexName, String inexType, List<AlbumIndex> list);
//
//    String delIndexByListAlbum(String indexName, String inexType, List<AlbumIndex> list, String[] albumids);
//
//    AlbumList searchForAlbumList(String indexName, String indexType, QueryBuilder queryBuilder, SearchRequest searchRequest);
}

package org.spider.es;

import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.IndexNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Map;

/**
 * es常用操作
 * <p>
 * Created by tianxudong on 2017/10/27.
 */
public class ElasticSearchServiceImpl implements IElasticSearchService {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);

    @Resource(type = ElasticSearchClient.class)
    private ElasticSearchClient elasticSearchClient;

    /**
     * 获取es操作客户端
     */
    public Client getClient() {
        try {
            return elasticSearchClient.getClient();
        } catch (Exception e) {
            logger.error("ES获取client失败 :" + e.getCause().getMessage());
            return null;
        }
    }

    public boolean addIndex(String indexName, String indexType, String id, JSONObject object) {
        try {
            IndexResponse response = this.getClient().prepareIndex(indexName, indexType, id).setSource(object).get();
            return response.isCreated();
        } catch (Exception e) {
            logger.error("单个索引创建失败 :" + e.getCause().getMessage());
            return false;
        }
    }

    public Map<String, Object> getIndexById(String indexName, String indexType, String id) {
        try {
            GetResponse response = this.getClient().prepareGet(indexName, indexType, id).get();
            Map<String, Object> map = response.getSource();
            return map;
        } catch (Exception e) {
            logger.error("查询索引失败 : id=" + id + "-----" + e.getCause().getMessage());
            return null;
        }
    }

    public boolean delIndexById(String indexName, String indexType, String id) {
        try {
            DeleteResponse response = this.getClient().prepareDelete(indexName, indexType, id).get();
            return response.isFound();
        } catch (Exception e) {
            logger.error("删除单条索引失败 : id=" + id + "-----" + e.getCause().getMessage());
            return false;
        }
    }

    public void delIndexByIndexName(String indexName) {
        try {
            DeleteIndexResponse deleteIndexResponse = this.getClient().admin().indices().prepareDelete(indexName).execute().actionGet();
            logger.info("索引【" + indexName + "】已被删除" + deleteIndexResponse.getHeaders());
        } catch (IndexNotFoundException e) {
            logger.error("删除索引失败,未发现此索引，索引可能已被删除 : indexName=" + indexName);
        } catch (Exception e) {
            logger.error("删除索引失败,发现未知异常 : indexName=" + indexName + e.getCause().getMessage());
        }
    }

//    /**
//     * 批量增加索引
//     * @param indexName
//     * @param indexType
//     * @param list
//     */
//    public String addIndexByListAlbum(String indexName, String indexType, List<AlbumIndex> list){
//        Client client = this.getClient();
//        BulkRequestBuilder bulkRequest = client.prepareBulk();
//        if (list != null && list.size() != 0) {
//            for (AlbumIndex albumIndex : list) {
//                JSONObject jsonObject = (JSONObject)JSON.toJSON(albumIndex);
//                bulkRequest.add(client.prepareIndex(indexName, indexType, albumIndex.getAlbumId()+"").setSource(jsonObject));
//            }
//            BulkResponse bulkResponse = bulkRequest.get();
//            if (bulkResponse.hasFailures()) {
//                return "批量增加索引异常，原因：" + bulkResponse.buildFailureMessage();
//            }
//        }
//        return "批量增加索引成功";
//    }
//
//    /**
//     * 批量更新索引
//     * @param indexName
//     * @param indexType
//     * @param list
//     * @return
//     */
//    public String updateIndexByListAlbum(String indexName, String indexType, List<AlbumIndex> list){
//        Client client = this.getClient();
//        BulkRequestBuilder bulkRequest = client.prepareBulk();
//        if (list != null && list.size() != 0) {
//            for (AlbumIndex albumIndex : list) {
//                JSONObject jsonObject = (JSONObject)JSON.toJSON(albumIndex);
//                bulkRequest.add(client.prepareUpdate(indexName, indexType, albumIndex.getAlbumId()+"").setDoc(jsonObject));
//            }
//            BulkResponse bulkResponse = bulkRequest.get();
//            if (bulkResponse.hasFailures()) {
//                return "批量更新索引异常，原因：" + bulkResponse.buildFailureMessage();
//            }
//        }
//        return "批量更新索引成功";
//    }
//
//    /**
//     * 批量删除索引
//     * @param indexName
//     * @param indexType
//     * @param list
//     * @param albumids
//     */
//    public String delIndexByListAlbum(String indexName, String indexType, List<AlbumIndex> list, String[] albumids){
//        Client client = this.getClient();
//        BulkRequestBuilder bulkRequest = client.prepareBulk();
//        if (list != null && list.size() != 0) {
//            for (AlbumIndex albumIndex : list) {
//                bulkRequest.add(client.prepareDelete(indexName, indexType, albumIndex.getAlbumId()+""));
//            }
//        } else {
//            for (String albumid : albumids) {
//                bulkRequest.add(client.prepareDelete(indexName, indexType, albumid));
//            }
//        }
//        BulkResponse bulkResponse = bulkRequest.get();
//        if (bulkResponse.hasFailures()) {
//            return "批量删除索引异常，原因：" + bulkResponse.buildFailureMessage();
//        }
//        return "批量删除索引成功";
//    }
//
//    /**
//     * 通用搜索返回aid集合
//     * @param indexType
//     * @param indexType
//     * @param queryBuilder
//     * @param searchRequest
//     * @return
//     */
//    public AlbumList searchForAlbumList(String indexName, String indexType, QueryBuilder queryBuilder, SearchRequest searchRequest){
//        List<Map<String, Object>> sortColumns = searchRequest.getSortColumn();
//        //构造排序参数
//        SortBuilder sortBuilder = null;
//        if (sortColumns != null && sortColumns.size() != 0) {
//            for (Map sortMaps : sortColumns) {
//                for (Object key : sortMaps.keySet()) {
//                    sortBuilder = SortBuilders.fieldSort((String) key).order(this.StringUtil(sortMaps.get(key)).toUpperCase().equals("ASC") ? SortOrder.ASC : SortOrder.DESC);
//                }
//            }
//        } else {
//            sortBuilder = SortBuilders.fieldSort("_score").order(SortOrder.DESC);
//        }
//        int page = (searchRequest.getPage() -1) * searchRequest.getPageSize();
//        SearchResponse searchResponse = this.getClient().prepareSearch(indexName).setTypes(indexType)
//                .setQuery(queryBuilder).setFrom(page).setSize(searchRequest.getPageSize()).addSort(sortBuilder).setExplain(true).execute().actionGet();
//        SearchHits hits = searchResponse.getHits();
//        SearchHit[] searchHists = hits.getHits();
//        logger.debug("-------当前页大小："+searchHists.length + "-------匹配到的个数:"+hits.getTotalHits());
//        List<Album> albumList = new ArrayList<Album>();
//        if (searchHists.length > 0) {
//            for (SearchHit hit : searchHists) {
//                Album album = new Album();
//                String albumid = hit.getSource().get(AlbumIndexField.ALBUMID).toString();
//                album.setAlbumId(Integer.parseInt(albumid));
//                albumList.add(album);
//            }
//        }
//        return new AlbumList(searchHists.length, albumList);
//    }

//    private String StringUtil(Object obj) {
//        if (obj != null && obj != "") {
//            return obj.toString().trim();
//        } else {
//            return null;
//        }
//    }
}

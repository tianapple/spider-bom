package org.spider.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;

/**
 * canal操作工具类
 * <p>
 * Created by tianapple on 2016/7/1.
 */
public class CanalUtil {
    /**
     * 获取更新之后的列集合
     *
     * @param eventType 类型
     * @param rowData   数据
     * @return 更新后列集合
     */
    public static List<CanalEntry.Column> getAfterColumns(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        List<CanalEntry.Column> columnsList;
        if (eventType == CanalEntry.EventType.INSERT) {
            columnsList = rowData.getAfterColumnsList();
        } else if (eventType == CanalEntry.EventType.UPDATE) {
            columnsList = rowData.getAfterColumnsList();
        } else {
            columnsList = rowData.getBeforeColumnsList();
        }
        return columnsList;
    }

    /**
     * 根据列名返回某个列对象
     *
     * @param list 列集合
     * @param name 列名称
     * @return 列对象
     */
    public static CanalEntry.Column getColumn(List<CanalEntry.Column> list, String name) {
        for (CanalEntry.Column column : list) {
            if (column.getName().equalsIgnoreCase(name)) {
                return column;
            }
        }
        return CanalEntry.Column.getDefaultInstance();
    }

    /**
     * 根据列名返回列整形值
     *
     * @param list 列集合
     * @param name 列名称
     * @return int值
     */
    public static int getInt(List<CanalEntry.Column> list, String name) {
        CanalEntry.Column column = getColumn(list, name);
        return Integer.valueOf(column.getValue());
    }

    /**
     * 根据列名返回列字符串值
     *
     * @param list 列集合
     * @param name 列名称
     * @return String值
     */
    public static String getString(List<CanalEntry.Column> list, String name) {
        CanalEntry.Column column = getColumn(list, name);
        return column.getValue();
    }
}

package com.itning.mysql_backup_to_sqlite.entry;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ning.wang
 * @since 2023/4/3 12:16
 */
@Data
public class RowData {

    private List<ItemData> itemData = new ArrayList<>();

    @Data
    public static class ItemData {
        private int columnIndex;
        private Object value;
    }
}

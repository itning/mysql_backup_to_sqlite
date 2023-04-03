package com.itning.mysql_backup_to_sqlite.entry;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ning.wang
 * @since 2023/4/3 10:32
 */
@Data
public class DataEntry {

    private final Map<String, List<ColumnInfo>> columnInfoMap = new HashMap<>();
    private final Map<String, List<RowData>> dataInfoMap = new HashMap<>();

}

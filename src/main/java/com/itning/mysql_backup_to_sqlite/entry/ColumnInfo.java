package com.itning.mysql_backup_to_sqlite.entry;

import lombok.Data;

/**
 * @author ning.wang
 * @since 2023/4/3 12:16
 */
@Data
public class ColumnInfo {
    private int index;
    private String columnName;
    private String columnTypeName;
    /**
     * @see java.sql.Types
     */
    private int columnType;
}

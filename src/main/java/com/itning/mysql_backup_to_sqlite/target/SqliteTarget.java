package com.itning.mysql_backup_to_sqlite.target;

import com.itning.mysql_backup_to_sqlite.entry.ColumnInfo;
import com.itning.mysql_backup_to_sqlite.entry.DataEntry;
import com.itning.mysql_backup_to_sqlite.entry.RowData;
import com.itning.mysql_backup_to_sqlite.entry.TargetResult;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.sql.Types.*;

/**
 * @author ning.wang
 * @since 2023/4/3 12:42
 */
public class SqliteTarget implements Target {
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TargetResult start(File outPutDir, String jobName, DataEntry dataEntry) throws Exception {
        File dbFile = getDbFile(outPutDir, jobName);
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + getDbFile(outPutDir, jobName))) {
            Map<String, List<ColumnInfo>> columnInfoMap = dataEntry.getColumnInfoMap();
            Map<String, List<RowData>> dataInfoMap = dataEntry.getDataInfoMap();
            for (Map.Entry<String, List<ColumnInfo>> tableItem : columnInfoMap.entrySet()) {
                String tableName = tableItem.getKey();
                List<ColumnInfo> columnInfos = tableItem.getValue();

                StringBuilder createTableSqlBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS '" + tableName + "'(");
                columnInfos.forEach(columnInfo -> createTableSqlBuilder
                        .append("'")
                        .append(columnInfo.getColumnName())
                        .append("' ")
                        .append(getSqliteColumnType(columnInfo.getColumnType()))
                        .append(","));

                String createTableSql = createTableSqlBuilder.toString();
                createTableSql = createTableSql.substring(0, createTableSql.length() - 1) + ")";
                try (Statement sqliteStatement = connection.createStatement()) {
                    sqliteStatement.executeUpdate(createTableSql);
                }

                List<RowData> rowData = dataInfoMap.get(tableName);

                StringBuilder insertSqlBuilder = new StringBuilder("INSERT INTO '" + tableName + "' VALUES (");
                columnInfos.forEach(columnInfo -> insertSqlBuilder.append(" ? ,"));
                String insertSql = insertSqlBuilder.toString();
                insertSql = insertSql.substring(0, insertSql.length() - 1) + ")";

                for (RowData row : rowData) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                        for (RowData.ItemData itemData : row.getItemData()) {
                            preparedStatement.setObject(itemData.getColumnIndex(), itemData.getValue());
                        }
                        preparedStatement.executeUpdate();
                    }
                }
            }
        }
        return new TargetResult(Collections.singletonList(dbFile), TargetResult.TargetType.SQLITE);
    }

    private File getDbFile(File outPutDir, String jobName) {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(outPutDir, name + "_" + jobName + "_backup.db");
    }

    private String getSqliteColumnType(int columnType) {
        switch (columnType) {
            case BIT, TINYINT, SMALLINT, INTEGER, BIGINT -> {
                return "INTEGER";
            }
            case FLOAT, REAL, DOUBLE, NUMERIC, DECIMAL -> {
                return "REAL";
            }
            case BINARY, VARBINARY, LONGVARBINARY, BLOB -> {
                return "BLOB";
            }
            default -> {
                return "TEXT";
            }
        }
    }
}

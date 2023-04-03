package com.itning.mysql_backup_to_sqlite.source;

import com.itning.mysql_backup_to_sqlite.entry.ColumnInfo;
import com.itning.mysql_backup_to_sqlite.entry.DataEntry;
import com.itning.mysql_backup_to_sqlite.entry.RowData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ning.wang
 * @since 2023/4/3 10:29
 */
public class MySQLSource implements Source {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataEntry start(String mysqlUrl, String mysqlUsername, String mysqlPassword, Set<String> tables) throws Exception {
        try (Connection connection = DriverManager.getConnection(mysqlUrl, mysqlUsername, mysqlPassword)) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tablesResultSet = metaData.getTables(null, null, null, new String[]{"TABLE"});

            DataEntry dataEntry = new DataEntry();
            Map<String, List<ColumnInfo>> columnInfoMap = dataEntry.getColumnInfoMap();
            Map<String, List<RowData>> rowDataMap = dataEntry.getDataInfoMap();
            while (tablesResultSet.next()) {
                String tableName = tablesResultSet.getString("TABLE_NAME");
                if (!tables.contains(tableName)) {
                    continue;
                }

                List<ColumnInfo> columnInfos = new ArrayList<>();
                columnInfoMap.put(tableName, columnInfos);

                try (Statement mysqlStatement = connection.createStatement()) {
                    ResultSet mysqlResult = mysqlStatement.executeQuery("SELECT * FROM " + tableName);
                    ResultSetMetaData mysqlMetaData = mysqlResult.getMetaData();
                    int columnCount = mysqlMetaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        ColumnInfo columnInfo = new ColumnInfo();
                        columnInfo.setIndex(i);
                        columnInfo.setColumnName(mysqlMetaData.getColumnName(i));
                        columnInfo.setColumnType(mysqlMetaData.getColumnType(i));
                        columnInfo.setColumnTypeName(mysqlMetaData.getColumnTypeName(i));
                        columnInfos.add(columnInfo);
                    }

                    List<RowData> rowDataList = new ArrayList<>();
                    rowDataMap.put(tableName, rowDataList);

                    while (mysqlResult.next()) {
                        RowData rowData = new RowData();
                        rowDataList.add(rowData);

                        for (int i = 1; i <= columnCount; i++) {
                            RowData.ItemData itemData = new RowData.ItemData();
                            itemData.setColumnIndex(i);
                            itemData.setValue(mysqlResult.getObject(i));
                            rowData.getItemData().add(itemData);
                        }
                    }
                }
            }
            return dataEntry;
        }
    }
}

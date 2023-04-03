package com.itning.mysql_backup_to_sqlite.target;

import com.itning.mysql_backup_to_sqlite.entry.ColumnInfo;
import com.itning.mysql_backup_to_sqlite.entry.DataEntry;
import com.itning.mysql_backup_to_sqlite.entry.RowData;
import com.itning.mysql_backup_to_sqlite.entry.TargetResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author ning.wang
 * @since 2023/4/3 13:49
 */
public class SqlFileTarget implements Target {
    @Override
    public TargetResult start(List<File> outPutDir, String jobName, DataEntry dataEntry) throws Exception {
        Map<String, List<ColumnInfo>> columnInfoMap = dataEntry.getColumnInfoMap();
        Map<String, List<RowData>> dataInfoMap = dataEntry.getDataInfoMap();

        List<File> result = new ArrayList<>(columnInfoMap.size());
        for (Map.Entry<String, List<ColumnInfo>> tableItem : columnInfoMap.entrySet()) {

            String tableName = tableItem.getKey();
            List<ColumnInfo> columnInfos = tableItem.getValue();

            StringBuilder sql = new StringBuilder(MessageFormat.format("""  
                    -- DATABASE TABLE BACKUP
                    -- JOB NAME {0}
                    -- TABLE NAME {1}
                    \n
                    """, jobName, tableName));

            StringBuilder createTableSqlBuilder = new StringBuilder("-- DDL start\nCREATE TABLE IF NOT EXISTS '" + tableName + "'(");

            columnInfos.forEach(columnInfo -> createTableSqlBuilder
                    .append("'")
                    .append(columnInfo.getColumnName())
                    .append("' ")
                    .append(columnInfo.getColumnTypeName())
                    .append(","));
            String createTableSql = createTableSqlBuilder.toString();
            sql.append(createTableSql, 0, createTableSql.length() - 1).append(");\n-- DDL end\n\n-- DML start\n");

            List<RowData> rowData = dataInfoMap.get(tableName);
            for (RowData row : rowData) {
                StringBuilder insertSqlBuilder = new StringBuilder("INSERT INTO '" + tableName + "' VALUES (");
                for (RowData.ItemData itemData : row.getItemData()) {
                    Object value = itemData.getValue();
                    if (value instanceof Number) {
                        insertSqlBuilder.append(value).append(",");
                        continue;
                    }

                    boolean containsApostrophe = value.toString().contains("'");
                    if (containsApostrophe) {
                        insertSqlBuilder.append("'").append(value.toString().replace("'", "''")).append("',");
                    } else {
                        insertSqlBuilder.append("'").append(value).append("',");
                    }
                }
                String insertSql = insertSqlBuilder.toString();
                sql.append(insertSql, 0, insertSql.length() - 1).append(");\n");
            }

            String sqlContent = sql.append("-- DML end\n").toString();
            for (int i = 0; i < outPutDir.size(); i++) {
                if (i == 0) {
                    result.add(write2File(outPutDir.get(i), jobName, tableName, sqlContent));
                } else {
                    write2File(outPutDir.get(i), jobName, tableName, sqlContent);
                }
            }
        }
        return new TargetResult(result, TargetResult.TargetType.SQL_FILE);
    }

    private File write2File(File outPutDir, String jobName, String tableName, String sql) throws IOException {
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File outFile = new File(outPutDir, time + "_" + jobName + "_" + tableName + ".sql");
        try (FileWriter fileWriter = new FileWriter(outFile, StandardCharsets.UTF_8, false)) {
            fileWriter.write(sql);
            fileWriter.flush();
        }
        return outFile;
    }
}

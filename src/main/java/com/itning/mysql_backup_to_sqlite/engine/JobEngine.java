package com.itning.mysql_backup_to_sqlite.engine;

import com.itning.mysql_backup_to_sqlite.config.BackupProperties;
import com.itning.mysql_backup_to_sqlite.entry.DataEntry;
import com.itning.mysql_backup_to_sqlite.entry.TargetResult;
import com.itning.mysql_backup_to_sqlite.plugin.TencentCloudCOSPlugin;
import com.itning.mysql_backup_to_sqlite.source.MySQLSource;
import com.itning.mysql_backup_to_sqlite.target.SqlFileTarget;
import com.itning.mysql_backup_to_sqlite.target.SqliteTarget;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author ning.wang
 * @since 2023/4/3 13:35
 */
@Slf4j
public class JobEngine implements Runnable {

    private static final MySQLSource MYSQL_SOURCE = new MySQLSource();
    private static final SqliteTarget SQLITE_TARGET = new SqliteTarget();
    private static final SqlFileTarget SQL_FILE_TARGET = new SqlFileTarget();
    private static final TencentCloudCOSPlugin TENCENT_CLOUD_COS_PLUGIN = new TencentCloudCOSPlugin();

    private final String name;
    private final BackupProperties.Job job;

    public JobEngine(String name, BackupProperties.Job job) {
        this.name = name;
        this.job = job;
    }

    @Override
    public void run() {
        log.info("start job {}", name);
        try {
            DataEntry dataEntry = handleSource();
            List<TargetResult> results = handleTarget(dataEntry);
            handlePlugin(results);
        } catch (Throwable e) {
            log.error("handler job {} exception", name, e);
        } finally {
            log.info("end job {}", name);
        }
    }

    private DataEntry handleSource() throws Exception {
        BackupProperties.MySQLSource mysqlSource = job.getMysqlSource();
        return MYSQL_SOURCE.start(mysqlSource.getMysqlUrl(), mysqlSource.getMysqlUsername(), mysqlSource.getMysqlPassword(), mysqlSource.getMysqlTables());
    }

    private List<TargetResult> handleTarget(DataEntry data) throws Exception {
        List<TargetResult> results = new ArrayList<>(2);
        BackupProperties.SqliteTarget sqliteTarget = job.getSqliteTarget();
        if (Objects.nonNull(sqliteTarget)) {
            results.add(SQLITE_TARGET.start(sqliteTarget.getOutPutDir(), name, data));
        }
        BackupProperties.SqlFileTarget sqlFileTarget = job.getSqlFileTarget();
        if (Objects.nonNull(sqlFileTarget)) {
            results.add(SQL_FILE_TARGET.start(sqlFileTarget.getOutPutDir(), name, data));
        }
        return results;
    }

    private void handlePlugin(List<TargetResult> files) throws Exception {
        BackupProperties.TencentCloudCOSPlugin tencentCloudCOSPlugin = job.getTencentCloudCosPlugin();
        if (Objects.nonNull(tencentCloudCOSPlugin)) {
            TENCENT_CLOUD_COS_PLUGIN.start(tencentCloudCOSPlugin, files);
        }
    }
}

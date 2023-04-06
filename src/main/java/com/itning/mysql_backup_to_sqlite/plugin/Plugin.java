package com.itning.mysql_backup_to_sqlite.plugin;

import com.itning.mysql_backup_to_sqlite.config.BackupProperties;
import com.itning.mysql_backup_to_sqlite.entry.DataEntry;
import com.itning.mysql_backup_to_sqlite.entry.TargetResult;

import java.util.List;

/**
 * @author ning.wang
 * @since 2023/4/3 12:42
 */
public interface Plugin {
    void start(String name, BackupProperties.Job job, DataEntry dataEntry, List<TargetResult> results) throws Exception;
}

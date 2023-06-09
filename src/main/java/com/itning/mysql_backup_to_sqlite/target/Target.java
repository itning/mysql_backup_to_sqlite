package com.itning.mysql_backup_to_sqlite.target;

import com.itning.mysql_backup_to_sqlite.entry.DataEntry;
import com.itning.mysql_backup_to_sqlite.entry.TargetResult;

import java.io.File;
import java.util.List;

/**
 * @author ning.wang
 * @since 2023/4/3 10:13
 */
public interface Target {
    TargetResult start(List<File> outPutDir, String jobName, DataEntry dataEntry) throws Exception;
}

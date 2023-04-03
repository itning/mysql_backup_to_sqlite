package com.itning.mysql_backup_to_sqlite.source;

import com.itning.mysql_backup_to_sqlite.entry.DataEntry;

import java.util.Set;

/**
 * @author ning.wang
 * @since 2023/4/3 10:13
 */
public interface Source {
    DataEntry start(String mysqlUrl, String mysqlUsername, String mysqlPassword, Set<String> tables) throws Exception;
}

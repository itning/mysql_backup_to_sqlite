package com.itning.mysql_backup_to_sqlite.entry;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.List;

/**
 * @author ning.wang
 * @since 2023/4/3 15:09
 */
@AllArgsConstructor
@Data
public class TargetResult {
    private List<File> files;
    private TargetType targetType;

    public enum TargetType {
        SQLITE,
        SQL_FILE,
    }
}

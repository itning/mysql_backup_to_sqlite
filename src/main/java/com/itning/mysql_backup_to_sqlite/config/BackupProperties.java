package com.itning.mysql_backup_to_sqlite.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ning.wang
 * @since 2023/4/3 10:04
 */
@ConfigurationProperties(prefix = "backup")
@Component
@Data
public class BackupProperties {
    private Map<String, Job> job;

    @Data
    public static class Job {
        private String cron;
        private MySQLSource mysqlSource;
        private SqliteTarget sqliteTarget;
        private SqlFileTarget sqlFileTarget;
        private TencentCloudCOSPlugin tencentCloudCosPlugin;
    }

    @Data
    public static class MySQLSource {
        private String mysqlUrl;
        private String mysqlUsername;
        private String mysqlPassword;
        private Set<String> mysqlTables;
    }

    @Data
    public static class SqliteTarget {
        private List<File> outPutDir;
    }

    @Data
    public static class SqlFileTarget {
        private List<File> outPutDir;
    }

    @Data
    public static class TencentCloudCOSPlugin {
        /**
         * SECRETID和SECRETKEY请登录<a href="https://console.cloud.tencent.com/cam/capi">访问管理控制台</a>进行查看和管理
         */
        private String secretId;

        /**
         * SECRETID和SECRETKEY请登录<a href="https://console.cloud.tencent.com/cam/capi">访问管理控制台</a>进行查看和管理
         */
        private String secretKey;

        /**
         * 设置 bucket 的地域, COS 地域的简称请参照<a href="https://cloud.tencent.com/document/product/436/6224">这里</a>
         */
        private String regionName;

        /**
         * 腾讯COS BucketName
         */
        private String bucketName;

        private String dirName;

        private boolean uploadSqliteFile = true;
        private boolean uploadSqlFile = true;
    }
}

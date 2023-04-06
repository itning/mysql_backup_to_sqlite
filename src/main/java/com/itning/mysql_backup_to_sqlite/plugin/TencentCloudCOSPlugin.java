package com.itning.mysql_backup_to_sqlite.plugin;

import com.itning.mysql_backup_to_sqlite.config.BackupProperties;
import com.itning.mysql_backup_to_sqlite.entry.DataEntry;
import com.itning.mysql_backup_to_sqlite.entry.TargetResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * @author ning.wang
 * @since 2023/4/3 14:41
 */
@Slf4j
public class TencentCloudCOSPlugin implements Plugin {

    @Override
    public void start(String name, BackupProperties.Job job, DataEntry dataEntry, List<TargetResult> results) throws Exception {
        BackupProperties.TencentCloudCOSPlugin tencentCloudCOSPlugin = job.getTencentCloudCosPlugin();
        if (Objects.isNull(tencentCloudCOSPlugin) || Objects.isNull(tencentCloudCOSPlugin.getBucketName()) || tencentCloudCOSPlugin.getBucketName().isBlank()) {
            return;
        }
        COSCredentials cred = new BasicCOSCredentials(tencentCloudCOSPlugin.getSecretId(), tencentCloudCOSPlugin.getSecretKey());
        Region region = new Region(tencentCloudCOSPlugin.getRegionName());
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(HttpProtocol.https);
        COSClient cosClient = new COSClient(cred, clientConfig);

        TransferManager transferManager = new TransferManager(cosClient);
        try {
            for (TargetResult result : results) {
                List<File> files = result.getFiles();
                TargetResult.TargetType targetType = result.getTargetType();

                if (!tencentCloudCOSPlugin.isUploadSqliteFile() && targetType == TargetResult.TargetType.SQLITE) {
                    continue;
                }
                if (!tencentCloudCOSPlugin.isUploadSqlFile() && targetType == TargetResult.TargetType.SQL_FILE) {
                    continue;
                }

                for (File file : files) {
                    String key = Objects.isNull(tencentCloudCOSPlugin.getDirName()) ? file.getName() : tencentCloudCOSPlugin.getDirName() + "/" + file.getName();
                    PutObjectRequest putObjectRequest = new PutObjectRequest(tencentCloudCOSPlugin.getBucketName(), key, file);
                    ObjectMetadata objectMetadata = new ObjectMetadata();
                    putObjectRequest.setMetadata(objectMetadata);
                    Upload upload = transferManager.upload(putObjectRequest);
                    UploadResult uploadResult = upload.waitForUploadResult();
                    log.info("upload cos result: {}", uploadResult.getKey());
                }
            }
        } finally {
            transferManager.shutdownNow(true);
        }
    }
}

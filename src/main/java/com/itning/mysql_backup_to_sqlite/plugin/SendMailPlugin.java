package com.itning.mysql_backup_to_sqlite.plugin;

import com.itning.mysql_backup_to_sqlite.config.BackupProperties;
import com.itning.mysql_backup_to_sqlite.config.BootBeanGetter;
import com.itning.mysql_backup_to_sqlite.entry.DataEntry;
import com.itning.mysql_backup_to_sqlite.entry.RowData;
import com.itning.mysql_backup_to_sqlite.entry.TargetResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author itning
 * @since 2023/4/6 21:52
 */
@Slf4j
public class SendMailPlugin implements Plugin {

    private static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER = new PropertyPlaceholderHelper("${", "}");
    private JavaMailSender emailSender;
    private MailProperties mailProperties;

    public SendMailPlugin() {
        try {
            this.emailSender = BootBeanGetter.getClassBean(JavaMailSender.class);
            this.mailProperties = BootBeanGetter.getClassBean(MailProperties.class);
        } catch (BeansException e) {
            log.info("ignore send email plugin:{}", e.getMessage());
        }
    }

    @Override
    public void start(String name, BackupProperties.Job job, DataEntry dataEntry, List<TargetResult> results) throws Exception {
        if (Objects.isNull(emailSender) || Objects.isNull(mailProperties)) {
            return;
        }
        long count = 0;
        Map<String, List<RowData>> dataInfoMap = dataEntry.getDataInfoMap();
        for (Map.Entry<String, List<RowData>> item : dataInfoMap.entrySet()) {
            count += item.getValue().size();
        }

        Properties properties = new Properties();
        properties.setProperty("name", name);
        properties.setProperty("database", String.valueOf(dataEntry.getColumnInfoMap().size()));
        properties.setProperty("total", String.valueOf(count));
        String text = """
                备份结果通知 ${name}
                备份了 ${database} 个数据库
                共计 ${total} 行数据
                """;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getUsername());
        message.setTo("itning@itning.top");
        message.setSubject("备份结果通知");
        message.setText(PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(text, properties));
        emailSender.send(message);
    }
}

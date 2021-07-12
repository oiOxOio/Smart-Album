package com.example.smartalbum.scheduled;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 * @date 2021/6/9 12:53
 */
@Component
public class DataSynchronizationScheduled {

    /**
     * 每1小时同步一次数据
     */
    @Scheduled(fixedDelay = 1000L * 60 * 60)
    public void updateScheduled() {
        // TODO: 2021/6/9 可能有些操作会导致数据库的数据与oss对象桶上的数据不同步，所以每过段时间把同步数据库和oss上的数据
        //  暂时待定，后面再考虑加不加这个功能
    }
}

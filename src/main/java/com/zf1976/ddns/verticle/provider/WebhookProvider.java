package com.zf1976.ddns.verticle.provider;

import com.zf1976.ddns.config.WebhookConfig;
import io.vertx.core.Future;

/**
 * @author mac
 * 2021/8/27 星期五 12:46 下午
 */
public interface WebhookProvider {

    Future<WebhookConfig> readWebhookConfig();

}

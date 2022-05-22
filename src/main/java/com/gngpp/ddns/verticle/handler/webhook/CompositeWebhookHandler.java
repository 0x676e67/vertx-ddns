/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gngpp.ddns.verticle.handler.webhook;

import com.gngpp.ddns.expression.ExpressionParser;
import com.gngpp.ddns.expression.TemplateExpressionHandler;
import com.gngpp.ddns.pojo.DnsRecordLog;
import com.gngpp.ddns.verticle.provider.WebhookProvider;
import com.gngpp.ddns.config.WebhookConfig;
import com.gngpp.ddns.config.webhook.BaseMessage;
import com.gngpp.ddns.config.webhook.DingDingMessage;
import com.gngpp.ddns.config.webhook.LarkMessage;
import com.gngpp.ddns.config.webhook.ServerJMessage;
import com.gngpp.ddns.util.CollectionUtil;
import com.gngpp.ddns.verticle.handler.spi.WebhookHandler;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author mac
 * 2021/8/28 星期六 1:40 下午
 */
@SuppressWarnings("rawtypes")
public class CompositeWebhookHandler {

    private final Logger log = LogManager.getLogger("[CompositeWebhookHandler]");
    private final WebhookProvider webhookProvider;
    private final ExpressionParser expressionParser = new TemplateExpressionHandler();
    private final List<WebhookHandler> webhookHandlerList = new ArrayList<>(2);

    public CompositeWebhookHandler(Vertx vertx, WebhookProvider webhookProvider, List<WebhookHandler<BaseMessage>> webhookHandlerList) {
        this(vertx, webhookProvider);
        this.webhookHandlerList.addAll(webhookHandlerList);
    }

    public CompositeWebhookHandler(Vertx vertx, WebhookProvider webhookProvider) {
        this.webhookProvider = webhookProvider;
        final var webClient = WebClient.create(vertx);
        final var serviceLoader = ServiceLoader.load(WebhookHandler.class);
        for (WebhookHandler webhookHandler : serviceLoader) {
            webhookHandler.initClient(webClient);
           this.webhookHandlerList.add(webhookHandler);
        }
    }


    public Future<CompositeFuture> send(DnsRecordLog dnsRecordLog) {
        return this.webhookProvider.readWebhookConfig()
                                   .compose(webhookConfig -> this.compositeExpressionParser(dnsRecordLog, webhookConfig))
                                   .compose(this::compositeSend);
    }

    private Future<CompositeFuture> compositeSend(WebhookConfig webhookConfig) {
        List<Future> futureList = new ArrayList<>();
        for (WebhookHandler webhookHandler : this.webhookHandlerList) {
            if (webhookHandler instanceof ServerJWebhookHandler serverJWebhookHandler) {
                final var serverJMessage = webhookConfig.getServerJMessage();
                if (serverJMessage != null && serverJMessage.getEnabled()) {
                    futureList.add(serverJWebhookHandler.send(serverJMessage));
                }
            }
            if (webhookHandler instanceof DingDingWebhookHandler dingDingWebhookHandler) {
                final var dingDingMessageList = webhookConfig.getDingDingMessageList();
                if (!CollectionUtil.isEmpty(dingDingMessageList)) {
                    for (DingDingMessage dingDingMessage : dingDingMessageList) {
                        if (dingDingMessage != null && dingDingMessage.getEnabled()) {
                            futureList.add(dingDingWebhookHandler.send(dingDingMessage));
                        }
                    }
                }
            }
            if (webhookHandler instanceof LarkWebhookHandler larkWebhookHandler) {
                final var larkMessage = webhookConfig.getLarkMessage();
                if (larkMessage != null && larkMessage.getEnabled()) {
                    futureList.add(larkWebhookHandler.send(larkMessage));
                }
            }
        }
        return Future.succeededFuture(CompositeFuture.all(futureList));
    }

    private Future<WebhookConfig> compositeExpressionParser(DnsRecordLog dnsRecordLog, WebhookConfig webhookConfig) {
        final var serverJMessage = webhookConfig.getServerJMessage();
        if (serverJMessage != null) {
            this.expressionParser(dnsRecordLog, serverJMessage);
        }
        final var dingDingMessageList = webhookConfig.getDingDingMessageList();
        if (!CollectionUtil.isEmpty(dingDingMessageList)) {
            for (DingDingMessage dingDingMessage : dingDingMessageList) {
                this.expressionParser(dnsRecordLog, dingDingMessage);
            }
        }
        final var larkMessage = webhookConfig.getLarkMessage();
        if (larkMessage != null) {
            this.expressionParser(dnsRecordLog, larkMessage);
        }
        return Future.succeededFuture(webhookConfig);
    }



    private void expressionParser(DnsRecordLog dnsRecordLog, BaseMessage baseMessage) {
        switch (baseMessage.getWebhookProviderType()) {
            case DING_DING -> {
                DingDingMessage dingDingMessage = (DingDingMessage) baseMessage;
                switch (dingDingMessage.getMsgType()) {
                    case LINK -> {
                        final var link = dingDingMessage.getLink();
                        final var parserContent = this.expressionParser.replaceParser(dnsRecordLog, link.getText());
                        link.setText(parserContent);
                    }
                    case MARKDOWN -> {
                        final var markdown = dingDingMessage.getMarkdown();
                        final var parserContent = this.expressionParser.replaceParser(dnsRecordLog, markdown.getText());
                        markdown.setText(parserContent);
                    }
                    case TEXT -> {
                        final var text = dingDingMessage.getText();
                        final var parserContent = this.expressionParser.replaceParser(dnsRecordLog, text.getContent());
                        text.setContent(parserContent);
                    }
                }
            }
            case SERVER_J -> {
                ServerJMessage serverJMessage = (ServerJMessage) baseMessage;
                final var parserContent = this.expressionParser.replaceParser(dnsRecordLog, serverJMessage.getContent());
                serverJMessage.setContent(parserContent);
            }
            case LARK -> {
                LarkMessage larkMessage = (LarkMessage) baseMessage;
                final var parserContent = this.expressionParser.replaceParser(dnsRecordLog, larkMessage.getContent().getText());
                larkMessage.getContent().setText(parserContent);
            }
        }
    }
}

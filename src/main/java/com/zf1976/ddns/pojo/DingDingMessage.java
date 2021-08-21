package com.zf1976.ddns.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zf1976.ddns.enums.DingDingMessageType;

import java.io.Serializable;

/**
 * @author mac
 * 2021/8/21 星期六 1:17 下午
 */
@SuppressWarnings({"SpellCheckingInspection", "unused"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DingDingMessage implements Serializable {

    @JsonProperty(value = "msgtype")
    private DingDingMessageType msgType;

    private LinkMessageBuilder link;

    private MarkdownMessageBuilder markdown;

    private At at;

    private DingDingMessage.Data text;

    public DingDingMessage() {

    }

    private DingDingMessage(LinkMessageBuilder linkMessage) {
        this.msgType = DingDingMessageType.LINK;
        this.link = linkMessage;
    }

    private DingDingMessage(TextMessageBuildr textMessage) {
        this.msgType = DingDingMessageType.TEXT;
        this.at = textMessage.at;
        this.text = textMessage.text;
    }

    private DingDingMessage(MarkdownMessageBuilder markdownMessage) {
        this.msgType = DingDingMessageType.MARKDOWN;
        this.at = markdownMessage.at;
        this.markdown = markdownMessage;
    }

    public static TextMessageBuildr newTextMessageBuilder() {
        return new TextMessageBuildr();
    }

    public static LinkMessageBuilder newLinkMessageBuilder() {
        return new LinkMessageBuilder();
    }

    public static MarkdownMessageBuilder newMarkdownMessageBuilder() {
        return new MarkdownMessageBuilder();
    }

    public MarkdownMessageBuilder getMarkdown() {
        return markdown;
    }

    public DingDingMessage setMarkdown(MarkdownMessageBuilder markdown) {
        this.markdown = markdown;
        return this;
    }

    public DingDingMessageType getMsgType() {
        return msgType;
    }

    public DingDingMessage setMsgType(DingDingMessageType msgType) {
        this.msgType = msgType;
        return this;
    }

    public LinkMessageBuilder getLink() {
        return link;
    }

    public DingDingMessage setLink(LinkMessageBuilder link) {
        this.link = link;
        return this;
    }

    public At getAt() {
        return at;
    }

    public DingDingMessage setAt(At at) {
        this.at = at;
        return this;
    }

    public DingDingMessage.Data getText() {
        return text;
    }

    public DingDingMessage setText(DingDingMessage.Data text) {
        this.text = text;
        return this;
    }

    @Override
    public String toString() {
        return "DingDingMessage{" +
                "msgType=" + msgType +
                ", link=" + link +
                ", markdown=" + markdown +
                ", at=" + at +
                ", text=" + text +
                '}';
    }

    @SuppressWarnings("unused")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LinkMessageBuilder {

        private String text;
        private String title;
        private String picUrl;
        private String messageUrl;

        public String getText() {
            return text;
        }

        public LinkMessageBuilder setText(String text) {
            this.text = text;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public LinkMessageBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public LinkMessageBuilder setPicUrl(String picUrl) {
            this.picUrl = picUrl;
            return this;
        }

        public String getMessageUrl() {
            return messageUrl;
        }

        public LinkMessageBuilder setMessageUrl(String messageUrl) {
            this.messageUrl = messageUrl;
            return this;
        }

        public DingDingMessage build() {
            return new DingDingMessage(this);
        }

        @Override
        public String toString() {
            return "LinkMessage{" +
                    "text='" + text + '\'' +
                    ", title='" + title + '\'' +
                    ", picUrl='" + picUrl + '\'' +
                    ", messageUrl='" + messageUrl + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TextMessageBuildr {

        private DingDingMessage.At at = new At();
        private DingDingMessage.Data text = new Data();

        public At getAt() {
            return at;
        }

        public TextMessageBuildr setAt(At at) {
            this.at = at;
            return this;
        }

        public Data getText() {
            return text;
        }

        public TextMessageBuildr setText(Data text) {
            this.text = text;
            return this;
        }

        public TextMessageBuildr setAtMobiles(String atMobiles) {
            this.at.atMobiles = atMobiles;
            return this;
        }

        public TextMessageBuildr setAtUserIds(String atUserIds) {
            this.at.atUserIds = atUserIds;
            return this;
        }

        public TextMessageBuildr setIsAtAll(Boolean isAtAll) {
            this.at.isAtAll = isAtAll;
            return this;
        }

        public TextMessageBuildr setContent(String content) {
            this.text.content = content;
            return this;
        }

        public DingDingMessage build() {
            return new DingDingMessage(this);
        }


        @Override
        public String toString() {
            return "TextMessage{" +
                    "at=" + at +
                    ", text=" + text +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Data {
        private String content;

        public String getContent() {
            return content;
        }

        public Data setContent(String content) {
            this.content = content;
            return this;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "content='" + content + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class At {
        private String atMobiles;
        private String atUserIds;
        private Boolean isAtAll;

        public String getAtMobiles() {
            return atMobiles;
        }

        public At setAtMobiles(String atMobiles) {
            this.atMobiles = atMobiles;
            return this;
        }

        public String getAtUserIds() {
            return atUserIds;
        }

        public At setAtUserIds(String atUserIds) {
            this.atUserIds = atUserIds;
            return this;
        }

        public Boolean getIsAtAll() {
            return isAtAll;
        }

        public At setIsAtAll(Boolean isAtAll) {
            this.isAtAll = isAtAll;
            return this;
        }

        @Override
        public String toString() {
            return "At{" +
                    "atMobiles='" + atMobiles + '\'' +
                    ", atUserIds='" + atUserIds + '\'' +
                    ", isAtAll='" + isAtAll + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MarkdownMessageBuilder {

        private String title;
        private String text;
        @JsonIgnore
        private DingDingMessage.At at = new At();

        public String getTitle() {
            return title;
        }

        public MarkdownMessageBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getText() {
            return text;
        }

        public MarkdownMessageBuilder setText(String text) {
            this.text = text;
            return this;
        }

        public At getAt() {
            return at;
        }

        public MarkdownMessageBuilder setAt(At at) {
            this.at = at;
            return this;
        }

        public MarkdownMessageBuilder setAtMobiles(String atMobiles) {
            this.at.atMobiles = atMobiles;
            return this;
        }

        public MarkdownMessageBuilder setAtUserIds(String atUserIds) {
            this.at.atUserIds = atUserIds;
            return this;
        }

        public MarkdownMessageBuilder setIsAtAll(Boolean isAtAll) {
            this.at.isAtAll = isAtAll;
            return this;
        }

        public DingDingMessage build() {
            return new DingDingMessage(this);
        }

        @Override
        public String toString() {
            return "MarkdownMessage{" +
                    "title='" + title + '\'' +
                    ", text='" + text + '\'' +
                    ", at=" + at +
                    '}';
        }
    }

}

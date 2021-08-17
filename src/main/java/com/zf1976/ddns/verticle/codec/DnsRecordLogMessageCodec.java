package com.zf1976.ddns.verticle.codec;

import com.zf1976.ddns.pojo.DnsRecordLog;
import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

import java.nio.charset.StandardCharsets;

/**
 * @author mac
 * 2021/8/17 星期二 3:18 上午
 */
public class DnsRecordLogMessageCodec implements MessageCodec<DnsRecordLog, DnsRecordLog> {
    @Override
    public void encodeToWire(Buffer buffer, DnsRecordLog dnsRecordLog) {
        final var jsonString = Json.encodePrettily(dnsRecordLog);
        buffer.appendInt(jsonString.length());
        buffer.appendBytes(jsonString.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public DnsRecordLog decodeFromWire(int pos, Buffer buffer) {
        final var length = buffer.getInt(pos);
        pos += 4;
        final var bytes = buffer.getBytes(pos, pos + length);
        final var jsonString = new String(bytes, CharsetUtil.UTF_8);
        return Json.decodeValue(jsonString, DnsRecordLog.class);
    }

    @Override
    public DnsRecordLog transform(DnsRecordLog dnsRecordLog) {
        return dnsRecordLog;
    }

    @Override
    public String name() {
        return "DnsRecordLog";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}

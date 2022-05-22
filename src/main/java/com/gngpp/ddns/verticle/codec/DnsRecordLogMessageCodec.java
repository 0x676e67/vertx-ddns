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

package com.gngpp.ddns.verticle.codec;

import com.gngpp.ddns.pojo.DnsRecordLog;
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

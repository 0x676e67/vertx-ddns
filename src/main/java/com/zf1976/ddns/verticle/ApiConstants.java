/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 zf1976
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

package com.zf1976.ddns.verticle;

/**
 * @author mac
 * @date 2021/7/9
 */
public interface ApiConstants {

    String SESSION_NAME = "ddns-vertx.session";

    String LOGIN_PATH = "/login.html";

    String INDEX_PATH = "/index.html";

    String DDNS_PROVIDER_TYPE = "dnsProviderType";

    String DOMAIN = "domain";

    String RECORD_ID = "recordId";

    String DNS_RECORD_TYPE = "dnsRecordType";

    String SERVER_PORT = "serverPort";

    String CONFIG_SUBJECT_ADDRESS = "config.subject.address";

    String DEFAULT_CONFIG_PERIODIC_ID = "default.config.periodic.id";

    String RUNNING_CONFIG_ID = "running.config.periodic.id";

    String VERTICLE_PERIODIC_DEPLOY_ID = "verticle.periodic.id";

    String SHARE_MAP_ID = "sockjs.d";

    String SOCKJS_WRITE_HANDLER_ID = "sockjs.write.handler.id";

    String SOCKJS_SELECT_PROVIDER_TYPE = "sockjs.select.provider.type";

    String PERIODIC = "periodicId";

    String IPV4_TYPE_KEY_WORD = "A";

    String IPV6_TYPE_KEY_WORD = "AAAA";
}

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

    String STORE_DNS_RECORD_LOG_ID = "store.dns.record.id.log.id";

    String PERIODIC = "periodicId";

    String IPV4_TYPE_KEY_WORD = "A";

    String IPV6_TYPE_KEY_WORD = "AAAA";
}

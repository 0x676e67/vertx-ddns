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

    String CONFIG_PERIODIC_ID = "config.periodic.id";

    String VERTICLE_PERIODIC_DEPLOY_ID = "verticle.periodic.id";

    String SOCKJS_ID = "sockjs__id";

    String SOCKJS_WRITE_HANDLER_ID = "sockjs_write_handler_id";

    String PERIODIC = "periodicId";

    String IPV4_TYPE_KEY_WORD = "A";

    String IPV6_TYPE_KEY_WORD = "AAAA";
}

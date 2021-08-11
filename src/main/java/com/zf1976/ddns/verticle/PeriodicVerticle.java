package com.zf1976.ddns.verticle;

import com.zf1976.ddns.verticle.timer.Observer;
import com.zf1976.ddns.verticle.timer.Subject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

/**
 * @author mac
 * 2021/8/9 星期一 11:22 下午
 */
public class PeriodicVerticle extends AbstractVerticle implements Subject {


    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void addObserver(Observer obj) {

    }

    @Override
    public void deleteObserver(Observer obj) {

    }

    @Override
    public void notifyObserver() {

    }
}

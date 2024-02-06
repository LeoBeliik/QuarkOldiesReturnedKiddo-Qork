package com.leobeliik.qork.base.proxy;

import com.leobeliik.qork.base.Qork;
import com.leobeliik.qork.base.QorkClient;
import com.leobeliik.qork.base.client.handler.ModelHandler;

public class ClientProxy extends CommonProxy {

    @Override
    public void start() {
        QorkClient.start();

        Qork.QORK_ZETA.loadBus.subscribe(ModelHandler.class);

        super.start();
    }
}

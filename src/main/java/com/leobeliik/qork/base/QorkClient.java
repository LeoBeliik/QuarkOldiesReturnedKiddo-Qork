package com.leobeliik.qork.base;

import org.violetmoon.zeta.client.ClientTicker;
import org.violetmoon.zeta.client.ZetaClient;
import org.violetmoon.zetaimplforge.client.ForgeZetaClient;

public class QorkClient {
    public static QorkClient instance;

    public static final ZetaClient QORK_ZETA_CLIENT = new ForgeZetaClient(Qork.QORK_ZETA);
    public static final ClientTicker ticker = QORK_ZETA_CLIENT.ticker;


    public static void start() {
        instance = new QorkClient();

        QORK_ZETA_CLIENT.start();
    }
}

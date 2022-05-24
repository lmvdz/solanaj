package org.p2p.solanaj.core;

import org.junit.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

public class BlockhashValidTest {

    @Test
    public void testBlockhashValidity() {
        final RpcClient client = new RpcClient(Cluster.MAINNET);
        String blockhash = "AxEpmSurL43n3oRs9jQ1uM68pjMgcRPnaEeBanQZsarj";
        try {
            assert !client.getApi().isBlockhashValid(blockhash);
        } catch (RpcException e) {
            e.printStackTrace();
        }
    }
}

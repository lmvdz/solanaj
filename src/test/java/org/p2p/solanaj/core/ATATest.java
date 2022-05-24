package org.p2p.solanaj.core;

import org.junit.Test;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;

import java.math.BigDecimal;

public class ATATest {

    @Test
    public void testATA() {

        final RpcClient client = new RpcClient(Cluster.MAINNET);
        final PublicKey owner = new PublicKey("8Ci5UbpoAFL5sAj4jeKwADceYrDxKQktXJnn1Vwgug5m");
        final PublicKey tokenMintAddress = new PublicKey("FTkj421DxbS1wajE74J34BJ5a1o9ccA97PkK6mYq9hNQ");
//        Mint tokenMint = Mint.getMint(client, tokenMintAddress);
        try {
            PublicKey ataAddress = Mint.getAssociatedTokenAddress(tokenMintAddress, owner);
            AssociatedTokenAccount account = AssociatedTokenAccount.getATA(client, ataAddress);
            BigDecimal balance = account.getBalance(client);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

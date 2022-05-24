package org.p2p.solanaj.programs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.Mint;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import static org.bitcoinj.core.Utils.*;
import static org.p2p.solanaj.core.Mint.*;

public class SystemProgram {
    public static final PublicKey PROGRAM_ID = new PublicKey("11111111111111111111111111111111");

    public static final int PROGRAM_INDEX_CREATE_ACCOUNT = 0;
    public static final int PROGRAM_INDEX_TRANSFER = 2;


    public static TransactionInstruction transfer(PublicKey fromPublicKey, PublicKey toPublicKey, long lamports) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(fromPublicKey, true, true));
        keys.add(new AccountMeta(toPublicKey, false, true));

        // 4 byte instruction index + 8 bytes lamports
        byte[] data = new byte[4 + 8];
        uint32ToByteArrayLE(PROGRAM_INDEX_TRANSFER, data, 0);
        int64ToByteArrayLE(lamports, data, 4);

        return new TransactionInstruction(PROGRAM_ID, keys, data);
    }


    public static TransactionInstruction createAccount(PublicKey fromPublicKey, PublicKey newAccountPublicKey,
            long lamports, long space, PublicKey programId) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(fromPublicKey, true, true));
        keys.add(new AccountMeta(newAccountPublicKey, true, true));

        byte[] data = new byte[4 + 8 + 8 + 32];
        uint32ToByteArrayLE(PROGRAM_INDEX_CREATE_ACCOUNT, data, 0);
        int64ToByteArrayLE(lamports, data, 4);
        int64ToByteArrayLE(space, data, 12);
        System.arraycopy(programId.toByteArray(), 0, data, 20, 32);

        return new TransactionInstruction(PROGRAM_ID, keys, data);
    }
}

package org.p2p.solanaj.core;

import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Base64;

import static org.bitcoinj.core.Utils.reverseBytes;

public class AssociatedTokenAccount {

    PublicKey address;
    PublicKey mint;
    PublicKey owner;
    BigInteger amount;
    PublicKey delegate;
    BigInteger delegatedAmount;
    boolean isInitialized;
    boolean isFrozen;
    boolean isNative;
    BigInteger rentExemptReserve;
    PublicKey closeAuthority;
    byte[] tlvData;

    public AssociatedTokenAccount() {

    }

    public AssociatedTokenAccount(PublicKey address, PublicKey mint, PublicKey owner, BigInteger amount, PublicKey delegate, BigInteger delegatedAmount, boolean isInitialized, boolean isFrozen, boolean isNative, BigInteger rentExemptReserve, PublicKey closeAuthority, byte[] tlvData) {
        this.address = address;
        this.mint = mint;
        this.owner = owner;
        this.amount = amount;
        this.delegate = delegate;
        this.delegatedAmount = delegatedAmount;
        this.isInitialized = isInitialized;
        this.isFrozen = isFrozen;
        this.isNative = isNative;
        this.rentExemptReserve = rentExemptReserve;
        this.closeAuthority = closeAuthority;
        this.tlvData = tlvData;
    }

    public BigDecimal getBalance(RpcClient client, Mint mint) {
        return mint.convertAmountToHuman(this.amount);
    }


    public static AssociatedTokenAccount getATA(RpcClient client, PublicKey address) {
        AssociatedTokenAccount ata = new AssociatedTokenAccount();

        try {
            AccountInfo.Value account = client.getApi().getAccountInfo(address).getValue();


            Base64.Decoder dec = Base64.getDecoder();
            byte[] mintData = dec.decode(account.getData().get(0));

            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(mintData))) {

                PublicKey mint = new PublicKey(in.readNBytes(32));
                PublicKey owner = new PublicKey(in.readNBytes(32));
                BigInteger amount = new BigInteger(reverseBytes(in.readNBytes(8)));
                int delegateOption = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                PublicKey delegate = new PublicKey(in.readNBytes(32));
                byte state = in.readByte();
                int isNativeOption = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                BigInteger isNative = new BigInteger(reverseBytes(in.readNBytes(8)));
                BigInteger delegatedAmount = new BigInteger(reverseBytes(in.readNBytes(8)));
                int closeAuthorityOption = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                PublicKey closeAuthority = new PublicKey(in.readNBytes(32));

//                System.out.printf("%s %s %s %s %s %s %s %s %s %s %s \n", mint, owner, amount, delegateOption, delegate, state, isNativeOption, isNative, delegatedAmount, closeAuthorityOption, closeAuthority);

                ata = new AssociatedTokenAccount(address, mint, owner, amount, delegateOption == 1 ? delegate : null, delegatedAmount, state == 0, state == 2, isNativeOption == 1, isNativeOption == 1 ? isNative : null, closeAuthorityOption == 1 ? closeAuthority : null, new byte[]{});
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (RpcException e) {
            e.printStackTrace();
        }
        return ata;
    }
}

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
import java.util.ArrayList;
import java.util.Base64;

import static org.bitcoinj.core.Utils.int64ToByteArrayLE;
import static org.bitcoinj.core.Utils.reverseBytes;

public class Mint {

    public static final PublicKey ASSOCIATED_TOKEN_PROGRAM_ID = new PublicKey("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL");
    public static final PublicKey TOKEN_PROGRAM_ID = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    public static final PublicKey TOKEN_PROGRAM_ID_2022 = new PublicKey("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");
    public static final int SPL_INDEX_TRANSFER = 3;

    public PublicKey address;
    public PublicKey mintAuthority;
    public BigInteger supply;
    public int decimals;
    public boolean isInitialized;
    public PublicKey freezeAuthority;
    public byte[] tlvData;


    public Mint() {

    }

    public Mint(PublicKey address, PublicKey mintAuthority, BigInteger supply, int decimals, boolean isInitialized, PublicKey freezeAuthority, byte[] tlvData) {
        this.address = address;
        this.mintAuthority = mintAuthority;
        this.supply = supply;
        this.decimals = decimals;
        this.isInitialized = isInitialized;
        this.freezeAuthority = freezeAuthority;
        this.tlvData = tlvData;
    }

    public BigInteger getMaxSupply() {
        return this.supply.divide(new BigInteger("10").pow(this.decimals));
    }

    public BigInteger convertAmountFromHuman(double amount) {
        return (new BigDecimal(String.valueOf(amount)).multiply(new BigDecimal("10").pow(this.decimals))).toBigInteger();
    }

    public BigDecimal convertAmountToHuman(BigInteger amount) {
        return new BigDecimal(amount).divide(new BigDecimal("10").pow(this.decimals));
    }

    public static ArrayList<AccountMeta> addSigners(ArrayList<AccountMeta> keys, PublicKey ownerOrAuthority, ArrayList<PublicKey> multiSigners) {
        if (multiSigners.size() > 0) {
            keys.add(new AccountMeta(ownerOrAuthority, false, false));
            for (PublicKey multiSigner : multiSigners) {
                keys.add(new AccountMeta(multiSigner, true, false));
            }
        } else {
            keys.add(new AccountMeta(ownerOrAuthority, true, false));
        }
        return keys;
    }

    public static PublicKey getAssociatedTokenAddress(PublicKey mint, PublicKey owner) throws Exception {
        return Mint.getAssociatedTokenAddress(mint, owner, false, TOKEN_PROGRAM_ID, ASSOCIATED_TOKEN_PROGRAM_ID);
    }

    public static PublicKey getAssociatedTokenAddress(PublicKey mint, PublicKey owner, boolean allowOwnerOffCurve, PublicKey programId, PublicKey associatedProgramId) throws Exception {

        if (!allowOwnerOffCurve && !PublicKey.isOnCurve(owner)) throw new Error("TokenOwnerOffCurveError");

        ArrayList<byte[]> seeds = new ArrayList<>();
        seeds.add(owner.toByteArray());
        seeds.add(programId.toByteArray());
        seeds.add(mint.toByteArray());
        return PublicKey.findProgramAddress(seeds, associatedProgramId).getAddress();
    }



    public static TransactionInstruction transferSPL(Mint tokenMint, PublicKey fromPublicKey, PublicKey toPublicKey, PublicKey owner, double amount) throws Exception {
        return transferSPL(tokenMint, fromPublicKey, toPublicKey, owner, amount, new ArrayList<>());
    }

    public static TransactionInstruction transferSPL(Mint tokenMint, PublicKey fromPublicKey, PublicKey toPublicKey, PublicKey owner, double amount, ArrayList<PublicKey> multiSigners) throws Exception {
        return transferSPL(tokenMint, fromPublicKey, toPublicKey, owner, amount, multiSigners, TOKEN_PROGRAM_ID);
    }

    public static TransactionInstruction transferSPL(Mint tokenMint, PublicKey fromPublicKey, PublicKey toPublicKey, PublicKey owner, double amount, ArrayList<PublicKey> multiSigners, PublicKey programId) throws Exception {

        PublicKey fromTokenPublicKey = getAssociatedTokenAddress(tokenMint.address, fromPublicKey, false, TOKEN_PROGRAM_ID, ASSOCIATED_TOKEN_PROGRAM_ID);
        PublicKey toTokenPublicKey = getAssociatedTokenAddress(tokenMint.address, toPublicKey, false, TOKEN_PROGRAM_ID, ASSOCIATED_TOKEN_PROGRAM_ID);

        ArrayList<AccountMeta> fromTo = new ArrayList<>();

        fromTo.add(new AccountMeta(fromTokenPublicKey, false, true));
        fromTo.add(new AccountMeta(toTokenPublicKey, false, true));

        ArrayList<AccountMeta> keys = addSigners(fromTo, owner, multiSigners);

        // 1 byte instruction index + 8 bytes lamports
        byte[] data = new byte[1 + 8];
        data[0] = SPL_INDEX_TRANSFER;

        int64ToByteArrayLE(tokenMint.convertAmountFromHuman(amount).longValue(), data, 1);

        return new TransactionInstruction(programId, keys, data);
    }

    public static Mint getMint(RpcClient client, PublicKey address) {
        Mint mint = new Mint();
        try {
            AccountInfo.Value account = client.getApi().getAccountInfo(address).getValue();
            if (account != null) {
                if (!account.getOwner().equals(TOKEN_PROGRAM_ID.toString())) {
                    throw new Error("TokenInvalidAccountOwnerError");
                }

                Base64.Decoder dec = Base64.getDecoder();
                byte[] mintData = dec.decode(account.getData().get(0));

                try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(mintData))) {

                    int mintAuthorityOption = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                    PublicKey mintAuthority = new PublicKey(in.readNBytes(32));
                    if (mintAuthorityOption == 0) {
                        mintAuthority = null;
                    }
                    BigInteger supply = new BigInteger(reverseBytes(in.readNBytes(8)));
                    byte decimals = in.readByte();
                    byte isInitialized = in.readByte();
                    int freezeAuthorityOption = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                    PublicKey freezeAuthority = new PublicKey(in.readNBytes(32));

                    if (freezeAuthorityOption == 0) {
                        freezeAuthority = null;
                    }
                    mint = new Mint(address, mintAuthority, supply, decimals, isInitialized == 1, freezeAuthority, new byte[]{});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                throw new Error("TokenAccountNotFound");
            }

        } catch (RpcException e) {
            e.printStackTrace();
        }
        return mint;
    }
}

package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;

public class BlockhashValid extends RpcResultObject {


    @Json(name = "value")
    private boolean value;

    public boolean isValid() {
        return value;
    }

}

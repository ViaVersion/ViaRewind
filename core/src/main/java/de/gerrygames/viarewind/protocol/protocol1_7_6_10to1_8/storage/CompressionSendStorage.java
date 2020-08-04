package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

public class CompressionSendStorage extends StoredObject {
    private boolean neutralizeCompression = false;

    public CompressionSendStorage(UserConnection user) {
        super(user);
    }

    public boolean isNeutralizeCompression() {
        return neutralizeCompression;
    }

    public void setNeutralizeCompression(boolean neutralizeCompression) {
        this.neutralizeCompression = neutralizeCompression;
    }
}

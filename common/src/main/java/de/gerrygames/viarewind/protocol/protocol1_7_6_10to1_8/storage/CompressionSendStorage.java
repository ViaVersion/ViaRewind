package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

public class CompressionSendStorage extends StoredObject {
	private boolean removeCompression = false;

	public CompressionSendStorage(UserConnection user) {
		super(user);
	}

	public boolean isRemoveCompression() {
		return removeCompression;
	}

	public void setRemoveCompression(boolean removeCompression) {
		this.removeCompression = removeCompression;
	}
}

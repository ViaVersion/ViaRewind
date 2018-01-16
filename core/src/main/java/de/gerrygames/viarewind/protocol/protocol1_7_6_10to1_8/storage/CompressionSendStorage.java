package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import lombok.Data;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

@Data
public class CompressionSendStorage extends StoredObject {

	private boolean compressionSend = false;

	public CompressionSendStorage(UserConnection user) {
		super(user);
	}
}

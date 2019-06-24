package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_7_1_5;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.remapper.ValueTransformer;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

// Based on https://github.com/Gerrygames/ClientViaVersion
public class Protocol1_7_6_10To1_7_1_5 extends Protocol {
    public static final ValueTransformer<String, String> INSERT_DASHES = new ValueTransformer<String, String>(Type.STRING) {
        @Override
        public String transform(PacketWrapper packetWrapper, String s) {
            StringBuilder builder = new StringBuilder(s);
            builder.insert(20, "-");
            builder.insert(16, "-");
            builder.insert(12, "-");
            builder.insert(8, "-");
            return builder.toString();
        }
    };

    @Override
    protected void registerPackets() {
        //Login Success
        this.registerOutgoing(State.LOGIN, 0x02, 0x02, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING, INSERT_DASHES);
            }
        });

        //Spawn Player
        this.registerOutgoing(State.PLAY, 0x0C, 0x0C, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.STRING, INSERT_DASHES);
                map(Type.STRING);
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper packetWrapper) {
                        packetWrapper.write(Type.VAR_INT, 0);
                    }
                });
            }
        });
    }

    @Override
    public void init(UserConnection userConnection) {
    }
}
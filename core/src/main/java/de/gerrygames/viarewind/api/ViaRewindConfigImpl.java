package de.gerrygames.viarewind.api;

import us.myles.ViaVersion.util.Config;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ViaRewindConfigImpl extends Config implements ViaRewindConfig {
    public ViaRewindConfigImpl(File configFile) {
        super(configFile);
        reloadConfig();
    }

    @Override
    public CooldownIndicator getCooldownIndicator() {
        return CooldownIndicator.valueOf(getString("cooldown-indicator", "TITLE").toUpperCase());
    }

    @Override
    public boolean isReplaceAdventureMode() {
        return getBoolean("replace-adventure", false);
    }

    @Override
    public boolean isReplaceParticles() {
        return getBoolean("replace-particles", false);
    }

    @Override
    public URL getDefaultConfigURL() {
        return getClass().getClassLoader().getResource("assets/viarewind/config.yml");
    }

    @Override
    protected void handleConfig(Map<String, Object> map) {

    }

    @Override
    public List<String> getUnsupportedOptions() {
        return Collections.emptyList();
    }
}

package com.siemens.cto.aem.control.jvm.command.impl;

import java.util.EnumMap;
import java.util.Map;

import com.siemens.cto.aem.control.jvm.command.ServiceCommandBuilder;
import com.siemens.cto.aem.control.jvm.command.windows.WindowsJvmNetOperation;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.platform.Platform;

public enum PlatformCommandProvider {

    WINDOWS(Platform.WINDOWS) {
        @Override
        public ServiceCommandBuilder getServiceCommandBuilderFor(final JvmControlOperation anOperation) {
            return WindowsJvmNetOperation.lookup(anOperation);
        }
    };

    private static final Map<Platform, PlatformCommandProvider> LOOKUP_MAP = new EnumMap<>(Platform.class);

    static {
        for (final PlatformCommandProvider p : values()) {
            LOOKUP_MAP.put(p.platform, p);
        }
    }

    private final Platform platform;

    private PlatformCommandProvider(final Platform thePlatform) {
        platform = thePlatform;
    }

    public static PlatformCommandProvider lookup(final Platform aPlatform) {
        return LOOKUP_MAP.get(aPlatform);
    }

    public abstract ServiceCommandBuilder getServiceCommandBuilderFor(JvmControlOperation anOperation);
}

package io.github.hadron13.rubberworks.config.server;

import net.createmod.catnip.config.ConfigBase;

public class GBServer extends ConfigBase {

    public final GBKinetics kinetics = this.nested(0, GBKinetics::new, "Parameters and abilities of Rubberworks's kinetic mechanisms");

    @Override
    public String getName() {
        return "server";
    }
}

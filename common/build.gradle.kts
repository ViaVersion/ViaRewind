plugins {
    id("via.addon_subproject")
    id("extra.fill_build_constants")
    id("via.run_viaproxy")
}

dependencies {
    compileOnly(libs.netty.all)
    compileOnly(libs.guava)
    compileOnly(libs.velocity.native)
}

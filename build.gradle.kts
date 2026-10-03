plugins {
    id("net.fabricmc.fabric-loom") version "1.15.5" apply false
}
tasks.register("buildAll") { dependsOn(":v26_1_2:build") }

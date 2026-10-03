plugins {
    id("net.fabricmc.fabric-loom")
    id("maven-publish")
}
val minecraft_version: String by project
val loader_version: String by project
val fabric_version: String by project
val malilib_version: String by project
val litematica_version: String by project
val mod_version: String by project
val archives_base_name: String by project
version = "$mod_version+mc$minecraft_version"
group = "me.aleksilassila"
base { archivesName.set(archives_base_name) }
repositories {
    mavenCentral()
    maven("https://maven.fallenbreath.me/releases")
    maven("https://masa.dy.fi/maven/sakura-ryoko")
}
dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    implementation("net.fabricmc:fabric-loader:$loader_version")
    compileOnly("io.github.llamalad7:mixinextras-fabric:0.5.4")
    annotationProcessor("io.github.llamalad7:mixinextras-fabric:0.5.4")
    implementation("fi.dy.masa.malilib:malilib-fabric-$minecraft_version:$malilib_version")
    implementation("fi.dy.masa.litematica:litematica-fabric-$minecraft_version:$litematica_version")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
}
java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
    options.encoding = "UTF-8"
}
tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") { expand("version" to project.version) }
}

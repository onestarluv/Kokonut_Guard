import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("buildlogic.platform")
}

dependencies {
    "api"(project(":worldguard-core"))
    "api"(libs.worldedit.bukkit) { isTransitive = false }
    "compileOnly"(libs.commandbook) { isTransitive = false }

    "compileOnly"(libs.jetbrains.annotations) {
        because("Resolving Spigot annotations")
    }
    "testCompileOnly"(libs.jetbrains.annotations) {
        because("Resolving Spigot annotations")
    }
    "compileOnly"(libs.paperApi) {
        exclude("org.slf4j", "slf4j-api")
        exclude("junit", "junit")
    }

    "implementation"(libs.paperLib)
    "implementation"(libs.bstats.bukkit)
}

tasks.named<Copy>("processResources") {
    val pluginVersion = "cbt-1.0"
    inputs.property("pluginVersion", pluginVersion)
    filesMatching("plugin.yml") {
        expand("pluginVersion" to pluginVersion)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("Kokonut_Guard-cbt-1.0.jar")
    dependencies {
        include(dependency(":worldguard-core"))
        include(dependency("org.bstats:bstats-bukkit"))
        include(dependency("org.bstats:bstats-base"))
        include(dependency("io.papermc:paperlib"))

        relocate("org.bstats", "com.sk89q.worldguard.bukkit.bstats")
        relocate("io.papermc.lib", "com.sk89q.worldguard.bukkit.paperlib")
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        from(components["java"])
    }
}

val collectPluginArtifact = tasks.register<Copy>("collectPluginArtifact") {
    val artifact = tasks.named<ShadowJar>("shadowJar")
    dependsOn(artifact)
    from(artifact.flatMap { it.archiveFile })
    into(rootProject.layout.projectDirectory.dir("../plugin"))
}
tasks.named("shadowJar") { finalizedBy(collectPluginArtifact) }

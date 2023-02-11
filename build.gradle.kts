import net.minecraftforge.gradle.userdev.UserDevExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.spongepowered.asm.gradle.plugins.MixinExtension

plugins {
    kotlin("jvm") version "1.7.21"
}

buildscript {
    repositories {
        maven { url = uri("https://repo.siro256.dev/repository/maven-public/") }
    }

    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:5.1.+") {
            isChanging = true
        }
        classpath("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    }
}

apply {
    plugin("net.minecraftforge.gradle")
    plugin("org.spongepowered.mixin")
}

group = "dev.siro256.forgemod.expandmaxcollision"
version = "1.0.0-SNAPSHOT"

repositories {
    maven { url = uri("https://repo.siro256.dev/repository/maven-public/") }
}

configurations.create("includeToJar")

dependencies {
    add("minecraft", "net.minecraftforge:forge:1.12.2-14.23.5.2860")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    implementation("org.spongepowered:mixin:0.8.5")
    add("includeToJar", "org.spongepowered:mixin:0.8.5")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

configure<UserDevExtension> {
    mappings("snapshot", "20171003-1.12")
}

configure<MixinExtension> {
    add(sourceSets.main.get(), "expandmaxcollision.refmap.json")
    config("expandmaxcollision.mixins.json")
}

tasks {
    processResources {
        //Replace placeholder
        filesMatching("mcmod.info") {
            mapOf(
                "modName" to project.name,
                "modId" to project.name.toLowerCase(),
                "modVersion" to project.version.toString()
            ).let {
                filter<ReplaceTokens>("tokens" to it)
            }
        }

        //Include license file
        from(project.file("LICENSE").path) {
            rename { "LICENSE_${project.name}" }
        }

        from(project.file("README.md").path) {
            rename { "README_${project.name}.md" }
        }
    }

    withType<Jar> {
        from(configurations.getByName("includeToJar").copy().apply { isCanBeResolved = true }
            .map { if (it.isDirectory) it else zipTree(it) })

        mapOf(
            "ForceLoadAsMod" to "true",
            "FMLCorePluginContainsFMLMod" to "true",
            "FMLCorePlugin" to "dev.siro256.forgemod.expandmaxcollision.ExpandMaxCollision",
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker"
        ).let { manifest.attributes(it) }
    }
}

rootProject.name = "dhis2-android-capture-app"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://central.sonatype.com/repository/maven-snapshots")
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

include(
    ":app",
    ":dhis_android_analytics", ":form", ":commons",
    ":dhis2_android_maps", ":compose-table",
    ":stock-usecase"
)
include(":dhis2-mobile-program-rules")
include(":tracker")
include(":aggregates")
include(":commonskmm")
include(":login")
include(":sync")

// SEMIS modules
include(":semis")
include(":semis:attendance")
include(":semis:performance")
include(":semis:transfer")
include(":semis:core")
include(":semis:core:data")
include(":semis:core:form")
include(":semis:core:designsystem")
include(":semis:core:utils")
include(":semis:app")
include(":semis:enrollment")
include(":semis-core")
include(":semis-core:data")
include(":semis-core:designsystems")
include(":semis-core:form")
include(":semis-core:navigation")
include(":semis-core:utils")

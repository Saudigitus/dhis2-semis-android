pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
include(
    ":app",
    ":dhis_android_analytics", ":form", ":commons",
    ":dhis2_android_maps", ":compose-table", ":ui-components",
    ":stock-usecase"
)
include(":dhis2-mobile-program-rules")
include(":tracker")
include(":aggregates")
include(":commonskmm")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
include(":semis")
include(":semis:attendance")
include(":semis:performance")
include(":semis:core")
include(":semis:core:data")
include(":semis:core:form")
include(":semis:core:designsystem")
include(":semis:core:utils")
include(":semis:app")
include(":semis-core")
include(":semis-core:data")
include(":semis-core:designsystems")
include(":semis-core:form")
include(":semis-core:navigation")
include(":semis-core:utils")
include(":semis:enrollment")

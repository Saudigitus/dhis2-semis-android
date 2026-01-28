package org.dhis2.usescases.about

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.bindings.buildInfo
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.homeRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun shouldCheckVersionsWhenOpenAboutScreen() {
        startActivity()
        val appVersion = getAppVersionName()
        val sdkVersion = getSDKVersionName()
        val semisVersion = getSEMISVersionName()
        val datastoreVersion = getDatastoreVersionName()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickAbout()
        }

        aboutRobot {
            checkVersionNames(appVersion, sdkVersion, semisVersion, datastoreVersion)
        }
    }

    private fun startActivity() {
        rule.launchActivity(null)
    }

    private fun getAppVersionName(): String {
        return context.buildInfo()
    }

    private fun getSDKVersionName() =
        String.format(context.getString(R.string.about_sdk), BuildConfig.SDK_VERSION)

    private fun getSEMISVersionName() =
        String.format(context.getString(R.string.about_semis), BuildConfig.SEMIS_VERSION)

    private fun getDatastoreVersionName() =
        String.format(context.getString(R.string.about_datastore), BuildConfig.DATASTORE_VERSION)

}

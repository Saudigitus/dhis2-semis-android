package org.dhis2.data.server

/**
 * The version this app gives the DHIS2 SDK for the User-Agent of every request.
 *
 * The SDK writes it as app/version/sdk/Android_<api>, the pattern the Capture App uses, so the
 * Capture App version goes into the version field as build metadata. The server logs then read,
 * for example, com.dhis2.semis.test/2.1.0-rc4+capture.3.4.2/1.14.2/Android_36: the channel, the
 * SEMIS version, the Capture App it is built on, the SDK and Android.
 */
fun clientVersion(
    semisVersion: String,
    captureVersion: String,
): String = "$semisVersion+capture.$captureVersion"

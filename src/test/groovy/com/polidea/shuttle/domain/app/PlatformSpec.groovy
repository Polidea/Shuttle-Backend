package com.polidea.shuttle.domain.app

import com.polidea.shuttle.TestConstants
import spock.lang.Specification

import static Platform.ANDROID
import static Platform.determinePlatformFromText

class PlatformSpec extends Specification {

    def "should throw NullPointerException when null is given"() {
        when:
        determinePlatformFromText(null)

        then:
        NullPointerException thrownException = thrown()
        thrownException.getMessage() == "'platform' must not be null" as String
    }

    def "should determine platform properly"() {
        when:
        Platform platform = determinePlatformFromText(TestConstants.ANDROID_PLATFORM_NAME)

        then:
        platform == ANDROID
    }

    def "should throw exception when invalid platform is given "() {
        given:
        def invalidPlatform = 'invalid'

        when:
        determinePlatformFromText(invalidPlatform)

        then:
        InvalidPlatformException thrownException = thrown()
        thrownException.getMessage() == "Platform '$invalidPlatform' is invalid" as String
    }
}

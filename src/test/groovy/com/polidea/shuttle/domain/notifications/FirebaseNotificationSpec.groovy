package com.polidea.shuttle.domain.notifications

import com.polidea.shuttle.domain.notifications.output.FirebaseNotification
import com.polidea.shuttle.domain.notifications.output.NotificationAboutPublishedBuild
import spock.lang.Specification

class FirebaseNotificationSpec extends Specification {

    def anyProjectId = 123

    def "throw exception if there are too many push tokens for FirebaseNotification"() {
        given:
        def pushTokens = (1..301).collect {
            it.toString()
        } as Set

        when:
        new NotificationAboutPublishedBuild(pushTokens, "buildVersion", anyProjectId, "appId", "appName", "appIconHref", "someIcon", 300)

        then:
        thrown(FirebaseNotification.TooManyPushTokensInBatchException)
    }

    def "no exception thrown if number of push tokens equals limit for FirebaseNotification"() {
        given:
        def pushTokens = (1..300).collect {
            it.toString()
        } as Set

        when:
        new NotificationAboutPublishedBuild(pushTokens, "buildVersion", anyProjectId, "appId", "appName", "appIconHref", "someIcon", 300)

        then:
        noExceptionThrown()
    }
}

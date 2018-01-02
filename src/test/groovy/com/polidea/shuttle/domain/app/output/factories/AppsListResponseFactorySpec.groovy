package com.polidea.shuttle.domain.app.output.factories

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.app.output.AdminAppListResponse
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks
import spock.lang.Specification

class AppsListResponseFactorySpec extends Specification {

    AppsListResponseFactory appsListResponseFactory = new AppsListResponseFactory(Mock(PermissionChecks))

    def "should return empty list response when empty app list is given"() {
        when:
        AdminAppListResponse result = appsListResponseFactory.createAdminAppListResponse(new ArrayList<App>(), false)

        then:
        result.apps == []
    }

    def "should return app responses listed when not empty app list is given"() {
        given:
        App givenApp = new App(project: anyProject())

        when:
        AdminAppListResponse result = appsListResponseFactory.createAdminAppListResponse([givenApp], false)

        then:
        result.apps.size() == 1
    }

    private Project anyProject() {
        return new Project()
    }
}

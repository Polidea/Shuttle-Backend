package com.polidea.shuttle.infrastructure

import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

class RegularExpressionsSpec extends Specification {

    @Unroll
    def "valid e-mail '#validEmail' should be recognized as correct"(String validEmail) {
        expect:
        Pattern.matches(RegularExpressions.EMAIL_REGEX, validEmail) == true

        where:
        validEmail << [
                'a@b.cd',
                'oneword@shuttle.com',
                'dots.between.words@shuttle.com',
                'pluses+between+words@shuttle.com',
                'dot.and+plus@shuttle.com'
        ]
    }

    @Unroll
    def "invalid e-mail '#invalidEmail' should be recognized as not correct (reason: #reason)"(String invalidEmail, String reason) {
        expect:
        Pattern.matches(RegularExpressions.EMAIL_REGEX, invalidEmail) == false

        where:
        invalidEmail | reason
        'a@b.c'      | 'top-domain has only only character'
        'a@.c'       | 'missing domain'
        'a@b.'       | 'missing top-domain'
        '@b.cd'      | 'missing user'
        '@'          | '@ only'
        ''           | 'empty e-mail'
    }

}

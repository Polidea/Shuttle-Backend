package com.polidea.shuttle.infrastructure.time

import spock.lang.Specification
import spock.lang.Unroll

class PeriodConverterSpec extends Specification {

    @Unroll
    def "should return #periodInMillis ms on #period period string"(String period, long periodInMillis) {
        expect:
        PeriodConverter.periodToMillis(period) == periodInMillis

        where:
        period         | periodInMillis
        '0s'           | 0
        '0s0m0d0h'     | 0
        '1s'           | 1000
        '1m1s'         | 61000
        '1s1m'         | 61000
        '1m2s'         | 62000
        '2m1s'         | 121000
        '2m10s'        | 130000
        '1h1m'         | 3660000
        '1d'           | 86400000
        '90d'          | 7776000000
        '30d'          | 2592000000
        '90d10h10m11s' | 7812611000
        '150d'         | 12960000000
        '240d'         | 20736000000
    }

    def "should throw InvalidFormatException when period is badly formatted"() {
        when:
        PeriodConverter.periodToMillis("invalidPeriodFormat")

        then:
        def exception = thrown(IllegalArgumentException)
        exception.getMessage() == 'invalidPeriodFormat is not a period that can be parsed.'

    }

    def "should make no difference if it's uppercase or lowercase"() {
        when:
        def uppercaseResult = PeriodConverter.periodToMillis('1D1H1M1S')
        def lowercaseResult = PeriodConverter.periodToMillis('1d1h1m1s')

        then:
        uppercaseResult == lowercaseResult
    }
}

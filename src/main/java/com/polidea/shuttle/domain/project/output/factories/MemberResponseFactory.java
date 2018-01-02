package com.polidea.shuttle.domain.project.output.factories;

import com.polidea.shuttle.domain.project.output.MemberResponse;
import com.polidea.shuttle.domain.user.User;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

class MemberResponseFactory {

    List<MemberResponse> createMemberResponsesFor(Set<User> members) {
        return members.stream()
                      .map(member -> new MemberResponse(member))
                      .collect(toList());
    }

}

package com.polidea.shuttle.domain.project.output;

import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.user.output.ClientProjectPermissionsResponse;

import java.util.List;

public class ClientProjectResponse {

    public Integer id;

    public String name;

    public String iconHref;

    public Boolean isMuted;

    public Long lastReleaseDate;

    public List<MemberResponse> teamMembers;

    public ClientProjectPermissionsResponse permissions;

    public ClientProjectResponse(Project project,
                                 Boolean isMuted,
                                 List<MemberResponse> teamMembers,
                                 ClientProjectPermissionsResponse permissions,
                                 Long lastReleaseDate) {
        this.id = project.id();
        this.name = project.name();
        this.iconHref = project.iconHref();
        this.isMuted = isMuted;
        this.lastReleaseDate = lastReleaseDate;
        this.teamMembers = teamMembers;
        this.permissions = permissions;
    }

}


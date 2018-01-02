package com.polidea.shuttle.domain.project.output;

import com.polidea.shuttle.domain.project.Project;

import java.util.List;

public class AdminProjectResponse {

    public Integer id;

    public String name;

    public String iconHref;

    public Long lastReleaseDate;

    public List<MemberResponse> teamMembers;

    public AdminProjectResponse(Project project,
                                List<MemberResponse> teamMembers,
                                Long lastReleaseDate) {
        this.id = project.id();
        this.name = project.name();
        this.iconHref = project.iconHref();
        this.teamMembers = teamMembers;
        this.lastReleaseDate = lastReleaseDate;
    }

}


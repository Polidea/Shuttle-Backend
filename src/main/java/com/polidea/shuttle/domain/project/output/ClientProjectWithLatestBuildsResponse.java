package com.polidea.shuttle.domain.project.output;

import com.polidea.shuttle.domain.app.output.ClientAppAndroidLatestBuildResponse;
import com.polidea.shuttle.domain.app.output.ClientAppIosLatestBuildResponse;
import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.user.output.ClientProjectPermissionsResponse;

import java.util.List;

public class ClientProjectWithLatestBuildsResponse extends ClientProjectResponse {

    public List<ClientAppAndroidLatestBuildResponse> latestAndroidBuilds;

    public List<ClientAppIosLatestBuildResponse> latestIosBuilds;

    public ClientProjectWithLatestBuildsResponse(Project project,
                                                 Boolean isMuted,
                                                 List<MemberResponse> teamMembers,
                                                 ClientProjectPermissionsResponse permissions,
                                                 List<ClientAppAndroidLatestBuildResponse> latestAndroidBuilds,
                                                 List<ClientAppIosLatestBuildResponse> latestIosBuilds,
                                                 Long lastReleaseDate) {
        super(project, isMuted, teamMembers, permissions, lastReleaseDate);
        this.latestAndroidBuilds = latestAndroidBuilds;
        this.latestIosBuilds = latestIosBuilds;
    }
}

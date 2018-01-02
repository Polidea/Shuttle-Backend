package com.polidea.shuttle.domain.project.output;

import com.polidea.shuttle.domain.project.Project;

public class ClientArchivedProjectResponse {

    public Integer id;

    public String name;

    public String iconHref;

    public ClientArchivedProjectResponse(Project project) {
        this.id = project.id();
        this.name = project.name();
        this.iconHref = project.iconHref();
    }

}

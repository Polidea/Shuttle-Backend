package com.polidea.shuttle.domain.app.output;

public class ClientAppByReleaseDateResponse {

    public String id;

    public String name;

    public String iconHref;

    public Long lastReleaseDate;

    public ClientAppByReleaseDateResponse(String id, String name, String iconHref, Long lastReleaseDate) {
        this.id = id;
        this.name = name;
        this.iconHref = iconHref;
        this.lastReleaseDate = lastReleaseDate;
    }
}

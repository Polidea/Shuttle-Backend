package com.polidea.shuttle.domain.app.output;

import com.polidea.shuttle.domain.app.App;

public class AdminAppResponse {

    public String id;

    public String name;

    public String iconHref;

    public Long lastReleaseDate;

    public AdminAppResponse(App app, boolean userCanViewNotPublished) {
        this.id = app.appId();
        this.name = app.name();
        app.lastBuildDate(userCanViewNotPublished).ifPresent(date -> this.lastReleaseDate = date);
        this.iconHref = app.iconHref();
    }

}

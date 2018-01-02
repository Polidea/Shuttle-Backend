package com.polidea.shuttle.data.datasets;

import com.polidea.shuttle.data.DataLoadHelper;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import org.slf4j.Logger;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.polidea.shuttle.domain.app.Platform.ANDROID;
import static com.polidea.shuttle.domain.user.permissions.PermissionType.ADMIN;
import static com.polidea.shuttle.domain.user.permissions.PermissionType.ARCHIVER;
import static com.polidea.shuttle.domain.user.permissions.PermissionType.MUTER;
import static com.polidea.shuttle.domain.user.permissions.PermissionType.PUBLISHER;
import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

public class AdditionalData {

    private static final Logger LOGGER = getLogger(AdditionalData.class);

    private final DataLoadHelper dataLoadHelper;

    public AdditionalData(DataLoadHelper dataLoadHelper) {
        this.dataLoadHelper = dataLoadHelper;
    }

    public void load() {
        LOGGER.info("Loading additional data...");

        createUser("nonglobaladmin@additional-data.com", "user w/o global admin permission",
                   emptyList());

        createUser("admin@additional-data.com", "Admin User",
                   newArrayList(ADMIN));

        createUser("admin+publisher@additional-data.com", "Admin-Publisher User",
                   newArrayList(ADMIN, PUBLISHER));

        createUser("member@additional-data.com", "Member User",
                   newArrayList());

        createUser("publisher+muter+archiver@additional-data.com", "Publisher-Muter-Archiver User",
                   newArrayList(PUBLISHER, MUTER, ARCHIVER));

        createUser("no-admin+publisher+muter+archiver@additional-data.com", "No-Admin-Publisher-Muter-Archiver User",
                   newArrayList(PUBLISHER, MUTER, ARCHIVER));

        createUser("admin-publisher+muter+archiver@additional-data.com", "Admin-Publisher-Muter-Archiver User",
                   newArrayList(ADMIN, PUBLISHER, MUTER, ARCHIVER));

        int project1Id = createProject1("admin@additional-data.com");
        int project2Id = createProject2();
        createProjectCats();
        createProjectDogs();

        dataLoadHelper.assignUserToProject("admin+publisher@additional-data.com", project1Id);
        dataLoadHelper.assignUserToProject("admin+publisher@additional-data.com", project2Id);

        dataLoadHelper.addMemberToProject("admin+publisher@additional-data.com", project1Id);

        dataLoadHelper.assignUserToProject("no-admin+publisher+muter+archiver@additional-data.com", project1Id);

        dataLoadHelper.setProjectPermissions(
            "admin+publisher@additional-data.com", project1Id, newArrayList(PUBLISHER, MUTER, ARCHIVER));

        dataLoadHelper.setProjectPermissions(
            "no-admin+publisher+muter+archiver@additional-data.com", project1Id, newArrayList(ADMIN));

        dataLoadHelper.assignUserToProject("publisher+muter+archiver@additional-data.com", project2Id);
        dataLoadHelper.archiveProjectByUser("publisher+muter+archiver@additional-data.com", project2Id);

        dataLoadHelper.favoriteBuildByUser("admin+publisher@additional-data.com", ANDROID, "app.id", "1");
    }

    private void createUser(String userEmail, String name, List<PermissionType> globalPermissions) {
        dataLoadHelper.createUserIfMissing(userEmail, name);
        dataLoadHelper.setGlobalPermissions(userEmail, globalPermissions);
    }

    private int createProject1(String buildsReleaserEmail) {
        int project1Id = dataLoadHelper.createProjectIfMissing("Project 1");

        dataLoadHelper.createAppIfMissing(project1Id, ANDROID, "app.id", "app.name");
        dataLoadHelper.createBuildIfMissing(
            ANDROID, "app.id", "1", "any_version_number", 4207955L,
            "https://url-to-your-shuttle/app-stage-release.apk",
            buildsReleaserEmail);

        return project1Id;
    }

    private int createProject2() {
        return dataLoadHelper.createProjectIfMissing("Project 2");
    }

    private int createProjectCats() {
        return dataLoadHelper.createProjectIfMissing("Project Cats");
    }

    private int createProjectDogs() {
        return dataLoadHelper.createProjectIfMissing("Project Dogs");
    }

}

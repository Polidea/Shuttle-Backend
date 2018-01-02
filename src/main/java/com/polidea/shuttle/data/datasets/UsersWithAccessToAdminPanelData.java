package com.polidea.shuttle.data.datasets;

import com.polidea.shuttle.data.DataLoadHelper;
import org.slf4j.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static com.polidea.shuttle.domain.user.permissions.PermissionType.ADMIN;
import static org.slf4j.LoggerFactory.getLogger;

public class UsersWithAccessToAdminPanelData {

    private static final Logger LOGGER = getLogger(UsersWithAccessToAdminPanelData.class);

    private final DataLoadHelper dataLoadHelper;

    public UsersWithAccessToAdminPanelData(DataLoadHelper dataLoadHelper) {
        this.dataLoadHelper = dataLoadHelper;
    }

    public void load() {
        LOGGER.info("Loading data of Users with access to Admin Panel...");

        dataLoadHelper.createUserIfMissing("admin@your-shuttle.com", "Admin");
        dataLoadHelper.setGlobalPermissions("admin@your-shuttle.com", newArrayList(ADMIN));
    }

}

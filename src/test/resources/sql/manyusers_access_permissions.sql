INSERT INTO projects (id, name) VALUES (1, 'Project 1');
INSERT INTO projects (id, name) VALUES (2, 'Project 2');
INSERT INTO store_users (id, email, name) VALUES (1, 'admin@email.com', 'Admin');
INSERT INTO store_users (id, email, name) VALUES (2, 'moderator@email.com', 'Moderator');
INSERT INTO store_users (id, email, name) VALUES (3, 'publisher@email.com', 'Publisher');
INSERT INTO store_users (id, email, name) VALUES (4, 'normaluser@email.com', 'Normal User');
INSERT INTO store_users (id, email, name) VALUES (5, 'userassignedapp@email.com', 'Assignee');
INSERT INTO store_users (id, email, name) VALUES (6, 'muter@email.com', 'Muter');
INSERT INTO store_users (id, email, name) VALUES (7, 'othermoderator@email.com', 'Other Moderator');
INSERT INTO store_users (id, email, name) VALUES (8, 'someuser@email.com', 'Some User');
INSERT INTO store_users (id, email, name) VALUES (9, 'buildviewer@email.com', 'Build Viewer');
ALTER SEQUENCE store_users_id_seq RESTART WITH 10;

INSERT INTO access_tokens (id, device_id, value, owner_id, type, creation_timestamp)
VALUES (1, 'device-1', 'tokenadmin', 1, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, device_id, value, owner_id, type, creation_timestamp)
VALUES (2, 'device-1', 'tokenmoderator', 2, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, device_id, value, owner_id, type, creation_timestamp)
VALUES (3, 'device-1', 'tokenpublisher', 3, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, device_id, value, owner_id, type, creation_timestamp)
VALUES (4, 'device-1', 'tokennormaluser', 4, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, device_id, value, owner_id, type, creation_timestamp)
VALUES (5, 'device-1', 'tokenuserassignedapp', 5, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, device_id, value, owner_id, type, creation_timestamp)
VALUES (6, 'device-1', 'tokenmuter', 6, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, device_id, value, owner_id, type, creation_timestamp)
VALUES (7, 'device-1', 'tokenothermoderator', 7, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, device_id, value, owner_id, type, creation_timestamp)
VALUES (8, 'device-1', 'tokensomeuser', 8, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, device_id, value, owner_id, type, creation_timestamp)
VALUES (9, 'device-1', 'tokenviewer', 9, 'CLIENT', '2000-01-01 00:00:00');

INSERT INTO global_permissions (id, type, user_id) VALUES (1, 'ADMIN', 1);
INSERT INTO global_permissions (id, type, user_id) VALUES (2, 'PUBLISHER', 3);
INSERT INTO global_permissions (id, type, user_id) VALUES (3, 'UNPUBLISHED_BUILDS_VIEWER', 9);
ALTER SEQUENCE global_permissions_id_seq RESTART WITH 4;

INSERT INTO apps (id, app_id, name, platform, project_id)
VALUES (1, 'app.id', 'someappname', 'ANDROID', 1);
INSERT INTO apps (id, app_id, name, platform, project_id)
VALUES (2, 'app.id', 'someappname', 'IOS', 1);
INSERT INTO apps (id, app_id, name, platform, project_id)
VALUES (3, 'com.polidea.shuttle', 'app.name', 'ANDROID', 1);
INSERT INTO apps (id, app_id, name, platform, project_id)
VALUES (4, 'com.polidea.shuttle', 'app.name', 'IOS', 1);

INSERT INTO project_permissions (id, user_id, project_id, type) VALUES (10, 2, 1, 'ADMIN');
INSERT INTO project_permissions (id, user_id, project_id, type) VALUES (11, 6, 1, 'MUTER');
INSERT INTO project_permissions (id, user_id, project_id, type) VALUES (12, 7, 2, 'ADMIN');

INSERT INTO builds (id, build_identifier, version_number, release_notes,
                    href, creation_timestamp, is_published, bytes, app_id, releaser_id)
VALUES (1, '123', '0.1.0', 'notes', 'href', 0, FALSE, 123, 1, 1);

INSERT INTO builds (id, build_identifier, version_number, release_notes,
                    href, creation_timestamp, is_published, bytes, app_id, releaser_id)
VALUES (2, '124', '0.1.1', 'notes', 'href', 0, FALSE, 123, 1, 1);

INSERT INTO builds (id, build_identifier, version_number, release_notes,
                    href, creation_timestamp, is_published, bytes, app_id, releaser_id)
VALUES (3, 'schema', '0.1.1', 'notes', 'href', 0, FALSE, 123, 2, 1);
INSERT INTO builds (id, build_identifier, version_number, creation_timestamp, href, is_published, app_id, releaser_id)
VALUES (5, 'version.id', 'version.number', 1467983302000,
        'https://some.domain.com/some/android/build.html',
        TRUE, 3, 1);
INSERT INTO builds (id, build_identifier, version_number, creation_timestamp, href, is_published, app_id, releaser_id)
VALUES (6, 'version.id', 'version.number', 1467983302001,
        'https://some.domain.com/some/ios/build.html',
        TRUE, 4, 1);

INSERT INTO users_assigned_to_projects (assignee_id, project_id) VALUES (5, 1);

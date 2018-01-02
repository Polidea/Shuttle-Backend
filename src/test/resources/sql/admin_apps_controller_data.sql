INSERT INTO projects (id, name) VALUES (1, 'Project 1');
INSERT INTO projects (id, name) VALUES (2, 'Project 2');
INSERT INTO apps (id, app_id, name, platform, project_id, icon_href)
VALUES (1, 'app.id1', 'app.name', 'ANDROID', 1, 'http://link.com/a.png');
INSERT INTO apps (id, app_id, name, platform, project_id, icon_href)
VALUES (2, 'app.id2', 'app.name', 'IOS', 1, 'http://link.com/a.png');
INSERT INTO apps (id, app_id, name, platform, project_id, icon_href)
VALUES (3, 'app.id.other', 'app.name', 'ANDROID', 1, 'http://link.com/a.png');
INSERT INTO apps (id, app_id, name, platform, project_id, icon_href)
VALUES (4, 'app.id1.some', 'app.name', 'ANDROID', 2, 'http://link.com/a.png');
ALTER SEQUENCE apps_id_seq RESTART WITH 5;

INSERT INTO store_users (id, email, name) VALUES (1, 'email@email.com', 'User 1;=');
INSERT INTO store_users (id, email, name) VALUES (2, 'email2@email.com', 'User 2');
INSERT INTO store_users (id, email, name) VALUES (3, 'emailtoken@email.com', 'User 3');

INSERT INTO builds (id, build_identifier, version_number, release_notes,
                    href, creation_timestamp, is_published, bytes, app_id, releaser_id)
VALUES (1, '123', '0.1.0', 'notes', 'href', 0, FALSE, 123, 1, 1);

INSERT INTO builds (id, build_identifier, version_number, release_notes,
                    href, creation_timestamp, is_published, bytes, app_id, releaser_id)
VALUES (2, '124', '0.1.1', 'notes', 'href', 0, FALSE, 123, 1, 1);

INSERT INTO builds (id, build_identifier, version_number, release_notes,
                    href, creation_timestamp, is_published, bytes, app_id, releaser_id)
VALUES (3, 'schema', '0.1.1', 'notes', 'href', 0, FALSE, 123, 3, 1);


INSERT INTO access_tokens (id, value, owner_id, type, creation_timestamp)
VALUES (1, 'token', 3, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, value, owner_id, type, creation_timestamp)
VALUES (2, 'tokennormal1', 1, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, value, owner_id, type, creation_timestamp)
VALUES (3, 'tokennormal2', 2, 'CLIENT', '2000-01-01 00:00:00');

INSERT INTO global_permissions (id, type, user_id) VALUES (1, 'ADMIN', 3);
INSERT INTO global_permissions (id, type, user_id) VALUES (2, 'PUBLISHER', 3);
ALTER SEQUENCE global_permissions_id_seq RESTART WITH 3;

INSERT INTO users_assigned_to_projects (assignee_id, project_id) VALUES (1, 1);
INSERT INTO users_assigned_to_projects (assignee_id, project_id) VALUES (2, 2);

INSERT INTO project_permissions (id, user_id, project_id, type) VALUES (1, 3, 1, 'ADMIN');
INSERT INTO project_permissions (id, user_id, project_id, type) VALUES (2, 3, 1, 'PUBLISHER');

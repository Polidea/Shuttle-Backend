INSERT INTO projects (id, name) VALUES (1, 'Project 1');
INSERT INTO apps (id, app_id, name, platform, project_id) VALUES (1, 'app.id', 'app.name', 'ANDROID', 1);
INSERT INTO apps (id, app_id, name, platform, project_id) VALUES (2, 'app.id', 'app.name', 'IOS', 1);

INSERT INTO store_users (id, email, name) VALUES (1, 'email@email.com', 'User');
INSERT INTO store_users (id, email, name) VALUES (2, 'normaluser@email.com', 'Normal User');

INSERT INTO builds (id, build_identifier, version_number, release_notes,
                    href, creation_timestamp, is_published, bytes, app_id, releaser_id)
VALUES (1, '123', '0.1.0', 'notes', 'href', 0, FALSE, 123, 1, 1);

INSERT INTO builds (id, build_identifier, version_number, release_notes,
                    href, creation_timestamp, is_published, bytes, app_id, releaser_id)
VALUES (2, '124', '0.1.1', 'notes', 'href', 0, FALSE, 123, 1, 1);

INSERT INTO builds (id, build_identifier, version_number, release_notes,
                    href, creation_timestamp, is_published, bytes, app_id, releaser_id)
VALUES (3, 'schema', '0.1.1', 'notes', 'href', 0, FALSE, 123, 2, 1);
INSERT INTO access_tokens (id, value, owner_id, type, creation_timestamp)
VALUES (1, 'token', 1, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, value, owner_id, type, creation_timestamp)
VALUES (2, 'tokennormaluser', 2, 'CLIENT', '2000-01-01 00:00:00');

INSERT INTO global_permissions (id, type, user_id) VALUES (1, 'ADMIN', 1);
INSERT INTO global_permissions (id, type, user_id) VALUES (2, 'PUBLISHER', 1);
INSERT INTO global_permissions (id, type, user_id) VALUES (3, 'ARCHIVER', 2);
INSERT INTO global_permissions (id, type, user_id) VALUES (4, 'MUTER', 1);
ALTER SEQUENCE global_permissions_id_seq RESTART WITH 5;

INSERT INTO users_assigned_to_projects (assignee_id, project_id) VALUES (1, 1);
INSERT INTO users_assigned_to_projects (assignee_id, project_id) VALUES (2, 1);

INSERT INTO project_permissions (id, user_id, project_id, type) VALUES (1, 1, 1, 'ADMIN');
INSERT INTO project_permissions (id, user_id, project_id, type) VALUES (2, 1, 1, 'PUBLISHER');

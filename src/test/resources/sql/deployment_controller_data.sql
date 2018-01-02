INSERT INTO projects (id, name) VALUES (1, 'Project 1');

INSERT INTO apps (id, app_id, name, platform, project_id) VALUES (1, 'app.id', 'app.name', 'ANDROID', 1);
INSERT INTO apps (id, app_id, name, platform, project_id) VALUES (2, 'app.id', 'app.name', 'IOS', 1);
ALTER SEQUENCE apps_id_seq RESTART WITH 3;

INSERT INTO store_users (id, email, name) VALUES (1, 'email@email.com', 'User 1');
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
VALUES (3, 'schema', '0.1.1', 'notes', 'href', 0, FALSE, 123, 2, 1);
ALTER SEQUENCE builds_id_seq RESTART WITH 4;

INSERT INTO access_tokens (id, value, owner_id, type, creation_timestamp)
VALUES (1, 'build_creator_token', 1, 'CONTINUOUS_DEPLOYMENT', '2000-01-01 00:00:00');

INSERT INTO global_permissions (id, type, user_id) VALUES (1, 'BUILD_CREATOR', 1);

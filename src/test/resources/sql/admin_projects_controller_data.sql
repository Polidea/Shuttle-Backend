INSERT INTO projects (id, name, icon_href) VALUES (1, 'Project 1', 'http://test.com/icon.png');
INSERT INTO projects (id, name, icon_href) VALUES (2, 'Project 2', 'http://test.com/icon.png');
ALTER SEQUENCE projects_id_seq RESTART WITH 3;

INSERT INTO apps (id, app_id, name, platform, project_id) VALUES (1, 'app.id', 'app.name', 'ANDROID', 1);
INSERT INTO apps (id, app_id, name, platform, project_id) VALUES (2, 'app.id', 'app.name', 'IOS', 1);

INSERT INTO store_users (id, email, name, avatar_href) VALUES (1, 'email@email.com', 'Janek', 'http://avatars.com/test.png');
INSERT INTO store_users (id, email, name) VALUES (2, 'email2@email.com', 'User 2');
INSERT INTO store_users (id, email, name) VALUES (3, 'emailtoken@email.com', 'User 3');
INSERT INTO store_users (id, email, name) VALUES (4, 'emailother@email.com', 'User 4');

INSERT INTO project_members (member_id, project_id) VALUES (1, 1);

INSERT INTO builds (id, build_identifier, version_number, release_notes,
                    href, creation_timestamp, is_published, bytes, app_id, releaser_id)
VALUES (1, '123', '0.1.0', 'notes', 'href', 0, FALSE, 123, 1, 1);

INSERT INTO access_tokens (id, value, owner_id, type, creation_timestamp)
VALUES (1, 'token', 3, 'CLIENT', '2000-01-01 00:00:00');
INSERT INTO access_tokens (id, value, owner_id, type, creation_timestamp)
VALUES (2, 'tokenuser', 1, 'CLIENT', '2000-01-01 00:00:00');

INSERT INTO global_permissions (id, type, user_id) VALUES (1, 'ADMIN', 3);
INSERT INTO global_permissions (id, type, user_id) VALUES (2, 'PUBLISHER', 3);
ALTER SEQUENCE global_permissions_id_seq RESTART WITH 3;

INSERT INTO users_assigned_to_projects (assignee_id, project_id) VALUES (1, 1);

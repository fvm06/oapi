drop table db_a3.users;
drop table db_a3.user2role; 

INSERT INTO db_a3.users (user_id,login, password, updatetime) VALUES (59,'PRINCIPAL', 'oapi+ogui', '2018-03-18 18:44:12.953')
CREATE TABLE db_a3.users 
(
    user_id serial NOT NULL, 
    login varchar(32) NOT NULL, 
    password varchar(32), 
    updateTime TIMESTAMP DEFAULT NOW() NOT NULL,
    PRIMARY KEY (user_id)
);

CREATE TABLE db_a3.roles 
(
    role_id serial NOT NULL, 
    description varchar(255), 
    role_name varchar(32) NOT NULL, 
    PRIMARY KEY (role_id)
);

INSERT INTO db_a3.user2role (user2role_id,user_id, role_id) VALUES (60,59, 58)
CREATE TABLE db_a3.user2role 
(
    user2role_id serial NOT NULL,
    user_id serial NOT NULL REFERENCES db_a3.users, 
    role_id serial NOT NULL REFERENCES db_a3.roles, 
    PRIMARY KEY (user2role_id)
);

SELECT nextval('db_a3.auth');
INSERT INTO db_a3.roles (role_id, description, "role_name") 
	VALUES (13, 'Клиент, желающий приобрести дебетовую карты', 'debit-card-lead');

SELECT nextval('db_a3.auth');
INSERT INTO db_a3.roles (role_id, description, "role_name") 
	VALUES (58, 'Интернет-пользователь', 'not-identity-principal');


{"sql" => "select ur.password, rs.role_name from db_a3.users as ur, db_a3.user2role as u2r, db_a3.roles as rs where u2r.user_id=ur.user_id and u2r.role_id=rs.role_id and ur.login=?","data-source" => "PostgresDS","attribute-mapping" => [{"to" => "groups","index" => 2}],"clear-password-mapper" => {"password-index" => 1}}

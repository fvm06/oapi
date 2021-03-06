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


{"sql" => "select ur.password, rs.role_name from db_a3.users as ur, db_a3.user2role as u2r, db_a3.roles as rs where u2r.user_id=ur.user_id and u2r.role_id=rs.role_id and ur.login=?","dat
a-source" => "PostgresDS","attribute-mapping" => [{"to" => "groups","index" => 2}],"clear-password-mapper" => {"password-index" => 1}}




/subsystem=undertow/application-security-domain=other:add(
    http-authentication-factory=application-http-authentication)

 ./subsystem=elytron/service-loader-http-server-mechanism-factory=
    custom-factory:add(module=org.wildfly.security.examples.custom-http)

./subsystem=elytron/http-authentication-factory=custom-mechanism:
    add(http-server-mechanism-factory=custom-factory, 
    security-domain=ApplicationDomain, 
    mechanism-configurations=[{mechanism-name=CUSTOM_MECHANISM}])


 ./subsystem=undertow/application-security-domain=other:
    write-attribute(name=http-authentication-factory, value=custom-mechanism)
./subsystem=undertow/application-security-domain=other:
    write-attribute(name=override-deployment-config, value=true)

./subsystem=elytron/custom-realm-mapper=add(module=jk.demo.)

module add --name=org.wildfly.security.oapiauth.custom-http --resources=/home/vlad/NetBeansProjects/elytron-examples-master/simple-http-mechanism/target/oapiauth-http-mechanism-1.00.00.jar --dependencies=org.wildfly.security.elytron,javax.api

module remove --name=org.wildfly.security.oapiauth.custom-http --resources=/home/vlad/NetBeansProjects/elytron-examples-master/simple-http-mechanism/target/oapiauth-http-mechanism-1.00.00.jar --dependencies=org.wildfly.security.elytron,javax.api

module add --name=jk.demo.my-custom-realm-mapper --resources=/home/vlad/NetBeansProjects/custom-elytron-realm-mapper/target/my-custom-realm-mapper-1.0-SNAPSHOT.jar --dependencies=org.wildfly.security.elytron

module remove --name=jk.demo.my-custom-realm-mapper --resources=/home/vlad/NetBeansProjects/custom-elytron-realm-mapper/target/my-custom-realm-mapper-1.0-SNAPSHOT.jar --dependencies=org.wildfly.security.elytron

/subsystem=elytron/custom-realm-mapper=myRealmMapper:add(module=jk.demo.my-custom-realm-mapper, class-name=MyRealmMapper)

eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJpc3MiOiJpc3N1ZXIud2lsZGZseS5vcmciLCJzdWIiOiJlbHl0cm9uQHdpbGRmbHkub3JnIiwiZXhwIjoyMDUxMjIyMzk5LCJhdWQiOiJlbHl0cm9uLXRlc3QiLCJncm91cHMiOiJkZWJpdC1jYXJkLWxlYWQifQ.

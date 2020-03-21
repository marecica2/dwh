truncate  master.tenants cascade;
insert into master.tenants (id, name, description, created_on, modified_on)
values ('000000-00000-00001', 'tenant-1', 'Tenant BMW', null, null);
insert into master.tenants (id, name, description, created_on, modified_on)
values ('000000-00000-00002', 'tenant-2', 'Tenant Audi', null, null);

SET SEARCH_PATH TO "000000-00000-00001";
DROP TABLE IF EXISTS person;
CREATE TABLE person
(
    id   int primary key not null,
    name varchar(50)
);
INSERT INTO person (id, name)
VALUES (456, 'John');

SET SEARCH_PATH TO "000000-00000-00002";
DROP TABLE IF EXISTS person;
CREATE TABLE person
(
    id   int primary key not null,
    name varchar(50)
);
INSERT INTO person (id, name)
VALUES (789, 'Mary');

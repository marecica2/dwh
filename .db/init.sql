CREATE SCHEMA "common";
CREATE SCHEMA "master";
CREATE SCHEMA "000000-00000-00001";
CREATE SCHEMA "000000-00000-00002";

create table master.roles
(
    id          bigserial not null
        constraint roles_pkey primary key,
    created_on  bigint,
    description varchar(255),
    modified_on bigint,
    name        varchar(255)
);

create table master.users
(
    id         bigserial not null
        constraint users_pkey primary key,
    email      varchar(255),
    first_name varchar(255),
    last_name  varchar(255),
    password   varchar(255),
    username   varchar(255)
);

create table master.user_roles
(
    user_id bigint not null
        constraint fk_user_roles_users references master.users,
    role_id bigint not null
        constraint fk_user_roles_roles references master.roles,
    constraint user_roles_pkey primary key (user_id, role_id)
);

create table if not exists master.tenants
(
    id          varchar(30) not null
        constraint tenants_pkey primary key,
    name        varchar(255),
    description varchar(255),
    created_on  bigint,
    modified_on bigint
);

create table if not exists master.user_tenants
(
    user_id   bigint      not null
        constraint fk_user_roles_users references master.users,
    tenant_id varchar(30) not null
        constraint fk_user_tenants references master.tenants
);

insert into master.tenants (id, name, description, created_on, modified_on)
values ('000000-00000-00001', 'tenant-1', 'Tenant BMW', null, null);
insert into master.tenants (id, name, description, created_on, modified_on)
values ('000000-00000-00002', 'tenant-2', 'Tenant Audi', null, null);

insert into master.roles(description, name)
values ('Admin', 'ADMIN');
insert into master.roles(description, name)
values ('User', 'USER');

-- setup admin user
insert into master.users (email, first_name, last_name, password, username)
values ('admin@gmail.com', 'Super', 'Admin', '$2a$04$EZzbSqieYfe/nFWfBWt2KeCdyq0UuDEM1ycFF8HzmlVR6sbsOnw7u', 'administrator');
insert into master.user_roles(user_id, role_id)
values (1, 1);
insert into master.user_tenants (user_id, tenant_id)
values (1, '000000-00000-00001');

-- setup tenant user
insert into master.users (email, first_name, last_name, password, username)
values ('user@gmail.com', 'John', 'Doe', '$2a$04$EZzbSqieYfe/nFWfBWt2KeCdyq0UuDEM1ycFF8HzmlVR6sbsOnw7u', 'user');
insert into master.user_roles(user_id, role_id)
values (2, 2);
insert into master.user_tenants (user_id, tenant_id)
values (2, '000000-00000-00002');

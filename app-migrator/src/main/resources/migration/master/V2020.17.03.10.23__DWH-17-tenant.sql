create table if not exists master.tenants
(
    id          varchar(30) not null constraint tenants_pkey primary key,
    name        varchar(255),
    description varchar(255),
    created_on  bigint,
    modified_on bigint
);

create table if not exists master.user_tenants
(
    user_id     bigint not null constraint fk_user_roles_users references master.users,
    tenant_id   varchar(30) not null constraint fk_user_tenants references master.tenants
);

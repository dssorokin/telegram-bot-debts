create table if not exists 'group'
(
    group_id bigserial not null
    constraint group_pkey
    primary key,
)
;

create table if not exists users
(
    user_id bigserial not null
    constraint users_pk
    primary key,
    name text,
    shipment_date date,
    group_id bigserial not null
    constraint users_group_group_id_fk
    references "group_chat"
    on delete cascade
);



create table if not exists debts
(
    debt_id bigserial not null
    constraint table_name_pkey
    primary key,
    description text not null,
    from_user_id bigserial not null
    constraint table_name_users_user_id_fk
    references users,
    to_user_id bigserial not null
    constraint table_name_users_user_id_fk_2
    references users,
    created_date date,
    amount numeric(15,2)
    );








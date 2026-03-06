create table vouchers (
    id bigserial primary key,
    code varchar(255) not null unique,
    valid_from timestamp not null,
    valid_until timestamp not null,
    quota_total integer not null check (quota_total > 0),
    quota_remaining integer not null check (quota_remaining >= 0 and quota_remaining <= quota_total),
    discount_percent integer not null default 0 check (discount_percent >= 0),
    terms text not null,
    active boolean not null default true,
    created_at timestamp not null default current_timestamp
);
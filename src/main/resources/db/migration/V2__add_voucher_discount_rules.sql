alter table vouchers
    add column minimum_purchase_amount bigint not null default 0,
    add column max_discount_amount bigint;

alter table vouchers
    drop constraint if exists vouchers_discount_percent_check;

alter table vouchers
    add constraint vouchers_discount_percent_check
    check (discount_percent >= 1 and discount_percent <= 100);

alter table vouchers
    add constraint vouchers_minimum_purchase_amount_check
    check (minimum_purchase_amount >= 0);

alter table vouchers
    add constraint vouchers_max_discount_amount_check
    check (max_discount_amount is null or max_discount_amount >= 0);
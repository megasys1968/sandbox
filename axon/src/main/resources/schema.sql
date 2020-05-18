drop table if exists invoices;
drop table if exists bill_items;

create table invoices (
  invoice_id uuid primary key,
  invoice_title varchar(255) not null unique
);

create table bill_items (
  bill_item_id uuid primary key,
  bill_item_name varchar(255) not null,
  invoice_id uuid not null,
  unique (bill_item_name, invoice_id),
  foreign key (invoice_id) references invoices (invoice_id) on delete cascade
);

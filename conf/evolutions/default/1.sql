# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table item (
  id                        varchar(255) not null,
  item_name                 varchar(255),
  price                     double,
  sold                      tinyint(1) default 0,
  list_date                 varchar(255),
  description               varchar(255),
  seller_username           varchar(255),
  sale_name                 varchar(255),
  bid_amount                double,
  biddable                  tinyint(1) default 0,
  bid_fraction              double,
  sale_id                   varchar(255),
  buyer_username            varchar(255),
  picture                   longblob,
  constraint pk_item primary key (id))
;

create table orders (
  id                        bigint auto_increment not null,
  username                  varchar(255),
  payment                   varchar(255),
  total                     double,
  sale_name                 varchar(255),
  constraint pk_orders primary key (id))
;

create table roles (
  id                        bigint auto_increment not null,
  user_username             varchar(255),
  sale_id                   varchar(255),
  seller                    tinyint(1) default 0,
  sales_admin               tinyint(1) default 0,
  super_user                tinyint(1) default 0,
  clerk                     tinyint(1) default 0,
  cashier                   tinyint(1) default 0,
  bookkeeper                tinyint(1) default 0,
  guest                     tinyint(1) default 0,
  constraint pk_roles primary key (id))
;

create table sale (
  id                        varchar(255) not null,
  sale_name                 varchar(255) not null,
  owner_username            varchar(255),
  inactive                  tinyint(1) default 0,
  date                      date not null,
  constraint pk_sale primary key (id))
;

create table user (
  username                  varchar(255) not null,
  email                     varchar(255),
  password                  varchar(255),
  salt                      varchar(255),
  name                      varchar(255),
  address                   varchar(255),
  description               varchar(255),
  phone_number              varchar(255),
  super_user                tinyint(1) default 0,
  locked                    tinyint(1) default 0,
  login_counter             integer,
  constraint pk_user primary key (username))
;


create table sale_item (
  sale_id                        varchar(255) not null,
  item_id                        varchar(255) not null,
  constraint pk_sale_item primary key (sale_id, item_id))
;
alter table item add constraint fk_item_seller_1 foreign key (seller_username) references user (username) on delete restrict on update restrict;
create index ix_item_seller_1 on item (seller_username);
alter table item add constraint fk_item_sale_2 foreign key (sale_id) references sale (id) on delete restrict on update restrict;
create index ix_item_sale_2 on item (sale_id);
alter table item add constraint fk_item_buyer_3 foreign key (buyer_username) references user (username) on delete restrict on update restrict;
create index ix_item_buyer_3 on item (buyer_username);
alter table roles add constraint fk_roles_user_4 foreign key (user_username) references user (username) on delete restrict on update restrict;
create index ix_roles_user_4 on roles (user_username);
alter table roles add constraint fk_roles_sale_5 foreign key (sale_id) references sale (id) on delete restrict on update restrict;
create index ix_roles_sale_5 on roles (sale_id);
alter table sale add constraint fk_sale_owner_6 foreign key (owner_username) references user (username) on delete restrict on update restrict;
create index ix_sale_owner_6 on sale (owner_username);



alter table sale_item add constraint fk_sale_item_sale_01 foreign key (sale_id) references sale (id) on delete restrict on update restrict;

alter table sale_item add constraint fk_sale_item_item_02 foreign key (item_id) references item (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table item;

drop table orders;

drop table roles;

drop table sale;

drop table sale_item;

drop table user;

SET FOREIGN_KEY_CHECKS=1;


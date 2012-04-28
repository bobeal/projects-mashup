# --- First database schema

# --- !Ups

create table users (
  email                     varchar(255) not null primary key,
  name                      varchar(255) not null
);

# --- !Downs

drop table if exists users;


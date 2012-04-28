# --- First database schema

# --- !Ups

create table project (
  name                     varchar(255) not null primary key
);

# --- !Downs

drop table if exists project;


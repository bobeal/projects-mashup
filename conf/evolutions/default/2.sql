# --- First database schema

# --- !Ups

create table project (
  id                       bigint not null primary key,
  name                     varchar(255) not null,
  user_email               varchar(255) not null references users(email)
);

create sequence project_seq start with 1000;

# --- !Downs

drop table if exists project;
drop sequence if exists project_seq;


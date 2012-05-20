# --- First database schema

# --- !Ups

alter table authorizations add column user_id varchar(255);

# --- !Downs

alter table authorizations drop column user_id;



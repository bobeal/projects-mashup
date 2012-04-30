# --- First database schema

# --- !Ups

create table authorizations (
  user_email	 	 varchar(255) not null references users(email),
  application		 varchar(255) not null,
  api_key		 varchar(255) not null
);

# --- !Downs

drop table if exists authorizations;

# --- First database schema

# --- !Ups

create table data_source (
  source_type		 varchar(255) not null,
  id			 varchar(255) not null,
  url			 varchar(255) not null,
  name                   varchar(255) not null,
  project_id		 bigint not null
);

alter table data_source 
  add constraint fk_project_id 
  foreign key (project_id) 
  references project;

# --- !Downs

drop table if exists data_source;

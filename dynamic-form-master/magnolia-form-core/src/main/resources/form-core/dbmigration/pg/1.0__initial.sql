-- apply changes
create table answer_options (
  id                            uuid not null,
  title                         varchar(255),
  label                         varchar(2000),
  value                         varchar(2000),
  image                         varchar(255),
  free_text                     varchar(255),
  free_text_label               varchar(255),
  question_id                   uuid not null,
  order_index                   integer not null,
  content                       json,
  version                       bigint not null,
  created                       timestamptz not null,
  created_by                    varchar(255) not null,
  modified                      timestamptz not null,
  modified_by                   varchar(255) not null,
  constraint pk_answer_options primary key (id)
);

create table answer_options_localized (
  id                            uuid not null,
  title                         varchar(255),
  label                         varchar(2000),
  free_text_label               varchar(255),
  locale                        varchar(255) not null,
  parent_id                     uuid not null,
  content                       json,
  version                       bigint not null,
  created                       timestamptz not null,
  created_by                    varchar(255) not null,
  modified                      timestamptz not null,
  modified_by                   varchar(255) not null,
  constraint uq_answer_options_localized_locale_parent_id unique (locale,parent_id),
  constraint pk_answer_options_localized primary key (id)
);

create table forms (
  id                            uuid not null,
  title                         varchar(255),
  description                   varchar(2000),
  anonymize                     boolean default false not null,
  only_authenticated            boolean default false not null,
  public_results                boolean default false not null,
  order_index                   integer not null,
  image                         varchar(255),
  rules                         varchar(2000),
  content                       json,
  version                       bigint not null,
  created                       timestamptz not null,
  created_by                    varchar(255) not null,
  modified                      timestamptz not null,
  modified_by                   varchar(255) not null,
  constraint pk_forms primary key (id)
);

create table forms_localized (
  id                            uuid not null,
  title                         varchar(255),
  description                   varchar(2000),
  locale                        varchar(255) not null,
  parent_id                     uuid not null,
  content                       json,
  version                       bigint not null,
  created                       timestamptz not null,
  created_by                    varchar(255) not null,
  modified                      timestamptz not null,
  modified_by                   varchar(255) not null,
  constraint uq_forms_localized_locale_parent_id unique (locale,parent_id),
  constraint pk_forms_localized primary key (id)
);

create table questions (
  id                            uuid not null,
  title                         varchar(255),
  question                      varchar(2000),
  question_type                 varchar(255),
  image                         varchar(255),
  range_from                    bigint,
  range_from_label              varchar(255),
  range_from_image              varchar(255),
  range_to                      bigint,
  range_to_label                varchar(255),
  range_to_image                varchar(255),
  custom_chart_id               varchar(255),
  section_id                    uuid,
  form_id                       uuid,
  order_index                   integer not null,
  content                       json,
  version                       bigint not null,
  created                       timestamptz not null,
  created_by                    varchar(255) not null,
  modified                      timestamptz not null,
  modified_by                   varchar(255) not null,
  constraint pk_questions primary key (id)
);

create table questions_localized (
  id                            uuid not null,
  title                         varchar(255),
  question                      varchar(2000),
  locale                        varchar(255) not null,
  parent_id                     uuid not null,
  content                       json,
  version                       bigint not null,
  created                       timestamptz not null,
  created_by                    varchar(255) not null,
  modified                      timestamptz not null,
  modified_by                   varchar(255) not null,
  constraint uq_questions_localized_locale_parent_id unique (locale,parent_id),
  constraint pk_questions_localized primary key (id)
);

create table responses (
  id                            uuid not null,
  form_id                       uuid not null,
  version                       bigint not null,
  created                       timestamptz not null,
  created_by                    varchar(255) not null,
  modified                      timestamptz not null,
  modified_by                   varchar(255) not null,
  constraint pk_responses primary key (id)
);

create table response_items (
  id                            uuid not null,
  question_id                   uuid not null,
  value                         varchar(2000),
  free_text_value               varchar(2000),
  response_id                   uuid not null,
  version                       bigint not null,
  created                       timestamptz not null,
  created_by                    varchar(255) not null,
  modified                      timestamptz not null,
  modified_by                   varchar(255) not null,
  constraint pk_response_items primary key (id)
);

create table sections (
  id                            uuid not null,
  title                         varchar(255),
  description                   varchar(2000),
  form_id                       uuid not null,
  order_index                   integer not null,
  content                       json,
  version                       bigint not null,
  created                       timestamptz not null,
  created_by                    varchar(255) not null,
  modified                      timestamptz not null,
  modified_by                   varchar(255) not null,
  constraint pk_sections primary key (id)
);

create table sections_localized (
  id                            uuid not null,
  title                         varchar(255),
  description                   varchar(2000),
  locale                        varchar(255) not null,
  parent_id                     uuid not null,
  content                       json,
  version                       bigint not null,
  created                       timestamptz not null,
  created_by                    varchar(255) not null,
  modified                      timestamptz not null,
  modified_by                   varchar(255) not null,
  constraint uq_sections_localized_locale_parent_id unique (locale,parent_id),
  constraint pk_sections_localized primary key (id)
);

create index ix_answer_options_question_id on answer_options (question_id);
alter table answer_options add constraint fk_answer_options_question_id foreign key (question_id) references questions (id) on delete restrict on update restrict;

create index ix_answer_options_localized_parent_id on answer_options_localized (parent_id);
alter table answer_options_localized add constraint fk_answer_options_localized_parent_id foreign key (parent_id) references answer_options (id) on delete restrict on update restrict;

create index ix_forms_localized_parent_id on forms_localized (parent_id);
alter table forms_localized add constraint fk_forms_localized_parent_id foreign key (parent_id) references forms (id) on delete restrict on update restrict;

create index ix_questions_section_id on questions (section_id);
alter table questions add constraint fk_questions_section_id foreign key (section_id) references sections (id) on delete restrict on update restrict;

create index ix_questions_form_id on questions (form_id);
alter table questions add constraint fk_questions_form_id foreign key (form_id) references forms (id) on delete restrict on update restrict;

create index ix_questions_localized_parent_id on questions_localized (parent_id);
alter table questions_localized add constraint fk_questions_localized_parent_id foreign key (parent_id) references questions (id) on delete restrict on update restrict;

create index ix_responses_form_id on responses (form_id);
alter table responses add constraint fk_responses_form_id foreign key (form_id) references forms (id) on delete restrict on update restrict;

create index ix_response_items_question_id on response_items (question_id);
alter table response_items add constraint fk_response_items_question_id foreign key (question_id) references questions (id) on delete restrict on update restrict;

create index ix_response_items_response_id on response_items (response_id);
alter table response_items add constraint fk_response_items_response_id foreign key (response_id) references responses (id) on delete restrict on update restrict;

create index ix_sections_form_id on sections (form_id);
alter table sections add constraint fk_sections_form_id foreign key (form_id) references forms (id) on delete restrict on update restrict;

create index ix_sections_localized_parent_id on sections_localized (parent_id);
alter table sections_localized add constraint fk_sections_localized_parent_id foreign key (parent_id) references sections (id) on delete restrict on update restrict;


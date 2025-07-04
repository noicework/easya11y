-- apply changes
create table answer_options (
  id                            varchar2(40) not null,
  title                         varchar2(255),
  label                         varchar2(2000),
  value                         varchar2(2000),
  image                         varchar2(255),
  free_text                     varchar2(255),
  free_text_label               varchar2(255),
  question_id                   varchar2(40) not null,
  order_index                   number(10) not null,
  content                       clob,
  version                       number(19) not null,
  created                       timestamp not null,
  created_by                    varchar2(255) not null,
  modified                      timestamp not null,
  modified_by                   varchar2(255) not null,
  constraint pk_answer_options primary key (id)
);

create table answer_options_localized (
  id                            varchar2(40) not null,
  title                         varchar2(255),
  label                         varchar2(2000),
  free_text_label               varchar2(255),
  locale                        varchar2(255) not null,
  parent_id                     varchar2(40) not null,
  content                       clob,
  version                       number(19) not null,
  created                       timestamp not null,
  created_by                    varchar2(255) not null,
  modified                      timestamp not null,
  modified_by                   varchar2(255) not null,
  constraint uq_answer_options_localized_locale_parent_id unique (locale,parent_id),
  constraint pk_answer_options_localized primary key (id)
);

create table forms (
  id                            varchar2(40) not null,
  title                         varchar2(255),
  description                   varchar2(2000),
  anonymize                     number(1) default 0 not null,
  only_authenticated            number(1) default 0 not null,
  public_results                number(1) default 0 not null,
  order_index                   number(10) not null,
  image                         varchar2(255),
  rules                         varchar2(2000),
  content                       clob,
  version                       number(19) not null,
  created                       timestamp not null,
  created_by                    varchar2(255) not null,
  modified                      timestamp not null,
  modified_by                   varchar2(255) not null,
  constraint pk_forms primary key (id)
);

create table forms_localized (
  id                            varchar2(40) not null,
  title                         varchar2(255),
  description                   varchar2(2000),
  locale                        varchar2(255) not null,
  parent_id                     varchar2(40) not null,
  content                       clob,
  version                       number(19) not null,
  created                       timestamp not null,
  created_by                    varchar2(255) not null,
  modified                      timestamp not null,
  modified_by                   varchar2(255) not null,
  constraint uq_forms_localized_locale_parent_id unique (locale,parent_id),
  constraint pk_forms_localized primary key (id)
);

create table questions (
  id                            varchar2(40) not null,
  title                         varchar2(255),
  question                      varchar2(2000),
  question_type                 varchar2(255),
  image                         varchar2(255),
  range_from                    number(19),
  range_from_label              varchar2(255),
  range_from_image              varchar2(255),
  range_to                      number(19),
  range_to_label                varchar2(255),
  range_to_image                varchar2(255),
  custom_chart_id               varchar2(255),
  section_id                    varchar2(40),
  form_id                       varchar2(40),
  order_index                   number(10) not null,
  content                       clob,
  version                       number(19) not null,
  created                       timestamp not null,
  created_by                    varchar2(255) not null,
  modified                      timestamp not null,
  modified_by                   varchar2(255) not null,
  constraint pk_questions primary key (id)
);

create table questions_localized (
  id                            varchar2(40) not null,
  title                         varchar2(255),
  question                      varchar2(2000),
  locale                        varchar2(255) not null,
  parent_id                     varchar2(40) not null,
  content                       clob,
  version                       number(19) not null,
  created                       timestamp not null,
  created_by                    varchar2(255) not null,
  modified                      timestamp not null,
  modified_by                   varchar2(255) not null,
  constraint uq_questions_localized_locale_parent_id unique (locale,parent_id),
  constraint pk_questions_localized primary key (id)
);

create table responses (
  id                            varchar2(40) not null,
  form_id                       varchar2(40) not null,
  version                       number(19) not null,
  created                       timestamp not null,
  created_by                    varchar2(255) not null,
  modified                      timestamp not null,
  modified_by                   varchar2(255) not null,
  constraint pk_responses primary key (id)
);

create table response_items (
  id                            varchar2(40) not null,
  question_id                   varchar2(40) not null,
  value                         varchar2(2000),
  free_text_value               varchar2(2000),
  response_id                   varchar2(40) not null,
  version                       number(19) not null,
  created                       timestamp not null,
  created_by                    varchar2(255) not null,
  modified                      timestamp not null,
  modified_by                   varchar2(255) not null,
  constraint pk_response_items primary key (id)
);

create table sections (
  id                            varchar2(40) not null,
  title                         varchar2(255),
  description                   varchar2(2000),
  form_id                       varchar2(40) not null,
  order_index                   number(10) not null,
  content                       clob,
  version                       number(19) not null,
  created                       timestamp not null,
  created_by                    varchar2(255) not null,
  modified                      timestamp not null,
  modified_by                   varchar2(255) not null,
  constraint pk_sections primary key (id)
);

create table sections_localized (
  id                            varchar2(40) not null,
  title                         varchar2(255),
  description                   varchar2(2000),
  locale                        varchar2(255) not null,
  parent_id                     varchar2(40) not null,
  content                       clob,
  version                       number(19) not null,
  created                       timestamp not null,
  created_by                    varchar2(255) not null,
  modified                      timestamp not null,
  modified_by                   varchar2(255) not null,
  constraint uq_sections_localized_locale_parent_id unique (locale,parent_id),
  constraint pk_sections_localized primary key (id)
);

create index ix_answer_options_question_id on answer_options (question_id);
alter table answer_options add constraint fk_answer_options_question_id foreign key (question_id) references questions (id);

create index ix_answr_ptns_lclzd_prnt_d on answer_options_localized (parent_id);
alter table answer_options_localized add constraint fk_answr_ptns_lclzd_prnt_d foreign key (parent_id) references answer_options (id);

create index ix_forms_localized_parent_id on forms_localized (parent_id);
alter table forms_localized add constraint fk_forms_localized_parent_id foreign key (parent_id) references forms (id);

create index ix_questions_section_id on questions (section_id);
alter table questions add constraint fk_questions_section_id foreign key (section_id) references sections (id);

create index ix_questions_form_id on questions (form_id);
alter table questions add constraint fk_questions_form_id foreign key (form_id) references forms (id);

create index ix_qstns_lclzd_prnt_d on questions_localized (parent_id);
alter table questions_localized add constraint fk_qstns_lclzd_prnt_d foreign key (parent_id) references questions (id);

create index ix_responses_form_id on responses (form_id);
alter table responses add constraint fk_responses_form_id foreign key (form_id) references forms (id);

create index ix_response_items_question_id on response_items (question_id);
alter table response_items add constraint fk_response_items_question_id foreign key (question_id) references questions (id);

create index ix_response_items_response_id on response_items (response_id);
alter table response_items add constraint fk_response_items_response_id foreign key (response_id) references responses (id);

create index ix_sections_form_id on sections (form_id);
alter table sections add constraint fk_sections_form_id foreign key (form_id) references forms (id);

create index ix_sctns_lclzd_prnt_d on sections_localized (parent_id);
alter table sections_localized add constraint fk_sctns_lclzd_prnt_d foreign key (parent_id) references sections (id);


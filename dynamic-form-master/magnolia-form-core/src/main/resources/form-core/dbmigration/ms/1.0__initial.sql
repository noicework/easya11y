-- apply changes
create table answer_options (
  id                            uniqueidentifier not null,
  title                         nvarchar(255),
  label                         nvarchar(2000),
  value                         nvarchar(2000),
  image                         nvarchar(255),
  free_text                     nvarchar(255),
  free_text_label               nvarchar(255),
  question_id                   uniqueidentifier not null,
  order_index                   integer not null,
  content                       nvarchar(max),
  version                       numeric(19) not null,
  created                       datetime2 not null,
  created_by                    nvarchar(255) not null,
  modified                      datetime2 not null,
  modified_by                   nvarchar(255) not null,
  constraint pk_answer_options primary key (id)
);

create table answer_options_localized (
  id                            uniqueidentifier not null,
  title                         nvarchar(255),
  label                         nvarchar(2000),
  free_text_label               nvarchar(255),
  locale                        nvarchar(255) not null,
  parent_id                     uniqueidentifier not null,
  content                       nvarchar(max),
  version                       numeric(19) not null,
  created                       datetime2 not null,
  created_by                    nvarchar(255) not null,
  modified                      datetime2 not null,
  modified_by                   nvarchar(255) not null,
  constraint pk_answer_options_localized primary key (id)
);
alter table answer_options_localized add constraint uq_answer_options_localized_locale_parent_id unique  (locale,parent_id);

create table forms (
  id                            uniqueidentifier not null,
  title                         nvarchar(255),
  description                   nvarchar(2000),
  anonymize                     bit default 0 not null,
  only_authenticated            bit default 0 not null,
  public_results                bit default 0 not null,
  order_index                   integer not null,
  image                         nvarchar(255),
  rules                         nvarchar(2000),
  content                       nvarchar(max),
  version                       numeric(19) not null,
  created                       datetime2 not null,
  created_by                    nvarchar(255) not null,
  modified                      datetime2 not null,
  modified_by                   nvarchar(255) not null,
  constraint pk_forms primary key (id)
);

create table forms_localized (
  id                            uniqueidentifier not null,
  title                         nvarchar(255),
  description                   nvarchar(2000),
  locale                        nvarchar(255) not null,
  parent_id                     uniqueidentifier not null,
  content                       nvarchar(max),
  version                       numeric(19) not null,
  created                       datetime2 not null,
  created_by                    nvarchar(255) not null,
  modified                      datetime2 not null,
  modified_by                   nvarchar(255) not null,
  constraint pk_forms_localized primary key (id)
);
alter table forms_localized add constraint uq_forms_localized_locale_parent_id unique  (locale,parent_id);

create table questions (
  id                            uniqueidentifier not null,
  title                         nvarchar(255),
  question                      nvarchar(2000),
  question_type                 nvarchar(255),
  image                         nvarchar(255),
  range_from                    numeric(19),
  range_from_label              nvarchar(255),
  range_from_image              nvarchar(255),
  range_to                      numeric(19),
  range_to_label                nvarchar(255),
  range_to_image                nvarchar(255),
  custom_chart_id               nvarchar(255),
  section_id                    uniqueidentifier,
  form_id                       uniqueidentifier,
  order_index                   integer not null,
  content                       nvarchar(max),
  version                       numeric(19) not null,
  created                       datetime2 not null,
  created_by                    nvarchar(255) not null,
  modified                      datetime2 not null,
  modified_by                   nvarchar(255) not null,
  constraint pk_questions primary key (id)
);

create table questions_localized (
  id                            uniqueidentifier not null,
  title                         nvarchar(255),
  question                      nvarchar(2000),
  locale                        nvarchar(255) not null,
  parent_id                     uniqueidentifier not null,
  content                       nvarchar(max),
  version                       numeric(19) not null,
  created                       datetime2 not null,
  created_by                    nvarchar(255) not null,
  modified                      datetime2 not null,
  modified_by                   nvarchar(255) not null,
  constraint pk_questions_localized primary key (id)
);
alter table questions_localized add constraint uq_questions_localized_locale_parent_id unique  (locale,parent_id);

create table responses (
  id                            uniqueidentifier not null,
  form_id                       uniqueidentifier not null,
  version                       numeric(19) not null,
  created                       datetime2 not null,
  created_by                    nvarchar(255) not null,
  modified                      datetime2 not null,
  modified_by                   nvarchar(255) not null,
  constraint pk_responses primary key (id)
);

create table response_items (
  id                            uniqueidentifier not null,
  question_id                   uniqueidentifier not null,
  value                         nvarchar(2000),
  free_text_value               nvarchar(2000),
  response_id                   uniqueidentifier not null,
  version                       numeric(19) not null,
  created                       datetime2 not null,
  created_by                    nvarchar(255) not null,
  modified                      datetime2 not null,
  modified_by                   nvarchar(255) not null,
  constraint pk_response_items primary key (id)
);

create table sections (
  id                            uniqueidentifier not null,
  title                         nvarchar(255),
  description                   nvarchar(2000),
  form_id                       uniqueidentifier not null,
  order_index                   integer not null,
  content                       nvarchar(max),
  version                       numeric(19) not null,
  created                       datetime2 not null,
  created_by                    nvarchar(255) not null,
  modified                      datetime2 not null,
  modified_by                   nvarchar(255) not null,
  constraint pk_sections primary key (id)
);

create table sections_localized (
  id                            uniqueidentifier not null,
  title                         nvarchar(255),
  description                   nvarchar(2000),
  locale                        nvarchar(255) not null,
  parent_id                     uniqueidentifier not null,
  content                       nvarchar(max),
  version                       numeric(19) not null,
  created                       datetime2 not null,
  created_by                    nvarchar(255) not null,
  modified                      datetime2 not null,
  modified_by                   nvarchar(255) not null,
  constraint pk_sections_localized primary key (id)
);
alter table sections_localized add constraint uq_sections_localized_locale_parent_id unique  (locale,parent_id);

create index ix_answer_options_question_id on answer_options (question_id);
alter table answer_options add constraint fk_answer_options_question_id foreign key (question_id) references questions (id);

create index ix_answer_options_localized_parent_id on answer_options_localized (parent_id);
alter table answer_options_localized add constraint fk_answer_options_localized_parent_id foreign key (parent_id) references answer_options (id);

create index ix_forms_localized_parent_id on forms_localized (parent_id);
alter table forms_localized add constraint fk_forms_localized_parent_id foreign key (parent_id) references forms (id);

create index ix_questions_section_id on questions (section_id);
alter table questions add constraint fk_questions_section_id foreign key (section_id) references sections (id);

create index ix_questions_form_id on questions (form_id);
alter table questions add constraint fk_questions_form_id foreign key (form_id) references forms (id);

create index ix_questions_localized_parent_id on questions_localized (parent_id);
alter table questions_localized add constraint fk_questions_localized_parent_id foreign key (parent_id) references questions (id);

create index ix_responses_form_id on responses (form_id);
alter table responses add constraint fk_responses_form_id foreign key (form_id) references forms (id);

create index ix_response_items_question_id on response_items (question_id);
alter table response_items add constraint fk_response_items_question_id foreign key (question_id) references questions (id);

create index ix_response_items_response_id on response_items (response_id);
alter table response_items add constraint fk_response_items_response_id foreign key (response_id) references responses (id);

create index ix_sections_form_id on sections (form_id);
alter table sections add constraint fk_sections_form_id foreign key (form_id) references forms (id);

create index ix_sections_localized_parent_id on sections_localized (parent_id);
alter table sections_localized add constraint fk_sections_localized_parent_id foreign key (parent_id) references sections (id);


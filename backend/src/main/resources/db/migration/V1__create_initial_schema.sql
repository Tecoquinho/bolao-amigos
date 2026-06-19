create table participants (
    id bigserial primary key,
    name varchar(120) not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp
);

create table teams (
    id bigserial primary key,
    name varchar(120) not null,
    fifa_code varchar(3) not null unique,
    flag_url varchar(255),
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp
);

create table matches (
    id bigserial primary key,
    phase varchar(30) not null,
    match_number integer not null,
    home_team_id bigint not null references teams (id),
    away_team_id bigint not null references teams (id),
    starts_at timestamp with time zone not null,
    status varchar(20) not null,
    venue varchar(120),
    home_score integer,
    away_score integer,
    official_result_at timestamp with time zone,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint chk_match_scores_non_negative
        check (
            (home_score is null or home_score >= 0)
            and (away_score is null or away_score >= 0)
        ),
    constraint chk_match_teams_different
        check (home_team_id <> away_team_id)
);

create table predictions (
    id bigserial primary key,
    participant_id bigint not null references participants (id),
    match_id bigint not null references matches (id),
    predicted_home_score integer not null,
    predicted_away_score integer not null,
    points_awarded integer not null default 0,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint uq_prediction_participant_match unique (participant_id, match_id),
    constraint chk_prediction_scores_non_negative
        check (
            predicted_home_score >= 0
            and predicted_away_score >= 0
            and points_awarded >= 0
        )
);

create index idx_matches_status_starts_at on matches (status, starts_at);
create index idx_matches_phase_starts_at on matches (phase, starts_at);
create index idx_predictions_participant_id on predictions (participant_id);
create index idx_predictions_match_id on predictions (match_id);

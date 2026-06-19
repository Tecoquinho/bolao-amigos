alter table participants
    add constraint uq_participants_name unique (name);

alter table matches
    add constraint uq_matches_match_number unique (match_number);

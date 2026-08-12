CREATE EXTENSION IF NOT EXISTS btree_gist;


CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(320) NOT NULL UNIQUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE calendars (
                           id UUID PRIMARY KEY,
                           user_id UUID NOT NULL UNIQUE,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_calendars_user
                               FOREIGN KEY (user_id)
                                   REFERENCES users(id)
                                   ON DELETE CASCADE
);


CREATE TABLE meetings (
                          id UUID PRIMARY KEY,

                          title VARCHAR(255) NOT NULL,
                          description TEXT,

                          start_time TIMESTAMPTZ NOT NULL,
                          end_time TIMESTAMPTZ NOT NULL,

                          organizer_id UUID NOT NULL,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT chk_meetings_time_range
                              CHECK (start_time < end_time),

                          CONSTRAINT fk_meetings_organizer
                              FOREIGN KEY (organizer_id)
                                  REFERENCES users(id)
                                  ON DELETE RESTRICT
);


CREATE TABLE time_slots (
                            id UUID PRIMARY KEY,

                            calendar_id UUID NOT NULL,

                            start_time TIMESTAMPTZ NOT NULL,
                            end_time TIMESTAMPTZ NOT NULL,

                            status VARCHAR(16) NOT NULL,

                            meeting_id UUID,

                            version BIGINT NOT NULL DEFAULT 0,

                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT chk_time_slots_time_range
                                CHECK (start_time < end_time),

                            CONSTRAINT chk_time_slots_status
                                CHECK (status IN ('FREE', 'BUSY')),

                            CONSTRAINT chk_meeting_slot_is_busy
                                CHECK (meeting_id IS NULL OR status = 'BUSY'),

                            CONSTRAINT fk_time_slots_calendar
                                FOREIGN KEY (calendar_id)
                                    REFERENCES calendars(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_time_slots_meeting
                                FOREIGN KEY (meeting_id)
                                    REFERENCES meetings(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT no_overlapping_time_slots
                                EXCLUDE USING gist (
            calendar_id WITH =,
            tstzrange(start_time, end_time, '[)') WITH &&
        )
);


CREATE TABLE meeting_participants (
                                      meeting_id UUID NOT NULL,
                                      user_id UUID NOT NULL,

                                      PRIMARY KEY (meeting_id, user_id),

                                      CONSTRAINT fk_meeting_participants_meeting
                                          FOREIGN KEY (meeting_id)
                                              REFERENCES meetings(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_meeting_participants_user
                                          FOREIGN KEY (user_id)
                                              REFERENCES users(id)
                                              ON DELETE RESTRICT
);


CREATE INDEX idx_time_slots_calendar_status_start
    ON time_slots(calendar_id, status, start_time);

CREATE INDEX idx_time_slots_meeting
    ON time_slots(meeting_id)
    WHERE meeting_id IS NOT NULL;

CREATE INDEX idx_meeting_participants_user
    ON meeting_participants(user_id, meeting_id);

CREATE INDEX idx_meetings_time
    ON meetings(start_time, end_time);
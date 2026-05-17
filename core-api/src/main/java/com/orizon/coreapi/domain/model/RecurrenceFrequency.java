package com.orizon.coreapi.domain.model;

import java.time.LocalDate;

public enum RecurrenceFrequency {
    DAILY {
        @Override
        public LocalDate next(LocalDate from) {
            return from.plusDays(1);
        }
    },
    WEEKLY {
        @Override
        public LocalDate next(LocalDate from) {
            return from.plusWeeks(1);
        }
    },
    MONTHLY {
        @Override
        public LocalDate next(LocalDate from) {
            return from.plusMonths(1);
        }
    },
    YEARLY {
        @Override
        public LocalDate next(LocalDate from) {
            return from.plusYears(1);
        }
    };

    public abstract LocalDate next(LocalDate from);
}

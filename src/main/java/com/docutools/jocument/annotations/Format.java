package com.docutools.jocument.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Can be applied to JSR-310 / java.time types that implement {@link java.time.temporal.Temporal} on field level, to
 * enforce a desired String representation in the {@link com.docutools.jocument.Document}.
 *
 * <code>
 * \@Format(value="dd.MM.yyyy", locale = "en-US", zone = "UTC")
 * private LocalDateTime time;
 * </code>
 *
 * @author codecitizen
 * @since 28.02.2020
 * @see java.time.temporal.Temporal
 * @see java.time.LocalDateTime
 * @see java.time.LocalDate
 * @see java.time.ZonedDateTime
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Format {
  String value();
  String zone() default "UTC";
  String locale() default "en/US";
}

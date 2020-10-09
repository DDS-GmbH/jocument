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
 * @see java.time.temporal.Temporal
 * @see java.time.LocalDateTime
 * @see java.time.LocalDate
 * @see java.time.ZonedDateTime
 * @since 28.02.2020
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Format {

  /**
   * Returns the time format string.
   *
   * @return the time format string the temporal should be formatted in
   */
  String value();

  /**
   * Returns the timezone of the temporal.
   *
   * @return the timezone the temporal is expressed in
   */
  String zone() default "UTC";

  /**
   * Returns the locale which should be used for formatting.
   *
   * @return the locale the temporal should be formatted in
   */
  String locale() default "en/US";
}

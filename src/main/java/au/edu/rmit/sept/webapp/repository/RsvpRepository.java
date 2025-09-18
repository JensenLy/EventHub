package au.edu.rmit.sept.webapp.repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.*;


@Repository
public class RsvpRepository {

    private final JdbcTemplate jdbcTemplate;

    public RsvpRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<RSVP> MAPPER = (ResultSet rs, int rowNum) ->
        new RSVP(
            rs.getLong("rsvp_id"),
            rs.getLong("user_id"),
            rs.getLong("event_id"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );

    public List<RSVP> findByEventId(Long eventId) {
        String sql = "SELECT rsvp_id, user_id, event_id, created_at FROM rsvp WHERE event_id = ?";
        return jdbcTemplate.query(sql, MAPPER, eventId);
    }

    public boolean save(RSVP rsvp) {
        String sql = "INSERT INTO rsvp (user_id, event_id, created_at) VALUES (?, ?, ?)";
        boolean status = jdbcTemplate.update(sql, rsvp.getUserId(), rsvp.getEventId(), rsvp.getCreatedAt()) > 0;
        return status;
    }

    public boolean checkUserAlreadyRsvped(Long userId, Long eventId) {
        String sql = "SELECT COUNT(*) FROM rsvp WHERE user_id = ? AND event_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, eventId);
        return count != null && count > 0;
    }

    public RSVP findByUserIdAndEventId(Long userId, Long eventId) {
        String sql = "SELECT rsvp_id, user_id, event_id, created_at FROM rsvp WHERE user_id = ? AND event_id = ?";
        List<RSVP> rsvps = jdbcTemplate.query(sql, MAPPER, userId, eventId);
        return rsvps.isEmpty() ? null : rsvps.get(0);
    }

    public boolean removeRSVPbyID(Long userId, Long eventId) {
        String sql = "DELETE FROM rsvp WHERE user_id = ? AND event_id = ?";
        boolean status = jdbcTemplate.update(sql, userId, eventId) > 0;
        return status;
    }

    public boolean removeRSVPbyEvent(Long eventId) {
        String sql = "DELETE FROM rsvp WHERE event_id = ?";
        boolean status = jdbcTemplate.update(sql, eventId) > 0;
        return status;
    }

    public static class AttendeeRow {
      private final String name;
      private final String email;
      public AttendeeRow(String name, String email) {
        this.name = name; this.email = email;
      }
      public String getName() { return name; }
      public String getEmail() { return email; }
    }

    public List<AttendeeRow> findAttendeesByEvent(Long eventId) {
      String sql = """
          SELECT u.name, u.email
          FROM rsvp r
          JOIN users u ON u.user_id = r.user_id
          WHERE r.event_id = ?
          ORDER BY u.name ASC
          """;
      return jdbcTemplate.query(sql, ps -> ps.setLong(1, eventId),
            (rs, i) -> new AttendeeRow(rs.getString("name"), rs.getString("email"))
            );
    }

    public List<Event> findEventsByUserId(Long userId) {
        String sql = """
            SELECT e.event_id, e.name, e.description, e.created_by_user_id,
                e.date_time, e.location, e.capacity, e.price
            FROM events e
            JOIN rsvp r ON e.event_id = r.event_id
            WHERE r.user_id = ?
            ORDER BY e.date_time ASC
        """;

    List<Event> events = jdbcTemplate.query(sql, ps -> ps.setLong(1, userId), (rs, rowNum) -> new Event(
            rs.getLong("event_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getObject("created_by_user_id") != null ? rs.getLong("created_by_user_id") : null,
            rs.getTimestamp("date_time").toLocalDateTime(),
            rs.getString("location"),
            new ArrayList<>(), // we’ll fill categories next
            rs.getObject("capacity") != null ? rs.getInt("capacity") : null,
            rs.getBigDecimal("price")
    ));

        for(Event event : events){
            String catSql = """
                    SELECT c.name
                    FROM category c
                    JOIN event_category ec ON c.category_id = ec.category_id
                    WHERE ec.event_id = ?
                    """;

            List<String> categories = jdbcTemplate.query(catSql, ps -> ps.setLong(1, event.getEventId()),
            (rs, rowNum) -> rs.getString("name"));
            event.setCategory(categories);
    }
            return events;
        }
}


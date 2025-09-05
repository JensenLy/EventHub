package au.edu.rmit.sept.webapp.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.Event;

@Repository
public class EventRepository {
  
  private final JdbcTemplate jdbcTemplate;

  public EventRepository(JdbcTemplate jdbcTemplate){
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final RowMapper<Event> MAPPER = (ResultSet rs, int rowNumber) -> 
                                    new Event(rs.getLong("event_id"),
                                              rs.getString("name"),
                                              rs.getString("description"),
                                              rs.getObject("created_by_user_id") != null ? rs.getLong("created_by_user_id") : null,
                                              rs.getTimestamp("date_time").toLocalDateTime(),
                                              rs.getString("location"),
                                              rs.getString("category"),
                                              rs.getObject("capacity") != null ? rs.getInt("capacity") : null,
                                              rs.getObject("category_fk_id") != null ? rs.getLong("category_fk_id") : null,
                                              rs.getBigDecimal("price")
  );

  public List<Event> findUpcomingEventsSorted () {
    String sql = """
        SELECT  event_id, name, description, created_by_user_id,
                date_time, location, category, capacity, category_fk_id, price
        FROM events
        WHERE date_time >= CURRENT_TIMESTAMP
        ORDER BY date_time ASC
        """;
    
    return jdbcTemplate.query(sql, MAPPER);
  }


  public Event createEvent(Event event) {
    String sql = """
        INSERT INTO events (name, description, created_by_user_id, date_time, location, category, capacity, category_fk_id, price)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(sql, new String[]{"event_id"});
        ps.setString(1, event.getName());
        ps.setString(2, event.getDescription());
        ps.setLong(3, event.getCreatedByUserId());
        ps.setObject(4, event.getDateTime());
        ps.setString(5, event.getLocation());
        ps.setString(6, event.getCategory());
        ps.setInt(7, event.getCapacity());
        ps.setLong(8, event.getCategoryFkId());
        ps.setBigDecimal(9, event.getPrice());
        return ps;
    }, keyHolder);
    Number key = keyHolder.getKey();
    if (key != null) {
        event.setEventId(key.longValue());
    }
    return event;
  }


  public boolean checkEventExists(Long organiserId, String name, String category, String location)
  {
    String sql = "SELECT COUNT(*) FROM events WHERE created_by_user_id = ? AND name = ? AND category = ? AND location = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, organiserId, name, category, location);

      return count != null && count > 0;
  }

}

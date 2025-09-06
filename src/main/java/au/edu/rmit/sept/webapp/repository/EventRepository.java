package au.edu.rmit.sept.webapp.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                                              List.of(rs.getString("category_name")),
                                              rs.getObject("capacity") != null ? rs.getInt("capacity") : null,
                                              List.of(rs.getObject("category_fk_id") != null ? rs.getLong("category_fk_id") : null),
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
        INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(sql, new String[]{"event_id"});
        ps.setString(1, event.getName());
        ps.setString(2, event.getDescription());
        ps.setLong(3, event.getCreatedByUserId());
        ps.setObject(4, event.getDateTime());
        ps.setString(5, event.getLocation());
        ps.setInt(6, event.getCapacity());
        ps.setBigDecimal(7, event.getPrice());
        return ps;
    }, keyHolder);
    Number key = keyHolder.getKey();
    if (key != null) {
        event.setEventId(key.longValue());
    }
    if (event.getCategoryFkIds() != null && !event.getCategoryFkIds().isEmpty()) {
    String joinSql = "INSERT INTO event_categories(event_id, category_id) VALUES (?, ?)"; 
    for(Long catId : event.getCategoryFkIds()){
      jdbcTemplate.update(joinSql, event.getEventId(), catId);
    }   
  }

    return event;
  }


  public boolean checkEventExists(Long organiserId, String name, List<Long> categoryFkIds, String location)
  {
     if(categoryFkIds == null || categoryFkIds.isEmpty()) return false;

    String placeholder = categoryFkIds.stream().map(id -> "?").collect(Collectors.joining(", "));

    String sql = "SELECT COUNT(*) FROM events" + 
    " WHERE created_by_user_id = ? AND name = ? AND location = ? AND category_fk_id IN (" + placeholder + ")";

    List<Object> params = new ArrayList<>();
    params.add(organiserId);
    params.add(name);
    params.add(location);
    params.addAll(categoryFkIds);

    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());

      return count != null && count > 0;
  }

}

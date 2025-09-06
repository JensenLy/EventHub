package au.edu.rmit.sept.webapp.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import au.edu.rmit.sept.webapp.model.EventCategory;
@Repository
public class CategoryRepository  {
    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository (JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EventCategory> findAll(){
        String sql = "SELECT category_id, name FROM categories";
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
                 new EventCategory(rs.getLong("category_id"), rs.getString("name")));
    }
}

package todo.manager;

import todo.db.DBConnectionProvider;
import todo.model.ToDo;
import todo.model.ToDoStatus;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ToDoManager {

    private Connection connection = DBConnectionProvider.getInstance().getConnection();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private UserManager userManager = new UserManager();

    public boolean create(ToDo todo) {
        String sql = "INSERT INTO todo(title,deadline,status,user_id) VALUES(?,?,?,?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, todo.getTitle());
            statement.setString(2, sdf.format(todo.getDeadline()));
            statement.setString(3, todo.getStatus().name());
            statement.setLong(4, todo.getUser().getId());
            statement.executeUpdate();

            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                todo.setId(rs.getLong(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ToDo getByID(long id) {
        String sql = "SELECT * FROM todo WHERE id=" + id;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                return getToDOFromResultSet(resultSet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(long id, ToDoStatus status) {
        String sql = "UPDATE todo SET status = '" + status.name() + "' WHERE id = " + id;
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(long id) {
        String sql = "DELETE FROM todo  WHERE id = " + id;
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ToDo> getAllToDosByUser(Long userId) {
        List<ToDo> toDos = new ArrayList<ToDo>();
        String sql = "SELECT * FROM todo WHERE user_id = " + userId;
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                toDos.add(getToDOFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toDos;
    }

    public List<ToDo> getAllToDosByUserAndStatus(Long userId, ToDoStatus status) {
        List<ToDo> toDos = new ArrayList<ToDo>();
        String sql = "SELECT * FROM todo WHERE user_id = ? AND status = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, userId);
            statement.setString(2, status.name());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                toDos.add(getToDOFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toDos;
    }

    private ToDo getToDOFromResultSet(ResultSet resultSet) {
        try {
            try {
                return ToDo.builder()
                        .id(resultSet.getLong(1))
                        .title(resultSet.getString(2))
                        .deadline(sdf.parse(resultSet.getString(3)))
                        .status(ToDoStatus.valueOf(resultSet.getString(4)))
                        .user(userManager.getByID(resultSet.getLong(5)))
                        .createdDate(sdf.parse(resultSet.getString(6)))
                        .build();
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

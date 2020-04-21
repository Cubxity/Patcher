package club.sk1er.patcher.database;

import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AssetsDatabase {

    private Connection connection;

    public AssetsDatabase() {
        try {
            File minecraftHome = Launch.minecraftHome;
            if (minecraftHome == null) minecraftHome = new File(".");
            connection = DriverManager.getConnection("jdbc:h2:" + minecraftHome.getAbsolutePath() + "/asset_cache_from_patcher_mod.h2", "", "");
            connection.prepareStatement("create table if not exists assets (pack varchar(256), name varchar(1024), data BINARY, mcmeta BINARY)").executeUpdate();
            connection.prepareStatement("create index name on assets (name)").executeUpdate();
            connection.prepareStatement("create index pack_name on assets (pack,name)").executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (connection != null)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }));
    }

    public DatabaseReturn getData(String name) {
        try {
            PreparedStatement statement = connection.prepareStatement("select * from assets where name=?");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                InputStream data = resultSet.getBinaryStream("data");
                InputStream mcmeta = resultSet.getBinaryStream("mcmeta");
                boolean noMeta = resultSet.wasNull();
                return new DatabaseReturn(IOUtils.readFully(data, data.available()), noMeta ? null : IOUtils.readFully(mcmeta, mcmeta.available()), resultSet.getString("pack"));
            }
            return null;
        } catch (SQLException | IOException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public void clearAll() {
        try {
            connection.prepareStatement("delete from assets").executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void clearPack(String pack) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("delete from assets where pack=?");
            preparedStatement.setString(1, pack);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void update(String pack, String name, byte[] data, byte[] mcMeta) {
        try {
            PreparedStatement statement = connection.prepareStatement("merge into assets key(pack,`name`) values (?,?,?,?)");
            statement.setString(1, pack);
            statement.setString(2, name);
            statement.setBinaryStream(3, new ByteArrayInputStream(data));
            statement.setBinaryStream(4, mcMeta == null ? null : new ByteArrayInputStream(mcMeta));

            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

}

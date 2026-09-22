package bureau.repository;

import java.util.List;

public interface TableRepository {
    List<String> findTableNames();
}

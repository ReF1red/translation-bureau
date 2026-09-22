package bureau.service;

import bureau.repository.TableRepository;

import java.util.List;

public class TableService {
    private final TableRepository tableRepository;

    public TableService(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    public List<String> getTableNames() {
        return tableRepository.findTableNames();
    }
}

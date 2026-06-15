package interfaces;

import java.util.List;

public interface Searchable<T> {
    List<T> searchByKeyword(String keyword);
    List<T> filterByCriteria(String criteria, Object value);
}
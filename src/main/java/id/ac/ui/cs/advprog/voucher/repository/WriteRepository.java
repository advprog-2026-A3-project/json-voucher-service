package id.ac.ui.cs.advprog.voucher.repository;

public interface WriteRepository<T> {
    T save(T entity);
    void delete(T entity);
}
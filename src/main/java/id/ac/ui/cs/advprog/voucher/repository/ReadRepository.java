package id.ac.ui.cs.advprog.voucher.repository;

import java.util.List;

public interface ReadRepository<T> {
    List<T> findAll();
}
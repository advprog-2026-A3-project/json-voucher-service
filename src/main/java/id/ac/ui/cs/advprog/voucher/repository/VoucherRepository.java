package id.ac.ui.cs.advprog.voucher.repository;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class VoucherRepository implements VoucherReadRepository, VoucherWriteRepository {
    private final JpaVoucherRepository jpaVoucherRepository;

    public VoucherRepository(JpaVoucherRepository jpaVoucherRepository){
        this.jpaVoucherRepository = jpaVoucherRepository;
    }

    @Override
    public List<Voucher> findAll(){
        return jpaVoucherRepository.findAll();
    }

    @Override
    public Optional<Voucher> findByVoucherCode(String voucherCode){
        return jpaVoucherRepository.findByVoucherCode(voucherCode);
    }

    @Override
    public List<Voucher> findAllByCreatedAtDesc(){
        return jpaVoucherRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Voucher save(Voucher entity){
        return jpaVoucherRepository.save(entity);
    }

    @Override
    public boolean existsByVoucherCode(String voucherCode){
        return jpaVoucherRepository.existsByVoucherCode(voucherCode);
    }
}
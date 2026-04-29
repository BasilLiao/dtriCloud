package dtri.com.tw.pgsql.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dtri.com.tw.pgsql.entity.MusUserSearch;

@Repository
public interface MusUserSearchDao extends JpaRepository<MusUserSearch, Long> {

    Optional<MusUserSearch> findByMusuid(Long musuid);

    Optional<MusUserSearch> findByMusuidAndSysstatus(Long musuid, Integer sysstatus);
}

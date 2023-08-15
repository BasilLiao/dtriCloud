package dtri.com.tw.pgsql.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import dtri.com.tw.pgsql.entity.BasicShippingList;

public interface BasicShippingListDao extends JpaRepository<BasicShippingList, Long> {

}
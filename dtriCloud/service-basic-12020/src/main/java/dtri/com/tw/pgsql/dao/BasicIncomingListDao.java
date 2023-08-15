package dtri.com.tw.pgsql.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import dtri.com.tw.pgsql.entity.BasicIncomingList;

public interface BasicIncomingListDao extends JpaRepository<BasicIncomingList, Long> {
	
}
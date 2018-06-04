package com.testapp.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.testapp.entity.ItemEntity;

@Repository
public interface ItemRepository extends CassandraRepository<ItemEntity>, ItemRepositoryCustom {

}
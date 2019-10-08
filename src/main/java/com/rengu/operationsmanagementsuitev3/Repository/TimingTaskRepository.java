package com.rengu.operationsmanagementsuitev3.Repository;


import com.rengu.operationsmanagementsuitev3.Entity.TimingTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimingTaskRepository extends JpaRepository<TimingTasks,String> {


}

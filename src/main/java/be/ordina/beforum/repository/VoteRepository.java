package be.ordina.beforum.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import be.ordina.beforum.model.Vote;

public interface VoteRepository extends PagingAndSortingRepository<Vote, String> {

    List<Vote> findByVoter(String voter);
    List<Vote> findById(String id);
    Vote findByIdAndVoter(String Id, String voter);

}
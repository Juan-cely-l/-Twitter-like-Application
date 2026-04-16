package edu.eci.co.service;

import edu.eci.co.dto.CreatePostRequest;
import edu.eci.co.dto.PostResponse;
import edu.eci.co.entity.Post;
import edu.eci.co.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository){
        this.postRepository=postRepository;
    }
    public PostResponse createPost(CreatePostRequest request,String authorId,String authorName){

        Post post =new Post(
                request.getContent(),
                authorId,
                authorName,
                Instant.now()
        );
        Post saved =postRepository.save(post);
        return new PostResponse(
                saved.getId(),
                saved.getContent(),
                saved.getAuthorId(),
                saved.getAuthorName(),
                saved.getCreatedAt()
        );}

    public List<PostResponse> getAllPosts(){
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(post-> new PostResponse(
                        post.getId(),
                        post.getContent(),
                        post.getAuthorId(),
                        post.getAuthorName(),
                        post.getCreatedAt()
                ))
                .toList();
    }
}


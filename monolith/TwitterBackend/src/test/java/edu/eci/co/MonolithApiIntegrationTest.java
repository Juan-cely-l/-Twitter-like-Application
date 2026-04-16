package edu.eci.co;

import edu.eci.co.support.TestJwtFactory;
import edu.eci.co.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MonolithApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
    }

    @Test
    void getPostsWithoutTokenShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getStreamWithoutTokenShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/stream"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void postPostsWithoutTokenShouldReturn401WithClearJson() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"post sin token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/api/posts"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void getMeWithoutTokenShouldReturn401WithClearJson() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/api/me"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void postPostsWithJwtButWithoutWriteScopeShouldReturn403WithClearJson() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(TestJwtFactory.jwtWithScopes("read:profile"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"post sin scope\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Insufficient scope for this resource"))
                .andExpect(jsonPath("$.path").value("/api/posts"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void getMeWithJwtButWithoutReadProfileScopeShouldReturn403WithClearJson() throws Exception {
        mockMvc.perform(get("/api/me")
                        .with(TestJwtFactory.jwtWithScopes("write:posts")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Insufficient scope for this resource"))
                .andExpect(jsonPath("$.path").value("/api/me"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void postPostsWithJwtAndWriteScopeShouldSucceed() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(TestJwtFactory.jwtWithScopes("write:posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Mi primer post\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("Mi primer post"))
                .andExpect(jsonPath("$.authorId").value(TestJwtFactory.SUBJECT))
                .andExpect(jsonPath("$.authorName").value(TestJwtFactory.NAME));
    }

    @Test
    void getMeWithJwtAndReadProfileScopeShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/me")
                        .with(TestJwtFactory.jwtWithScopes("read:profile")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sub").value(TestJwtFactory.SUBJECT))
                .andExpect(jsonPath("$.name").value(TestJwtFactory.NAME))
                .andExpect(jsonPath("$.nickname").value(TestJwtFactory.NICKNAME))
                .andExpect(jsonPath("$.email").value(TestJwtFactory.EMAIL));
    }

    @Test
    void postPostsWithEmptyContentShouldReturn400WithValidationJson() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(TestJwtFactory.jwtWithScopes("write:posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/posts"))
                .andExpect(jsonPath("$.validationErrors.content").value("must not be blank"));
    }

    @Test
    void postPostsWithBlankContentShouldReturn400WithValidationJson() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(TestJwtFactory.jwtWithScopes("write:posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/posts"))
                .andExpect(jsonPath("$.validationErrors.content").value("must not be blank"));
    }

    @Test
    void postPostsWithContentOver140CharsShouldReturn400WithValidationJson() throws Exception {
        String tooLongContent = "a".repeat(141);
        mockMvc.perform(post("/api/posts")
                        .with(TestJwtFactory.jwtWithScopes("write:posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + tooLongContent + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/posts"))
                .andExpect(jsonPath("$.validationErrors.content", containsString("140")));
    }

    @Test
    void postPostsWithExactly140CharsShouldSucceed() throws Exception {
        String exactContent = "b".repeat(140);
        mockMvc.perform(post("/api/posts")
                        .with(TestJwtFactory.jwtWithScopes("write:posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + exactContent + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(exactContent));
    }

    @Test
    void createPostResponseShouldContainAllExpectedFields() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(TestJwtFactory.jwtWithScopes("write:posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"post con respuesta completa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("post con respuesta completa"))
                .andExpect(jsonPath("$.authorId").value(TestJwtFactory.SUBJECT))
                .andExpect(jsonPath("$.authorName").value(TestJwtFactory.NAME))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void getPostsShouldContainCreatedPost() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(TestJwtFactory.jwtWithScopes("write:posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"post visible en listado\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].content", hasItem("post visible en listado")))
                .andExpect(jsonPath("$[*].authorId", hasItem(TestJwtFactory.SUBJECT)));
    }

    @Test
    void createPostShouldPersistPostInDatabase() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(TestJwtFactory.jwtWithScopes("write:posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"post persistido\"}"))
                .andExpect(status().isOk());

        assertThat(postRepository.findAll()).hasSize(1);
        assertThat(postRepository.findAll().getFirst().getContent()).isEqualTo("post persistido");
    }
}

package com.example.week5;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.week5.repository.BoardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BoardFlowTest {
    @Autowired MockMvc mvc;
    @Autowired BoardRepository repository;

    @Test
    void pageFlowCreatesListsUpdatesAndDeletes() throws Exception {
        mvc.perform(get("/boards")).andExpect(status().isOk())
                .andExpect(view().name("board/list"));

        mvc.perform(post("/boards").param("title", "첫 글").param("content", "내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/boards"));

        long id = repository.findAll().stream().filter(b -> b.getTitle().equals("첫 글"))
                .findFirst().orElseThrow().getId();
        mvc.perform(get("/boards/" + id)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("첫 글")));

        mvc.perform(post("/boards/" + id + "/edit").param("title", "수정한 글").param("content", "수정"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/boards/" + id));

        mvc.perform(get("/api/boards/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정한 글"));

        mvc.perform(post("/boards/" + id + "/delete"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/boards/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void restFlowAndPracticeEndpoints() throws Exception {
        mvc.perform(get("/practice/add").param("a", "2").param("b", "3"))
                .andExpect(status().isOk()).andExpect(view().name("practice/result"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("결과")));

        mvc.perform(get("/api/practice/multiply").param("a", "2").param("b", "3"))
                .andExpect(status().isOk()).andExpect(content().string("6"));

        mvc.perform(post("/api/boards").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"API 글\",\"content\":\"본문\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("API 글"));

        long id = repository.findAll().stream().filter(b -> b.getTitle().equals("API 글"))
                .findFirst().orElseThrow().getId();
        mvc.perform(put("/api/boards/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"API 수정\",\"content\":\"본문 2\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("API 수정"));

        mvc.perform(delete("/api/boards/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/boards/" + id)).andExpect(status().isNotFound());
    }
}

package com.ypm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.ypm.service.PlayListService;
import com.ypm.service.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PlayListControllerTest {

    @MockBean
    private PlayListService playListService;

    @MockBean
    private VideoService videosService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenCorrectRequest_whenGetPlayLists_thenResponseContainsPlayListName() throws Exception {
        var login = oauth2Login()
            .clientRegistration(this.clientRegistrationRepository.findByRegistrationId("google"));

        Playlist playlist = new Playlist();
        playlist.setSnippet(new PlaylistSnippet().setTitle("Test Playlist"));
        List<Playlist> playlists = Collections.singletonList(playlist);

        when(playListService.getPlayLists(any())).thenReturn(playlists);

        mockMvc.perform(get("/playlists").with(login))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(playlists)));

        verify(playListService, times(1)).getPlayLists(any());
    }

    @Test
    void givenCorrectRequest_whenGetPlayListVideos_thenResponseContainsVideoTitle() throws Exception {
        var login = oauth2Login()
            .clientRegistration(this.clientRegistrationRepository.findByRegistrationId("google"));

        PlaylistItem video = new PlaylistItem();
        video.setSnippet(new PlaylistItemSnippet().setTitle("New Title"));
        List<PlaylistItem> videos = List.of(video);

        when(videosService.getPlayListVideos(any(), any())).thenReturn(videos);

        mockMvc.perform(get("/playlists/{playlistId}", "someId").with(login))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(videos)));

        verify(videosService, times(1)).getPlayListVideos(any(), any());
    }

    @Test
    void givenCorrectRequest_whenUpdatePlayListTitle_thenResponseContainsUpdatedTitle() throws Exception {
        var login = oauth2Login()
            .clientRegistration(this.clientRegistrationRepository.findByRegistrationId("google"));

        Playlist playlist = new Playlist();
        playlist.setSnippet(new PlaylistSnippet().setTitle("New Title"));

        when(playListService.updatePlayListTitle(any(), any(), any())).thenReturn(playlist);

        mockMvc.perform(put("/playlists/{playlistId}", "someId")
                .with(login)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playlist)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(playlist)));

        verify(playListService, times(1))
            .updatePlayListTitle(any(), any(), any());
    }

    @Test
    void givenCorrectRequest_whenMoveVideos_thenResponseContainsMovedVideos() throws Exception {
        var login = oauth2Login()
            .clientRegistration(this.clientRegistrationRepository.findByRegistrationId("google"));

        List<String> videosIds = List.of("videoId1", "videoId2");
        List<PlaylistItem> videos = List.of(new PlaylistItem(), new PlaylistItem());

        when(videosService.moveVideos(any(), any(), any(), any())).thenReturn(videos);

        mockMvc.perform(put("/playlists/{playlistId}/{targetPlaylistId}",
                "someId", "targetId")
                .with(login)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(videosIds)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(videos)));

        verify(videosService, times(1))
            .moveVideos(any(), any(), any(), any());
    }
}

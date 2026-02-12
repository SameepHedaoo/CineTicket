package com.cineticket.movie.Service;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GitHubImageStorageService {

    private final String posterStorage;
    private final String token;
    private final String owner;
    private final String repo;
    private final String branch;
    private final String folder;
    private final RestTemplate restTemplate = new RestTemplate();

    public GitHubImageStorageService(
            @Value("${app.poster.storage:local}") String posterStorage,
            @Value("${github.token:}") String token,
            @Value("${github.repo.owner:}") String owner,
            @Value("${github.repo.name:}") String repo,
            @Value("${github.repo.branch:main}") String branch,
            @Value("${github.repo.folder:posters}") String folder) {
        this.posterStorage = posterStorage;
        this.token = token;
        this.owner = owner;
        this.repo = repo;
        this.branch = branch;
        this.folder = folder;
    }

    public boolean isEnabled() {
        return "github".equalsIgnoreCase(posterStorage)
                && isPresent(token)
                && isPresent(owner)
                && isPresent(repo);
    }

    public String uploadPoster(byte[] bytes, String fileName) {
        if (!isEnabled()) {
            throw new IllegalStateException("GitHub poster storage is not configured.");
        }

        String repoPath = normalizeFolder(folder) + fileName;
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + repoPath;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Upload movie poster: " + fileName);
        body.put("content", Base64.getEncoder().encodeToString(bytes));
        body.put("branch", branch);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        headers.set(HttpHeaders.USER_AGENT, "CineTicket");

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(body, headers),
                    Map.class);
        } catch (HttpClientErrorException.Forbidden ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "GitHub rejected poster upload (403). Check token permissions and repo access.");
        } catch (HttpClientErrorException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "GitHub rejected poster upload: " + ex.getStatusCode().value());
        }

        Map responseBody = response.getBody();
        if (responseBody != null) {
            Object contentObj = responseBody.get("content");
            if (contentObj instanceof Map contentMap) {
                Object downloadUrl = contentMap.get("download_url");
                if (downloadUrl instanceof String urlValue && !urlValue.isBlank()) {
                    return urlValue;
                }
            }
        }

        return "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + branch + "/" + repoPath;
    }

    private String normalizeFolder(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.trim().replace("\\", "/");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}

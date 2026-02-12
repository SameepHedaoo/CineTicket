package com.cineticket.movie.dto;

public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private String language;
    private Integer durationMinutes;
    private String genre;
    private String posterUrl;

    public MovieResponse(Long id, String title, String description, String language, Integer durationMinutes,
            String genre, String posterUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.language = language;
        this.durationMinutes = durationMinutes;
        this.genre = genre;
        this.posterUrl = posterUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
}

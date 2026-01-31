package com.cineticket.booking.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.cineticket.auth.entity.UserEntity;
import com.cineticket.show.Entity.Show;
import com.cineticket.show.Entity.ShowSeat;

import jakarta.persistence.*;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserEntity user;

    @ManyToOne
    private Show show;

    @ManyToMany
    private List<ShowSeat> showSeats;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime lockExpiryTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public List<ShowSeat> getShowSeats() {
        return showSeats;
    }

    public void setShowSeats(List<ShowSeat> showSeats) {
        this.showSeats = showSeats;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getLockExpiryTime() {
        return lockExpiryTime;
    }

    public void setLockExpiryTime(LocalDateTime lockExpiryTime) {
        this.lockExpiryTime = lockExpiryTime;
    }

}

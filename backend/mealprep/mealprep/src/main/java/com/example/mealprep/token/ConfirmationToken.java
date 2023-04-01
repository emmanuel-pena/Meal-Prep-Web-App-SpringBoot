package com.example.mealprep.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ConfirmationToken {
    private UUID id;
    private String token;
    private String createdAt;
    private String expiresAt;
    private String confirmedAt;
    private UUID memberId;

    public ConfirmationToken(UUID id,
                             String token,
                             String createdAt,
                             String expiresAt,
                             UUID memberId)
    {
        this.id = id;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.memberId = memberId;
    }

}

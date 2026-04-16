package edu.eci.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MeResponse", description = "Authenticated user profile claims")
public class MeResponse {
    @Schema(description = "Auth0 subject identifier", example = "google-oauth2|105063856870618633482")
    private String sub;
    @Schema(description = "Display name from custom claim", example = "gptazo")
    private String name;
    @Schema(description = "Nickname from custom claim", example = "gptazo7")
    private String nickname;
    @Schema(description = "Email from custom claim", example = "gptazo7@gmail.com")
    private String email;

    public MeResponse(){}

    public MeResponse(String sub , String name, String nickname,String email){
        this.sub=sub;
        this.name=name;
        this.nickname=nickname;
        this.email=email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSub() {
        return sub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }
}

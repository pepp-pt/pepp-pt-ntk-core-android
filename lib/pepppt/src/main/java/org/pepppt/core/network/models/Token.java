package org.pepppt.core.network.models;

import org.jetbrains.annotations.NotNull;


/**
 * Model class of API Backend token
 * String Token is the token itself
 */
class Token {
    // Token Data
    private String _APPLICATION;
    private String _IDENTITY_ID;
    private String expires_at;
    private String type;
    private String token;
    private String _IDENTITY;

    public Token(String _APPLICATION, String _IDENTITY_ID, String expires_at, String type, String token, String _IDENTITY) {
        this._APPLICATION = _APPLICATION;
        this._IDENTITY_ID = _IDENTITY_ID;
        this.expires_at = expires_at;
        this.type = type;
        this.token = token;
        this._IDENTITY = _IDENTITY;
    }

    public String get_APPLICATION() {
        return _APPLICATION;
    }

    public void set_APPLICATION(String _APPLICATION) {
        this._APPLICATION = _APPLICATION;
    }

    public String get_IDENTITY_ID() {
        return _IDENTITY_ID;
    }

    public void set_IDENTITY_ID(String _IDENTITY_ID) {
        this._IDENTITY_ID = _IDENTITY_ID;
    }

    public String getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(String expires_at) {
        this.expires_at = expires_at;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String get_IDENTITY() {
        return _IDENTITY;
    }

    public void set_IDENTITY(String _IDENTITY) {
        this._IDENTITY = _IDENTITY;
    }

    @NotNull
    @Override
    public String toString() {
        return "CGAToken{" +
                "_APPLICATION='" + _APPLICATION + '\'' +
                ", _IDENTITY_ID='" + _IDENTITY_ID + '\'' +
                ", expires_at='" + expires_at + '\'' +
                ", type='" + type + '\'' +
                ", token='" + token + '\'' +
                ", _IDENTITY='" + _IDENTITY + '\'' +
                '}';
    }
}

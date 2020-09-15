package com.example.converter;

public class Valute {
    private String Name;
    private String CharCode;
    private String Value;
    private String Previous;

    public Valute(String Name, String CharCode, String Value, String Previous) {
        this.Name = Name;
        this.CharCode = CharCode;
        this.Value = Value;
        this.Previous = Previous;
    }

    public String getName() {
        return Name;
    }

    public String getCharCode() {
        return CharCode;
    }

    public String getValue() {
        return Value;
    }

    public String getPrevious() {
        return Previous;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public void setCharCode(String CharCode) {
        this.CharCode = CharCode;
    }

    public void setValue(String Value) {
        this.Value = Value;
    }

    public void setPrevious(String Previous) {
        this.Previous = Previous;
    }
}


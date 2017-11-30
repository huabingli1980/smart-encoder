// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.models;

public class UserLoginModel
{
    private Integer id;
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String mobile;
    private String email;
    
    public Integer getId() {
        return this.id;
    }
    
    public void setId(final Integer id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        final UserLoginModel person = (UserLoginModel)obj;
        if (this.firstName != null) {
            if (this.firstName.equals(person.firstName)) {
                return true;
            }
        }
        else if (person.firstName == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "UserLoginModel [id=" + this.id + ", firstName=" + this.firstName + ", lastName=" + this.lastName + ", userName=" + this.userName + ", password=" + this.password + ", mobile=" + this.mobile + ", email=" + this.email + "]";
    }
    
    public String getUserName() {
        return this.userName;
    }
    
    public void setUserName(final String userName) {
        this.userName = userName;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(final String password) {
        this.password = password;
    }
    
    public String getMobile() {
        return this.mobile;
    }
    
    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }
    
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(final String email) {
        this.email = email;
    }
}

// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.service;

import com.sqlite.models.UserLoginModel;
import com.sqlite.models.LoginModel;

public interface IUserService
{
    UserLoginModel getUser(final LoginModel p0);
}

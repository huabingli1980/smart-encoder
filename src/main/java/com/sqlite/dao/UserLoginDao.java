// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.dao;

import com.sqlite.entities.UserLogin;
import org.springframework.data.repository.CrudRepository;

public interface UserLoginDao extends CrudRepository<UserLogin, Integer>
{
    UserLogin findUserLoginByUserNameAndPassword(final String p0, final String p1);
}

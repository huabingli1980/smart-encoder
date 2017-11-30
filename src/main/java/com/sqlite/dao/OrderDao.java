// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.dao;

import com.sqlite.model.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderDao extends CrudRepository<Order, Integer>
{
}

// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.dao;

import com.sqlite.model.Order;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.sqlite.entities.PassInspect;
import java.util.List;
import com.sqlite.domain.ContextManager;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class PassInspectDao
{
    @PersistenceContext
    EntityManager entityManager;
    String tableName;
    
    public PassInspectDao() {
        this.tableName = "pass_inspects_" + ContextManager.dataStr;
    }
    
    public List getAllByOrderNum(final String key, final String orderNum) {
        final String query = "select * from pass_inspects_" + key + "  where order_num = ?1";
        return this.entityManager.createNativeQuery(query).setParameter(1, (Object)orderNum).getResultList();
    }
    
    public int save(final PassInspect passInspect) {
        final String query = "insert into " + this.tableName + " (epc, order_num, seq, status, tid, time, epc_multiple, is_leading) values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)";
        this.entityManager.createNativeQuery(query).setParameter(1, (Object)passInspect.getEpc()).setParameter(2, (Object)passInspect.getOrderNum()).setParameter(3, (Object)passInspect.getSeq()).setParameter(4, (Object)passInspect.getStatus()).setParameter(5, (Object)passInspect.getTid()).setParameter(6, (Object)passInspect.getTime()).setParameter(7, (Object)passInspect.getEpcMultiple()).setParameter(8, (Object)passInspect.isLeading()).executeUpdate();
        final String retriveLastInsertedIdSql = "SELECT max(rowid) from " + this.tableName;
        final int rowId = (int)this.entityManager.createNativeQuery(retriveLastInsertedIdSql).getSingleResult();
        return rowId;
    }
    
    public void update(final PassInspect passInspect) {
        final String query = "UPDATE " + this.tableName + "\n" + "SET status = 'bad'\n" + "WHERE\n" + "\trowid = (\n" + "\t\tSELECT\n" + "\t\t\trowid\n" + "\t\tFROM\n" + "\t\t\t" + this.tableName + "\n" + "\t\tWHERE\n" + "\t\t\torder_num = ?1 AND is_leading == 0\n" + "\t\tORDER BY\n" + "\t\t\tseq DESC\n" + "\t\tLIMIT 1\n" + "\t)";
        this.entityManager.createNativeQuery(query).setParameter(1, (Object)passInspect.getOrderNum()).executeUpdate();
    }
    
    public void updateOrderNumOfPrevious(final String orderNumFrom, final String orderNumTo) {
        final String query = "UPDATE " + this.tableName + "\n" + "SET status = 'bad', order_num = ?2\n" + "WHERE\n" + "\trowid = (\n" + "\t\tSELECT\n" + "\t\t\trowid\n" + "\t\tFROM\n" + "\t\t\t" + this.tableName + "\n" + "\t\tWHERE\n" + "\t\t\torder_num = ?1 AND is_leading == 0\n" + "\t\tORDER BY\n" + "\t\t\tseq DESC\n" + "\t\tLIMIT 1\n" + "\t)";
        this.entityManager.createNativeQuery(query).setParameter(1, (Object)orderNumFrom).setParameter(2, (Object)orderNumTo).executeUpdate();
    }
    
    public void updateFirstOfOrder(final String orderNum) {
        final String query = "UPDATE " + this.tableName + "\n" + "SET status = 'bad'\n" + "WHERE\n" + "\trowid = (\n" + "\t\tSELECT\n" + "\t\t\trowid\n" + "\t\tFROM\n" + "\t\t\t" + this.tableName + "\n" + "\t\tWHERE\n" + "\t\t\torder_num = ?1\n" + "\t\tORDER BY\n" + "\t\t\tseq ASC\n" + "\t\tLIMIT 1\n" + "\t)";
        this.entityManager.createNativeQuery(query).setParameter(1, (Object)orderNum).executeUpdate();
    }
    
    public void updateDamage(final String key, final Set<String> epcs, final String orderNum) {
        final StringBuilder list = this.toList(epcs);
        final String query = "UPDATE " + this.tableName + "\n" + "SET status = 'damage'\n" + "WHERE\n" + "\tepc IN (" + list.toString() + ") and order_num =?1";
        this.entityManager.createNativeQuery(query).setParameter(1, (Object)orderNum).executeUpdate();
    }
    
    public static void main(final String[] args) {
        final Set<String> set = new HashSet<String>();
        set.add("ab");
        set.add("ab5");
        final String str = new PassInspectDao().toList((Set)set).toString();
        System.out.println(str);
    }
    
    private StringBuilder toList(final Set<String> epcs) {
        final StringBuilder list = new StringBuilder();
        final Object[] arr = epcs.toArray();
        for (int length = arr.length, i = 0; i < length; ++i) {
            final String string = (String)arr[i];
            list.append("'").append(string).append("'");
            if (i < length - 1) {
                list.append(",");
            }
        }
        return list;
    }
    
    public int updateEmptyRead(final int rowId, final String epcStr, final String tidStr) {
        final String time = new Date().toLocaleString();
        final String query = "UPDATE " + this.tableName + "\n" + "SET tid = ?2, epc = ?3, status='Good After Re-enter', time = ?4\n" + "WHERE\n" + " rowid = ?1";
        return this.entityManager.createNativeQuery(query).setParameter(1, (Object)rowId).setParameter(2, (Object)tidStr).setParameter(3, (Object)epcStr).setParameter(4, (Object)time).executeUpdate();
    }
    
    public void saveOrder(final Order currentOrder2) {
        System.out.println("Saving order" + currentOrder2.getOrderNum());
    }
}

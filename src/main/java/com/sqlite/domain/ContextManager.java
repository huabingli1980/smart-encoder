// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.domain;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import com.impinj.octane.OctaneSdkException;
import com.sqlite.utils.ReaderManager;
import boot.ApplicationConfig;
import com.sqlite.SampleSqliteApplication;
import com.sqlite.dao.OrderDao;
import org.springframework.beans.BeanUtils;
import java.util.Iterator;
import java.util.Collection;
import java.util.Arrays;
import boot.Pass;
import java.util.List;
import com.sqlite.model.Order;
import java.util.LinkedList;

public class ContextManager
{
    public static LinkedList<Order> orders;
    public static Order currentOrder;
    public static Order lastOrder;
    public static List<Pass> passesInspected;
    public static Pass lastPass;
    public static boolean switchAndKeepFirstPiece;
    public static String dataStr;
    public static boolean isLastPassEndOrder;
    
    public static void main(final String[] args) {
        final LinkedList<Integer> orders = new LinkedList<Integer>();
        System.out.println(orders.peek());
        orders.offer(1);
        orders.offer(2);
        orders.offer(3);
        orders.offer(4);
        orders.addAll(Arrays.asList(5, 6, 7));
        System.out.println(orders.peek());
        System.out.println("pull" + orders.poll());
        for (final Integer integer : orders) {
            System.out.println(integer);
        }
    }
    
    public static void init() {
        OIC.loadOrder(ContextManager.currentOrder = ContextManager.orders.poll());
    }
    
    public static void addOrders(final List<Order> myorders) {
        ContextManager.orders.addAll(myorders);
    }
    
    public static void appendOrder(final Order order) {
        ContextManager.orders.offer(order);
    }
    
    public static int getOrderSkuCount() {
        return ContextManager.currentOrder.getSkuCount();
    }
    
    public static Order getCurrentOrder() {
        return ContextManager.orders.element();
    }
    
    public static boolean isOnlyOneOrderLeft() {
        return ContextManager.orders.size() == 1;
    }
    
    public static boolean loadNextOrder() {
        if (ContextManager.orders.peek() == null) {
            return false;
        }
        BeanUtils.copyProperties((Object)ContextManager.currentOrder, (Object)ContextManager.lastOrder);
        System.out.println("------------------------------------------");
        saveOrder(ContextManager.currentOrder);
        final String currentOrderNum = ContextManager.currentOrder.getOrderNum();
        final int currentSkuCount = ContextManager.currentOrder.getSkuCount();
        (ContextManager.currentOrder = ContextManager.orders.poll()).incrPiecesCount();
        System.out.println("switching order from " + currentOrderNum + "(" + currentSkuCount + ") to " + ContextManager.currentOrder.getOrderNum());
        return true;
    }
    
    public static void saveOrder(final Order currentOrder2) {
        final OrderDao passInspectDao = (OrderDao)SampleSqliteApplication.getBean("orderDao");
        passInspectDao.save(currentOrder2);
    }
    
    private static void onOrderBegin() throws OctaneSdkException {
        final Boolean stopOnOrderChange = Boolean.valueOf(ApplicationConfig.get("ctrl.stop_on_order_change", "false"));
        if (stopOnOrderChange) {
            System.out.println("Stopping printer for order change...");
            ReaderManager.stop();
        }
    }
    
    public static void switchBack() {
        final LinkedList<Order> myorders = new LinkedList<Order>();
        myorders.add(ContextManager.currentOrder);
        myorders.addAll(ContextManager.orders);
        ContextManager.orders = myorders;
        final String currentOrderNum = ContextManager.currentOrder.getOrderNum();
        final int currentSkuCount = ContextManager.currentOrder.getSkuCount();
        OIC.loadOrder(ContextManager.currentOrder = ContextManager.lastOrder);
        System.out.println("------------------------------------------");
        System.out.println("switching back order from " + currentOrderNum + "(" + currentSkuCount + ") to " + ContextManager.currentOrder.getOrderNum());
    }
    
    public static void incrGoodCount() {
        ContextManager.currentOrder.incrGoodCount();
    }
    
    public static void incrDuplicateCount() {
        ContextManager.currentOrder.incrDuplicateCount();
    }
    
    public static void incrLeadingCount() {
        ContextManager.currentOrder.incrLeadingCount();
    }
    
    public static void incrPiecesCount() {
        ContextManager.currentOrder.incrPiecesCount();
    }
    
    public static void incrUnreadableCount() {
        ContextManager.currentOrder.incrUnreadableCount();
    }
    
    public static void incrWrongCount() {
        ContextManager.currentOrder.incrWrongCount();
    }
    
    public static void incrReadMultipleCount() {
        ContextManager.currentOrder.incrReadMultipleCount();
    }
    
    public static void incrUnEncodedCount() {
        ContextManager.currentOrder.incrUnEncodedCount();
    }
    
    static {
        ContextManager.orders = new LinkedList();
        ContextManager.lastOrder = new Order();
        ContextManager.passesInspected = new ArrayList();
        ContextManager.dataStr = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
    }
}

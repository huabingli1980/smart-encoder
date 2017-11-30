// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.controller;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.io.IOException;
import java.io.FileNotFoundException;
import com.sqlite.utils.OrderFileUtils;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InputController
{
    @RequestMapping(value = { "/getSkuCountByOrderNum" }, produces = { "text/plain" })
    @ResponseBody
    public String getSkuCountByOrderNum(final String orderNum, final int orderType) throws FileNotFoundException, IOException {
        final int skuCount = OrderFileUtils.getSkuCountByOrderNum(orderNum, orderType);
        final String valueOf = String.valueOf(skuCount);
        return valueOf;
    }
}

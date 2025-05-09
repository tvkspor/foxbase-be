package com.be.java.foxbase.controller;

import com.be.java.foxbase.dto.zalopay.BookItem;
import com.be.java.foxbase.service.PurchaseService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;

@RestController
@RequestMapping("/callback")
public class ZaloPayCallbackController {
    @Autowired
    private PurchaseService purchaseService;

    private static final Logger logger = LoggerFactory.getLogger(ZaloPayCallbackController.class);
    private static final String KEY2 = "uUfsWgfLkRLzq6W2uNXTCxrfxs51auny";

    private Mac hmacSHA256;

    @PostConstruct
    public void init() throws Exception {
        hmacSHA256 = Mac.getInstance("HmacSHA256");
        hmacSHA256.init(new SecretKeySpec(KEY2.getBytes(), "HmacSHA256"));
    }

    @PostMapping
    public String callback(@RequestBody String jsonStr) {
        JSONObject result = new JSONObject();

        try {
            JSONObject cbdata = new JSONObject(jsonStr);
            String dataStr = cbdata.getString("data");
            String reqMac = cbdata.getString("mac");

            byte[] hashBytes = hmacSHA256.doFinal(dataStr.getBytes());
            String mac = Hex.encodeHexString(hashBytes);

            if (!reqMac.equals(mac)) {
                result.put("returncode", -1);
                result.put("returnmessage", "mac not equal");
            } else {
                JSONObject data = new JSONObject(dataStr);
                String appTransId = data.getString("apptransid");
                String item = data.getString("item");

                ObjectMapper objectMapper = new ObjectMapper();
                List<BookItem> items = objectMapper.readValue(item, new TypeReference<List<BookItem>>() {});

                purchaseService.purchaseBookByZaloPay(items.getFirst().getBookId());

                result.put("returncode", 1);
                result.put("returnmessage", "success");
            }
        } catch (Exception ex) {
            logger.error("Error while processing ZaloPay callback", ex);
            result.put("returncode", 0);
            result.put("returnmessage", ex.getMessage());
        }

        return result.toString();
    }
}

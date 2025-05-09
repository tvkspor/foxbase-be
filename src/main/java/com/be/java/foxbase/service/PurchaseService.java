package com.be.java.foxbase.service;

import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.db.entity.PurchasedBook;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.db.key.UserBookId;
import com.be.java.foxbase.dto.request.PurchaseBookRequest;
import com.be.java.foxbase.dto.request.PurchaseWalletRequest;
import com.be.java.foxbase.dto.request.ZaloPayOrderRequest;
import com.be.java.foxbase.dto.response.PurchaseBookResponse;
import com.be.java.foxbase.dto.response.PurchaseWalletResponse;
import com.be.java.foxbase.dto.response.ZaloPayOrderResponse;
import com.be.java.foxbase.dto.zalopay.Order;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.repository.BookRepository;
import com.be.java.foxbase.repository.PurchasedBookRepository;
import com.be.java.foxbase.repository.UserRepository;
import com.be.java.foxbase.vn.zalopay.crypto.HMACUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PurchaseService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PurchasedBookRepository purchasedBookRepository;

    private final WebClient webClient = WebClient.create();

    private String getCurrentUsername(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public PurchaseWalletResponse purchaseWallet(PurchaseWalletRequest purchaseWalletRequest) {
        User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        user.setBalance(user.getBalance() + purchaseWalletRequest.getAmount());

        userRepository.save(user);
        return PurchaseWalletResponse.builder()
                .success(true)
                .newBalance(user.getBalance())
                .build();
    }

    public PurchaseBookResponse purchaseBookByWallet(PurchaseBookRequest purchaseBookRequest) {
        User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        Book book = bookRepository.findById(purchaseBookRequest.getBookId()).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );

        user.setBalance(user.getBalance() - book.getPrice());

        PurchasedBook purchasedBook = PurchasedBook.builder()
                .book(book)
                .user(user)
                .id(new UserBookId(getCurrentUsername(), purchaseBookRequest.getBookId()))
                .paid(true)
                .paidAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        purchasedBookRepository.save(purchasedBook);

        return PurchaseBookResponse.builder()
                .bookTitle(book.getTitle())
                .bookPrice(book.getPrice())
                .buyer(getCurrentUsername())
                .purchaseAt(LocalDateTime.now())
                .newBalance(user.getBalance())
                .success(true)
                .build();
    }

    public void purchaseBookByZaloPay(Long bookId) {
        User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );


        PurchasedBook purchasedBook = purchasedBookRepository.findById(new UserBookId(getCurrentUsername(), bookId)).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );

        purchasedBook.setPaid(true);
        purchasedBook.setPaidAt(LocalDateTime.now());
        purchasedBookRepository.save(purchasedBook);

    }

    private ZaloPayOrderResponse sendOrderRequest(Order order){
        String endpoint = "https://sandbox.zalopay.com.vn/v001/tpe/createorder";

        // Prepare form data
        MultiValueMap<String, String> formData = getStringStringMultiValueMap(order);

        // Send the request with WebClient
        return webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(ZaloPayOrderResponse.class) // Expect a ZaloPayOrderResponse
                .block(); // Make it synchronous, if needed
    }

    private static MultiValueMap<String, String> getStringStringMultiValueMap(Order order) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("appid", String.valueOf(order.getAppid()));
        formData.add("apptransid", order.getApptransid());
        formData.add("appuser", order.getAppuser());
        formData.add("amount", String.valueOf(order.getAmount()));
        formData.add("apptime", String.valueOf(order.getApptime()));
        formData.add("embeddata", order.getEmbeddata());
        formData.add("item", order.getItem());
        formData.add("mac", order.getMac());
        return formData;
    }

    public ZaloPayOrderResponse createOrder(ZaloPayOrderRequest zaloPayOrderRequest) {
        // zalopay sandbox info
        int appid = 554;
        String key1 = "8NdU5pG5R2spGHGhyO99HN1OhD8IQJBn";
        Map<String,String> embeddata = new HashMap<>(){
            {put("merchantinfo","fox-base");}
        };

        String apptransid = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "_" + UUID.randomUUID();
        Long apptime = System.currentTimeMillis();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.convertValue(zaloPayOrderRequest.getItem(), ObjectNode.class);
        String item = node.toString();
        String strEmbeddata = new JSONObject(embeddata).toString();

        List<String> list = Arrays.asList(
                String.valueOf(appid), // appid
                apptransid,             // apptransid
                getCurrentUsername(),   // appuser
                zaloPayOrderRequest.getAmount().toString(), // amount
                apptime.toString(),     // apptime
                strEmbeddata,           // embeddata
                node.toString());       // item

        String macInput = String.join("|", list);
        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, key1, macInput);

        Order order = Order.builder()
                .appid(appid)
                .appuser(getCurrentUsername())
                .apptime(apptime)
                .amount(zaloPayOrderRequest.getAmount())
                .apptransid(apptransid)
                .item(item)
                .embeddata(strEmbeddata)
                .mac(mac)
                .callback_url("")
                .build();

        User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        var bookId = zaloPayOrderRequest.getItem().getBookId();

        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );

        PurchasedBook purchasedBook = PurchasedBook.builder()
                .book(book)
                .user(user)
                .id(new UserBookId(getCurrentUsername(), bookId))
                .paid(false)
                .paidAt(null)
                .build();

        purchasedBookRepository.save(purchasedBook);
        return sendOrderRequest(order);
    }

    public PurchaseBookResponse checkPaymentStatus(Long bookId) {
        PurchasedBook purchasedBook = purchasedBookRepository.findById(new UserBookId(getCurrentUsername(), bookId)).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );

        User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        return PurchaseBookResponse.builder()
                .success(purchasedBook.isPaid())
                .bookTitle(purchasedBook.getBook().getTitle())
                .bookPrice(purchasedBook.getBook().getPrice())
                .newBalance(user.getBalance())
                .purchaseAt(purchasedBook.getPaidAt())
                .buyer(getCurrentUsername())
                .build();
    }
}

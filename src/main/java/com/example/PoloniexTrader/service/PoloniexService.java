package com.example.PoloniexTrader.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import com.example.PoloniexTrader.config.PoloniexProperties;
import com.example.PoloniexTrader.dto.Currency;
import com.example.PoloniexTrader.dto.balance.BalanceResponse;
import com.example.PoloniexTrader.dto.chartData.Candle;
import com.example.PoloniexTrader.dto.chartData.ChartData;
import com.example.PoloniexTrader.dto.orderbook.Order;
import com.example.PoloniexTrader.dto.orderbook.OrderBook;
import com.example.PoloniexTrader.dto.orderbook.OrderBookModel;
import com.example.PoloniexTrader.dto.trade.TradeResult;
import com.example.PoloniexTrader.service.model.*;
import java.math.BigDecimal;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PoloniexService {
    private static final String POLONIEX_PUBLIC_URL = "https://poloniex.com/public";
    private static final String POLONIEX_PRIVATE_URL = "https://poloniex.com/tradingApi";
    private static final ZoneId POLONIEX_LOCAL_ZONE = ZoneId.of("UTC");
    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final String CONTENT_TYPE_URLENCODED = "application/x-www-form-urlencoded";
    private static final String TRADE_STRATEGY_APPLY = "1";
    private static final Logger LOGGER = LoggerFactory.getLogger(PoloniexService.class);
    private final RestTemplate restTemplate;
    private final PoloniexProperties poloniexProperties;

    public PoloniexService(
            final RestTemplate restTemplate, final PoloniexProperties poloniexProperties) {
        this.restTemplate = restTemplate;
        this.poloniexProperties = poloniexProperties;
    }

    public OrderBookModel getOrderBook(CurrencyPair currencyPair, int depth) {
        UriComponentsBuilder builder =
                fromUriString(POLONIEX_PUBLIC_URL)
                        .queryParam(
                                RequestField.COMMAND.getQuery(),
                                Command.RETURN_ORDER_BOOK.getPoloniexCommand())
                        .queryParam(RequestField.CURRENCY_PAIR.getQuery(), currencyPair)
                        .queryParam(RequestField.DEPTH.getQuery(), depth);
        OrderBook orderBook = restTemplate.getForObject(builder.toUriString(), OrderBook.class);
        return prepareOrderBookModel(orderBook);
    }

    private OrderBookModel prepareOrderBookModel(OrderBook orderBook) {
        if (orderBook != null) {
            List<Order> asks =
                    orderBook.getAsks().stream()
                            .map(
                                    order ->
                                            new Order(
                                                    Double.parseDouble(
                                                            ((ArrayList) order).get(0).toString()),
                                                    Double.parseDouble(
                                                            ((ArrayList) order).get(1).toString())))
                            .collect(Collectors.toList());
            List<Order> bids =
                    orderBook.getBids().stream()
                            .map(
                                    order ->
                                            new Order(
                                                    Double.parseDouble(
                                                            ((ArrayList) order).get(0).toString()),
                                                    Double.parseDouble(
                                                            ((ArrayList) order).get(1).toString())))
                            .collect(Collectors.toList());
            return new OrderBookModel(asks, bids);
        }
        return new OrderBookModel(emptyList(), emptyList());
    }

    public List<Candle> getTradeHistory(
            LocalDateTime start, LocalDateTime end, String currencyPair) {
        UriComponentsBuilder builder =
                fromUriString(POLONIEX_PUBLIC_URL)
                        .queryParam(
                                RequestField.COMMAND.getQuery(),
                                Command.RETURN_CHART_DATA.getPoloniexCommand())
                        .queryParam(RequestField.CURRENCY_PAIR.getQuery(), currencyPair)
                        .queryParam(
                                RequestField.START.getQuery(), mapDateToSecondsSinceEpoch(start))
                        .queryParam(RequestField.END.getQuery(), mapDateToSecondsSinceEpoch(end))
                        .queryParam(
                                RequestField.PERIOD.getQuery(),
                                poloniexProperties.getPeriod().getPeriodString());
        ChartData[] rawHistory =
                restTemplate.getForObject(builder.toUriString(), ChartData[].class);
        return mapChartDataToCandle(rawHistory);
    }

    public TradeResult trade(
            Command command, CurrencyPair currencyPair, BigDecimal rate, BigDecimal amount) {
        Map<RequestField, String> paramMap = new LinkedHashMap<>();
        paramMap.put(RequestField.COMMAND, command.getPoloniexCommand());
        paramMap.put(RequestField.CURRENCY_PAIR, currencyPair.toString());
        paramMap.put(RequestField.RATE, rate.toPlainString());
        paramMap.put(RequestField.AMOUNT, amount.toPlainString());
        paramMap.put(RequestField.NONCE, getNonce());
        paramMap.put(RequestField.IMMEDIATE_OR_CANCEL, TRADE_STRATEGY_APPLY);

        RequestEntity<String> body = prepareRequestBody(paramMap);
        ResponseEntity<TradeResult> result = null;
        if (nonNull(body)) {
            result = restTemplate.exchange(body, TradeResult.class);
        }
        if (nonNull(result) && nonNull(result.getBody())) {
            return result.getBody();
        }
        return null;
    }

    public BalanceResponse getBalanceForCurrency() {
        Map<RequestField, String> paramMap = new LinkedHashMap<>();
        paramMap.put(RequestField.COMMAND, Command.RETURN_BALANCES.getPoloniexCommand());
        paramMap.put(RequestField.NONCE, getNonce());

        RequestEntity<String> body = prepareRequestBody(paramMap);
        ResponseEntity<BalanceResponse> result = null;
        if (nonNull(body)) {
            result = restTemplate.exchange(body, BalanceResponse.class);
        }
        if (nonNull(result) && nonNull(result.getBody())) {
            return result.getBody();
        }
        LOGGER.error("Balance request return null body");
        return null;
    }

    public Map<String, Currency> getCurrencies() {
        UriComponentsBuilder builder =
                fromUriString(POLONIEX_PUBLIC_URL)
                        .queryParam(
                                RequestField.COMMAND.getQuery(),
                                Command.GET_CURRENCIES.getPoloniexCommand());

        ParameterizedTypeReference<Map<String, Currency>> responseType =
                new ParameterizedTypeReference<>() {};

       ResponseEntity<Map<String, Currency>> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, null, responseType);
       return response.getBody();
    }

    public List<String> getActiveCurrencies(Map<String, Currency> currencies) {
        return currencies.keySet().stream().filter(key -> isCurrencyActive(currencies.get(key))).collect(Collectors.toList());
    }

    private boolean isCurrencyActive(Currency currency) {
        return currency.getDelisted() == 0 && currency.getDisabled() == 0 && currency.getFrozen() == 0;
    }

    private RequestEntity<String> prepareRequestBody(final Map<RequestField, String> paramMap) {
        final String body = requestBodyAsQueryParamList(paramMap);
        final HttpHeaders headers =
                setHeaders(
                        prepareSignHeader(body, poloniexProperties.getSecret()),
                        poloniexProperties.getKey());

        RequestEntity<String> requestEntity = null;
        try {
            requestEntity =
                    RequestEntity.post(new URI(POLONIEX_PRIVATE_URL)).headers(headers).body(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestEntity;
    }

    private String requestBodyAsQueryParamList(Map<RequestField, String> map) {
        return map.entrySet().stream()
                .map(m -> m.getKey().getQuery() + "=" + m.getValue())
                .collect(Collectors.joining("&"));
    }

    private long mapDateToSecondsSinceEpoch(LocalDateTime date) {
        return date.atZone(POLONIEX_LOCAL_ZONE).toEpochSecond();
    }

    private LocalDateTime mapSecondsSinceEpochToDate(long secondsSinceEpoch) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(secondsSinceEpoch), POLONIEX_LOCAL_ZONE);
    }

    private List<Candle> mapChartDataToCandle(ChartData[] chartData) {
        if (chartData != null) {
            return Arrays.stream(chartData)
                    .map(
                            data ->
                                    new Candle(
                                            mapSecondsSinceEpochToDate(data.getDate()),
                                            data.getHigh(),
                                            data.getLow(),
                                            data.getOpen(),
                                            data.getClose()))
                    .collect(Collectors.toList());
        }
        return emptyList();
    }

    private String prepareSignHeader(String body, String secretKey) {
        if (StringUtils.hasLength(body) && StringUtils.hasLength(secretKey)) {
            return encodeDataWithHashingKey(body, secretKey);
        }
        LOGGER.error("Sign header was not prepared because body or secret were null");
        return "";
    }

    private String encodeDataWithHashingKey(String dataToEncode, String hashingKey) {
        final SecretKeySpec secretKey = new SecretKeySpec(hashingKey.getBytes(), HMAC_SHA512);
        Mac authenticationCode = null;
        try {
            authenticationCode = Mac.getInstance(HMAC_SHA512);
            authenticationCode.init(secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return mapByteArrayToHexString(
                authenticationCode != null
                        ? authenticationCode.doFinal(dataToEncode.getBytes())
                        : new byte[0]);
    }

    private String mapByteArrayToHexString(byte[] bytes) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }

    private HttpHeaders setHeaders(String signHeader, String keyHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(Headers.KEY.getHeaderName(), keyHeader);
        headers.add(Headers.SIGN.getHeaderName(), signHeader);
        headers.add(Headers.CONTENT_TYPE.getHeaderName(), CONTENT_TYPE_URLENCODED);
        return headers;
    }

    private String getNonce() {
        return String.valueOf(System.currentTimeMillis());
    }
}

package ai.yue.library.test.base.crypto;

import ai.yue.library.base.crypto.constant.key.exchange.ExchangeKeyEnum;
import ai.yue.library.base.util.IdUtils;
import ai.yue.library.base.view.Result;
import ai.yue.library.test.ipo.UserIPO;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

/**
 * 密钥交换
 *
 * @author ylyue
 * @since 2021/4/12
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KeyExchangeTest {

    /**
     * @LocalServerPort 提供了 @Value("${local.server.port}") 的代替
     */
    @LocalServerPort
    private int port;

    @Autowired
//    private TestRestTemplate restTemplate;
    private RestTemplate restTemplate = new RestTemplate();

//    private String serverUrl;
    private String serverUrl = "http://localhost:8080";

//    @Before
//    public void setUp() throws Exception {
//        serverUrl = String.format("http://localhost:%d/", port);
//        System.out.println(serverUrl);
//        System.out.println(String.format("port is : [%d]", port));
//    }

    /**
     * 通信过程加密
     * 1. 前端大写UUID     →     后端返回RSA公钥
     * 2. 前端生成RSA密钥对 → 前端通过后端RSA公钥加密前端RSA公钥并传输给后端     →     后端通过私钥解密前端的RSA公钥 → 后端通过前端RSA公钥加密AES密钥响应给前端
     */
    @Test
    public void aes() {
        // 第一步
        String uuid = IdUtils.getSimpleUUID();
        String url = serverUrl + "/open/v2.3/keyExchange/" + uuid + "?exchangeKeyType={exchangeKeyType}";
        Result exchangeKeyResult = restTemplate.postForObject(url, null, Result.class, ExchangeKeyEnum.RSA_AES);
        exchangeKeyResult.successValidate();
        String data = (String) exchangeKeyResult.getData();
        RSA rsa = SecureUtil.rsa();
        RSA serverRsa = SecureUtil.rsa(null, data);
        String encryptedClientPublicKey = serverRsa.encryptBase64(rsa.getPublicKeyBase64(), KeyType.PublicKey);
        JSONObject paramJson = new JSONObject();
        paramJson.put("encryptedClientPublicKey", encryptedClientPublicKey);

        // 第二步
        Result exchangeKeyResult2 = restTemplate.postForObject(url, paramJson, Result.class, ExchangeKeyEnum.RSA_AES);
        exchangeKeyResult2.successValidate();
        String serverRsaEncryptClientAesKey = (String) exchangeKeyResult2.getData();
        String clientAesKey = rsa.decryptStr(serverRsaEncryptClientAesKey, KeyType.PrivateKey);
        AES aes = SecureUtil.aes(clientAesKey.getBytes());
        String encryptBase64 = aes.encryptBase64("123456");
        System.out.println(encryptBase64);
        String decryptStr = aes.decryptStr(encryptBase64);
        System.out.println(decryptStr);
        Assert.assertEquals("123456", decryptStr);

        // 第三步
        String storageKeyAlias = IdUtils.getSimpleUUID();
        JSONObject paramJson2 = new JSONObject();
        paramJson2.put("storageKeyAlias", storageKeyAlias);
        Result exchangeKeyResult3 = restTemplate.postForObject(serverUrl + "/open/v2.3/keyExchange/" + uuid + "/addAlias", paramJson2, Result.class, ExchangeKeyEnum.RSA_AES);
        exchangeKeyResult3.successValidate();

        // 业务接口请求解密测试
        UserIPO userIPO = new UserIPO();
        userIPO.setCellphone("18523146311");
        userIPO.setNickname("123456");
        userIPO.setBirthday(LocalDate.now());
        String userIPOToEncryptBase64 = aes.encryptBase64(JSONObject.toJSONString(userIPO));
        System.out.println(userIPOToEncryptBase64);
        MultiValueMap headers = new LinkedMultiValueMap();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity httpEntity = new HttpEntity<>(userIPOToEncryptBase64, headers);
        Result exchangeKeyResult4 = restTemplate.postForObject(serverUrl + "/controllerEncrypt/decrypt?access_token=" + storageKeyAlias, httpEntity, Result.class);
        exchangeKeyResult4.successValidate();
        System.out.println(exchangeKeyResult4);

        // 业务接口响应加密测试
        Result exchangeKeyResult5 = restTemplate.getForObject(serverUrl + "/controllerEncrypt/encrypt?access_token=" + storageKeyAlias, Result.class);
        exchangeKeyResult5.successValidate();
        String serverEncryptContent = (String) exchangeKeyResult5.getData();
        Assert.assertEquals("encrypt", aes.decryptStr(serverEncryptContent));
    }

    /**
     * 通信过程加密
     * 1. 前端大写UUID     →     后端返回RSA公钥
     * 2. 前端生成RSA密钥对 → 前端通过后端RSA公钥加密前端RSA公钥并传输给后端     →     后端通过私钥解密前端的RSA公钥 → 后端通过前端RSA公钥加密AES密钥响应给前端
     */
    @Test
    public void sm4() {
        // 第一步
        String uuid = IdUtils.getSimpleUUID();
        String url = serverUrl + "/open/v2.3/keyExchange/" + uuid + "?exchangeKeyType={exchangeKeyType}";
        Result exchangeKeyResult = restTemplate.postForObject(url, null, Result.class, ExchangeKeyEnum.SM2_SM4);
        exchangeKeyResult.successValidate();
        String data = (String) exchangeKeyResult.getData();
        SM2 sm2 = SmUtil.sm2();
        SM2 serverSm2 = SmUtil.sm2(null, data);
        String encryptedClientPublicKey = serverSm2.encryptBase64(sm2.getPublicKeyBase64(), KeyType.PublicKey);
        JSONObject paramJson = new JSONObject();
        paramJson.put("encryptedClientPublicKey", encryptedClientPublicKey);

        // 第二步
        Result exchangeKeyResult2 = restTemplate.postForObject(url, paramJson, Result.class, ExchangeKeyEnum.SM2_SM4);
        exchangeKeyResult2.successValidate();
        String serverRsaEncryptClientAesKey = (String) exchangeKeyResult2.getData();
        String clientAesKey = sm2.decryptStr(serverRsaEncryptClientAesKey, KeyType.PrivateKey);
        SymmetricCrypto sm4 = SmUtil.sm4(clientAesKey.getBytes());
        String encryptBase64 = sm4.encryptBase64("123456");
        System.out.println(encryptBase64);
        String decryptStr = sm4.decryptStr(encryptBase64);
        System.out.println(decryptStr);
        Assert.assertEquals("123456", decryptStr);
    }

}

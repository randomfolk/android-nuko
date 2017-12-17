package rehanced.com.simplenukowallet.network;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rehanced.com.simplenukowallet.APIKey;
import rehanced.com.simplenukowallet.interfaces.LastIconLoaded;
import rehanced.com.simplenukowallet.interfaces.StorableWallet;
import rehanced.com.simplenukowallet.utils.Key;
import rehanced.com.simplenukowallet.utils.RequestCache;
import rehanced.com.simplenukowallet.utils.TokenIconCache;
import rehanced.com.simplenukowallet.utils.TxCache;
import rehanced.com.simplenukowallet.utils.UniqueNonceGetter;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.Service.*;
import 	android.util.Log;

public class EtherscanAPI {

    private String token;

    private static EtherscanAPI instance;

    public static EtherscanAPI getInstance() {
        if (instance == null)
            instance = new EtherscanAPI();
        return instance;
    }

    public void getPriceChart(long starttime, int period, boolean usd, Callback b) throws IOException {
        get("http://poloniex.com/public?command=returnChartData&currencyPair=" + (usd ? "USDT_ETH" : "BTC_ETH") + "&start=" + starttime + "&end=9999999999&period=" + period, b);
    }


    /** Not used in nuko, need better block explorer services
     * Retrieve all internal transactions from address like contract calls, for normal transactions @see rehanced.com.simplenukowallet.network.EtherscanAPI#getNormalTransactions() )
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simplenukowallet.fragments.FragmentTransactions#update() or @see rehanced.com.simplenukowallet.fragments.FragmentTransactionsAll#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getInternalTransactions(String address, Callback b, boolean force) throws IOException {
        if (!force && RequestCache.getInstance().contains(RequestCache.TYPE_TXS_INTERNAL, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("http://api.etherscan.io/api?module=account&action=txlistinternal&address=" + address + "&startblock=0&endblock=99999999&sort=asc&apikey=" + token)
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_TXS_INTERNAL, address))).build());
            return;
        }
        get("http://api.etherscan.io/api?module=account&action=txlistinternal&address=" + address + "&startblock=0&endblock=99999999&sort=asc&apikey=" + token, b);
    }


    /**
     * Retrieve all normal ether transactions from address (excluding contract calls etc, @see rehanced.com.simplenukowallet.network.EtherscanAPI#getInternalTransactions() )
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simplenukowallet.fragments.FragmentTransactions#update() or @see rehanced.com.simplenukowallet.fragments.FragmentTransactionsAll#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getNormalTransactions(String address, Callback b, boolean force) throws IOException {
        Set<String> tx_set = TxCache.getInstance().getKeysToUpdate();
        if(!tx_set.isEmpty()){
            String tx_string = "";
            for (String tx : tx_set) {
                tx_string += tx + ",";
            };
            tx_string  = tx_string.substring(0, tx_string.length() - 1);//remove last comma ,
            String url = "http://52.77.216.165/nekonium-api/getmultitxinfo?tx="+tx_string +"&nonce="+ UniqueNonceGetter.getInstance().getNonce();
            Log.d("get",url);
            get(url, b);
        }
        else{
          // String url = "http://52.77.216.165/nekonium-api/gettx?address=" + address +"&nonce=" + UniqueNonceGetter.getInstance().getNonce();
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("http://52.77.216.165/nekonium-api/gettx?address=" + address +"&nonce=1")
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"),"")).build());
            return;
           // Log.d("get",url);
          //  get(url, b);
        }

    /*
        if (!force && RequestCache.getInstance().contains(RequestCache.TYPE_TXS_NORMAL, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("http://52.77.216.165/nekonium-api/gettx?address=" + address +"&nonce=1")
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_TXS_NORMAL, address))).build());
            return;
        }
        get("http://52.77.216.165/nekonium-api/gettx?address=" + address +"&nonce=1", b);
        */

    }


    public void getEtherPrice(Callback b) throws IOException {
      //  https://api.coinmarketcap.com/v1/ticker/nekonium/
        get("https://api.coinmarketcap.com/v1/ticker/nekonium/", b);
       // get("http://api.etherscan.io/api?module=stats&action=ethprice&apikey=" + token, b);
    }


    public void getGasPrice(Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=proxy&action=eth_gasPrice&apikey=" + token, b);
    }


    /**
     * Get token balances via ethplorer.io
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simplenukowallet.fragments.FragmentDetailOverview#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getTokenBalances(String address, Callback b, boolean force) throws IOException {

        if (!force && RequestCache.getInstance().contains(RequestCache.TYPE_TOKEN, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("https://api.ethplorer.io/getAddressInfo/" + address + "?apiKey=freekey")
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_TOKEN, address))).build());
            return;
        }
        get("http://api.ethplorer.io/getAddressInfo/" + address + "?apiKey=freekey", b);
    }


    /** Token is a future function, need a token tracker service
     * Download and save token icon in permanent image cache (TokenIconCache)
     *
     * @param c         Application context, used to load TokenIconCache if reinstanced
     * @param tokenName Name of token
     * @param lastToken Boolean defining whether this is the last icon to download or not. If so callback is called to refresh recyclerview (notifyDataSetChanged)
     * @param callback  Callback to @see rehanced.com.simplenukowallet.fragments.FragmentDetailOverview#onLastIconDownloaded()
     * @throws IOException Network exceptions
     */
    public void loadTokenIcon(final Context c, String tokenName, final boolean lastToken, final LastIconLoaded callback) throws IOException {
        if (tokenName.indexOf(" ") > 0)
            tokenName = tokenName.substring(0, tokenName.indexOf(" "));
        if (TokenIconCache.getInstance(c).contains(tokenName)) return;

        final String tokenNamef = tokenName;
        get("http://etherscan.io//token/images/" + tokenNamef + ".PNG", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (c == null) return;
                ResponseBody in = response.body();
                InputStream inputStream = in.byteStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                final Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                TokenIconCache.getInstance(c).put(c, tokenNamef, new BitmapDrawable(c.getResources(), bitmap).getBitmap());
                // if(lastToken) // TODO: resolve race condition
                callback.onLastIconDownloaded();
            }
        });
    }


    public void getGasLimitEstimate(String to, Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=proxy&action=eth_estimateGas&to=" + to + "&value=0xff22&gasPrice=0x051da038cc&gas=0xffffff&apikey=" + token, b);
    }


    public void getBalance(String address, Callback b) throws IOException {
        String url = "http://52.77.216.165/nekonium-api/getbalance?address=" + address +"&nonce=" + UniqueNonceGetter.getInstance().getNonce();
        Log.d("get",url);
        get(url, b);
/*
        Web3j web3j = Web3jFactory.build(new HttpService("http://73.193.5.56:28568/"));  //
        Log.v("test","Connected to Ethereum client version: " + web3j.web3ClientVersion().send().getWeb3ClientVersion());
        try {
            EthGetBalance ethGetBalance = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
                    .get();
            BigInteger wei = ethGetBalance.getBalance();
        }catch (Exception e){
            Log.v("e", e.getMessage());
        }
*/


    }



    public void getNonceForAddress(String address, Callback b) throws IOException {
        String url = "http://52.77.216.165/nekonium-api/gettxcount?address=" + address +"&nonce="+UniqueNonceGetter.getInstance().getNonce();
        Log.d("get",url);
        get(url, b);
    }


    public void getPriceConversionRates(String currencyConversion, Callback b) throws IOException {
        get("https://api.fixer.io/latest?base=USD&symbols=" + currencyConversion, b);
    }


    public void getBalances(ArrayList<StorableWallet> addresses, Callback b) throws IOException {

        String url = "http://52.77.216.165/nekonium-api/getmultibalance?address=";

        String addr = "";
        for (StorableWallet address : addresses) {
            url += address.getPubKey() + ",";
            addr =  address.toString();
        }
        url = url.substring(0, url.length() - 1); // remove last , AND add token
        url = url  +"&nonce="+UniqueNonceGetter.getInstance().getNonce();
        Log.d("get",url);
/*
        Web3j web3j = Web3jFactory.build(new HttpService("http://73.193.5.56:28568/"));
        Log.v("test","Connected to Ethereum client version: " + web3j.web3ClientVersion().send().getWeb3ClientVersion());
        try {
            EthGetBalance ethGetBalance = web3j
                    .ethGetBalance(addr, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
            BigInteger wei = ethGetBalance.getBalance();
        }catch (Exception e){
            Log.v("e", e.getMessage());
        }
        */
        get(url, b);

    }


    public void forwardTransaction(String raw, Callback b) throws IOException {
        String url = "http://52.77.216.165/nekonium-api/sendtx?hex=" + raw +"&nonce="+ UniqueNonceGetter.getInstance().getNonce();
        Log.d("get",url );
        get(url, b);

    }


    public void get(String url, Callback b) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(b);
    }


    private EtherscanAPI() {
        token = new Key(APIKey.API_KEY).toString();
    }

}

# Nekonium Android Wallet
<p align="center"><img width= "256px" src="https://raw.githubusercontent.com/nekonium/nekonium.github.io/master/img/nekonium_512x512.png" > </p>

This project based on Lunary wallet GPLV3. I particularly changed some components to make it compatible with Nekonium current network:

* No etherScan.io service for Nekonium, I implemented a server side in nodejs to relay the requests to gnekonium/ processing user batch request
* Price data comes from coinmarketcap api
* Price chart is not currently supported
* Transactions history follows Mist style, only transactions made by the smartphone app will be stored for later retrieval 
* Transactions history is stored locally, only new transactions with < 12 confirmations are updated via batch request to server.
* All other functionalities are the same as in Lunary wallet
* Thanks to @CS and @Jiluco（汁゛粉） on discord  for UI suggestion + pictures.
* Thanks to @mike_theminer on discord for server side support. 

## If you want to see more feature:
I will continue support this project for a while with 
Donations for Mobile app development & maintenance:
0x7201bc1de01Ca412b6106dF436b524F872fd54b1


## The below is the original readme file of Lunary project

### Lunary Ethereum Wallet

Lunary is a beautifully designed, easy to use, secure and Open Source Ethereum Wallet for Android.

<img src="http://rehanced.com/apps/lunary/githubbanner.png" >

### Features
* Multi wallet support  
* Support for Watch only wallets  
* Send / Request payments  
* Token Balances  
* Notification on incoming transactions  
* Combined transaction history  
* Addressbook and address naming  
* Importing / Exporting wallets  
* Display amounts and token in ETH, USD or BTC  
* No registration or sign up required  
* Price history charts  
* Fingerprint / Password protection
* ERC-67 and ICAP Support
* Adjustable gas price with minimum at 0.1 up to 32 gwei
* Supporting 8 Currencies: USD, EUR, GBP, CHF, AUD, CAD, JPY, RUB  
* Available in English, German, Spanish, Portuguese and Hungarian

### Build:
Google Play flavor requires own API key:
```
public class APIKey {
    public static final String API_KEY = ""; // Enter your API Key obtained from Etherscan.io
}
```

### Dependencies:
* [Web3j](https://github.com/web3j/web3j)
* [FloatingActionButton](https://github.com/Clans/FloatingActionButton)
* [MaterialDrawer](https://github.com/mikepenz/MaterialDrawer)
* [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
* [zxing](https://github.com/zxing/zxing)
* [RateThisApp](https://github.com/kobakei/Android-RateThisApp)
* [AppIntro](https://github.com/apl-devs/AppIntro)

### Web APIs:
* [Etherscan.io](https://etherscan.io/)  
* [Poloniex.com](https://poloniex.com/)  
* [Ethplorer.io](https://ethplorer.io)

### Donations / Support Lunary 
Ethereum: 0xa9981a33f6b1A18da5Db58148B2357f22B44e1e0

### Licence
GPL3

### Community
Visit us on [/r/Lunary](https://www.reddit.com/r/lunary/)

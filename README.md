# merchantApi
Api for Ownbit Merchant Wallet

Ownbit Merchant Wallet helps merchant accept Bitcoin & other cryptocurrencies for online payments. Integrating Ownbit Merchant Api is easy and straightforward. There are three parties: Merchant Website(your website for selling online goods), Ownbit Merchant Wallet, and Ownbit Platform. This is how the Api works:

1. You create a Ownbit Merchant Wallet (you own the wallet mnemonics, thus control the coming crypto-payments fully), search Ownbit in iOS AppStore or download at https://ownbit.io.
2. You integrate Ownbit Merchant Api into your existing website, so that your customer can select pay for your goods by Bitcoin & other cryptocurrencies.
3. The customer paid, your Ownbit Merchant Wallet received the payment directly, and you can spend the received payment immediately, since you control the Ownbit Merchant Wallet fully.
4. The Ownbit Platform call a callback_url (you provided when configuring the Ownbit Merchant Wallet) to notify your website that a payment is received (or a payment is canceled or failed in case of blockchain rollbacks).

##TERMS & DEFINITION

1. **order_id**: any string that identify an order, length: 1-64, must be unique among the system.
2. **order_price**: format: amountCURRENCY_SYMBOL, can be both fiat and crypto, example: 9.9USD, means 9.9 US Dollar, 0.23BTC, means 0.23 Bitcoin. ATTENTION: NO space between amount and symbol.
3. **coin_type**: coin symbols separated by |, example: BTC|LTC|BSV|DASH, one coin only example: BTC

Merchant Api Supported Crypto: 
> BTC|BCH|LTC|BSV|DASH|ZEC|DOGE|DCR|DGB|RVN|ZEN|XZC. 
Merchant Api Supported Fiat: 
> Almost all popular, USD, CNY, EUR, JPY, and other 100+


```
function test() {
  console.log("notice the blank line before this function?");
}
```

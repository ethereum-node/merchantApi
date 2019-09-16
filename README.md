# merchantApi
Api for Ownbit Merchant Wallet

Ownbit Merchant Wallet helps merchant accept Bitcoin & other cryptocurrencies for online payments. Integrating Ownbit Merchant Api is easy and straightforward. There are three parties: Merchant Website(your website for selling online goods), Ownbit Merchant Wallet, and Ownbit Platform. This is how the Api works:

1. You create a Ownbit Merchant Wallet (you own the wallet mnemonics, thus control the coming crypto-payments fully), search Ownbit in iOS AppStore or download at https://ownbit.io.
2. You integrate Ownbit Merchant Api into your existing website, so that your customer can select pay for your goods by Bitcoin & other cryptocurrencies.
3. The customer paid, your Ownbit Merchant Wallet received the payment directly, and you can spend the received payment immediately, since you control the Ownbit Merchant Wallet fully.
4. The Ownbit Platform call a callback_url (you provided when configuring the Ownbit Merchant Wallet) to notify your website that a payment is received (or a payment is canceled or failed in case of blockchain rollbacks).

### TERMS & DEFINITION

- **orderId**: Any string that identify an order, length: 1-64, must be unique among the system.
- **orderPrice**: Format: amountCURRENCY_SYMBOL, can be both fiat and crypto, example: 9.9USD, means 9.9 US Dollar, 0.23BTC, means 0.23 Bitcoin. ATTENTION: NO space between amount and symbol.
- **walletId**: The Wallet ID you inputted when creating the Ownbit Merchant Wallet.
- **extendedKeysHash**: The MD5 hash of your Ownbit Merchat Wallet's BTC Extended Public Key, to authenticate the wallet. Can be found in your Ownbit Merchant Wallet's Wallet Configuration page.
- **coinType**: Coin symbols separated by |, example: BTC|LTC|BSV|DASH, one coin only example: BTC

Merchant Api Supported Crypto: 
> BTC|BTC_SW_D|BTC_SW_P|BCH|LTC|BSV|DASH|ZEC|DOGE|DCR|DGB|RVN|ZEN|XZC. (BTC for BTC legacy address (none-segwit, starts with 1), BTC_SW_D for BTC segwit default address (starts with bc1), BTC_SW_P for BTC segwit p2sh address (starts with 3))

Merchant Api Supported Fiat: 
> Almost all popular, USD, CNY, EUR, JPY, and other 100+

### Api

- **https://walletservice.bittool.com:14443/bitbill/merchant/cryptoInfoByOrderId** 
> Get crypto info by an order ID, method: POST, POST data is in JSON format:

```
{"orderId":"order12345", "orderPrice":"9.9USD", "walletId":"rgfeqfi5quit", "extendedKeysHash":"8A3A5B18E94F166FD728B454ED63C1D1", "coinType":"BTC|BTC_SW_P|LTC"}
```

When the first time of this interface is called for a specific order ID, a new address for each requested coin types is allocated. Success Response:

```
{
  "orderId":"order12345", 
  "BTC": {
      "address": "1n2NpgP32GemjDY1xBcrgktXNYsJNNhvM",
      "index": 12,   --> The BIP32 index for generating the address, example: m/44'/0'/0'/0/5, 5 is the index
      "amount": "0.123", --> The amount for the specific coin (example is BTC) the customer should pay
      "received": 0,  ---> in satoshi, if received 0.1 BTC, the value is 10000000
      "status": 0  ---> The status for current pay
   },
  "BTC": {
      "address": "3Mp9bmahViLyw9gMAVy4BWfBSvieUBEJJt",
      "index": 2,
      "amount": "0.123",
      "received": 0,
      "status": 0 
   },
   "LTC": {
      "address": "LMwP5XFRd8vUfViH5gmzoTcKsCdAsQ7chs",
      "index": 20,
      "amount": "17.826",
      "received": 0,
      "status": 0 
   }
}
```

**status** can have the following value:
- **0**: Initial status, no payment;
- **1**: Payment received, and the transaction is unconfirmed, the received value is less than what the order expected. Example, the order is expecting to receive 0.123BTC, but received 0.12299999BTC for the address;
- **2**: Payment received, and the transaction is unconfirmed, the received value is equal or greater than what the order expected. Example, the order is expecting to receive 0.123BTC, and received 0.123BTC or 0.1231BTC;
- **10**: The same as value 1, but the transaction is confirmed;
- **20**: The same as value 2, but the transaction is confirmed;
- **9**: The transaction is canceled or failed;

> Note: The merchant should handle status 9 in a proper manner. Status 9 can happen even after a transaction is confirmed (in case of blockchain rollback).






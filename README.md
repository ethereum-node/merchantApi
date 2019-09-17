# merchantApi
Api for Ownbit Merchant Wallet

Ownbit Merchant Wallet helps merchant accept Bitcoin & other cryptocurrencies for online payments. Integrating Ownbit Merchant Api is easy and straightforward. There are three parties: Merchant Website(your website for selling online goods), Ownbit Merchant Wallet, and Ownbit Platform. This is how the Api works:

1. You create a Ownbit Merchant Wallet (you own the wallet mnemonics, thus control the coming crypto-payments fully), search Ownbit in iOS AppStore or download at https://ownbit.io.
2. You integrate Ownbit Merchant Api into your existing website, so that your customer can select pay for your goods by Bitcoin & other cryptocurrencies.
3. The customer paid, your Ownbit Merchant Wallet received the payment directly, and you can spend the received payment immediately, since you control the Ownbit Merchant Wallet fully.
4. The Ownbit Platform call a callback_url (you provided when configuring the Ownbit Merchant Wallet) to notify your website that a payment is received (or a payment is canceled or failed in case of blockchain rollbacks).

### TERMS & DEFINITION

- **orderId**: Any string that identify an order, length: 1-64, must be unique among the system.
- **orderPrice**: Format: amount CURRENCY_SYMBOL, can be both fiat and crypto, example: 9.9 USD, means 9.9 US Dollar, 0.23 BTC, means 0.23 Bitcoin. ATTENTION: There's one space between amount and symbol.
- **walletId**: The Wallet ID you inputted when creating the Ownbit Merchant Wallet.
- **extendedKeysHash**: The MD5 hash of your Ownbit Merchat Wallet's BTC Extended Public Key, to authenticate the wallet. Can be found in your Ownbit Merchant Wallet's Wallet Configuration page.
- **coinType**: Coin symbols separated by |, example: BTC|LTC|BSV|DASH, one coin only example: BTC

Merchant Api Supported Crypto Coin Type: 
> BTC|BCH|LTC|BSV|DASH|ZEC|DOGE|DCR|DGB|RVN|ZEN|XZC

Merchant Api Supported Fiat: 
> Almost all popular, USD, CNY, EUR, JPY, and other 100+

### Api

- **https://walletservice.bittool.com:14443/bitbill/merchant/getCryptoByOrderId** 
> Get crypto info by an order ID, method: POST, POST data is in JSON format:

```
{
  "orderId":"order12345", 
  "orderPrice":"9.9 USD", 
  "walletId":"rgfeqfi5quit", 
  "extendedKeysHash":"8A3A5B18E94F166FD728B454ED63C1D1", 
  "coinType":"BTC|LTC"
}
```

> You should turn on the sepcific coin in your Ownbit Merchant Wallet before making the call.

When the first time of this interface is called for a specific order ID, a new address for each requested coin types is allocated. Success Response:

```
{
  "orderId":"order12345", 
  "orderPrice":"9.9 USD", 
  "crypto": [{
      "coinType": "BTC",
      "address": "3Mp9bmahViLyw9gMAVy4BWfBSvieUBEJJt",
      "index": 12,   --> The BIP32 index for generating the address, example: m/49'/0'/0'/0/5, 5 is the index
      "amount": "0.123" --> The amount for the specific coin the customer should pay
   },{
      "coinType": "LTC",
      "address": "LMwP5XFRd8vUfViH5gmzoTcKsCdAsQ7chs",
      "index": 20,
      "amount": "17.826"
   }],
   "payment": {
      "txHash": "ea6b0490a2e62d841677fc62cc1dd48eb987e8bc121c25ec0d4af9db116e6e9b",
      "coinType": "BTC",
      "amount": "0.123", --> received amount 
      "status": 2,
      "confirmations": 0
   }
}
```

**status** can have the following value:
- **0**: Initial status, no payment;
- **1**: Payment received, the received value is less than what the order expected. Example, the order is expecting to receive 0.123 BTC, but received 0.12299999 BTC for the address;
- **2**: Payment received, the received value is equal or greater than what the order expected. Example, the order is expecting to receive 0.123 BTC, and received 0.123 BTC or 0.1231 BTC;
- **9**: The transaction is canceled or failed;

> Note: The merchant should handle status 9 in a proper manner. Status 9 can happen even after a transaction is confirmed (in case of blockchain rollback).

- **Fee**

The Ownbit Platform charges **0.5%** of transaction amount as the processing fee. And the fee must be deposited into your Ownbit Merchant Wallet before hand. If the current fee is insufficient, no notification will be sent. And the Api will return the following error:

```
{
  "orderId":"order12345", 
  "orderPrice":"9.9 USD", 
  "error": "Insufficient fee",
  "code": -1
}
```

- **Callback**

The Ownbit Platform will call the merchant's callback_url to notify the merchant that a payment state is changed. callback_url must be a POST interface. POST data is passed as the following:

```
{
  "orderId":"order12345", 
  "orderPrice":"9.9 USD", 
  "payment": {
      "txHash": "ea6b0490a2e62d841677fc62cc1dd48eb987e8bc121c25ec0d4af9db116e6e9b",
      "coinType": "BTC",
      "amount": "0.123", --> received amount 
      "status": 2,
      "confirmations": 0
   }
}
```

The Ownbit Platform expects a plain string: "SUCCESS" as the response. If the response is not SUCCESS, the platform will continuously to call in specific time gap, until it fails after 10 times. Example response:

```
SUCCESS
```

**Situations the callback is triggered** 
- **A: Payment received/Unconfirmed**: status: 1/2, confirmations: 0;
- **B: Payment Confirmed**: status: 1/2, confirmations: 1;
- **C: Payment canceled or failed**: status: 9, confirmations: 0;

The merchant might get multiple notifications for a payment. Possible notification cases are as follows:

> CASE 1: A -> B (First get notification A, then get B, the pyament comes to unconfirmed first, and then unconfirmed)  
> CASE 2: B (the payment goes to confirmed directly, no unconfirmed state)  
> CASE 3: A -> C (the payment goes to unconfirmed, and then canceled)   
> CASE 4: A -> B -> C (the payment goes to unconfirmed, and then unconfirmed, but finally canceled)  
> CASE 5: B -> C (the payment goes to confirmed, and then canceled)  
> CASE 6: ... -> C -> B (the payment goes to canceled, but then goes to confirmed again , in case of blockchain rollback)  

The merchant should have a policy that always trust the last state. And the merchant should also has a mechanism to handle some unusual sutiation.







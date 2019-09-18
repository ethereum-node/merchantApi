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
> BTC|ETH|USDT|BCH|LTC|BSV|DASH|ZEC|DOGE|DCR|DGB|RVN|ZEN|XZC   
> Note: Only ERC20 USDT supported

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
  "coinType":"BTC|ETH|USDT|LTC"
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
      "amount": "0.123786" --> The amount for the specific coin the customer should pay
   },{
      "coinType": "ETH",
      "address": "0xd449a416328A3530715Bee067D93f7B672bd8553",
      "index": 0,
      "amount": "1.234782"
   },{
      "coinType": "USDT",
      "address": "0xd449a416328A3530715Bee067D93f7B672bd8553",
      "contractAddress": "0xdac17f958d2ee523a2206206994597c13d831ec7",
      "index": 0,
      "amount": "18.345301"
   },{
      "coinType": "LTC",
      "address": "LMwP5XFRd8vUfViH5gmzoTcKsCdAsQ7chs",
      "index": 20,
      "amount": "17.826569"
   }],
   "payment": {
      "txHash": "ea6b0490a2e62d841677fc62cc1dd48eb987e8bc121c25ec0d4af9db116e6e9b",
      "coinType": "BTC",
      "amount": "0.123786", --> received amount 
      "status": 1,
      "confirmations": 0,
      "rbf": false  --> whether the payment can be rbf (replaced-by-fee), only valid when confirmations is 0.
   }
}
```

> The "payment" block only exists after payment received.

**amount** rules:
- **For ETH/USDT**: The received amount should be **exactly the same** as requested. Less or greater than requested will be treated as an invalid payment. Example, the requested amount is 1.234523 ETH, and the user paid 1.234524 ETH or 1.234522 ETH, will all be treated as invalid payments.
- **For other coins**: The received amount should be **equal or greater than** requested. Example, the request is 0.123456 BTC, the payment is 0.123455 BTC, the payment treated as invalid, no payment info will be returned, and no notification will be sent.

**The Merchant's Payment UI should always ask the customer to pay the exact amount showing in the page.**

**status** can have the following value:
- **0**: Initial status, no payment (or invalid payments received);
- **1**: A valid payment is received, status: unconfirmed;
- **2**: A valid payment is received, status: confirmed;
- **9**: The payment transaction is canceled or failed;

> Note: The merchant should handle status 9 in a proper manner. Status 9 can happen even after a transaction is confirmed (in case of blockchain rollback).

- **Fee**

The Ownbit Platform charges **0.5%** of transaction amount as the processing fee. And the fee must be deposited into your Ownbit Merchant Wallet before hand. If the current fee is insufficient, no notification will be sent. And the Api will return the following error:

```
{
  "orderId":"order12345", 
  "walletId":"rgfeqfi5quit", 
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
      "amount": "0.123765", --> received amount 
      "status": 2,
      "confirmations": 1,
      "rbf": false
   }
}
```

The Ownbit Platform expects a plain string: "SUCCESS" as the response. If the response is not SUCCESS, the platform will continuously to call in specific time gap, until it fails after 10 times. Example response:

```
SUCCESS
```

**Situations the callback is triggered** 
- **A: Payment received/Unconfirmed**: status: 1, confirmations: 0;
- **B: Payment Confirmed**: status: 2, confirmations: 1;
- **C: Payment canceled or failed**: status: 9, confirmations: 0;

The merchant might get multiple notifications for a payment. Possible notification cases are as follows:

> CASE 1: A -> B (First get notification A, then get B, the pyament comes to unconfirmed first, and then confirmed)  
> CASE 2: B (the payment goes to confirmed directly, no unconfirmed state)  
> CASE 3: A -> C (the payment goes to unconfirmed, and then canceled)    

**Trust Unconfirmed or Not?**
Ownbit suggests a general rule for merchants to follow:
- **For account based coins, like: ETH/USDT**, always trust **confirmed** payments only, ship your digital contents to your cusomter only after payment transaction get confirmed.
- **For UTXO based coins, like: BTC/BCH/LTC...**, merchants can trust **unconfirmed** payments when **rbf** is false. The merchant can ship the digital contents immediately in this sutiation.

> - Unconfirmed payments with **rbf** equals to true, can be canceled in technical very easily. If merchants trust such payments, they should have a mechanism to get their goods back if the payments get canceled.  
> - Merchants should get well prepared for handle notification status 9, to deal with payments cancelation.







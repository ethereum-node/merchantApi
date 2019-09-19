# merchantApi
Api for Ownbit Merchant Wallet

Ownbit Merchant Wallet helps merchant accept Bitcoin & other cryptocurrencies for online payments. Integrating Ownbit Merchant Api is easy and straightforward. There are three parties: Merchant Website(your website for selling online goods), Ownbit Merchant Wallet, and Ownbit Platform. This is how the Api works:

1. You create a Ownbit Merchant Wallet (you own the wallet mnemonics, thus control the coming crypto-payments fully), search Ownbit in iOS AppStore or download at https://ownbit.io.
2. You integrate Ownbit Merchant Api into your existing website, so that your customer can select pay for your goods by Bitcoin & other cryptocurrencies.
3. The customer paid, your Ownbit Merchant Wallet received the payment directly, and you can spend the received payment immediately, since you control the Ownbit Merchant Wallet fully.
4. The Ownbit Platform call a callback_url (you provided when configuring the Ownbit Merchant Wallet) to notify your website that a payment is received (or a payment is canceled or failed in case of blockchain rollbacks).

### TERMS & DEFINITION

- **apiKey**: The Api Key for your Ownbit Merchant Wallet. It can be found in your Ownbit Merchant Wallet's Wallet Configuration page.
- **orderId**: Any string that identify an order, length: 1-64, must be unique among the system.
- **orderPrice**: Format: amount CURRENCY_SYMBOL, can be both fiat and crypto, example: 9.9 USD, means 9.9 US Dollar, 0.23 BTC, means 0.23 Bitcoin. ATTENTION: There's one space between amount and symbol.
- **coinType**: Coin symbols separated by |, example: BTC|LTC|BSV|DASH, one coin only example: BTC

Merchant Api Supported Crypto Coin Type: 
> BTC|ETH|USDT|BCH|LTC|BSV|DASH|ZEC|DOGE|DCR|DGB|RVN|ZEN|XZC   
> Note: For USDT, only ERC20 supported

Merchant Api Supported Fiat: 
> Almost all popular, USD, CNY, EUR, JPY, and other 100+

### Api

- **https://walletservice.bittool.com:14443/bitbill/merchant/getCryptoByOrderId** 
> Get crypto info by an order ID, method: POST, POST data is in JSON format:

```
{
  "apiKey":"8A3A5B18E94F166FD728B454ED63C1D1", 
  "orderId":"order12345", 
  "orderPrice":"9.9 USD", 
  "coinType":"BTC|ETH|USDT|LTC"
}
```

> You should turn on the sepcific coin in your Ownbit Merchant Wallet before making the call.

Another example with fixed crypto rate:

```
{
  "apiKey":"8A3A5B18E94F166FD728B454ED63C1D1", 
  "orderId":"order12345", 
  "orderPrice":"0.12 BTC" --> ask the customer pay 0.12 BTC regardless of the exchange rate
}
```

When the first time of this interface is called for a specific order ID, a new address for each requested coin types is allocated. Success Response:

```
{
  "orderId":"order12345", 
  "orderPrice":"9.9 USD", 
  "crypto": [{
      "coinType": "BTC",
      "address": "3Mp9bmahViLyw9gMAVy4BWfBSvieUBEJJt",
      "indexNo": 12,   --> The BIP32 index for generating the address, example: m/49'/0'/0'/0/5, 5 is the index
      "requestedAmount": "0.123786" --> The amount for the specific coin the customer should pay
   },{
      "coinType": "ETH",
      "address": "0xd449a416328A3530715Bee067D93f7B672bd8553",
      "indexNo": 0,
      "requestedAmount": "1.234782"
   },{
      "coinType": "USDT",
      "address": "0xd449a416328A3530715Bee067D93f7B672bd8553",
      "contractAddress": "0xdac17f958d2ee523a2206206994597c13d831ec7",
      "indexNo": 0,
      "requestedAmount": "18.345301"
   },{
      "coinType": "LTC",
      "address": "LMwP5XFRd8vUfViH5gmzoTcKsCdAsQ7chs",
      "indexNo": 20,
      "requestedAmount": "17.826569"
   }],
   "payment": {
      "txHash": "ea6b0490a2e62d841677fc62cc1dd48eb987e8bc121c25ec0d4af9db116e6e9b",
      "coinType": "BTC",
      "amount": "0.123786", --> received amount 
      "paymentStatus": 1,
      "confirmations": 0,
      "rbf": false  --> whether the payment can be rbf (replaced-by-fee), only valid when confirmations is 0.
   },
   "status": 0
}
```

> The "payment" block only exists after a valid payment is received.  
> If an order is not paid within 24 hours, the allocated address will be revoked and reused.

**amount** rules:
- **For ETH/USDT**: The received amount should be **exactly the same** as requested. Less or greater than requested will be treated as an invalid payment. Example, the requested amount is 1.234523 ETH, and the user paid 1.234524 ETH or 1.234522 ETH, will all be treated as invalid payments.
- **For other coins**: The received amount should be **equal or greater than** requested. Example, the request is 0.123456 BTC, the payment is 0.123455 BTC, the payment treated as invalid, no payment info will be returned, and no notification will be sent.

**The Merchant's Payment UI should always ask the customer to pay the exact amount showing in the page.**

**paymentStatus** can have the following value:
- **0**: Initial status, no payment (or invalid payments);
- **1**: A valid payment is received, status: unconfirmed;
- **2**: A valid payment is received, status: confirmed;
- **9**: The payment transaction is canceled or failed;

> Note: The merchant should handle paymentStatus 9 in a proper manner. paymentStatus 9 can happen even after a transaction is confirmed (in case of blockchain rollback, even though it is very rare).

If something is wrong for processing, or the merchant passed the wrong parameters, the Api returns corresponding error code:

```
{
  "message": <error message>,
  "status": <error code>
}
```

A list of status codes are:
- **0**: success
- **-1**: Insufficient fee
- **-31**: Lack of madatory parameters
- **-41**: Invalid Api Key
- **-61**: Invalid Order Price
- **-62**: Unsupported Fiat Currency
- **-63**: Unsupported Coin Type
- **-64**: Coin not open in corresponding Merchant Wallet
- **-66**: Can't generate more address 

### Fee

The Ownbit Platform charges **2% - 0.5%** of transaction amount as the processing fee according to how much volume the merchant has processed. And the fee must be deposited into your Ownbit Merchant Wallet before hand. If the current fee is insufficient, no notification will be sent. And the Api will return the following error:

```
{
  "message": "Insufficient fee",
  "status": -1
}
```

Fee rate for different volume range :
- **2%**: Total processed volume less than 1,000 USD
- **1.5%**: Total processed volume between 1,000 USD - 10,000 USD
- **1%**: Total processed volume between 10,000 USD - 100,000 USD
- **0.9%**: Total processed volume between 100,000 USD - 500,000 USD
- **0.8%**: Total processed volume between 500,000 USD - 1,000,000 USD
- **0.7%**: Total processed volume between 1,000,000 USD - 5,000,000 USD
- **0.6%**: Total processed volume between 5,000,000 USD - 10,000,000 USD
- **0.5%**: Total processed volume between 10,000,000 USD - 50,000,000 USD
- **0.4%**: Total processed volume between 50,000,000 USD - 100,000,000 USD
- **0.3%**: Total processed volume > 100,000,000 USD


### Callback

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

The Ownbit Platform expects a plain string: "SUCCESS" as the response. If the response is not SUCCESS, the platform will continuously to call in specific time gap (30 secs, 1 min, 2 mins, 5 mins, 30 mins, 2 hours, 6 hours, 1 day, 2 days), until it fails after 10 times. Example response:

```
SUCCESS
```

**Situations the callback is triggered** 
- **A: Payment received/Unconfirmed**: paymentStatus: 1, confirmations: 0;
- **B: Payment Confirmed**: paymentStatus: 2, confirmations: 1;
- **C: Payment canceled or failed**: paymentStatus: 9, confirmations: 0;

The merchant might get multiple notifications for a payment. Possible notification cases are as follows:

> CASE 1: A -> B (First get notification A, then get B, the pyament comes to unconfirmed first, and then confirmed)  
> CASE 2: B (the payment goes to confirmed directly, no unconfirmed state)  
> CASE 3: A -> C (the payment goes to unconfirmed, and then canceled)    

**Trust Unconfirmed or Not?**
Ownbit suggests a general rule for merchants to follow:
- **For account based coins, like: ETH/USDT**, always trust **confirmed** payments only, ship your digital contents to your cusomter only after payment transaction get confirmed.
- **For UTXO based coins, like: BTC/BCH/LTC...**, merchants can trust **unconfirmed** payments when **rbf** is false. The merchant can ship the digital contents immediately in this sutiation.

> - Unconfirmed payments with **rbf** equals to true, can be canceled in technical very easily. If merchants trust such payments, they should have a mechanism to get their goods back if the payments get canceled.  
> - Merchants should get well prepared for handle notification of paymentStatus 9, to deal with payments cancelation.







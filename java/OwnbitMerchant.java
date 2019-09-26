package com.bitbill.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.net.HttpURLConnection;
import java.io.DataOutputStream;

public class OwnbitMerchant {
	
	public static final String OWNBIT_MERCHANT_API_URL = "https://walletservice.bittool.com:14443/bitbill/merchant/getCryptoByOrderId";
	public static final String OWNBIT_MERCHANT_API_KEY = "8A3A5B18E94F166FD728B454ED63C1D1";
	public static final String OWNBIT_MERCHANT_WALLET_ID = "r89fdk3mrf1d";
	
	public static void handleOrderExample() {
		String orderId = "order-example-12345";
		String orderPrice = "128.35 AUD";
		
		String hashStr = OWNBIT_MERCHANT_WALLET_ID + orderId + orderPrice + OWNBIT_MERCHANT_API_KEY;
		String orderHash = EncryptUtil.bytesToHex(Sha256Hash.hash(hashStr.getBytes()));
		
		JSONObject paramObj = new JSONObject();
		paramObj.put("orderPrice", orderPrice);
		paramObj.put("walletId", OWNBIT_MERCHANT_WALLET_ID);
		paramObj.put("orderId", orderId);
		paramObj.put("orderHash", orderHash);
		paramObj.put("coinType", "BTC|ETH|USDT|BCH|LTC|BSV|DASH|DOGE|DCR|DGB");
		
		String ret = callPost(OWNBIT_MERCHANT_API_URL, paramObj.toJSONString());
		try {
			JSONObject retObj = JSON.parseObject(ret);
			
			JSONObject dataObj = retObj.getJSONObject("data");
			if (dataObj != null) {
				//success 
				JSONArray cryptoArray = dataObj.getJSONArray("crypto");
				for (int i = 0; i < cryptoArray.size(); i++) {
					JSONObject aCrypto = cryptoArray.getJSONObject(i);
					String coinType = aCrypto.getString("coinType");
					String address = aCrypto.getString("address");
					String requestedAmount = aCrypto.getString("requestedAmount");
					int indexNo = aCrypto.getIntValue("indexNo");
					String contractAddress = aCrypto.getString("contractAddress");
					
					String qrcodeStr = getScheme(coinType);
					if (qrcodeStr.length() > 0) {
						qrcodeStr += ":";
					}
					qrcodeStr += address;
					qrcodeStr += "?amount=" + requestedAmount;
					
					if (contractAddress != null && contractAddress.length() > 0) {
						qrcodeStr += "&contractAddress=" + contractAddress;
					}
					
					//use qrcodeStr to generate QR Code
					//and ask your user to pay exactly the requestedAmount
				}
				
				JSONObject paymentObj = dataObj.getJSONObject("payment");
				if (paymentObj != null) {
					//the order get payment
					String txHash = paymentObj.getString("txHash");
					String coinType = paymentObj.getString("coinType");
					String receivedAmount = paymentObj.getString("amount");
					int paymentStatus = paymentObj.getIntValue("paymentStatus");
					boolean rbf = paymentObj.getBooleanValue("rbf");
					int confirmations = paymentObj.getIntValue("confirmations");
					
					if (paymentStatus == 2) {
						//OK, the payment is confirmed
						//check confirmations for how many confirmation is has got
						if (confirmations >= 6) {
							//You can decide how many confirmations you trust
							//You may define different rules for different coinTypes
						}
					} else if (paymentStatus == 1) {
						//the payment is unconfirmed
						if (rbf) {
							//Can be canceled very easily, be careful to trust this
						} else {
							//I can trust it.
						}
					} else if (paymentStatus == 9) {
						//payment canceled
						//get your goods back if you have already delivered.
					}
				}
			} else {
				//Error occured 
				int status = retObj.getIntValue("status");
				if (status == -1) {
					//Deposit fee in your Ownbit Merchant Wallet
				} else if (status == -68) {
					//order hash error
				} 
				//...
				//See merchantApi for a full reference
			}
		} catch(Exception ex) {
			//TO DO, handle JSON exception
		}
	}
	
	public static String getScheme(String coinType) {
		if (coinType.contentEquals("BTC")) {
			return "bitcoin";
		} else if (coinType.contentEquals("ETH")) {
			return "ethereum";
		} else if (coinType.contentEquals("USDT")) {
			return "ethereum";
		} else if (coinType.contentEquals("BCH")) {
			return "bitcoincash";
		} else if (coinType.contentEquals("LTC")) {
			return "litecoin";
		} else if (coinType.contentEquals("BSV")) {
			return "bitcoinsv";
		} else if (coinType.contentEquals("DASH")) {
			return "dash";
		} else if (coinType.contentEquals("ZEC")) {
			return "zcash";
		} else if (coinType.contentEquals("DOGE")) {
			return "dogecoin";
		} else if (coinType.contentEquals("DCR")) {
			return "decred";
		} else if (coinType.contentEquals("DGB")) {
			return "digibyte";
		} else if (coinType.contentEquals("RVN")) {
			return "ravencoin";
		} else if (coinType.contentEquals("ZEN")) {
			return "horizen";
		} else if (coinType.contentEquals("XZC")) {
			return "zcoin";
		} else {
			return "";
		}
	}
	
	public static String callPost(String url, String postData) {
		try {
			String ret = "";
			
			int postDataLength = postData.length();
			java.net.URL u = new java.net.URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			conn.setRequestMethod( "POST" );
			conn.setRequestProperty( "Content-Type", "application/json"); 
			conn.setRequestProperty( "charset", "utf-8");
			conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));

			byte[] postDataBytes = postData.toString().getBytes("UTF-8");
			DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
			wr.write(postDataBytes);

			String encoding = "utf-8";
			int code = conn.getResponseCode();
			if (code == 200) {
				int mn = 300000;
				byte[] _b = new byte[300000];
				int m = 0;
				int n = 0;
				while ((m = conn.getInputStream().read(_b, n, mn - n)) > 0) {
					n += m;
				}
				conn.getInputStream().close();

				ret = new String(_b, encoding);
				ret = ret.trim();
			}
			
			return ret;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static void computeOrderhashExample() {
		String orderId = "order-example-0034";
		String orderPrice = "1.3 CNY";
		String walletId = "r81qipv5nd4x";
		String apiKey = "a40bc292e139b4f1b7f6ad94edd0d878";
		
		String hashStr = walletId + orderId + orderPrice + apiKey;
		String recomputeOrderHash = EncryptUtil.bytesToHex(Sha256Hash.hash(hashStr.getBytes()));
		System.out.println(recomputeOrderHash.toLowerCase());
	}
	
	public static void main(String[] args)throws Throwable{
		computeOrderhashExample();
	}

}

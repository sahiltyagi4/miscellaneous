package com.eclipse.blockchain;

import com.chain.api.Account;
import com.chain.api.Asset;
import com.chain.api.MockHsm;
import com.chain.api.Transaction;
import com.chain.exception.ChainException;
import com.chain.http.Client;
import com.chain.signing.HsmSigner;

public class Chain1 {
	public static void main(String[] args) {
		try {
			Client client = new 	Client();
			//MockHsm.Key key = MockHsm.Key.create(client);
			//System.out.println(key.xpub);
			
			String xpub = "df03e8afe5127f8a8ccbbb9f4cf21f8e27463bb6e8c19c7c976029a9d6809d11470297fa0fe33287c7383cc1ab8054579dc51e7d2401c4e92cd9ddaeca868247";
			HsmSigner.addKey(xpub, MockHsm.getSignerClient(client));
			
			//HsmSigner.addKey(key, MockHsm.getSignerClient(client));
//			Asset asset = new Asset.Builder().setAlias("gold").addRootXpub(key.xpub).setQuorum(1).create(client);
//			
//			Account a = new Account.Builder().setAlias("a").addRootXpub(key.xpub).setQuorum(1).create(client);
//			Account b = new Account.Builder().setAlias("b").addRootXpub(key.xpub).setQuorum(1).create(client);
//			
//			//issue
//			Transaction.Template issuance = new Transaction.Builder().addAction(new Transaction.Action.Issue().setAssetAlias("gold").setAmount(12))
//									.addAction(new Transaction.Action.ControlWithAccount().setAccountAlias("b").setAssetAlias("gold").setAmount(12)).build(client);
//			
//			Transaction.Template iss = HsmSigner.sign(issuance);
//			Transaction.submit(client, iss);
			
			Transaction.Template spend = new Transaction.Builder().addAction(new Transaction.Action.SpendFromAccount().setAccountAlias("a").
						setAssetAlias("gold").setAmount(15)).addAction(new Transaction.Action.ControlWithAccount().setAccountAlias("b").setAssetAlias("gold")
						.setAmount(15)).build(client);
			Transaction.submit(client, HsmSigner.sign(spend));
			
			
			
			System.out.println("complete running chain core");
		} catch(ChainException ch) {
			ch.printStackTrace();
		}
	}
}

package rehanced.com.simplenukowallet.fragments;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rehanced.com.simplenukowallet.activities.MainActivity;
import rehanced.com.simplenukowallet.data.TransactionDisplay;
import rehanced.com.simplenukowallet.interfaces.StorableWallet;
import rehanced.com.simplenukowallet.network.EtherscanAPI;
import rehanced.com.simplenukowallet.utils.AppBarStateChangeListener;
import rehanced.com.simplenukowallet.utils.RequestCache;
import rehanced.com.simplenukowallet.utils.ResponseParser;
import rehanced.com.simplenukowallet.utils.WalletStorage;

import static android.view.View.GONE;


public class FragmentTransactionsAll extends FragmentTransactionsAbstract {

    protected TransactionDisplay unconfirmed;
    private long unconfirmed_addedTime;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("fracTx","oncreate");
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        MainActivity ac = (MainActivity) this.ac;
        if (ac != null && ac.getAppBar() != null) {
            ac.getAppBar().addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.COLLAPSED) {
                        fabmenu.hideMenu(true);
                    } else {
                        fabmenu.showMenu(true);
                    }
                }
            });
        }
        return rootView;
    }


    public void update(boolean force) {
        if (ac == null) return;
        getWallets().clear();
        if (swipeLayout != null)
            swipeLayout.setRefreshing(true);
        resetRequestCount();
        final ArrayList<StorableWallet> storedwallets = new ArrayList<StorableWallet>(WalletStorage.getInstance(ac).get());
        if (storedwallets.size() == 0) {
            nothingToShow.setVisibility(View.VISIBLE);
            onItemsLoadComplete();
        } else {
            nothingToShow.setVisibility(GONE);
            //dont have to loop over all wallet, because we cache all txs, more efficient
           // for (int i = storedwallets.size()-1; i < storedwallets.size(); i++) {
                try {
                    Log.d("Txall","in loop ");
                    final StorableWallet currentWallet = storedwallets.get(0);

                    EtherscanAPI.getInstance().getNormalTransactions(currentWallet.getPubKey(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (isAdded()) {
                                ac.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onItemsLoadComplete();
                                        ((MainActivity) ac).snackError("No internet connection");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String restring = response.body().string();
                            //if (restring != null && restring.length() > 2)
                            //    RequestCache.getInstance().put(RequestCache.TYPE_TXS_NORMAL, currentWallet.getPubKey(), restring);
                            final ArrayList<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", currentWallet.getPubKey(), TransactionDisplay.NORMAL,true));
                            if (isAdded()) {
                                ac.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onComplete(w, storedwallets);
                                    }
                                });
                            }
                        }
                    }, force);
                    /*
                    EtherscanAPI.getInstance().getInternalTransactions(currentWallet.getPubKey(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (isAdded()) {
                                ac.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onItemsLoadComplete();
                                        ((MainActivity) ac).snackError("No internet connection");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String restring = response.body().string();
                            if (restring != null && restring.length() > 2)
                                RequestCache.getInstance().put(RequestCache.TYPE_TXS_INTERNAL, currentWallet.getPubKey(), restring);
                            final ArrayList<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", currentWallet.getPubKey(), TransactionDisplay.CONTRACT));
                            if (isAdded()) {
                                ac.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onComplete(w, storedwallets);
                                    }
                                });
                            }
                        }
                    }, force);*/
                } catch (IOException e) {
                    if (isAdded()) {
                        if (ac != null)
                            ((MainActivity) ac).snackError("Can't fetch account balances. No connection?");

                        // So "if(getRequestCount() >= storedwallets.size()*2)" limit can be reached even if there are expetions for certain addresses (2x because of internal and normal)
                        //addRequestCount();
                        addRequestCount();
                        onItemsLoadComplete();
                        e.printStackTrace();
                    }
                }


            //}
        }
    }

    private void onComplete(ArrayList<TransactionDisplay> w, ArrayList<StorableWallet> storedwallets) {
        addToWallets(w);
       // addRequestCount();
     /*   if(w.size()==0) {
            nothingToShow.setVisibility(View.VISIBLE);
            onItemsLoadComplete();
        }
*/

/*
            // If transaction was send via App and has no confirmations yet (Still show it when users refreshes for 10 minutes)
            if (unconfirmed_addedTime + 1 * 60 * 1000 < System.currentTimeMillis()) // After 1 minutes remove unconfirmed (should now have at least 1 confirmation anyway)
                unconfirmed = null;
            if (unconfirmed != null && wallets.size() > 0) {
                if (wallets.get(0).getAmount() == unconfirmed.getAmount()) {
                    unconfirmed = null;
                } else {
                    wallets.add(0, unconfirmed);
                }
            }
*/
            nothingToShow.setVisibility(wallets.size() == 0 ? View.VISIBLE : GONE);
            walletAdapter.notifyDataSetChanged();
         onItemsLoadComplete();
       /* if (getRequestCount() >= 1) {
            onItemsLoadComplete();
        }*/
    }


    //not used
    public void addUnconfirmedTransaction(String from, String to, BigInteger amount) {
        return;
        /*
        Log.d("addtx","addUnconfirm tx");
        unconfirmed = new TransactionDisplay(from, to, amount, 0, System.currentTimeMillis(), "", TransactionDisplay.NORMAL, "", "0", 0, 1, 1, false);
        unconfirmed_addedTime = System.currentTimeMillis();

        wallets.add(0, unconfirmed);
        notifyDataSetChanged();
        */
    }


}
package io.clearstreet.swdn.position;

import io.clearstreet.swdn.api.PositionApi;
import io.clearstreet.swdn.api.TradeApi;
import io.clearstreet.swdn.model.*;
import io.clearstreet.swdn.refdata.ReferenceDataRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionManager implements TradeApi, PositionApi {

  private final ReferenceDataRepository referenceDataManager;
  private final List<Trade> trades = new ArrayList<>();

  public PositionManager(ReferenceDataRepository referenceDataManager) {
    this.referenceDataManager = referenceDataManager;
  }

  @Override
  public boolean enterTrade(Trade trade) {
    boolean flag = false;
    if(trade.tradeType()== TradeType.NEW) {
      trades.add(trade);
      flag = true;
    }
    else if(trade.tradeType()==TradeType.CANCEL){
      for(int i=0;i< trades.size();i++){
        if(trade.tradeId().equals(trades.get(i).tradeId())){
          trades.remove(i);
          flag = true;
        }
      }
    }
    else{
      for(int i=0;i< trades.size();i++){
        if(trade.tradeId().equals(trades.get(i).tradeId())){
          trades.remove(i);
          flag = true;
        }
      }
      if(flag)
        trades.add(trade);
    }
    return flag;
  }

  @Override
  public List<Position> getPositionsForMember(String memberName) {
    List<Account> accountList = new ArrayList<>(referenceDataManager.getAccounts().values());

    Map<MemberKey,Position> positions = new HashMap<>();

    for(Account account : accountList) {
      if(account.memberName().equals(memberName)) {
        for (Trade trade : trades) {
           if(trade.accountName().equals(account.accountName())){
             MemberKey key = new MemberKey(memberName,trade.instrumentName());
             Position position = positions.get(key);
             if(position==null){
                position = new Position(memberName,trade.instrumentName(),0,0);
             }
             double qty = 0.0;
             double intialVal = 0.0;
             if(trade.side() == TradeSide.BUY) {
                qty = position.quantity() +trade.quantity();
                intialVal = position.initialValue()+ (trade.quantity()* trade.price());
             }
             else{
               qty = position.quantity() -trade.quantity();
               intialVal = position.initialValue()- (trade.quantity()* trade.price());
             }
             positions.put(key,new Position(memberName,trade.instrumentName(),
                     qty,
                     intialVal));
           }
        }
      }
    }
    return new ArrayList<>(positions.values());
    //throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public List<Position> getPositionsForAccount(String accountName) {
    Map<PositionKey, Position> positions = new HashMap<>();
    for (Trade trade : trades) {
      if (trade.accountName().equals(accountName)) {
        PositionKey key = new PositionKey(trade.accountName(), trade.instrumentName());
        Position position = positions.get(key);
        if (position == null) {
          position = new Position(trade.accountName(), trade.instrumentName(), 0, 0);
        }
        double qty = 0.0;
        double intialVal = 0.0;
        if(trade.side() == TradeSide.BUY) {
          qty = position.quantity() +trade.quantity();
          intialVal = position.initialValue()+ (trade.quantity()* trade.price());
        }
        else{
          qty = position.quantity() -trade.quantity();
          intialVal = position.initialValue()- (trade.quantity()* trade.price());
        }
        positions.put(key, new Position(trade.accountName(), trade.instrumentName(),
            qty,
            intialVal));
      }
    }
    return new ArrayList<>(positions.values());
  }

  private record PositionKey(String accountName, String instrumentName) {
  }

  private record MemberKey(String memberName, String instrumentName){}
}

package io.clearstreet.swdn.risk;

import io.clearstreet.swdn.api.RiskApi;
import io.clearstreet.swdn.model.Instrument;
import io.clearstreet.swdn.model.InstrumentType;
import io.clearstreet.swdn.model.Position;
import io.clearstreet.swdn.position.PositionManager;
import io.clearstreet.swdn.price.PriceRepository;
import io.clearstreet.swdn.refdata.ReferenceDataRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RiskCalculator implements RiskApi {

  private final PositionManager positionManager;
  private final PriceRepository priceRepository;
  private final ReferenceDataRepository referenceDataRepository;

  public RiskCalculator(PositionManager positionManager, PriceRepository priceRepository,
      ReferenceDataRepository referenceDataRepository) {
    this.positionManager = positionManager;
    this.priceRepository = priceRepository;
    this.referenceDataRepository = referenceDataRepository;
  }

  @Override
  public double calculateAccountPnl(String accountName) {
    double pnl = 0;
    for (Position position : positionManager.getPositionsForAccount(accountName)) {
      pnl += calculatePositionPnl(position);
    }
    return pnl;
  }

  @Override
  public double calculateMemberPnl(String memberName) {
    double pnl = 0;
    for (Position position : positionManager.getPositionsForMember(memberName)) {
      pnl += calculatePositionPnl(position);
    }
    return pnl;
  }

  @Override
  public double calculateMemberMargin(String memberName) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public double calculateMarketRisk(String memberName){
    List<Position> positionList = positionManager.getPositionsForMember(memberName);

    double marketRisk = 0.0;
    double up = 0.0;
    double down = 0.0;
    for(Position position : positionList){
      Optional<Instrument> instrument = referenceDataRepository.getInstrument(position.instrumentName());
        if(instrument.isPresent()){
          Instrument instrument1 = instrument.get();
          if(instrument1.type()== InstrumentType.OPTION){
              up += (.15)*(position.quantity()*priceRepository.getPrice(instrument1.instrumentName()).get());
              down += (-.10)*(position.quantity()*priceRepository.getPrice(instrument1.instrumentName()).get());
          }
          else{
            up += (.2)*(position.quantity()*priceRepository.getPrice(instrument1.instrumentName()).get());
            down += (-.2)*(position.quantity()*priceRepository.getPrice(instrument1.instrumentName()).get());
          }
        }
    }
    return Math.max(up,down);

  }

  private double calculatePositionPnl(Position position) {
    double price = priceRepository.getPrice(position.instrumentName()).orElseThrow();
    return position.quantity() * price - position.initialValue();
  }


}

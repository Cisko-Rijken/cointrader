package org.cryptocoinpartners.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang.NotImplementedException;

/**
 * Many Owners may have Stakes in the Portfolio, but there is only one PortfolioManager, who is not necessarily an Owner.  The
 * Portfolio has multiple Positions.
 *
 * @author Tim Olson
 */
@Entity
public class Portfolio extends EntityBase {

	/** returns all Positions, whether they are tied to an open Order or not.  Use getTradeablePositions() */
	public @Transient
	Collection<Position> getPositions() {
		ArrayList<Position> allPositions = new ArrayList<Position>();
		Iterator<Asset> it = positions.keySet().iterator();
		while (it.hasNext()) {
			Asset asset = it.next();
			Iterator<Exchange> ite = positions.get(asset).keySet().iterator();
			while (ite.hasNext()) {
				Exchange exchange = ite.next();
				Iterator<Position> itp = positions.get(asset).get(exchange).iterator();
				while (itp.hasNext()) {
					Position pos = itp.next();
					allPositions.add(pos);
				}

			}
		}

		return allPositions;
	}

	public @Transient
	Collection<Position> getPositions(Asset asset, Exchange exchange) {
		ArrayList<Position> allPositions = new ArrayList<Position>();
		Iterator<Position> itp = positions.get(asset).get(exchange).iterator();
		while (itp.hasNext()) {
			Position pos = itp.next();
			allPositions.add(pos);
		}

		return allPositions;
	}

	public @Transient
	long getLongPosition(Asset asset, Exchange exchange) {
		long longVolumeCount = 0;
		if (positions.get(asset) != null && positions.get(asset).get(exchange) != null) {
			Iterator<Position> itp = positions.get(asset).get(exchange).iterator();
			while (itp.hasNext()) {
				Position pos = itp.next();
				longVolumeCount += pos.getLongVolumeCount();
			}

		}

		return longVolumeCount;
	}

	public @Transient
	long getPosition(Asset asset, Exchange exchange) {
		long netVolumeCount = 0;
		if (positions.get(asset) != null && positions.get(asset).get(exchange) != null) {
			Iterator<Position> itp = positions.get(asset).get(exchange).iterator();
			while (itp.hasNext()) {
				Position pos = itp.next();
				netVolumeCount += pos.getVolumeCount();
			}

		}

		return netVolumeCount;
	}

	public @Transient
	long getShortPosition(Asset asset, Exchange exchange) {
		long shortVolumeCount = 0;
		if (positions.get(asset) != null && positions.get(asset).get(exchange) != null) {
			Iterator<Position> itp = positions.get(asset).get(exchange).iterator();

			while (itp.hasNext()) {
				Position pos = itp.next();
				shortVolumeCount += pos.getShortVolumeCount();
			}

		}
		return shortVolumeCount;
	}

	// public @OneToMany ConcurrentHashMap<BalanceType, List<Wallet>> getBalances() { return balances; }

	/**
	 * Returns all Positions in the Portfolio which are not reserved as payment for an open Order
	 */
	@Transient
	public Collection<Position> getTradeablePositions() {
		throw new NotImplementedException();
	}

	@Transient
	public boolean addTransactions(Transaction transaction) {
		return this.transactions.add(transaction);
	}

	@Transient
	public Collection<Transaction> getTransactions() {
		return this.transactions;
	}

	/**
	 * This is the main way for a Strategy to determine what assets it has available for trading
	 */
	@Transient
	public Collection<Position> getReservePositions() {
		throw new NotImplementedException();
	}

	/**
	 * This is the main way for a Strategy to determine how much of a given asset it has available for trading
	 * @param f
	 * @return
	 */
	@Transient
	public Collection<Position> getTradeablePositionsOf(Asset f) {

		throw new NotImplementedException();
	}

	/**
	 * Finds a Position in the Portfolio which has the same Asset as p, then breaks it into the amount p requires
	 * plus an unreserved amount.  The resevered Position is then associated with the given order, while
	 * the unreserved remainder of the Position has getOrder()==null.  To un-reserve the Position, call release(order)
	 *
	 * @param order the order which will be placed
	 * @param p the cost of the order.  could be a different fungible than the order's quote fungible
	 * @throws IllegalArgumentException
	 */
	@Transient
	public void reserve(SpecificOrder order, Position p) throws IllegalArgumentException {
		throw new NotImplementedException();
	}

	@Transient
	public void release(SpecificOrder order) {
		throw new NotImplementedException();
	}

	/**
	 * finds other Positions in this portfolio which have the same Exchange and Asset and merges this position's
	 * amount into the found position's amount, thus maintaining only one Position for each Exchange/Asset pair.
	 * this method does not remove the position from the positions list.
	 * @return true iff another position was found and merged
	 */
	@Transient
	private boolean merge(Position position) {
		ConcurrentHashMap<Exchange, ArrayList<Position>> assetPosition = positions.get(position.asset);
		if (assetPosition == null) {
			ArrayList<Position> detailPosition = new ArrayList<Position>();
			detailPosition.add(position);
			assetPosition = new ConcurrentHashMap<Exchange, ArrayList<Position>>();
			assetPosition.put(position.getExchange(), detailPosition);
			positions.put(position.asset, assetPosition);
			return true;
		} else {
			//asset is present, so check the market
			ArrayList<Position> exchangePositions = assetPosition.get(position.getExchange());
			if (exchangePositions == null) {
				ArrayList<Position> detailPosition = new ArrayList<Position>();
				detailPosition.add(position);
				assetPosition.put(position.getExchange(), detailPosition);
				return true;
			} else {

				for (Position p : exchangePositions) {
					if (p.getExchange().equals(position.getExchange()) && p.getAsset().equals(position.getAsset())) {
						p.setLongVolumeCount(p.getLongVolumeCount() + position.getLongVolumeCount());
						p.setShortVolumeCount(p.getShortVolumeCount() + position.getShortVolumeCount());
						// if the long and short volumes are zero we can remove the position
						if (p.getShortVolumeCount() * -1 == p.getLongVolumeCount()) {
							exchangePositions.remove(p);
						}
						return true;
					} else {
						return false;

					}
				}
				exchangePositions.add(position);
				return true;

			}

		}

	}

	public Portfolio(String name, PortfolioManager manager) {
		this.name = name;
		this.manager = manager;
		this.positions = new ConcurrentHashMap<Asset, ConcurrentHashMap<Exchange, ArrayList<Position>>>();
		this.balances = new ArrayList<>();
		this.transactions = new ArrayList<>();
	}

	private String name;

	public String getName() {
		return name;
	}

	@OneToMany
	public Collection<Stake> getStakes() {
		return stakes;
	}

	@ManyToOne
	public Asset getBaseAsset() {
		return baseAsset;
	}

	@ManyToOne
	public PortfolioManager getManager() {
		return manager;
	}

	/**
	 * Adds the given position to this Portfolio.  Must be authorized.
	 * @param position
	 * @param authorization
	 */
	@Transient
	protected void modifyPosition(Position position, Authorization authorization) {
		assert authorization != null;
		assert position != null;
		boolean modifiedExistingPosition = false;
		merge(position);

		// if 

		//		for (Position curPosition : positions) {
		//			if (curPosition.merge(position)) {
		//				modifiedExistingPosition = true;
		//				break;
		//			}
		//		}
		//		if (!modifiedExistingPosition)
		//			positions.add(position);
	}

	@Override
	public String toString() {

		return getName();
	}

	// JPA
	protected Portfolio() {
	}

	protected void setPositions(ConcurrentHashMap<Asset, ConcurrentHashMap<Exchange, ArrayList<Position>>> positions) {
		this.positions = positions;
	}

	protected void setBalances(Collection<Balance> balances) {
		this.balances = balances;
	}

	public void setBaseAsset(Asset baseAsset) {
		this.baseAsset = baseAsset;
	}

	protected void setTransactions(Collection<Transaction> transactions) {
		this.transactions = transactions;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setStakes(Collection<Stake> stakes) {
		this.stakes = stakes;
	}

	protected void setManager(PortfolioManager manager) {
		this.manager = manager;
	}

	public static final class Factory {
		/**
		 * Constructs a new instance of {@link Tick}.
		 * @return new TickImpl()
		 */
		public static Portfolio newInstance() {
			return new Portfolio();
		}

		public static Portfolio newInstance(String name, PortfolioManager manager) {
			final Portfolio entity = new Portfolio(name, manager);
			return entity;
		}

		// HibernateEntity.vsl merge-point
	}

	private PortfolioManager manager;

	private Asset baseAsset;
	private ConcurrentHashMap<Asset, ConcurrentHashMap<Exchange, ArrayList<Position>>> positions;
	private Collection<Balance> balances = Collections.emptyList();
	private Collection<Transaction> transactions = Collections.emptyList();
	private Collection<Stake> stakes = Collections.emptyList();
}